package com.jezhumble.javasysmon;

public class MacOsXMonitor implements Monitor {

    private static Monitor monitor = null;

    static {
        if (System.getProperty("os.name").toLowerCase().equals("mac os x")) {
            System.loadLibrary("javasysmon");
            monitor = new MacOsXMonitor();
        }
    }

    public MacOsXMonitor() {
        if (monitor != null) {
            JavaSysMon.setMonitor(monitor);
        }
    }

    public native float cpuUsage();

    public native long totalMemory();

    public native long freeMemory();

}
