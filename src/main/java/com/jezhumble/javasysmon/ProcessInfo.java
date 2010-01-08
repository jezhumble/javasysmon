package com.jezhumble.javasysmon;

import java.text.DecimalFormat;

/**
 * This object represents JavaSysMon's understanding of a process.
 * You can get all the information JavaSysMon knows about a
 * particular process from this object.
 * <p>
 * There are also convenience methods that can be used to print out
 * a process table in a monospace font (for example, on the console).
 */
public class ProcessInfo {

    // Process id info
    private int pid;
    private int parentPid;
    private String command;
    private String name;
    private String owner;

    // Performance info
    private long userMillis;
    private long systemMillis;
    private long residentBytes;
    private long totalBytes;

    public ProcessInfo(int pid, int parentPid, String command, String name, String owner,
                       long userMillis, long systemMillis, long residentBytes, long totalBytes) {

        this.pid = pid;
        this.parentPid = parentPid;
        this.command = command;
        this.name = name;
        this.owner = owner;
        this.userMillis = userMillis;
        this.systemMillis = systemMillis;
        this.residentBytes = residentBytes;
        this.totalBytes = totalBytes;
    }

    /**
     * The id of this process
     *
     * @return The id of this process
     */
    public int getPid() {
        return pid;
    }

    /**
     * The id of the parent process of this parent
     *
     * @return The id of the parent process of this parent
     */
    public int getParentPid() {
        return parentPid;
    }

    /**
     * The command that was originally used to start this process.
     * Not currently available on Mac OSX or Solaris (the C source
     * contains some information on how to get this data for anyone
     * interested in implementing it)
     *
     * @return A string representing the command that was originally
     * used to start this process.
     */
    public String getCommand() {
        return command;
    }

    /**
     * The name of this process. This is for display purposes only.
     *
     * @return The name of this process.
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the owner of this process. This is derived from
     * the uid, not the effective id. On Windows, this is in the format
     * DOMAIN\USERNAME
     *
     * @return The name of the owner of this process.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * The number of milliseconds that this process has been running
     * on the CPUs in user mode. Note that this is not "wall time".
     * On Mac OSX this information is only available for the current process.
     *
     * @return The number of milliseconds that this process has been
     * running on the CPUs in user mode.
     */
    public long getUserMillis() {
        return userMillis;
    }

    /**
     * The number of milliseconds that this process has been running
     * on the CPUs in kernel mode. Note that this is not "wall time".
     * On Mac OSX this information is only available for the current process.
     *
     * @return The number of milliseconds that this process has been
     * running on the CPUs in kernel mode.
     */
    public long getSystemMillis() {
        return systemMillis;
    }

    /**
     * The number of bytes used by this process that are currently in physical
     * memory. On Mac OSX this information is only available for the current
     * process.
     *
     * @return The number of bytes used by this process that are currently in
     * physical memory.
     */
    public long getResidentBytes() {
        return residentBytes;
    }

    /**
     * The total size of this process in bytes. On Mac OSX this information
     * is only available for the current process.
     *
     * @return The total number of bytes used by this process.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Prints out a header that can be used along with {@link #toString}
     * (assuming you use a monospace font).
     *
     * @return A header that can be used when printing out the process table.
     */
    public static String header() {
        return "  pid name        ppid user        total    res     time command\n" +
               "================================================================================";
    }

    /**
     * A one-line string representation of some of the information in this object
     * that can be used to print out a single line in a process table.
     * Fields have a fixed length so that the table looks nice in a monospace font.
     *
     * @return a single line representing some of the information about this process.
     */
    public String toString() {
        // No bloody string formatting in Java 1.4. Starting to reconsider support for it.
        // Even C can do this ffs 
        return stringFormat(pid, 5) + " " +
                stringFormat(name, 10) + " " +
                stringFormat(parentPid, 5) + " " +
                stringFormat(owner, 10) + " " +
                stringFormat(totalBytes / (1024 * 1024), 4) + "Mb " +
                stringFormat(residentBytes / (1024 * 1024), 4) + "Mb " +
                formatMillisecs(userMillis + systemMillis) + " " +
                stringFormat(command, 23);
    }

    private static String stringFormat(int intToFormat, int fieldSize) {
        return stringFormat(Integer.toString(intToFormat), fieldSize, true);
    }

    private static String stringFormat(long longToFormat, int fieldSize) {
        return stringFormat(Long.toString(longToFormat), fieldSize, true);
    }

    private static String stringFormat(String stringToFormat, int fieldSize) {
        return stringFormat(stringToFormat, fieldSize, false);
    }

    private static String stringFormat(String stringToFormat, int fieldSize, boolean rightJustify) {
        // and Java doesn't really excel at this kind of thing either
        if (stringToFormat.length() >= fieldSize) {
            return stringToFormat.substring(0, fieldSize);
        } else {
            return rightJustify ?
                    PADDING.substring(0, fieldSize - stringToFormat.length()) + stringToFormat:
                    stringToFormat + PADDING.substring(0, fieldSize - stringToFormat.length());
        }
    }

    // gotta love this hack
    final private static String PADDING =
            "                                                                                   ";

    private static String formatMillisecs(long millisecs) {
        long secs = millisecs / 1000;
        long hours = secs / 3600;
        long mins = (secs - (hours * 3600)) / 60;
        secs = (secs - (hours * 3600) - (mins * 60));
        DecimalFormat format = new DecimalFormat("00");
        return format.format(hours) + ":" + format.format(mins) + ":" + format.format(secs); 
    }
}