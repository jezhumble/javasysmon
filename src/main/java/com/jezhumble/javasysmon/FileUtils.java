package com.jezhumble.javasysmon;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience methods for interacting with the filesystem.
 */
public class FileUtils {

    private static final Pattern PROC_DIR_PATTERN = Pattern.compile("([\\d]*)");

    private final static FilenameFilter PROCESS_DIRECTORY_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            File fileToTest = new File(dir, name);
            return fileToTest.isDirectory() && PROC_DIR_PATTERN.matcher(name).matches();
        }
    };

    /**
     * If you're using an operating system that supports the proc filesystem,
     * this returns a list of all processes by reading the directories under
     * /proc
     *
     * @return An array of the ids of all processes running on the OS.
     */
    public String[] pidsFromProcFilesystem() {
        return new File("/proc").list(FileUtils.PROCESS_DIRECTORY_FILTER);
    }

    /**
     * Given a filename, reads the entire file into a string.
     *
     * @param fileName The path of the filename to read. Should be absolute.
     * @return A string containing the entire contents of the file
     * @throws IOException If there's an IO exception while trying to read the file
     */
    public String slurp(String fileName) throws IOException {
        return slurpFromInputStream(new FileInputStream(fileName));
    }

    /**
     * Given a filename, reads the entire file into a byte array.
     *
     * @param fileName The path of the filename to read. Should be absolute.
     * @return A byte array containing the entire contents of the file
     * @throws IOException If there's an IO exception while trying to read the file
     */
    public byte[] slurpToByteArray(String fileName) throws IOException {
        File fileToRead = new File(fileName);
        byte[] contents = new byte[(int) fileToRead.length()];
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileToRead);
            inputStream.read(contents);
            return contents;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Given an InputStream, reads the entire file into a string.
     *
     * @param stream The InputStream representing the file to read
     * @return A string containing the entire contents of the input stream
     * @throws IOException If there's an IO exception while trying to read the input stream
     */
    public String slurpFromInputStream(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sw.write(line);
                sw.write('\n');
            }
        } finally {
            stream.close();
        }
        return sw.toString();
    }

    /**
     * Runs a regular expression on a file, and returns the first match.
     *
     * @param pattern The regular expression to use.
     * @param filename The path of the filename to match against. Should be absolute.
     * @return The first match found. Null if no matches.
     */
    public String runRegexOnFile(Pattern pattern, String filename) {
        try {
            final String file = slurp(filename);
            Matcher matcher = pattern.matcher(file);
            matcher.find();
            final String firstMatch = matcher.group(1);
            if (firstMatch != null && firstMatch.length() > 0) {
                return firstMatch;
            }
        } catch (IOException e) {
            // return null to indicate failure
        }
        return null;
    }

}
