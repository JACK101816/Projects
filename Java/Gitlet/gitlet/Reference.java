
package gitlet;

import java.io.Serializable;

/**
 * @author Wenqu Wang
 */

public class Reference implements Serializable {

    /** The hash of this reference. */
    private String _hash;

    /** Get the hash of this reference.
     * @return str */
    public String getHash() {
        return _hash;
    }

    /** Type of reference. */
    private Objects _type;

    /** Gets the type of this reference.
     * @return obj */
    public Objects getType() {
        return _type;
    }

    /** Construct a reference with TYPE and STRINGHASH.*/
    public Reference(Objects type, String stringHash) {
        _type = type;
        _hash = stringHash;
    }

    /** Construct a reference with HASH.*/
    public Reference(String hash) {
        this(Objects.NONE, hash);
    }

    /** Set the HASH of this reference. */
    public void setHash(String hash) {
        _hash = hash;
    }
}
