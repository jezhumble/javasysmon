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

    public native int numCpus();
    public native long cpuFrequencyInHz();
    public native long uptimeInSeconds();
    public native int currentPid();

    public CpuTimes cpuTimes() {
        return new CpuTimes(0, 0, 0);
    }

    public MemoryStats physical() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public MemoryStats swap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProcessInfo[] processTable() {
        return new ProcessInfo[0];
    }
}
