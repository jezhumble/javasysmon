package com.jezhumble.javasysmon;

public interface Monitor {
    public String osName();
    public int numCpus();
    public long cpuFrequencyInHz();
    public CpuTimes cpuTimes();
    public MemoryStats physical();
    public MemoryStats swap();
    public long uptimeInSeconds();
    public int currentPid();
    ProcessInfo[] processTable();
    public void killProcess(int pid);
}
