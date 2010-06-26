package com.jezhumble.javasysmon;

class NullMonitor implements Monitor {

    public NullMonitor() {
        JavaSysMon.setMonitor(this);
    }

    public String osName() {
        return System.getProperty("os.name");
    }

    public int numCpus() {
        return 0;
    }

    public long cpuFrequencyInHz() {
        return 0;
    }

    public CpuTimes cpuTimes() {
        return null;
    }

    public MemoryStats physical() {
        return null;
    }

    public MemoryStats swap() {
        return null;
    }

    public long uptimeInSeconds() {
        return 0;
    }

    public int currentPid() {
        return 0;
    }

    public ProcessInfo[] processTable() {
        return new ProcessInfo[0];
    }

    public void killProcess(int pid) {
    }
}
