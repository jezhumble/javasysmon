package com.jezhumble.javasysmon;

/**
 * This object represents a snapshot detailing the total memory of
 * some type (physical or swap) available to the operating system,
 * and the amount that is currently free.
 */
public class MemoryStats {

    private final static int ONE_MB = 1024 * 1024;
    private final long free;
    private final long total;
    private final long usable;

    public MemoryStats(long free, long total, long usable) {
        this.free = free;
        this.total = total;
        this.usable = usable;
    }

    /**
     * The amount of memory that is currently free, in bytes.
     *
     * @return The amount of memory that is currently free.
     */
    public long getFreeBytes() {
        return free;
    }
    
    /**
     * The amount of memory that is currently usable (free + buffers + cache), in bytes.
     *
     * @return The amount of memory that is currently usable.
     */
    public long getUsableBytes() {
        return usable;
    }

    /**
     * The amount of memory that is available to the operating system,
     * in bytes.
     *
     * @return The total amount of memory that is available.
     */
    public long getTotalBytes() {
        return total;
    }

    public String toString() {
        return "total: " + total / ONE_MB + "Mb free: " + free / ONE_MB + "Mb usable: " + usable / ONE_MB + "Mb";
    }
}
