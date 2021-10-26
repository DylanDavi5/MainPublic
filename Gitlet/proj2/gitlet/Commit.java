package gitlet;




import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *
 *
 *  @author Dylan Davis
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /**date the commit was made*/
    private Date date;
    /** parent of this commit*/
    private String parent;
    /** possible second parent*/
    private String secondParent;
    /** all files tracked by commit*/
    private TreeMap<File, String> trackedFiles;


    Commit(String message, String parent, String secondParent) {
        this.message = message;
        this.parent = parent;
        this.secondParent = secondParent;
        if (parent == null) {
            this.date = new Date(0);
            trackedFiles = new TreeMap<File, String>();
        } else {
            this.date = new Date();
            /** copy the tracked files from parent */
            File parentCommitFile = join(Repository.COMMIT_DIR, parent);
            Commit parentCommitObj = readObject(parentCommitFile, Commit.class);
            this.trackedFiles = parentCommitObj.getTrackedFiles();
        }
    }

    Commit(String message, String parent) {
        this(message, parent, null);
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getParentAddress() {
        return parent;
    }

    public String getFileContentsAsString(File f) {
        String hash = trackedFiles.get(f);
        if (hash != null) {
            File blobFile = join(Repository.BLOB_DIR, hash);
            return readContentsAsString(blobFile);
        }
        return "";
    }

    public Commit getParentCommit() {
        if (parent != null) {
            File parentCommitFile = join(Repository.COMMIT_DIR, parent);
            Commit parentCommit = readObject(parentCommitFile, Commit.class);
            return parentCommit;
        }
        return null;
    }

    public String getSecondParentAddress() {
        return secondParent;
    }

    public Commit getSecondParentCommit() {
        if (secondParent != null) {
            File parentCommitFile = join(Repository.COMMIT_DIR, secondParent);
            Commit parentCommit = readObject(parentCommitFile, Commit.class);
            return parentCommit;
        }
        return null;
    }
    public TreeMap<File, String> getTrackedFiles() {
        return trackedFiles;
    }

    public String getCommitId() {
        return sha1(serialize(this));
    }

    /** used to update commit with files provided by staging area */
    public void updateCommit(TreeMap<File, String> filesAdded, TreeSet<File> filesRemoved) {
        trackedFiles.putAll(filesAdded);
        for (File f : filesRemoved) {
            trackedFiles.remove(f);
        }
    }

    public boolean equals(Commit other) {
        return this.getCommitId().equals(other.getCommitId());
    }

    /** prints out the commit formatted for the log*/
    public void printCommit() {
        System.out.println("===");
        System.out.println("commit " + this.getCommitId());
        SimpleDateFormat projFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        String dateFormatted = projFormat.format(this.date);
        System.out.println("Date: " + dateFormatted);
        System.out.println(this.message);
        System.out.println();
    }



}
