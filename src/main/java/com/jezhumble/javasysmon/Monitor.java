package com.jezhumble.javasysmon;

public interface Monitor {
    public float cpuUsage();
    public long totalMemory();
    public long freeMemory();
}
