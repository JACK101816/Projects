package gitlet;
import static gitlet.Objects.HEAD;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 * @author Wenqu Wang
 */
public class Main {
    /** Check whether COMMITTREE is initialized.
     * @return a*/
    private static Boolean initiationCheck(CommitTree commitTree) {
        return commitTree.isInitialized();
    }
    /** Check whether ARGS has EXPECTED length.
     * @return a*/
    private static Boolean commandCheck(int expected, String[] args) {
        if (args == null) {
            return expected == 0;
        }
        return args.length == expected;
    }

    /** Get required ARGS.
     * @return a*/
    public static String[] getToken(String... args) {
        String[] token = new String[0];
        if (args.length == 2) {
            token = new String[]{args[1]};
        }
        if (args.length == 3) {
            token = new String[]{args[1], args[2]};
        }
        if (args.length == 4) {
            token = new String[]{args[1], args[2], args[3]};
        }
        return token;
    }

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        CommitTree t1 = new CommitTree(System.getProperty("user.dir"));
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String[] token = getToken(args);
        if (args[0].equals("init")) {
            t1.init();
        } else {
            if (!initiationCheck(t1)) {
                System.out.println("Not in an initialized Gitlet directory");
                return;
            }
            switch (args[0]) {
            case "add":
                Commands.add(t1, token[0], token.length);
                break;
            case "commit":
                Commands.commit(t1, token[0], token.length);
                break;
            case "rm":
                Commands.remove(t1, token[0],
                        t1.objects().get(Commit.class,
                                t1.refs().resolve(HEAD)), token.length);
                break;
            case "log":
                Commands.log(t1, token.length);
                break;
            case "global-log":
                Commands.globalLog(t1, token.length);
                break;
            case "find":
                Commands.find(t1, token[0], token.length);
                break;
            case "status":
                Commands.status(t1, token.length);
                break;
            case "checkout":
                Commands.checkout(t1, token, token.length);
                break;
            case "branch":
                Commands.branch(t1, token[0], token.length);
                break;
            case "rm-branch":
                Commands.removeBranch(t1, token[0], token.length);
                break;
            case "reset":
                Commands.reset(t1, token[0], token.length);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
        }
        t1.saveProgress();
    }
}
