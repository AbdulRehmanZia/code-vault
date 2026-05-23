package com.codevault.models;

/**
 * Version — a data model representing one saved snapshot of code.
 *
 * Think of this like a single commit in Git:
 *   - id        : unique number for this version
 *   - message   : the commit message the user typed
 *   - code      : the full source code at the time of this commit
 *   - timestamp : human-readable time like "May 23, 11:30 AM"
 *
 * This class is a plain "data container" — it only holds data,
 * has no logic. In Java this is called a POJO (Plain Old Java Object).
 */
public class Version {

    private int    id;
    private String message;
    private String code;
    private String timestamp;

    // Constructor — called when creating a new Version object
    public Version(int id, String message, String code, String timestamp) {
        this.id        = id;
        this.message   = message;
        this.code      = code;
        this.timestamp = timestamp;
    }

    // Getters — standard way to read private fields from outside the class
    public int    getId()        { return id; }
    public String getMessage()   { return message; }
    public String getCode()      { return code; }
    public String getTimestamp() { return timestamp; }

    /**
     * toString() is called automatically when Java needs to display this object
     * as text — for example inside a JList. We return the commit message.
     */
    @Override
    public String toString() {
        return message;
    }
}
