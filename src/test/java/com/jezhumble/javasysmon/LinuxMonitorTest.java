package com.jezhumble.javasysmon;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LinuxMonitorTest extends TestCase {

    public void testShouldRetrieveTotalMemory() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long totalMemory = monitor.physical().getTotalBytes();
        Assert.assertEquals((long)368640, totalMemory/1024);
    }

    public void testShouldRetrieveFreeMemory() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long freeMemory = monitor.physical().getFreeBytes();
        Assert.assertEquals((long)195608, freeMemory/1024);
    }

    public void testShouldRetrieveTotalSwap() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long totalSwap = monitor.swap().getTotalBytes();
        Assert.assertEquals((long)262144, totalSwap/1024);
    }

    public void testShouldRetrieveFreeSwap() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long freeSwap = monitor.swap().getFreeBytes();
        Assert.assertEquals((long)260123, freeSwap/1024);
    }

    public void testShouldCalculateNumCpus() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final int numCpus = monitor.numCpus();
        Assert.assertEquals(2, numCpus);
    }

    public void testShouldCalculateCpuFrequency() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long cpuFrequency = monitor.cpuFrequencyInHz();
        Assert.assertEquals(2400000000l, cpuFrequency);
    }

    public void testShouldReturnUptime() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long uptime = monitor.uptimeInSeconds();
        Assert.assertEquals(22550744l, uptime);
    }

    public void testShouldReturnPid() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final int pid = monitor.currentPid();
        Assert.assertEquals(31912, pid);
    }

    public void testShouldReturnTheProcessTable() {
        LinuxMonitor linuxMonitor = new LinuxMonitor();
        Assert.assertNotNull(linuxMonitor.processTable());
    }
}
