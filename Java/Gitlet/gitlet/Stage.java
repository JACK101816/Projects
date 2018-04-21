package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

/** A object that represents stage area of gitlet.
 * @author Wenqu Wang
 */
public class Stage implements Serializable {

    /** A HashMap of _blobs.*/
    private TreeMap<String, String> _blobs;

    /** A TreeMap of the _staged files.*/
    private TreeMap<String, String> _staged;

    /** A TreeMap of the _removed files.*/
    private TreeMap<String, String> _removed;

    /** File _blobs.
     * @return blob */
    public TreeMap<String, String> getBlobs() {
        return _blobs;
    }

    /** Removed files.
     * @return treemap */
    public TreeMap<String, String> getRemoved() {
        return _removed;
    }

    /** Staged files.
     * @return treemap */
    public TreeMap<String, String> getStaged() {
        return _staged;
    }

    /** Construct a stage. */
    public Stage() {
        _blobs = new TreeMap<>();
        _removed = new TreeMap<>();
        _staged = new TreeMap<>();
    }

    /** Determine whether the staging area has changed since last commit.
     * @return boo*/
    public boolean changed() {
        return _removed.size() != 0 | _staged.size() != 0;
    }

    /** Clear the staging area when committing and return all the _blobs.*/
    public TreeMap<String, String> commitStage() {
        if (!changed()) {
            System.out.println("No changes added to the commit.");
            return null;
        }
        clearStage();
        return _blobs;
    }

    /** Checkout a file with FILENAME, getHash HASH.
     * STAGE means whether to add it to the staging area  */
    public void checkout(String filename, String hash, boolean stage) {
        if (stage) {
            add(filename, hash);
        } else {
            _blobs.put(filename, hash);
            _staged.remove(filename);
            _removed.remove(filename);
        }
    }
    /** Checkout COMMIT,clean staging area. */
    public void checkout(Commit commit) {
        clearStage();
        _blobs = commit.getBlobs();
    }

    /** Add a file blob with name FILENAME and hash HASH to the current stage.
     * If it is contained in _removed, remove it from it.
     */
    public void add(String fileName, String hash) {
        if (_removed.containsKey(fileName)) {
            String removedHash = _removed.remove(fileName);
            _blobs.put(fileName, removedHash);
        } else {
            if (!_blobs.containsKey(fileName)
                    || !_blobs.get(fileName).equals(hash)) {
                _staged.put(fileName, hash);
            }
            _blobs.put(fileName, hash);
        }

    }

    /** Remove a file blob with FILENAME from the current staging area.
     * If COMMIT, add it to _removed
     * If REMOVE, remove it from _blobs */
    public void remove(String fileName, boolean commit, boolean remove) {
        remove(fileName);
        if (commit) {
            _removed.put(fileName, _blobs.get(fileName));
        }
        if (remove) {
            _blobs.remove(fileName);
        }
    }

    /** Remove a file blob with FILENAME from the current staging area.*/
    private void remove(String fileName) {
        if (!_blobs.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        _staged.remove(fileName);
    }

    /** Clear the staging area. */
    private void clearStage() {
        this._removed.clear();
        this._staged.clear();
    }
}
