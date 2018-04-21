package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author Wenqu Wang
 */
public class ReferenceSerializer {
    /**
     * The database name.
     */
    public static final String NAME = "SERIALIZER";
    /**
     * All loaded objects.
     */
    private HashMap<String, Serializable> _loaded;
    /**
     * List of all objects that could possibly be loaded.
     */
    protected HashMap<Class<?>, Set<String>> _objects;
    /** Base file object directory. */
    private Path _directory;
    /** If the Serializer is start.*/
    private boolean _initialized;

    /**Construct BASE.*/
    public ReferenceSerializer(Path base) {
        this._directory = base;
        this._initialized = false;
        this._loaded = new HashMap<>();
        this._objects = new LinkedHashMap<>();
    }
    /**If the serializer is initialized.
     * @return boo*/
    public boolean isInitialized() {
        return this._initialized;
    }
    /**TYPE FILENAME REF.
     * @return ref*/
    public Reference add(Objects type, String fileName, Reference ref) {
        try {
            this.add(type.getBaseDir() + fileName, ref);
        } catch (IllegalStateException e) {
            System.out.println(
                    "A " + type.toString().toLowerCase()
                            + " with that name already exists.");
            return null;
        }
        return ref;
    }
    /**TYPE REF.
     * @return ref*/
    public Reference add(Objects type, Reference ref) {
        return this.add(type, type.toString(), ref);
    }

    /**FILE TOADD.<S>*/
    public <S extends Serializable> void add(String file, S toAdd) {

        if (this._loaded.get(file) != null
                || this.load(file) != null) {
            throw new IllegalStateException(toAdd.getClass().getSimpleName()
                    + " as specified already exists.");
        }
        this._loaded.put(file, toAdd);
        Set<String> tracked = this._objects.get(toAdd.getClass());
        if (tracked == null) {
            tracked = new LinkedHashSet<>();
            this._objects.put(toAdd.getClass(), tracked);
        }
        tracked.add(file);
    }
    /** FILE.
     * @return a. */
    @SuppressWarnings("unchecked")
    private Serializable load(String file) {
        Path filePath = this._directory.resolve(file);
        try {
            Object read;
            read = Utils.deserialize(filePath);
            Serializable loaded = (Serializable) read;
            this._loaded.put(file, loaded);
            return loaded;

        } catch (IOException i) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    /**TYPE FILENAME.
     * @return ref*/
    public Reference get(Objects type, String fileName) {
        Reference ref =
                this.get(Reference.class, type.getBaseDir() + fileName);
        if (ref == null) {
            throw new IllegalStateException(
                    "No such " + type.toString().toLowerCase() + " exists.");
        }
        return ref;

    }
    /**TYPE.
     * @return ref*/
    public Reference get(Objects type) {
        return this.get(type, type.toString());
    }

    /**<S> TYPE FILE.
     * @return boo*/
    public <S extends Serializable> S get(Class<S> type, String file) {
        try {
            Serializable obj = this._loaded.get(file);
            if (obj != null) {
                return type.cast(obj);
            } else {
                return safeLoad(type, file);
            }
        } catch (ClassCastException e) {
            System.out.println(
                    type.getSimpleName() + " as specified does not exist.");
            return null;
        }
    }
    /** Resolve.TYPE FILENAME.
     * @return str*/
    public String resolve(Objects type, String fileName) {
        Reference cur = this.get(type, fileName);
        while (cur.getType() != Objects.NONE) {
            cur = this.get(cur.getType(), cur.getHash());
        }
        return cur.getHash();
    }
    /** Resolve.TYPE.
     * @return str*/
    public String resolve(Objects type) {
        return this.resolve(type, type.toString());
    }
    /** Remove.TYPE FILENAME.*/
    public void remove(Objects type, String fileName) {
        try {
            this.remove(Reference.class, type.getBaseDir() + fileName);
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "A " + type.toString().toLowerCase()
                            + " with that name does not exist.");
            return;
        }
    }
    /** Removes a hash from the object store.TYPE FILE.<S>*/
    public <S extends Serializable> void remove(Class<S> type, String file) {
        try {
            Path filePath = this._directory.resolve(file);

            if (!this._objects.containsKey(type)
                    || !this._objects.get(type).contains(file)) {
                throw new IllegalArgumentException(type.getSimpleName()
                        + " as specified does not exist.");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            this._objects.get(type).remove(file);
            this._loaded.remove(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Iterates over the Serializer.TYPE ACTION.*/
    public void iterate(Objects type,
                        BiConsumer<? super String, Reference> action) {
        this.iterate(Reference.class, (file, ref) -> {
                if (file.startsWith(type.getBaseDir())) {
                    action.accept(file.replace(type.getBaseDir(), ""), ref);
                }
            });
    }
    /** Iterates over the Serializer.TYPE ACTION <S>.*/
    public <S extends Serializable> void iterate(
            Class<S> type, final BiConsumer<? super String, ? super S> action) {
        if (!this._objects.containsKey(type)) {
            String name = type.getSimpleName();
            throw new IllegalStateException(
                    "No " + name.toLowerCase() + "s exist.");
        }
        this._objects.get(type)
                .forEach(file -> action.accept(
                        file, this.safeLoad(type, file)));
    }
    /** Load.TYPE FILE <S>.
     * @return a*/
    private <S extends Serializable> S safeLoad(Class<S> type, String file)
            throws ClassCastException {
        Path filePath = this._directory.resolve(file);

        try {
            Object read;
            read = Utils.deserialize(filePath);
            S loaded = type.cast(read);

            this._loaded.put(file, loaded);
            return loaded;

        } catch (IOException i) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    /** @return a */
    public Path getDirectory() {
        return this._directory;
    }
    /** Start.*/
    public void start() {
        this._initialized = true;

        if (!Files.exists(this.getDirectory())) {
            try {
                Files.createDirectories(this.getDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        HashMap<Class<?>, Set<String>> trck =
                (HashMap<Class<?>, Set<String>>) this.load(NAME);

        if (trck == null) {
            this._loaded.put(NAME, this._objects);
        } else {
            this._objects = trck;
        }

    }
    /** Save progress. */
    public void saveProgress() {
        if (this.isInitialized()) {
            this._initialized = false;
            this.save(NAME, this._objects);
            this._loaded.forEach((file, obj) -> this.save(file, obj));
        }
    }
    /** save. FILE OBJECT */
    private void save(String file, Object object) {
        Path filePath = this._directory.resolve(file);
        File target = new File(filePath.toString());
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdir();
        }
        byte[] content = Utils.serialize(object);
        Utils.writeContents(target, content);
    }
}
