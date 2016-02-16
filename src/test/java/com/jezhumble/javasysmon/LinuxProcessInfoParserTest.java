package com.jezhumble.javasysmon;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LinuxProcessInfoParserTest {

    private static final int USER_HZ = 100;
    private static final String COMMAND_LINE = "cl";
    private static final HashMap UIDS = new HashMap();
    private static final int EXPECTED_PID = 25883;
    private static final int EXPECTED_PPID = 25097;

    static {
        UIDS.put("1000", "tbombadil");
    }

    private FileUtils fileUtils;

    @Before
    public void setUp() throws Exception {
        fileUtils = new FileUtils();
    }

    @Test
    public void shouldParseExpectedCase() throws IOException {
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

    @Test
    public void shouldParseCommandWithSpaces() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_spaces"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
    }

    @Test
    public void shouldParseCommandWithClosingParen() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_paren"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
    }

    @Test
    public void shouldParseCommandWithClosingParenThenNumbers() throws IOException {
        LinuxProcessInfoParser parser = new LinuxProcessInfoParser(getTestFileContents("test_stat_numbers"),
                                                                   getTestFileContents("test_status"),
                                                                   "cl", UIDS, USER_HZ);
        ProcessInfo processInfo = parser.parse();
        assertEquals(EXPECTED_PID, processInfo.getPid());
        assertEquals(EXPECTED_PPID, processInfo.getParentPid());
    }

    @Test
    public void shouldParseInvalidStatNumbers() throws IOException {
        invalidStatTest("test_stat_invalid_numbers", "Unable to parse stat");
    }

    @Test
    public void shouldParseInvalidStatNoParens() throws IOException {
        invalidStatTest("test_stat_no_parens", "does not include expected parens around process name");
    }

    @Test
    public void shouldParseInvalidStatTooFewFields() throws IOException {
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
        return fileUtils.slurpFromInputStream(this.getClass().getClassLoader().getResourceAsStream(filename));
    }
}
