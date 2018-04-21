
package gitlet;

/**
 * Represents an set of reference objects.
 * Consulted the skeleton file of Proj2 PieceColor.java
 * @author Wenqu Wang
 */
public enum Objects {

    NONE,
    HEAD {
        @Override
        String getBaseDir() {
            return "";
        }
    },
    MESSAGE {
        @Override
        String getBaseDir() {
            return "messages/";
        }
    },
    BRANCH {
        @Override
        String getBaseDir() {
            return "branches/";
        }
    };

    /**@return the base dir. */
    String getBaseDir() {
        return null;
    }
}
