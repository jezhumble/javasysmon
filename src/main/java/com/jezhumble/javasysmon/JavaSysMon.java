package com.jezhumble.javasysmon;

import java.util.ArrayList;
import java.util.Iterator;

public class JavaSysMon implements Monitor {

    private static Monitor monitor = null;
    private static ArrayList supported = new ArrayList();

    public static void setMonitor(Monitor myMonitor) {
        monitor = myMonitor;
    }

    public static void addSupportedConfig(String config) {
        supported.add(config);
    }

    static {
        new MacOsXMonitor();
        new LinuxMonitor();
        new WindowsMonitor();
        new SolarisMonitor();
    }

    public static void main (String[] params) throws Exception {
        if (monitor == null) {
            System.err.println("Couldn't find an implementation for OS: " + System.getProperty("os.name"));
            System.err.println("Supported configurations:");
            for (Iterator iter = supported.iterator(); iter.hasNext(); ) {
                String config = (String) iter.next();
                System.err.println(config);
            }
        } else {
            System.out.println("OS name: " + monitor.osName());
            System.out.println("Uptime: " + secsInDaysAndHours(monitor.uptimeInSeconds()));
            System.out.println("Number of CPUs: " + monitor.numCpus());
            System.out.println("CPU frequency: " + monitor.cpuFrequency() / (1000*1000) + " MHz");
            System.out.println("Total memory: " + monitor.totalMemory() / (1024*1024) + " Mb");
            System.out.println("Free memory: " + monitor.freeMemory() / (1024*1024) + " Mb");
            System.out.println("Total swap: " + monitor.totalSwap() / (1024*1024) + " Mb");
            System.out.println("Free swap: " + monitor.freeSwap() / (1024*1024) + " Mb");
            System.out.println("Sampling CPU usage...");
            Thread.sleep(5000);
            System.out.println("CPU Usage: " + monitor.cpuUsage());
        }
    }

    private static String secsInDaysAndHours(long seconds) {
        long days = seconds / (60 * 60 * 24);
        long hours = (seconds / (60 * 60)) - (days * 24);
        return days + " days " + hours + " hours";
    }

    public String osName() {
        return monitor.osName();
    }

    public float cpuUsage() {
        return monitor.cpuUsage();
    }

    public long totalMemory() {
        return monitor.totalMemory();
    }

    public long freeMemory() {
        return monitor.freeMemory();
    }

    public long totalSwap() {
        return monitor.totalSwap();
    }

    public long freeSwap() {
        return monitor.freeSwap();
    }

    public int numCpus() {
        return monitor.numCpus();
    }

    public long cpuFrequency() {
        return monitor.cpuFrequency();
    }

    public long uptimeInSeconds() {
        return monitor.uptimeInSeconds();
    }
}
