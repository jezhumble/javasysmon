package com.jezhumble.javasysmon;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LinuxProcessInfoParser {
    private final String stat;
    private final String status;
    private final String cmdline;
    private final HashMap uids;
    private final int userHz;

    private static final Pattern STATUS_NAME_MATCHER =
            Pattern.compile("Name:\\s+(\\w+)", Pattern.MULTILINE);
    private static final Pattern STATUS_UID_MATCHER =
            Pattern.compile("Uid:\\s+(\\d+)\\s.*", Pattern.MULTILINE);
    private static final Pattern STATUS_VM_SIZE_MATCHER =
            Pattern.compile("VmSize:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern STATUS_VM_RSS_MATCHER =
            Pattern.compile("VmRSS:\\s+(\\d+) kB", Pattern.MULTILINE);

    public LinuxProcessInfoParser(String stat, String status, String cmdline, HashMap uids, int userHz) {
        this.stat = stat;
        this.status = status;
        this.cmdline = cmdline;
        this.uids = uids;
        this.userHz = userHz;
    }

    public ProcessInfo parse() throws ParseException {
        int openParen = stat.indexOf("(");
        int closeParen = stat.lastIndexOf(")");
        if (openParen <= 1 || closeParen < 0 || closeParen > stat.length() - 2) {
            throw new ParseException("Stat '" + stat + "' does not include expected parens around process name");
        }

        // Start splitting after close of proc name
        String[] statElements = stat.substring(closeParen + 2).split(" ");
        if (statElements.length < 13) {
            throw new ParseException("Stat '" + stat + "' contains fewer elements than expected");
        }
        
        String pidStr = stat.substring(0, openParen - 1);

        int pid;
        int parentPid;
        long userMillis;
        long systemMillis;
        try
        {
            pid = Integer.parseInt(pidStr);
            parentPid = Integer.parseInt(statElements[1]);
            userMillis = Long.parseLong(statElements[11]) * (1000 / userHz);
            systemMillis = Long.parseLong(statElements[12]) * (1000 / userHz);
        }
        catch (NumberFormatException e)
        {
            throw new ParseException("Unable to parse stat '" + stat + "'");
        }

        long residentBytes;
        long totalBytes;
        try
        {
            residentBytes = Long.parseLong(getFirstMatch(STATUS_VM_RSS_MATCHER, status)) * 1024;
            totalBytes = Long.parseLong(getFirstMatch(STATUS_VM_SIZE_MATCHER, status)) * 1024;
        }
        catch (NumberFormatException e)
        {
            throw new ParseException("Unable to extract memory usage information from status '" + status + "'");
        }

        return new ProcessInfo(pid,
                parentPid,
                trim(cmdline),
                getFirstMatch(STATUS_NAME_MATCHER, status),
                (String) uids.get(getFirstMatch(STATUS_UID_MATCHER, status)),
                userMillis,
                systemMillis,
                residentBytes,
                totalBytes);
    }

    private String trim(String cmdline) {
        return cmdline.replace('\000', ' ').replace('\n', ' ');
    }

    public String getFirstMatch(Pattern pattern, String string) {
        try {
            Matcher matcher = pattern.matcher(string);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }
}
