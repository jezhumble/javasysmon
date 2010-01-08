package com.jezhumble.javasysmon;

class WindowsMonitor implements Monitor {
    private static Monitor monitor = null;

    static {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
		if (System.getProperty("os.arch").indexOf("64") > -1) {
	            new NativeLibraryLoader().loadLibrary("javasysmon64.dll");
		} else {
	            new NativeLibraryLoader().loadLibrary("javasysmon.dll");
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
    public native long cpuFrequencyInHz();
    public native long uptimeInSeconds();
    public native CpuTimes cpuTimes();
    public native MemoryStats physical();
    public native MemoryStats swap();
    public native ProcessInfo[] processTable();
    public native void killProcess(int pid);
}
