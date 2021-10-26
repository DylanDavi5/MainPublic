package gitlet;



import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;




/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author Dylan Davis
 */
public class Repository {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** A directory that stores the commits*/
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    /** staging area for adding files */
    public static final File STAGEDTOADD = join(GITLET_DIR, "StagedToAdd");
    /** staging area for adding files */
    public static final File STAGEDTOREMOVE = join(GITLET_DIR, "StagedToRemove");
    /** a directory that stores all the blobs */
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    /** a directory that stores all the branches */
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");
    /** pointer to head branch */
    public static final File HEAD = join(GITLET_DIR, "HEAD");



    /** initializes a gitlet repository and creates an initial commit*/
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.print("A Gitlet version-control system already"
                    + " exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        BRANCH_DIR.mkdir();
        Commit initialCommit = new Commit("initial commit", null);

        /** pointer to master branch */
        File master = join(BRANCH_DIR, "master");

        try {
            master.createNewFile();
            HEAD.createNewFile();
            STAGEDTOADD.createNewFile();
            STAGEDTOREMOVE.createNewFile();
        } catch (IOException e) {
            new GitletException();
        }

        /** make master and head pointers track hash of initial commit */
        String commitHash = sha1(serialize(initialCommit));
        writeContents(master, commitHash);
        writeObject(HEAD, master);

        /** add initial commit to commit dir */
        File initalCommitFile = join(COMMIT_DIR, commitHash);
        writeObject(initalCommitFile, initialCommit);

        /** using a tree map to track the staging area*/
        clearStagingArea();
    }

    /** adds a file to the addition staging area */
    public static void add(String fileName) {
        add(fileName, getHeadCommit());
    }

    private static void add(String fileName, Commit givenCommit) {
        File fileAdded = join(CWD, fileName);
        if (!fileAdded.exists()) {
            System.out.print("File does not exist.");
            System.exit(0);
        }

        /** write contents of staged file to a blob */
        byte[] blob = readContents(fileAdded);
        String blobHash = sha1(blob);
        File blobFile = join(BLOB_DIR, blobHash);
        writeContents(blobFile, blob);

        TreeMap<File, String> stagingArea = readObject(STAGEDTOADD, TreeMap.class);
        TreeSet<File> stagingRemoval = readObject(STAGEDTOREMOVE, TreeSet.class);

        TreeMap<File, String> currentlyTracked = givenCommit.getTrackedFiles();
        /** add a mapping of file in CWD to hash of blob which is the file name of the blob
         * if the file added is already being tracked in current commit simply unremove it */
        if (stagingArea.containsKey(fileAdded)) {
            stagingArea.replace(fileAdded, blobHash);
        } else if (!blobHash.equals(currentlyTracked.get(fileAdded))) {
            stagingArea.put(fileAdded, blobHash);
        }
        /** if file is staged for removal it will no longer be staged for removal */
        if (stagingRemoval.contains(fileAdded)) {
            stagingRemoval.remove(fileAdded);
        }

        writeObject(STAGEDTOADD, stagingArea);
        writeObject(STAGEDTOREMOVE, stagingRemoval);
    }

    /** removes files and stages it for removal */
    public static void remove(String fileName) {
        remove(fileName, getHeadCommit());
    }

    private static void remove(String fileName, Commit givenCommit) {
        File fileRemoved = join(CWD, fileName);

        TreeMap<File, String> stagingArea = readObject(STAGEDTOADD, TreeMap.class);
        TreeSet<File> stagingRemoval = readObject(STAGEDTOREMOVE, TreeSet.class);

        TreeMap<File, String> headCommitTrackedFiles = givenCommit.getTrackedFiles();

        /** if file is not staged and not tracked in head commit then
         * it prints error message and exits*/
        if (!stagingArea.containsKey(fileRemoved)
                && !headCommitTrackedFiles.containsKey(fileRemoved)) {
            System.out.print("No reason to remove the file.");
            System.exit(0);
        }

        /** removes file from staging area if it is there */
        if (stagingArea.containsKey(fileRemoved)) {
            stagingArea.remove(fileRemoved);
        }
        /** stages file for removal if they are tracked in current commit */
        TreeMap<File, String> currentlyTrackedFiles = givenCommit.getTrackedFiles();
        if (currentlyTrackedFiles.containsKey(fileRemoved)) {
            stagingRemoval.add(fileRemoved);
            restrictedDelete(fileRemoved);
        }


        writeObject(STAGEDTOADD, stagingArea);
        writeObject(STAGEDTOREMOVE, stagingRemoval);
    }

    /** creates a new commit*/
    public static void commit(String message) {
        if (message.equals("")) {
            System.out.print("Please enter a commit message.");
            System.exit(0);
        }

        TreeMap<File, String> filesAdded = readObject(STAGEDTOADD, TreeMap.class);
        TreeSet<File> filesRemoved = readObject(STAGEDTOREMOVE, TreeSet.class);
        /** check if any files are staged for addition or removal */
        if (filesAdded.isEmpty() && filesRemoved.isEmpty()) {
            System.out.print("No changes added to the commit.");
            System.exit(0);
        }
        /** read parent commit */
        File currentBranch = readObject(HEAD, File.class);
        String parentCommitAddress = readContentsAsString(currentBranch);

        /** create new commit and update with staged files */
        Commit newCommit = new Commit(message, parentCommitAddress);
        newCommit.updateCommit(filesAdded, filesRemoved);
        byte[] newCommitSerialized = serialize(newCommit);
        String newCommitHash = sha1(newCommitSerialized);
        File newCommitFile = join(COMMIT_DIR, newCommitHash);
        writeContents(newCommitFile, newCommitSerialized);



        /** update head branch */
        writeContents(currentBranch, newCommitHash);

        /** clear staging area with blank hash map */
        clearStagingArea();
    }

    /** prints out a log of the current branch starting from head commit until initial commit*/
    public static void log() {
        Commit headCommit = getHeadCommit();
        while (headCommit.getParentAddress() != null) {
            headCommit.printCommit();
            headCommit = headCommit.getParentCommit();
        }
        headCommit.printCommit();
    }

    /** prints a log of every commit ever made */
    public static void globalLog() {
        List<String> allCommits = plainFilenamesIn(COMMIT_DIR);
        for (String s : allCommits) {
            Commit c = readObject(join(COMMIT_DIR, s), Commit.class);
            c.printCommit();
        }
    }

    /** prints the ids of all the commits with the given message */
    public static void find(String message) {
        List<String> allCommits = plainFilenamesIn(COMMIT_DIR);
        boolean printed = false;
        for (String s : allCommits) {
            Commit c = readObject(join(COMMIT_DIR, s), Commit.class);
            if (c.getMessage().equals(message)) {
                System.out.println(c.getCommitId());
                printed = true;
            }
        }
        if (!printed) {
            System.out.print("Found no commit with that message.");
        }
    }

    /** prints our the status of the gitlet directory*/
    public static void status() {
        /** BRANCHES */
        File headBranch = readObject(HEAD, File.class);
        List<String> branches = plainFilenamesIn(BRANCH_DIR);
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            File branchFile = join(BRANCH_DIR, branch);
            if (branchFile.equals(headBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();

        /** STAGED FILES */
        TreeMap<File, String> stagedFiles = readObject(STAGEDTOADD, TreeMap.class);
        System.out.println("=== Staged Files ===");
        for (File staged : stagedFiles.keySet()) {
            System.out.println(staged.getName());
        }
        System.out.println();

        /** REMOVED FILES */
        TreeSet<File> removedFiles = readObject(STAGEDTOREMOVE, TreeSet.class);
        System.out.println("=== Removed Files ===");
        for (File removed : removedFiles) {
            System.out.println(removed.getName());
        }
        System.out.println();

        /** MODIFIED NOT STAGED FOR COMMIT */
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        /** UNTRACKED FILES */
        System.out.println("=== Untracked Files ===");
        System.out.println();

    }

    /** checks out a specific file from given commit */
    public static void checkoutFile(String fileName, String commitAddress) {
        commitAddress = getFullId(commitAddress);
        File commitFile = join(COMMIT_DIR, commitAddress);
        if (!commitFile.exists()) {
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }

        Commit commitObj = readObject(commitFile, Commit.class);
        TreeMap<File, String> commitTrackedFiles = commitObj.getTrackedFiles();
        File targetFile = join(CWD, fileName);
        if (!commitTrackedFiles.containsKey(targetFile)) {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }
        /** get the information stored in the blob */
        String blobHash = commitTrackedFiles.get(targetFile);
        File blobFile = join(BLOB_DIR, blobHash);
        byte[] blobContents = readContents(blobFile);

        writeContents(targetFile, blobContents);
    }

    /** when checkoutfile is not given a commit address is uses the head commit by default*/
    public static void checkoutFile(String fileName) {
        File currentBranch = readObject(HEAD, File.class);
        String commitAddress = readContentsAsString(currentBranch);
        checkoutFile(fileName, commitAddress);
    }

    /** checks out given branch */
    public static void checkoutBranch(String branchName) {
        File givenBranch = join(BRANCH_DIR, branchName);
        if (!givenBranch.exists()) {
            System.out.print("No such branch exists.");
            System.exit(0);
        }
        Commit givenBranchCommit = getBranchCommit(givenBranch);
        TreeMap<File, String> givenBranchTrackedFiles = givenBranchCommit.getTrackedFiles();
        File headBranch = readObject(HEAD, File.class);
        Commit headBranchCommit = getHeadCommit();
        TreeMap<File, String> headBranchTrackedFiles = headBranchCommit.getTrackedFiles();
        if (headBranch.equals(givenBranch)) {
            System.out.print("No need to checkout the current branch.");
            System.exit(0);
        }
        /** checks out commit at given branch head */
        checkoutCommit(givenBranchCommit);
        /** updates Head pointer*/
        writeObject(HEAD, givenBranch);
    }

    /** checks out given commit */
    public static void checkoutCommit(Commit givenCommit) {
        TreeMap<File, String> commitFiles = givenCommit.getTrackedFiles();
        TreeMap<File, String> headFiles = getHeadCommit().getTrackedFiles();
        /** checks if a file would be overwritten that is not tracked on head branch*/
        for (File checkedOut : commitFiles.keySet()) {
            if (checkedOut.exists() && !headFiles.containsKey(checkedOut)) {
                System.out.print("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        /** write files tracked by branch to CWD */
        for (File checkedOut : givenCommit.getTrackedFiles().keySet()) {
            if (!checkedOut.exists()) {
                try {
                    checkedOut.createNewFile();
                } catch (IOException e) {
                    new GitletException();
                }
            }
            File blobFile = join(BLOB_DIR, commitFiles.get(checkedOut));
            byte[] blobContents = readContents(blobFile);
            writeContents(checkedOut, blobContents);
        }
        /** deletes files in current branch but not in checked out branch*/
        for (File f : headFiles.keySet()) {
            if (!commitFiles.containsKey(f)) {
                restrictedDelete(f);
            }
        }
        clearStagingArea();
    }

    /** creates a new branch */
    public static void branch(String branchName) {
        File newBranch = join(BRANCH_DIR, branchName);
        if (newBranch.exists()) {
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            newBranch.createNewFile();
        } catch (IOException e) {
            new GitletException();
        }
        String headCommitHash = sha1(serialize(getHeadCommit()));
        writeContents(newBranch, headCommitHash);
    }

    /** deletes a branch with the given name */
    public static void removeBranch(String branchName) {
        File removedBranch = join(BRANCH_DIR, branchName);
        if (!removedBranch.exists()) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        }
        File currentBranch = readObject(HEAD, File.class);
        if (removedBranch.equals(currentBranch)) {
            System.out.print("Cannot remove the current branch.");
            System.exit(0);
        }
        removedBranch.delete();
    }

    /** checks out all files at given commit */
    public static void reset(String commitId) {
        File commitFile = join(COMMIT_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }
        Commit givenCommit = readObject(commitFile, Commit.class);
        checkoutCommit(givenCommit);
        File currentBranch = readObject(HEAD, File.class);
        writeContents(currentBranch, commitId);
    }


    /***** MERGE *****/

    /** creates a merge commit by merging the given branch with the head branch*/
    public static void merge(String otherBranchName) {
        TreeMap<File, String> filesAdded = readObject(STAGEDTOADD, TreeMap.class);
        TreeSet<File> filesRemoved = readObject(STAGEDTOREMOVE, TreeSet.class);
        if (!filesRemoved.isEmpty() || !filesAdded.isEmpty()) {
            System.out.print("You have uncommitted changes.");
            System.exit(0);
        }
        File otherBranch = join(BRANCH_DIR, otherBranchName);
        if (!otherBranch.exists()) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit otherBranchCommit = getBranchCommit(otherBranch);
        Commit headCommit = getHeadCommit();
        Commit splitPoint = latestCommonAncestor(headCommit, otherBranchCommit);
        TreeMap<File, String> headTrackedFiles = headCommit.getTrackedFiles();
        TreeMap<File, String> otherTrackedFiles = otherBranchCommit.getTrackedFiles();
        TreeMap<File, String> splitTrackedFiles = splitPoint.getTrackedFiles();
        for (File checkedOut : otherTrackedFiles.keySet()) {
            if (checkedOut.exists() && !headTrackedFiles.containsKey(checkedOut)) {
                System.out.print("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        if (otherBranch.equals(readObject(HEAD, File.class))) {
            System.out.print("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (splitPoint.equals(otherBranchCommit)) {
            System.out.print("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPoint.equals(headCommit)) {
            System.out.print("Current branch fast-forwarded.");
            checkoutBranch(otherBranchName);
            System.exit(0);
        }
        TreeSet<File> allFiles = new TreeSet<>();
        allFiles.addAll(headTrackedFiles.keySet());
        allFiles.addAll(otherTrackedFiles.keySet());
        allFiles.addAll(splitTrackedFiles.keySet());
        boolean conflictOccurred = false; //tracks if merge conflict happened
        /** loops through all the files that need to be addressed*/
        for (File f : allFiles) {
            String headFile = headTrackedFiles.get(f);
            String otherFile = otherTrackedFiles.get(f);
            String splitFile = splitTrackedFiles.get(f);
            int mergeResult = mergeOutcome(headFile, otherFile, splitFile);
            if (mergeResult == 0) {
                filesAdded.put(f, otherTrackedFiles.get(f));
            } else if (mergeResult == 1) {
                remove(f.getName(), headCommit);
            } else if (mergeResult == 2) {
                String conflictHash = conflict(headCommit, otherBranchCommit, f);
                filesAdded.put(f, conflictHash);
                conflictOccurred = true;
            }
        }
        filesRemoved = readObject(STAGEDTOREMOVE, TreeSet.class);
        /** create new commit and update with staged files */
        File currentBranch = readObject(HEAD, File.class);
        String currentBranchName = currentBranch.getName();
        String message = "Merged " + otherBranchName + " into " + currentBranchName + ".";
        if (conflictOccurred) {
            System.out.println("Encountered a merge conflict.");
        }
        Commit newCommit = new Commit(message, headCommit.getCommitId(),
                otherBranchCommit.getCommitId());
        newCommit.updateCommit(filesAdded, filesRemoved);
        byte[] newCommitSerialized = serialize(newCommit);
        String newCommitHash = sha1(newCommitSerialized);
        File newCommitFile = join(COMMIT_DIR, newCommitHash);
        writeContents(newCommitFile, newCommitSerialized);
        /** update head branch */
        writeContents(currentBranch, newCommitHash);
        /** clear staging area with blank hash map */
        clearStagingArea();
        checkoutCommit(newCommit);
    }

    /** reutnrs the an int to represent what should happen
     * 0 -> add other file, 1 -> remove file, 2 -> conflict, 3 -> do nothing*/
    private static int mergeOutcome(String headFile, String otherFile, String splitFile) {
        if ((headFile == null && splitFile == null && otherFile != null)
                || (otherFile != null && headFile != null && headFile.equals(splitFile)
                && !otherFile.equals(splitFile))) {
            return 0;
        } else if (headFile != null && otherFile == null && headFile.equals(splitFile)) {
            return 1;
        } else if ((splitFile == null && headFile != null && otherFile != null)
                || (splitFile != null && !splitFile.equals(otherFile)
                && !splitFile.equals(headFile))) {
            if ((headFile != null && !headFile.equals(otherFile))
                    || (otherFile != null && !otherFile.equals(headFile))) {
                return 2;
            }
        }
        return 3;
    }

    /** writes a conflict to the given file */
    private static String conflict(Commit head, Commit other, File f) {
        /** contents of file*/
        String conflictContents = "<<<<<<< HEAD\n";
        String headContents = head.getFileContentsAsString(f);
        String otherContents = other.getFileContentsAsString(f);
        if (headContents != null) {
            conflictContents = conflictContents.concat(headContents);
        }
        conflictContents = conflictContents.concat("=======\n");
        if (otherContents != null) {
            conflictContents = conflictContents.concat(otherContents);
        }
        conflictContents = conflictContents.concat(">>>>>>>");

        conflictContents = "<<<<<<< HEAD\n" + headContents + "=======\n"
                + otherContents + ">>>>>>>\n";

        /** write file to blob*/
        String blobHash = sha1(conflictContents);
        File blobFile = join(BLOB_DIR, blobHash);
        writeContents(blobFile, conflictContents);
        

        return blobHash;
    }

    /** returns the latest common ancestor between two given Commits */
    private static Commit latestCommonAncestor(Commit firstCommit, Commit secondCommit) {
        TreeSet<String> ancestors = ancestorCommitIds(firstCommit);
        ArrayDeque<Commit> commitQueue = new ArrayDeque<>();
        commitQueue.addLast(secondCommit);
        while (!commitQueue.isEmpty()) {
            Commit currentCommit = commitQueue.removeFirst();
            if (ancestors.contains(currentCommit.getCommitId())) {
                return currentCommit;
            }
            Commit firstParent = currentCommit.getParentCommit();
            Commit secondParent = currentCommit.getSecondParentCommit();
            if (firstParent != null) {
                commitQueue.addLast(firstParent);
            }
            if (secondParent != null) {
                commitQueue.addLast(secondParent);
            }
        }
        /**in theory this should never happen*/
        System.out.println("two commits not in same commit graph");
        return null;
    }

    /** returns a set with all the ancestors of the given commit including the given commit*/
    private static TreeSet<String> ancestorCommitIds(Commit givenCommit) {
        return ancestorCommitId(givenCommit, new TreeSet<String>());
    }

    /** helper */
    private static TreeSet<String> ancestorCommitId(Commit givenCommit, TreeSet<String> resultSet) {
        Commit firstParent = givenCommit.getParentCommit();
        Commit secondParent = givenCommit.getSecondParentCommit();
        resultSet.add(givenCommit.getCommitId());
        /* potentially optimizable as it can double count */
        if (firstParent != null) {
            resultSet.addAll(ancestorCommitId(firstParent, resultSet));
        }
        if (secondParent != null) {
            resultSet.addAll(ancestorCommitId(secondParent, resultSet));
        }
        return resultSet;
    }



    /***** HELPER METHODS *****/

    /** helper method that gets head commit */
    private static Commit getHeadCommit() {
        File currentBranch = readObject(HEAD, File.class);
        String headCommitAdress = readContentsAsString(currentBranch);
        File headCommitFile = join(COMMIT_DIR, headCommitAdress);
        Commit headCommit = readObject(headCommitFile, Commit.class);
        return headCommit;
    }

    /** gets the commit associated with the given branch */
    private static Commit getBranchCommit(File branch) {
        String branchCommitAdress = readContentsAsString(branch);
        File branchCommitFile = join(COMMIT_DIR, branchCommitAdress);
        Commit branchCommit = readObject(branchCommitFile, Commit.class);
        return branchCommit;
    }

    /** clears the staging area*/
    private static void clearStagingArea() {
        TreeMap<File, String> filesAdded = new TreeMap<>();
        writeObject(STAGEDTOADD, filesAdded);
        TreeSet<File> filesRemoved = new TreeSet<>();
        writeObject(STAGEDTOREMOVE, filesRemoved);
    }

    /** get full commit id given shortened id */
    private static String getFullId(String shortenedId) {
        List<String> allCommitIds = plainFilenamesIn(COMMIT_DIR);
        String actualId = shortenedId;
        int shortendIdLength = shortenedId.length();
        if (shortendIdLength == 40) {
            return actualId;
        }
        for (String s : allCommitIds) {
            String abbreviatedCommit = s.substring(0, shortendIdLength);
            if (abbreviatedCommit.equals(shortenedId)) {
                actualId = s;
            }
        }
        return actualId;
    }
}
