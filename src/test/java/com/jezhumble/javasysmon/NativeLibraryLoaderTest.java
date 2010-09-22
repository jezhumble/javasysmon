package com.jezhumble.javasysmon;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class NativeLibraryLoaderTest extends TestCase {
    private Properties initProperties;

    public void setUp() throws Exception {
        initProperties = System.getProperties();
    }

    public void tearDown() throws Exception {
        System.clearProperty(NativeLibraryLoader.JAVA_SYS_MON_TEMP_DIR);
        System.setProperties(initProperties);
    }

    public void testShouldCreateTempFileUnderSysmonTempDirIfPropertyIsSet() throws IOException {
        File currentDir = new File(".");
        System.setProperty(NativeLibraryLoader.JAVA_SYS_MON_TEMP_DIR, currentDir.getAbsolutePath());
        NativeLibraryLoader loader = new NativeLibraryLoader();
        File tempFile = loader.getTempFile("javasysmon64.dll");
        assertEquals(currentDir.getAbsolutePath(), tempFile.getParentFile().getAbsolutePath());
    }

    public void testShouldCreateTempFileUnderDefaultTempDirIfPropertyIsNotSet() throws IOException {
        NativeLibraryLoader loader = new NativeLibraryLoader();
        File tempFile = loader.getTempFile("javasysmon64.dll");
        assertEquals(new File(System.getProperty("java.io.tmpdir")), tempFile.getParentFile());
    }
}
