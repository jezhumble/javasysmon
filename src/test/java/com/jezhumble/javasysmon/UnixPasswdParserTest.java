package com.jezhumble.javasysmon;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;

public class UnixPasswdParserTest extends TestCase {

    public void testShouldHandleEmptyLineInPasswdFile() {
        String emptyLine = "+::::::\n";
        BufferedReader reader = new BufferedReader(new StringReader(emptyLine));
        UnixPasswdParser unixPasswdParser = new UnixPasswdParser();
        final HashMap passwd = unixPasswdParser.parse(reader);
        Assert.assertEquals(0, passwd.size());
    }

}
