package com.jezhumble.javasysmon;

import junit.framework.Assert;
import org.junit.Test;

public class OsProcessTest {
    private static ProcessInfo[] info = {
            new ProcessInfo(0, 0, "", "init", "", 0, 0, 0, 0),
            new ProcessInfo(1, 0, "", "login", "", 0, 0, 0, 0),
            new ProcessInfo(2, 0, "", "daemon", "", 0, 0, 0, 0),
            new ProcessInfo(3, 1, "", "bash", "", 0, 0, 0, 0),
            new ProcessInfo(4, 5, "", "orphan", "", 0, 0, 0, 0)
    };

    @Test
    public void shouldCreateProcessTree() {
        OsProcess virtualNode = OsProcess.createTree(info);
        Assert.assertEquals(virtualNode.children().size(), 2);
    }

    @Test
    public void shouldFindDescendants() {
        OsProcess virtualNode = OsProcess.createTree(info);
        Assert.assertEquals("bash", virtualNode.find(3).processInfo().getName());
        Assert.assertEquals(virtualNode.find(50), null);       
    }
}
