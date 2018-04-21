/**
 *
 */
package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
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
public class ObjectSerializer {

    /**
     * The database name.
     */
    public static final String N = "SERIALIZER";
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

    /**If the serializer is initialized.*/
    private boolean _initialized;

    /**Construct BASE.*/
    public ObjectSerializer(Path base) {
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
    /**<S> TYPE HASH.
     * @return boo*/
    public <S extends Serializable> S get(Class<S> type, String hash) {
        return get2(type, getFile(hash));
    }

    /**<S> TYPE FILE.
     * @return boo*/
    public <S extends Serializable> S get2(Class<S> type, String file) {
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

    /** iterate over the serializer.<S> TYPE ACTION*/
    public <S extends Serializable> void iterate(
            Class<S> type, BiConsumer<? super String, ? super S> action) {
        BiConsumer<? super String, ? super S> hashedAction = (file, com) -> {
            action.accept(file.replaceFirst("/", ""), com);
        };
        iterate2(type, hashedAction);
    }
    /** iterate over the serializer.<S> TYPE ACTION*/
    public <S extends Serializable> void iterate2(
            Class<S> type, final BiConsumer<? super String, ? super S> action) {
        if (!this._objects.containsKey(type)) {
            String name = type.getSimpleName();
            throw new IllegalStateException(
                    "No " + name.toLowerCase() + "s exist.");
        }
        this._objects.get(type)
                .forEach(file ->
                        action.accept(file, this.safeLoad(type, file)));
    }

    /** Load serializer.<S> TYPE FILE.
     * @return S */
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
    /** Contains. TYPE HASH.
     * @return boo*/
    public boolean contains(Class<?> type,
            String hash) {
        return contain(type, getFile(hash));
    }


    /** Contains. FILE
     * @return boo*/
    public boolean contains(String file) {
        for (Class<?> type : this._objects.keySet()) {
            if (this.contains(type, file)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the serial manager contains a given file.
     * @param type
     *            The type to check./
     * @param file
     *            The fuile name.
     * @return If it does.
     */
    public boolean contain(Class<?> type, String file) {
        Set<String> files = this._objects.get(type);
        if (files == null) {
            return false;
        }

        return files.contains(file);
    }

    /**Put. OBJ
     * @return str.
     */
    public String put(Serializable obj) {
        String hash = Utils.sha1(Utils.serialize(obj));
        if (!contains(hash)) {
            add(getFile(hash), obj);
        }
        return hash;
    }
    /** Add. <S>,FILE,TOADD*/
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

    /** Removes a hash from the object store.TYPE HASH.<S>*/
    public <S extends Serializable> void remove(Class<S> type, String hash) {
        remove2(type, getFile(hash));
    }

    /** Removes a hash from the object store.TYPE FILE.<S>*/
    public <S extends Serializable> void remove2(Class<S> type, String file) {
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

    /** @return a */
    public Path getDirectory() {
        return this._directory;
    }
    /**Find.<S> TYPE SEARCH @return a */
    public <S extends Serializable> S find(Class<S> type, String search) {
        Set<String> contents = this._objects.get(type);
        if (contents == null || contents.isEmpty()) {
            return null;
        }

        String delim =
                search.substring(0, Math.min(search.length(), 2));

        String rest = "";
        if (search.length() > 2) {
            rest = search.substring(2, search.length());
        }

        Path base = this.getDirectory();
        try (DirectoryStream<Path> str =
                Files.newDirectoryStream(base, x -> Files.isDirectory(x))) {

            for (Path entry : str) {
                String directoryName = entry.getFileName().toString();

                if (directoryName.startsWith(delim)) {
                    DirectoryStream<Path> substr =
                            Files.newDirectoryStream(entry);

                    for (Path subEntry : substr) {
                        String fileName = subEntry.getFileName().toString();

                        if (fileName.startsWith(rest)) {
                            String targetHash = directoryName + fileName;
                            if (contents.contains(
                                    directoryName + "/" + fileName)) {
                                return this.get(type, targetHash);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** return filename from a string HASH. */
    private static String getFile(String hash) {
        return hash.substring(0, 2) + "/"
                + hash.substring(2, hash.length());
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
                (HashMap<Class<?>, Set<String>>) this.load(N);

        if (trck == null) {
            this._loaded.put(N, this._objects);
        } else {
            this._objects = trck;
        }

    }
    /** Save progress. */
    public void saveProgress() {
        if (this.isInitialized()) {
            this._initialized = false;
            this.save(N, this._objects);
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
