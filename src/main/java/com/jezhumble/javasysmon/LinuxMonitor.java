package com.jezhumble.javasysmon;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Network stats will come from /proc/net/dev; disk stats will be from /proc/diskstats
public class LinuxMonitor implements Monitor {

    private static final Pattern TOTAL_MEMORY_PATTERN =
            Pattern.compile("MemTotal:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern FREE_MEMORY_PATTERN =
            Pattern.compile("MemFree:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern TOTAL_SWAP_PATTERN =
            Pattern.compile("SwapTotal:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern FREE_SWAP_PATTERN =
            Pattern.compile("SwapFree:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern CPU_JIFFIES_PATTERN =
            Pattern.compile("cpu\\s+(.*)", Pattern.MULTILINE);
    private static final Pattern NUM_CPU_PATTERN =
            Pattern.compile("processor\\s+:\\s+(\\d+)", Pattern.MULTILINE);
    private static final Pattern CPU_FREQ_PATTERN =
            Pattern.compile("model name[^@]*@\\s+([0-9.A-Za-z]*)", Pattern.MULTILINE);
    private static final Pattern UPTIME_PATTERN =
            Pattern.compile("([\\d]*).*");
    private static final Pattern PID_PATTERN =
            Pattern.compile("([\\d]*).*");

    private FileUtils fileUtils;
    private String previousJiffies;
    private float previousCpuUsage = 0;
    private int userHz = 100; // Shouldn't be hardcoded. See below.

    LinuxMonitor(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
        previousJiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat");
    }

    public LinuxMonitor() {
        fileUtils = new FileUtils();
        JavaSysMon.addSupportedConfig("Linux (only tested with x86)");
        if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            JavaSysMon.setMonitor(this);
            JavaSysMon.addSupportedConfig("Linux (only tested with x86)");
            long uptimeInSeconds = uptimeInSeconds();
            previousJiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat");
            // The next two lines should work in theory, but in fact they don't. Weird.
//            long uptimeInJiffies = getTotalJiffies(previousJiffies.split("\\s+"));
//            userHz = (int) (uptimeInJiffies / uptimeInSeconds);
        }
    }

    public String osName() {
        return "Linux"; // TODO: distro detection
    }

    public float cpuUsage() {
        String jiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat");
        String[] parsedPreviousJiffies = previousJiffies.split("\\s+");
        String[] parsedJiffies = jiffies.split("\\s+");
        long totalPreviousJiffies = getTotalJiffies(parsedPreviousJiffies);
        long totalJiffies = getTotalJiffies(parsedJiffies);
        long idlePreviousJiffies = Long.parseLong(parsedPreviousJiffies[3]);
        long idleJiffies = Long.parseLong(parsedJiffies[3]);
        previousJiffies = jiffies;
        if (idlePreviousJiffies != idleJiffies && totalPreviousJiffies != totalJiffies) {
            float idleTicksDiff = idleJiffies - idlePreviousJiffies;
            float totalTicksDiff = totalJiffies - totalPreviousJiffies;
            previousCpuUsage = 1 - idleTicksDiff / totalTicksDiff;
            return 1 - idleTicksDiff / totalTicksDiff;
        }
        return previousCpuUsage;
    }

    public long totalMemory() {
        String totalMemory = fileUtils.runRegexOnFile(TOTAL_MEMORY_PATTERN, "/proc/meminfo");
        return Long.parseLong(totalMemory) * 1024;
    }

    public long freeMemory() {
        String freeMemory = fileUtils.runRegexOnFile(FREE_MEMORY_PATTERN, "/proc/meminfo");
        return Long.parseLong(freeMemory) * 1024;
    }

    public long totalSwap() {
        String totalMemory = fileUtils.runRegexOnFile(TOTAL_SWAP_PATTERN, "/proc/meminfo");
        return Long.parseLong(totalMemory) * 1024;
    }

    public long freeSwap() {
        String freeMemory = fileUtils.runRegexOnFile(FREE_SWAP_PATTERN, "/proc/meminfo");
        return Long.parseLong(freeMemory) * 1024;
    }

    public int numCpus() {
        int numCpus = 0;
        try {
            String cpuInfo = fileUtils.slurp("/proc/cpuinfo");
            Matcher matcher = NUM_CPU_PATTERN.matcher(cpuInfo);
            while (matcher.find()) {
                numCpus++;
            }
            return numCpus;
        } catch (IOException ioe) {
            // return nothing
        }
        return 0;
    }

    public long cpuFrequency() {
        String cpuFrequencyAsString = fileUtils.runRegexOnFile(CPU_FREQ_PATTERN, "/proc/cpuinfo");
        int strLen = cpuFrequencyAsString.length();
        BigDecimal cpuFrequency = new BigDecimal(cpuFrequencyAsString.substring(0, strLen - 3));
        long multiplier = getMultiplier(cpuFrequencyAsString.charAt(strLen - 3));
        return cpuFrequency.multiply(new BigDecimal(Long.toString(multiplier))).longValue();
    }

    public long uptimeInSeconds() {
        String uptime = fileUtils.runRegexOnFile(UPTIME_PATTERN, "/proc/uptime");
        return Long.parseLong(uptime);
    }

    public int currentPid() {
        String pid = fileUtils.runRegexOnFile(PID_PATTERN, "/proc/self/stat");
        return Integer.parseInt(pid);
    }

    public ProcessInfo[] processTable() {
        try {
            final String[] pids = new File("/proc").list(FileUtils.PROCESS_DIRECTORY_FILTER);
            ProcessInfo[] processTable = new ProcessInfo[pids.length];
            for (int i = 0; i < pids.length; i++) {
                String stat = fileUtils.slurp("/proc/" + pids[i] + "/stat");
                String status = fileUtils.slurp("/proc/" + pids[i] + "/status");
                String cmdline = fileUtils.slurp("/proc/" + pids[i] + "/cmdline");
                UnixPasswdParser passwdParser = new UnixPasswdParser();
                final LinuxProcessInfoParser parser = new LinuxProcessInfoParser(stat, status, cmdline, passwdParser.parse(), userHz);
                processTable[i] = parser.parse();
            }
            return processTable;
        } catch (IOException ioe) {
            System.err.println("Error getting process table: " + ioe.getMessage());
            return new ProcessInfo[0];
        }
    }

    private long getMultiplier(char multiplier) {
        switch (multiplier) {
            case 'G':
                return 1000000000;
            case 'M':
                return 1000000;
            case 'k':
                return 1000;
        }
        return 0;
    }

    private long getTotalJiffies(String[] jiffyString) {
        long totalJiffies = 0;
        for (int i = 0; i < jiffyString.length; i++) {
            totalJiffies += Long.parseLong(jiffyString[i]);
        }
        return totalJiffies;
    }
}
