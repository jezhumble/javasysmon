package com.jezhumble.javasysmon;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ProcessTreeFunctionalTest extends TestCase {
    public void testShouldKillChildProcesses() {
        try {
            JavaSysMon monitor = new JavaSysMon();
            Assert.assertEquals(0, monitor.processTree().find(monitor.currentPid()).children().size());
            Runtime.getRuntime().exec("ant sleep");
            Runtime.getRuntime().exec("sleep 50");
            Assert.assertEquals(2, monitor.processTree().find(monitor.currentPid()).children().size());
            monitor.infanticide();
            Thread.sleep(500);
            Assert.assertEquals(0, monitor.processTree().find(monitor.currentPid()).children().size());
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }
}
