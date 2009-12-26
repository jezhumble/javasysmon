package com.jezhumble.javasysmon;

public class MemoryStats {

    private final static int ONE_MB = 1024 * 1024;
    private final long free;
    private final long total;

    public MemoryStats(long free, long total) {
        this.free = free;
        this.total = total;
    }

    public long getFreeBytes() {
        return free;
    }

    public long getTotalBytes() {
        return total;
    }

    public String toString() {
        return "total: " + total / ONE_MB + "Mb free: " + free / ONE_MB + "Mb";
    }
}
