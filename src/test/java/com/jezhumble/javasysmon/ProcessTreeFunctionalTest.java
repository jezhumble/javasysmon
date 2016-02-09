package com.jezhumble.javasysmon;

import junit.framework.Assert;
import org.junit.Test;

public class ProcessTreeFunctionalTest {
    @Test
    public void shouldKillChildProcesses() {
        try {
            JavaSysMon monitor = new JavaSysMon();
            Assert.assertEquals(0, monitor.processTree().find(monitor.currentPid()).children().size());
            Runtime.getRuntime().exec("ant sleep");
            Runtime.getRuntime().exec("sleep 51");
            Assert.assertEquals(2, monitor.processTree().find(monitor.currentPid()).children().size());
            Thread.sleep(500);
            monitor.infanticide();
            Thread.sleep(500);
            Assert.assertEquals(0, monitor.processTree().find(monitor.currentPid()).children().size());
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }
}
