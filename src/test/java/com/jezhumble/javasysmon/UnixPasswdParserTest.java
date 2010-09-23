package com.jezhumble.javasysmon;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;

public class UnixPasswdParserTest extends TestCase {

    public void testShouldHandleEmptyLineInPasswdFile() {
        String emptyLine = "+::::::\n";
        BufferedReader reader = new BufferedReader(new StringReader(emptyLine));
        UnixPasswdParser unixPasswdParser = new UnixPasswdParser();
        unixPasswdParser.parse(reader);
    }

}
