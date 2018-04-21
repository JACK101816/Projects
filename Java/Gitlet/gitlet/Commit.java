
package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import static gitlet.Utils.serialize;
import static gitlet.Utils.sha1;

/** Representing a commit object.
 * @author Wenqu Wang
 */
public class Commit implements Serializable {

    /** the _parent. */
    private String _parent;

    /** the _message. */
    private String _message;

    /** the _date. */
    private Date _date;

    /** The getHash map of filename-sha1. */
    private TreeMap<String, String> _blobs;

    /** get _PARENT.
     * @return _parent */
    String getParent() {
        return _parent;
    }

    /** get _message.
     * @return _message*/
    public String getMessage() {
        return _message;
    }

    /** get _blobs.
     * @return _blobs*/
    public TreeMap<String, String> getBlobs() {
        return _blobs;
    }

    /** Create a commit object with MESSAGES, DATE, PARENT and BLOBS. */
    public Commit(String messages, Date date, String parent,
            TreeMap<String, String> blobs) {

        if (messages == null || messages.isEmpty() || messages.equals("")) {
            System.out.println("Please enter a commit _message.");
            return;
        }

        _parent = parent;
        _message = messages;
        _date = date;
        _blobs = blobs;
    }

    /** Get the String representation of the commit. */
    @Override
    public String toString() {
        SimpleDateFormat s = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        String date = "Date: " + s.format(this._date);
        return "===\n" + "commit " + sha1(serialize(this)) + "\n"
                + date + "\n" + _message + "\n";
    }

    /** Determines if the commit contains a FILENAME.
     * @return boo */
    public boolean containsFile(Object fileName) {
        return _blobs.containsKey(fileName);
    }

    /** Gets its hash from a FILENAME.
     * @return filename*/
    public String get(Object fileName) {
        return this._blobs.get(fileName);
    }
}
