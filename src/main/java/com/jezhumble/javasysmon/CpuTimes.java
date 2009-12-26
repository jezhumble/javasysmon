package com.jezhumble.javasysmon;

public class CpuTimes {
    private final long userMillis;
    private final long systemMillis;
    private final long idleMillis;

    public CpuTimes(long userMillis, long systemMillis, long idleMillis) {
        this.userMillis = userMillis;
        this.systemMillis = systemMillis;
        this.idleMillis = idleMillis;
    }

    public long getUserMillis() {
        return userMillis;
    }

    public long getSystemMillis() {
        return systemMillis;
    }

    public long getIdleMillis() {
        return idleMillis;
    }

    public long getTotalMillis() {
        return userMillis + systemMillis + idleMillis;
    }

    public float getCpuUsage(CpuTimes previous) {
        if (idleMillis == previous.idleMillis || getTotalMillis() == previous.getTotalMillis()) {
            return 0;
        }
        return 1 - ((float) (idleMillis - previous.idleMillis)) /
                (float) (getTotalMillis() - previous.getTotalMillis());
    }
}
