package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Dylan Davis
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.print("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.print("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                checkOperands(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length == 1) {
                    System.out.print("Please enter a commit message.");
                    System.exit(0);
                }
                checkOperands(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                checkOperands(args, 2);
                Repository.remove(args[1]);
                break;
            case "log":
                checkOperands(args, 1);
                Repository.log();
                break;
            case "global-log":
                checkOperands(args, 1);
                Repository.globalLog();
                break;
            case "find":
                checkOperands(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                checkOperands(args, 1);
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) { //checkout branch
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3) { //checkout file from current commit
                    if (!args[1].equals("--")) {
                        System.out.print("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4) { //checkout file from specified commit
                    if (!args[2].equals("--")) {
                        System.out.print("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutFile(args[3], args[1]);
                } else {
                    System.out.print("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                checkOperands(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkOperands(args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                checkOperands(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkOperands(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.print("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void checkOperands(String[] args, int length) {
        if (args.length != length) {
            System.out.print("Incorrect operands.");
            System.exit(0);
        }
        if (!Repository.GITLET_DIR.exists()) {
            System.out.print("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }


}
