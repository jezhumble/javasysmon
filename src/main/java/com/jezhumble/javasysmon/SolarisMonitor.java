package com.jezhumble.javasysmon;

public class SolarisMonitor implements Monitor {
    private static Monitor monitor = null;

    static {
        if (System.getProperty("os.name").toLowerCase().startsWith("sunos")) {
	    if (System.getProperty("os.arch").toLowerCase().startsWith("x86")) {
	        System.loadLibrary("javasysmonsolx86");
                monitor = new SolarisMonitor();
            }
        }
    }

    public SolarisMonitor() {
        JavaSysMon.addSupportedConfig("Solaris (x86)");
        if (monitor != null) {
            JavaSysMon.setMonitor(monitor);
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
    public native long uptimeInSeconds();
    public native int currentPid();
}
