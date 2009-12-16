package com.jezhumble.javasysmon;

public class WindowsMonitor implements Monitor {
    private static Monitor monitor = null;

    static {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            System.loadLibrary("javasysmon");
            monitor = new WindowsMonitor();
        }
    }

    public WindowsMonitor() {
        if (monitor != null) {
            JavaSysMon.setMonitor(monitor);
            JavaSysMon.addSupportedConfig("Windows (x86)");
        }
    }

    public String osName() {
        return System.getProperty("os.name");
    }

    public native float cpuUsage();
    public native long totalMemory();
    public native long freeMemory();
    public native long totalSwap();
    public native long freeSwap();
    public native int numCpus();
    public native long cpuFrequency();
}
