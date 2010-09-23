package com.jezhumble.javasysmon;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CpuTimesTest extends TestCase {

    public void testShouldReturn100ForCpuFullyUsed() {
        CpuTimes cpuTimesPrev = new CpuTimes(100, 100, 0);
        CpuTimes cpuTimesNext = new CpuTimes(200, 200, 0);
        final float cpuUsage = cpuTimesNext.getCpuUsage(cpuTimesPrev);
        Assert.assertTrue((100f - cpuUsage) < 0.001f);
    }
}
