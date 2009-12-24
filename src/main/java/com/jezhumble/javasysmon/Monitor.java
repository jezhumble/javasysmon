package com.jezhumble.javasysmon;

public interface Monitor {
    public String osName();
    public float cpuUsage();
    public long totalMemory();
    public long freeMemory();
    public long totalSwap();
    public long freeSwap();
    public int numCpus();
    public long cpuFrequency();
    public long uptimeInSeconds();
    public int currentPid();
}
