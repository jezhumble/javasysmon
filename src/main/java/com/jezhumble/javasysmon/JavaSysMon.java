package com.jezhumble.javasysmon;

public class JavaSysMon implements Monitor {

    private static Monitor monitor = null;

    public static void setMonitor(Monitor myMonitor) {
        monitor = myMonitor;
    }

    static {
        new MacOsXMonitor();        
    }

    public static void main (String[] params) {
        if (monitor == null) {
            System.err.println("Couldn't find an implementation for OS: " + System.getProperty("os.name"));
        } else {
            System.out.println("CPU Usage: " + monitor.cpuUsage());
            System.out.println("Total memory: " + monitor.totalMemory());
            System.out.println("Free memory: " + monitor.totalMemory());
        }
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
}