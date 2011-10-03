package com.jezhumble.javasysmon;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashMap;

public class LinuxProcessInfoParserTest extends TestCase {

    private static final int USER_HZ = 100;
    private static final String COMMAND_LINE = "cl";
    private static final HashMap UIDS = new HashMap();
    private static final int EXPECTED_PID = 25883;
    private static final int EXPECTED_PPID = 25097;

    static {
        UIDS.put("1000", "tbombadil");
    }

    private FileUtils fileUtils;

    public void setUp() throws Exception {
        fileUtils = new FileUtils();
    }

    public void testExpectedCase() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_expected"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
        assertEquals("test_command", processInfo.getName());
        assertEquals(COMMAND_LINE, processInfo.getCommand());
        assertEquals("tbombadil", processInfo.getOwner());
        assertEquals(1519616L, processInfo.getResidentBytes());
        assertEquals(0L, processInfo.getSystemMillis());
        assertEquals(4554752L, processInfo.getTotalBytes());
        assertEquals(0L, processInfo.getUserMillis());
    }

    public void testCommandWithSpaces() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_spaces"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
    }

    public void testCommandWithClosingParen() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_paren"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
    }

    public void testCommandWithClosingParenThenNumbers() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_numbers"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
    }

    public void testInvalidStatNumbers() throws IOException {
        invalidStatTest("test_stat_invalid_numbers", "Unable to parse stat");
    }

    public void testInvalidStatNoParens() throws IOException {
        invalidStatTest("test_stat_no_parens", "does not include expected parens around process name");
    }

    public void testInvalidStatTooFewFields() throws IOException {
        invalidStatTest("test_stat_few_fields", "contains fewer elements than expected");
    }

    private void invalidStatTest(String statFile, String errorSnippet) throws IOException
    {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents(statFile),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        try
        {
            parser.parse();
            fail("Should not be able to parse invalid stat");
        }
        catch (ParseException e)
        {
            assertTrue("Message '" + e.getMessage() + "' does not contain '" + errorSnippet + "'",
                       e.getMessage().contains(errorSnippet));
        }
    }

    private String getTestFileContents(String filename) throws IOException
    {
        return fileUtils.slurpFromInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(filename));
    }
}
