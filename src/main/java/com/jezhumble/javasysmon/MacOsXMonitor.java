package com.jezhumble.javasysmon;

class MacOsXMonitor implements Monitor {

    private static Monitor monitor = null;

    static {
        if (System.getProperty("os.name").toLowerCase().equals("mac os x")) {
            new NativeLibraryLoader().loadLibrary("libjavasysmon.jnilib");
            monitor = new MacOsXMonitor();
        }
    }

    public MacOsXMonitor() {
        JavaSysMon.addSupportedConfig("Mac Os X (PPC, x86, X86_64)");
        if (monitor != null) {
            JavaSysMon.setMonitor(monitor);
        }
    }

    public String osName() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version");
    }

    public native int numCpus();
    public native long cpuFrequencyInHz();
    public native long uptimeInSeconds();
    public native int currentPid();
    public native CpuTimes cpuTimes();
    public native MemoryStats physical();
    public native MemoryStats swap();
    public native ProcessInfo[] processTable();
    public native void killProcess(int pid);
}
