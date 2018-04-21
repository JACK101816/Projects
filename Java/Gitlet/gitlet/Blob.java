
package gitlet;

import java.io.Serializable;

/** the Blob class representing a file blob.
 *  @author Wenqu Wang
 */

public class Blob implements Serializable {

    /** A byte[] representing the content of a Blob. */
    private byte[] content;

    /** return the content of this Blob. */
    public byte[] getContent() {
        return this.content;
    }

    /** A constructor taken CONTENTS as input. */
    public Blob(byte[] contents) {
        this.content = contents;
    }
}
