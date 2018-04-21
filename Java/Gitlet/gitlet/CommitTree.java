package gitlet;

import static gitlet.Objects.BRANCH;
import static gitlet.Objects.HEAD;
import static gitlet.Objects.MESSAGE;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.io.File;

/**
 * A gitlet commit tree.
 * @author Wenqu Wang
 */
public class CommitTree {

    /** The working directory.*/
    private Path _wkDir;

    /** The gitlet directory.*/
    private Path _repository;

    /** An object serializer.*/
    private ObjectSerializer _objSer;

    /** A reference serializer.*/
    private ReferenceSerializer _refSer;

    /** The database name. */
    public static final String NAME = "SERIALIZER";

    /** A hashmap of loaded objects.*/
    private HashMap<String, Serializable> _loaded;

    /** A hashmap of all objects.*/
    protected HashMap<Class<?>, Set<String>> _objects;

    /** The .gitlet directory. */
    private Path _directory;

    /** If CommitTree is initialized. */
    private boolean _initialized;

    /** Creates a CommitTree at WKDIR. */
    public CommitTree(String wkDir) {
        _directory = Paths.get(wkDir).resolve(".gitlet");
        _initialized = false;
        _loaded = new HashMap<>();
        _objects = new LinkedHashMap<>();
        _wkDir = getDirectory().getParent();
        _repository = getDirectory();
        _objSer = new ObjectSerializer(_repository.resolve("objects/"));
        _refSer = new ReferenceSerializer(_repository.resolve("reference/"));
        File dir = new File(_repository.toString());
        if (dir.exists()) {
            this.start();
        }

    }

    /** get the current stage.
     * @return a */
    public Stage stage() {
        return get(Stage.class, "stage");
    }
    /** get <S> TYPE,FILE.
     * @return a */
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

    /** Load FILE of TYPE. <S>
     * @return a */
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

    /** @return a. */
    public ObjectSerializer objects() {
        return this._objSer;
    }

    /** @return a. */
    public ReferenceSerializer refs() {
        return _refSer;
    }

    /** @return a .*/
    public boolean isInitialized() {
        return this._initialized;
    }

    /** Initializes a commit tree. */
    public void init() {
        if (this.isInitialized()) {
            System.out.println(
                    "A gitlet version-control system "
                            + "already exists in the current directory.");
            return;
        }
        start();
        String initial = objects().put(
                new Commit("initial commit", new Date(0), "", new TreeMap<>()));
        refs().add(BRANCH, "master", new Reference(initial));
        refs().add(HEAD, new Reference(BRANCH, "master"));
        refs().add(MESSAGE, "initial", new Reference(initial));
        add("stage", new Stage());
    }

    /** FILE TOADD. <S>*/
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

    /** Checks out COMMIT. */
    public void checkout(Commit commit) {

        Stage stage = this.stage();
        try {
            for (Path entry : Files.newDirectoryStream(_wkDir)) {
                if (!Files.isDirectory(entry)) {
                    String fileName = entry.getFileName().toString();
                    if (commit.containsFile(fileName)
                            && (!stage.getBlobs().containsKey(fileName)
                            || stage.getStaged().containsKey(fileName))) {
                        System.out.println("There is an untracked file "
                                + "in the way; delete it or add it first.");
                        return;
                    }
                }
                String name = entry.getFileName().toString();
                if (stage.getBlobs().containsKey(name)) {
                    Files.delete(entry);
                }
            }
        } catch (IOException e) {
            return;
        }
        commit.getBlobs().forEach((file, hash) -> {
                Blob blob = objects().get(Blob.class, hash);
                Path filePath = _wkDir.resolve(file);
                File target = new File(filePath.toString());
                Utils.writeContents(target, blob.getContent());
            });
        stage.checkout(commit);
    }

    /** Checks out a given FILENAME in a COMMIT.
     * If STAGE, add it to staging area. */
    public void checkout(Commit commit, String filename, boolean stage) {
        String hash = commit.get(filename);
        if (hash == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob blob = objects().get(Blob.class, hash);
        Path filePath = this._wkDir.resolve(filename);
        File target = new File(filePath.toString());
        Utils.writeContents(target, blob.getContent());
        Stage stage1 = this.stage();
        stage1.checkout(filename, hash, stage);
    }

    /** Assign HEAD to a commit with MESSAGE and BLOBS, return its SHA-1 hash.*/
    public String head(String message,
                       TreeMap<String, String> blobs) {
        String headHash = refs().resolve(HEAD);
        String commitHash =
                objects().put(new Commit(message, new Date(), headHash, blobs));

        getBranch().setHash(commitHash);
        return commitHash;
    }

    /** get the current branch.
     * @return a*/
    public Reference getBranch() {
        Reference head = this.refs().get(HEAD);
        return refs().get(BRANCH, head.getHash());
    }

    /** set the current BRANCH to head. */
    public void setBranch(String branch) {
        refs().get(BRANCH, branch);
        refs().get(HEAD).setHash(branch);
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
        this._refSer.start();
        this._objSer.start();

    }

    /** Save progress. */
    public void saveProgress() {
        if (this.isInitialized()) {
            this._initialized = false;
            this.save(NAME, this._objects);
            this._loaded.forEach((file, obj) -> save(file, obj));
            this._refSer.saveProgress();
            this._objSer.saveProgress();
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

    /** Get the working directory.
     * @return a*/
    public Path getWkDir() {
        return this._wkDir;
    }
}
