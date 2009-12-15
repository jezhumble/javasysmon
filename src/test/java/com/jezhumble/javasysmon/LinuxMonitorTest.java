package com.jezhumble.javasysmon;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LinuxMonitorTest extends TestCase {

    public void testShouldRetrieveTotalMemory() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long totalMemory = monitor.totalMemory();
        Assert.assertEquals((long)368640, totalMemory/1024);
    }

    public void testShouldRetrieveFreeMemory() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long freeMemory = monitor.freeMemory();
        Assert.assertEquals((long)195608, freeMemory/1024);
    }

    public void testShouldRetrieveTotalSwap() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long totalSwap = monitor.totalSwap();
        Assert.assertEquals((long)262144, totalSwap/1024);
    }

    public void testShouldRetrieveFreeSwap() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long freeSwap = monitor.freeSwap();
        Assert.assertEquals((long)260123, freeSwap/1024);
    }

    public void testShouldCalculateCpuUsage() {
        float totalTicksDiff = (4377795025l - 4377756391l);
        float idleTicksDiff = (4359776821l - 4359738256l);
        float expectedCpuUsage = 1 - (idleTicksDiff / totalTicksDiff);

        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final float actualCpuUsage = monitor.cpuUsage();
        Assert.assertTrue(Math.abs(expectedCpuUsage - actualCpuUsage) < 0.000000001f);

        final float newActualCpuUsage = monitor.cpuUsage();
        Assert.assertTrue(Math.abs(newActualCpuUsage - actualCpuUsage) < 0.000000001f);
    }

}
