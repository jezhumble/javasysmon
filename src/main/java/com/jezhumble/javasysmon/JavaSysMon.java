package com.jezhumble.javasysmon;

import java.util.ArrayList;
import java.util.HashMap;
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
            System.out.println("Total memory: " + monitor.totalMemory() / (1024*1024) + " Mb");
            System.out.println("Free memory: " + monitor.freeMemory() / (1024*1024) + " Mb");
            System.out.println("Total swap: " + monitor.totalSwap() / (1024*1024) + " Mb");
            System.out.println("Free swap: " + monitor.freeSwap() / (1024*1024) + " Mb");
            System.out.println("Sampling CPU usage...");
            Thread.sleep(500);
            System.out.println("CPU Usage: " + monitor.cpuUsage());
        }
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
}