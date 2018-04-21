package gitlet;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.ArrayList;
import static gitlet.Objects.*;

/**
 * All the commands for Gitlet, the tiny stupid version-control system.
 * @author Wenqu Wang
 */
public class Commands {
    /** The magic number. */
    private static final int MAGIC = 40;

    /** The add command, taken TREE and FILENAME as inputs. Expected LENGTH.*/
    public static void add(CommitTree tree, String fileName, int length) {
        if (length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        String file = fileName;
        File target = new File(file);
        if (!target.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob fileBlob = new Blob(Utils.readContents(target));
        String blobHash = tree.objects().put(fileBlob);
        Stage stage = tree.stage();
        stage.add(file, blobHash);

    }
    /** the commit command, taken TREE and MESSAGE as inputs. Expected LENGTH.*/
    public static void commit(CommitTree tree, String message, int length) {
        if (length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Stage stage = tree.stage();
        TreeMap<String, String> blobs = stage.commitStage();
        tree.head(message, blobs);
    }

    /** the remove command, taken TREE, FILE and HEAD as inputs.
     * Expected LENGTH.*/
    public static void remove(CommitTree tree, String file,
                              Commit head, int length) {
        if (length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Stage stage = tree.stage();

        if (head.containsFile(file)) {
            stage.remove(file, true, true);
            File target = new File(file);
            if (target.exists()) {
                target.delete();
            }
        } else {
            stage.remove(file, false, true);
        }
    }
    /** the log command, taken TREE as input. Expected LENGTH.*/
    public static void log(CommitTree tree, int length) {
        if (length != 0) {
            System.out.println("Incorrect operands.");
            return;
        }
        String commitHash = tree.refs().resolve(HEAD);
        while (commitHash != null && !commitHash.equals("")) {
            Commit commit = tree.objects().get(Commit.class, commitHash);
            System.out.println(commit.toString());
            commitHash = commit.getParent();
        }
    }
    /** the globalLog command, taken TREE as input. Expected LENGTH.*/
    public static void globalLog(CommitTree tree, int length) {
        if (length != 0) {
            System.out.println("Incorrect operands.");
            return;
        }
        tree.objects().iterate(Commit.class, (hash, com) -> {
                System.out.println(com);
            });

    }
    /** the find command, taken TREE and MESSAGE as inputs. Expected LENGTH.*/
    public static void find(final CommitTree tree, String message, int length) {
        if (length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        final int[] iter = new int[] { 0 };
        tree.objects().iterate(Commit.class, (hash, com) -> {
                if (com.getMessage().equals(message)) {
                    iter[0]++;
                    System.out.println(hash);
                }
            });
        if (iter[0] == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }

    }
    /** the status command, taken TREE as input.Expected LENGTH.
     * parts came from https://stackoverflow.com/questions/1318980/
     * how-to-iterate-over-a-treemap */
    public static void status(CommitTree tree, int length) {
        if (length != 0) {
            System.out.println("Incorrect operands.");
            return;
        }
        String currentBranch = tree.refs().get(HEAD).getHash();
        System.out.println("=== Branches ===");

        tree.refs().iterate(BRANCH, (String name, Reference branch) -> {
                if (name.equals(currentBranch)) {
                    System.out.print('*');
                }
                System.out.println(name);
            });

        Stage stage = tree.stage();
        Path workingDir = tree.getWkDir();

        System.out.println("\n=== Staged Files ===");
        for (Map.Entry<String, String> entry : stage.getStaged().entrySet()) {
            String key = entry.getKey();
            System.out.println(key);
        }

        System.out.println("\n=== Removed Files ===");
        for (Map.Entry<String, String> entry : stage.getRemoved().entrySet()) {
            String key = entry.getKey();
            System.out.println(key);
        }

        try {
            difference(stage, workingDir);
        } catch (IOException e) {
            return;
        }

    }

    /**
     * Outputs the difference between the STAGE and the WORKINGDIR.
     */
    private static void difference(Stage stage,
                                   Path workingDir) throws IOException {
        HashMap<String, String> curBlobs = new HashMap<String, String>();

        for (Path entry : Files.newDirectoryStream(workingDir)) {
            if (!Files.isDirectory(entry)) {
                File target = new File(entry.toString());
                String name = entry.getFileName().toString();
                Blob entryBlob = new Blob(Utils.readContents(target));
                curBlobs.put(name, Utils.sha1(Utils.serialize(entryBlob)));
            }
        }

        List<String> untracked = new ArrayList<>();
        List<String> notStaged = new ArrayList<>();

        curBlobs.forEach((name, hash) -> {
                if (!stage.getBlobs().containsKey(name)) {
                    untracked.add(name);
                } else if (!stage.getBlobs().get(name).equals(hash)) {
                    notStaged.add(name + " (modified)");
                }
            });

        stage.getBlobs().forEach((name, hash) -> {
                if (!curBlobs.containsKey(name)) {
                    notStaged.add(name + " (deleted)");
                }
            });

        notStaged.sort(String::compareTo);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String name : notStaged) {
            System.out.println(name);
        }

        untracked.sort(String::compareTo);
        System.out.println("\n=== Untracked Files ===");
        for (String name : untracked) {
            System.out.println(name);
        }
        System.out.println("");
    }
    /** the checkout command, taken TREE and ARGS as inputs.Expected LENGTH.*/
    public static void checkout(CommitTree tree, String[] args, int length) {
        try {
            if (length < 1 | length > 3) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (args.length == 1) {
                checkoutBranch(tree, args[0]);
            } else if (args.length == 2) {
                if (!args[0].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                checkoutFile(tree, tree.refs().resolve(HEAD), args[1]);
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                checkoutFile(tree, args[0], args[2]);
            }
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
    /** checkout file, taken TREE COMMITHASH and FILENAME as inputs.*/
    public static void checkoutFile(CommitTree tree, String commitHash,
                                    String filename) {
        Commit toCheck;

        if (commitHash.length() == MAGIC) {
            toCheck = tree.objects().get(Commit.class, commitHash);
        } else {
            toCheck = tree.objects().find(Commit.class, commitHash);
        }

        if (toCheck == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        tree.checkout(toCheck, filename, false);
    }

    /** checkout branch taken TREE and BRANCHNAME as inputs.*/
    public static void checkoutBranch(CommitTree tree, String branchName) {
        Reference branch = tree.refs().get(BRANCH, branchName);
        if (branch.equals(tree.getBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        String commitHash = branch.getHash();
        tree.checkout(tree.objects().get(Commit.class, commitHash));
        tree.setBranch(branchName);
        tree.getBranch().setHash(commitHash);

    }
    /** The branch command, taken TREE and BRANCHNAME as inputs.
     * Expected LENGTH.*/
    public static void branch(CommitTree tree,
                              String branchName, int length) {
        if (length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        String headCommit = tree.refs().resolve(HEAD);
        tree.refs().add(BRANCH, branchName, new Reference(headCommit));

    }
    /** The re-branch command, taken TREE and BRANCHNAME as inputs.
     *  Expected LENGTH.*/
    public static void removeBranch(CommitTree tree,
                                    String branchName, int length) {
        if (length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        String branch = branchName;
        if (tree.refs().get(HEAD).getHash().equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        tree.refs().remove(BRANCH, branchName);
    }
    /** The reset command, taken TREE and COMMITHASH as inputs.
     * Expected LENGTH.*/
    public static void reset(CommitTree tree, String commitHash, int length) {
        try {
            if (length != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            Commit toCheck;
            if (commitHash.length() == MAGIC) {
                toCheck = tree.objects().get(Commit.class, commitHash);
            } else {
                toCheck = tree.objects().find(Commit.class, commitHash);
            }

            if (toCheck == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            tree.checkout(toCheck);
            tree.getBranch().setHash(Utils.sha1(Utils.serialize(toCheck)));
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
