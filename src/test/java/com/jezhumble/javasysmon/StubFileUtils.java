package com.jezhumble.javasysmon;

import java.io.IOException;
import java.io.InputStream;

public class StubFileUtils extends FileUtils {
    boolean alreadyGotStat = false;

    public String slurp(String fileName) throws IOException {
        InputStream testFile = null;
        if (fileName.equals("/proc/uptime")) {
            testFile = getTestFile("test_uptime");
        }
        if (fileName.equals("/proc/self/stat")) {
            testFile = getTestFile("test_self_stat");
        }
        if (fileName.equals("/proc/meminfo")) {
            testFile = getTestFile("test_meminfo");
        }
        if (fileName.equals("/proc/cpuinfo")) {
            testFile = getTestFile("test_cpuinfo");
        }
        if (fileName.equals("/proc/stat")) {
            testFile = alreadyGotStat ? getTestFile("test_stat_1") : getTestFile("test_stat_0");
            alreadyGotStat = true;
        }
        if (testFile != null) {
            return slurpFromInputStream(testFile);
        }
        return null;
    }

    private InputStream getTestFile(String filename) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(filename);        
    }
}
