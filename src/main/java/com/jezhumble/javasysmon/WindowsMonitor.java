package com.jezhumble.javasysmon;

public class WindowsMonitor implements Monitor {
    private static Monitor monitor = null;

    static {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
		if (System.getProperty("os.arch").indexOf("64") > -1) {
	            System.loadLibrary("javasysmon64");
		} else {
	            System.loadLibrary("javasysmon");
		}
            monitor = new WindowsMonitor();
        }
    }

    public WindowsMonitor() {
        JavaSysMon.addSupportedConfig("Windows (x86)");
        if (monitor != null) {
            JavaSysMon.setMonitor(monitor);
        }
    }

    public String osName() {
        return System.getProperty("os.name");
    }

    public native int numCpus();
    public native int currentPid();
    public native long cpuFrequency();
    public native long uptimeInSeconds();

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
