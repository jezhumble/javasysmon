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
            System.out.println("OS name: " + monitor.osName() +
                    "  Uptime: " + secsInDaysAndHours(monitor.uptimeInSeconds()) +
                    "  Current PID: " + monitor.currentPid());
            System.out.println("Number of CPUs: " + monitor.numCpus() +
                    "  CPU frequency: " + monitor.cpuFrequency() / (1000*1000) + " MHz");
            System.out.println("RAM total: " + monitor.totalMemory() / (1024*1024) + " Mb" +
                    " free: " + monitor.freeMemory() / (1024*1024) + " Mb" +
                    "  SWAP total: " + monitor.totalSwap() / (1024*1024) + " Mb" +
                    " free: " + monitor.freeSwap() / (1024*1024) + " Mb");
            System.out.println("Sampling CPU usage...");
            Thread.sleep(500);
            System.out.println("CPU Usage: " + monitor.cpuUsage());
            System.out.println("\n" + ProcessInfo.header());
            ProcessInfo[] processes = monitor.processTable();
            for (int i = 0; i < processes.length; i++) {
                System.out.println(processes[i].toString());
            }
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

    public int currentPid() {
        return monitor.currentPid();
    }

    public ProcessInfo[] processTable() {
        return monitor.processTable();
    }
}
