package com.jezhumble.javasysmon;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assume.assumeTrue;

public class LinuxMonitorTest {

    @Test
    public void shouldRetrieveTotalMemory() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long totalMemory = monitor.physical().getTotalBytes();
        Assert.assertEquals((long)368640, totalMemory/1024);
    }

    @Test
    public void shouldRetrieveFreeMemory() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long freeMemory = monitor.physical().getFreeBytes();
        Assert.assertEquals((long)195608, freeMemory/1024);
    }

    @Test
    public void shouldRetrieveTotalSwap() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long totalSwap = monitor.swap().getTotalBytes();
        Assert.assertEquals((long)262144, totalSwap/1024);
    }

    @Test
    public void shouldRetrieveFreeSwap() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long freeSwap = monitor.swap().getFreeBytes();
        Assert.assertEquals((long)260123, freeSwap/1024);
    }

    @Test
    public void shouldCalculateNumCpus() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final int numCpus = monitor.numCpus();
        Assert.assertEquals(2, numCpus);
    }

    @Test
    public void shouldCalculateCpuFrequency() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long cpuFrequency = monitor.cpuFrequencyInHz();
        Assert.assertEquals(2400000000l, cpuFrequency);
    }

    @Test
    public void shouldReturnUptime() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final long uptime = monitor.uptimeInSeconds();
        Assert.assertEquals(22550744l, uptime);
    }

    @Test
    public void shouldReturnPid() {
        LinuxMonitor monitor = new LinuxMonitor(new StubFileUtils());
        final int pid = monitor.currentPid();
        Assert.assertEquals(31912, pid);
    }

    @Test()
    public void shouldReturnTheProcessTable() {
        assumeTrue(new File("/proc").exists());
        LinuxMonitor linuxMonitor = new LinuxMonitor();
        Assert.assertNotNull(linuxMonitor.processTable());
    }
}
