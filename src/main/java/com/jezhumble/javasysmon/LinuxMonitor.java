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
    private int userHz = 100; // Shouldn't be hardcoded. See below.

    LinuxMonitor(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public LinuxMonitor() {
        fileUtils = new FileUtils();
        JavaSysMon.addSupportedConfig("Linux (only tested with x86)");
        if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            JavaSysMon.setMonitor(this);
            JavaSysMon.addSupportedConfig("Linux (only tested with x86)");
//  In theory, this calculation should return userHz. It doesn't seem to work though.
//            long uptimeInSeconds = uptimeInSeconds();
//            previousJiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat");
//            long uptimeInJiffies = getTotalJiffies(previousJiffies.split("\\s+"));
//            userHz = (int) (uptimeInJiffies / uptimeInSeconds);
        }
    }

    public String osName() {
        return "Linux"; // TODO: distro detection
    }

    public MemoryStats physical() {
        String totalMemory = fileUtils.runRegexOnFile(TOTAL_MEMORY_PATTERN, "/proc/meminfo");
        long total = Long.parseLong(totalMemory) * 1024;
        String freeMemory = fileUtils.runRegexOnFile(FREE_MEMORY_PATTERN, "/proc/meminfo");
        long free = Long.parseLong(freeMemory) * 1024;
        return new MemoryStats(free, total);
    }

    public MemoryStats swap() {
        String totalMemory = fileUtils.runRegexOnFile(TOTAL_SWAP_PATTERN, "/proc/meminfo");
        long total = Long.parseLong(totalMemory) * 1024;
        String freeMemory = fileUtils.runRegexOnFile(FREE_SWAP_PATTERN, "/proc/meminfo");
        long free = Long.parseLong(freeMemory) * 1024;
        return new MemoryStats(free, total);
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

    public long cpuFrequencyInHz() {
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

    public CpuTimes cpuTimes() {
        String[] parsedJiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat").split("\\s+");
        long userJiffies = Long.parseLong(parsedJiffies[0]) + Long.parseLong(parsedJiffies[1]);
        long idleJiffies = Long.parseLong(parsedJiffies[3]);
        long systemJiffies = Long.parseLong(parsedJiffies[2]);
        // this is for Linux >= 2.6
        if (parsedJiffies.length > 4) {
            for (int i = 4; i < parsedJiffies.length; i++) {
                systemJiffies += Long.parseLong(parsedJiffies[i]);
            }
        }
        return new CpuTimes(toMillis(userJiffies), toMillis(systemJiffies), toMillis(idleJiffies));
    }

    private long toMillis(long jiffies) {
        int multiplier = 1000 / userHz;
        return jiffies * multiplier;
    }
}
