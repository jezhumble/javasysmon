package com.jezhumble.javasysmon;

import java.io.IOException;
import java.util.ArrayList;

class SolarisMonitor implements Monitor {
    private static Monitor monitor = null;
    private final FileUtils fileUtils;

    static {
        if (System.getProperty("os.name").toLowerCase().startsWith("sunos")) {
            if (System.getProperty("os.arch").toLowerCase().startsWith("x86")) {
                new NativeLibraryLoader().loadLibrary("javasysmonsolx86.so");
                monitor = new SolarisMonitor();
            }
        }
    }

    public SolarisMonitor() {
        JavaSysMon.addSupportedConfig("Solaris (x86)");
        if (monitor != null) {
            JavaSysMon.setMonitor(monitor);
        }
        fileUtils = new FileUtils();
    }

    public String osName() {
        return System.getProperty("os.name");
    }

    public ProcessInfo[] processTable() {
        ArrayList processTable = new ArrayList();
        final String[] pids = fileUtils.pidsFromProcFilesystem();
        for (int i = 0; i < pids.length; i++) {
            try {
                byte[] psinfo = fileUtils.slurpToByteArray("/proc/" + pids[i] + "/psinfo");
                byte[] usage = fileUtils.slurpToByteArray("/proc/" + pids[i] + "/usage");
                processTable.add(psinfoToProcess(psinfo, usage));
            } catch (IOException e) {
                // process doesn't exist any more
            }
        }
        return (ProcessInfo[]) processTable.toArray(new ProcessInfo[processTable.size()]);
    }

    public native ProcessInfo psinfoToProcess(byte[] psinfo, byte[] usage);
    public native int numCpus();
    public native long cpuFrequencyInHz();
    public native long uptimeInSeconds();
    public native int currentPid();
    public native CpuTimes cpuTimes();
    public native MemoryStats physical();
    public native MemoryStats swap();
    public native void killProcess(int pid);
}
