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

    public ProcessInfo parse() {
        String[] statElements = stat.split(" ");
        return new ProcessInfo(Integer.parseInt(statElements[0]),
                Integer.parseInt(statElements[3]),
                trim(cmdline), getFirstMatch(STATUS_NAME_MATCHER, status),
                (String) uids.get(getFirstMatch(STATUS_UID_MATCHER, status)),
                Long.parseLong(statElements[13]) * (1000 / userHz),
                Long.parseLong(statElements[14]) * (1000 / userHz),
                Long.parseLong(getFirstMatch(STATUS_VM_RSS_MATCHER, status)) * 1024,
                Long.parseLong(getFirstMatch(STATUS_VM_SIZE_MATCHER, status)) * 1024);
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
