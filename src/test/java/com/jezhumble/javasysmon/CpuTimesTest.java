package com.jezhumble.javasysmon;

import junit.framework.Assert;
import org.junit.Test;

public class CpuTimesTest  {

    @Test
    public void shouldReturn1ForCpuFullyUsed() {
        CpuTimes cpuTimesPrev = new CpuTimes(100, 100, 0);
        CpuTimes cpuTimesNext = new CpuTimes(200, 200, 0);
        final float cpuUsage = cpuTimesNext.getCpuUsage(cpuTimesPrev);
        assertEqual(1f, cpuUsage);
    }

    @Test
    public void shouldReturn1IfNoChange() {
        CpuTimes cpuTimesPrev = new CpuTimes(100, 100, 0);
        CpuTimes cpuTimesNext = new CpuTimes(100, 100, 0);
        final float cpuUsage = cpuTimesNext.getCpuUsage(cpuTimesPrev);
        assertEqual(1f, cpuUsage);
    }

    @Test
    public void shouldReturn0IfIdle() {
        CpuTimes cpuTimesPrev = new CpuTimes(100, 100, 0);
        CpuTimes cpuTimesNext = new CpuTimes(100, 100, 100);
        final float cpuUsage = cpuTimesNext.getCpuUsage(cpuTimesPrev);
        assertEqual(0f, cpuUsage);
    }

    @Test
    public void shouldReturnHalfForCpuHalfUsed() {
        CpuTimes cpuTimesPrev = new CpuTimes(100, 100, 0);
        CpuTimes cpuTimesNext = new CpuTimes(200, 200, 200);
        final float cpuUsage = cpuTimesNext.getCpuUsage(cpuTimesPrev);
        assertEqual(0.5f, cpuUsage);
    }

    private void assertEqual(float expected, float actual) {
        Assert.assertTrue("Expected: " + expected + " got: " + actual, Float.compare(expected, actual) == 0);
    }
}
