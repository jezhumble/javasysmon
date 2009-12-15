package com.jezhumble.javasysmon;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private FileUtils fileUtils;

    private String previousJiffies;
    private float previousCpuUsage = 0;

    LinuxMonitor(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
        previousJiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat");
    }

    public LinuxMonitor() {
        fileUtils = new FileUtils();
        if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            JavaSysMon.setMonitor(this);
            JavaSysMon.addSupportedConfig("Linux (only tested with x86)");
            previousJiffies = fileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat");        
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
