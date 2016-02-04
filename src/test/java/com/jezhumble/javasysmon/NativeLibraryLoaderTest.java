package com.jezhumble.javasysmon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class NativeLibraryLoaderTest  {
    private Properties initProperties;

    @Before
    public void setUp() throws Exception {
        initProperties = System.getProperties();
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(NativeLibraryLoader.JAVA_SYS_MON_TEMP_DIR);
        System.setProperties(initProperties);
    }

    @Test
    public void shouldCreateTempFileUnderSysmonTempDirIfPropertyIsSet() throws IOException {
        File currentDir = new File(".");
        System.setProperty(NativeLibraryLoader.JAVA_SYS_MON_TEMP_DIR, currentDir.getAbsolutePath());
        NativeLibraryLoader loader = new NativeLibraryLoader();
        File tempFile = loader.getTempFile("javasysmon64.dll");
        assertEquals(currentDir.getAbsolutePath(), tempFile.getParentFile().getAbsolutePath());
    }

    @Test
    public void shouldCreateTempFileUnderDefaultTempDirIfPropertyIsNotSet() throws IOException {
        NativeLibraryLoader loader = new NativeLibraryLoader();
        File tempFile = loader.getTempFile("javasysmon64.dll");
        assertEquals(new File(System.getProperty("java.io.tmpdir")), tempFile.getParentFile());
    }
}
