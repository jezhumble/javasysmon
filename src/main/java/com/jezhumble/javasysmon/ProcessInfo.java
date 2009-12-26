package com.jezhumble.javasysmon;

import java.text.DecimalFormat;

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


    public int getPid() {
        return pid;
    }

    public int getParentPid() {
        return parentPid;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public long getUserMillis() {
        return userMillis;
    }

    public long getSystemMillis() {
        return systemMillis;
    }

    public long getResidentBytes() {
        return residentBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public static String header() {
        return "  pid name        ppid user        total    res     time command\n" +
               "================================================================================";
    }

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