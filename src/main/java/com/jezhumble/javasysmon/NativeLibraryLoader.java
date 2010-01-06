package com.jezhumble.javasysmon;

import java.io.*;

// This is "optimised" based on the fact we only load each native library once.
public class NativeLibraryLoader {
    public void loadLibrary(String libraryName) {
        try {
            InputStream is = this.getClass().getResourceAsStream("/" + libraryName);
            File tempNativeLib = getTempFile(libraryName);
            FileOutputStream os = new FileOutputStream(tempNativeLib);
            copyAndClose(is, os);
            System.load(tempNativeLib.getAbsolutePath());
        } catch (IOException ioe) {
            throw new RuntimeException("Couldn't load native library " + libraryName, ioe);
        }
    }

    private void copyAndClose(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int len = is.read(buffer);
            if (len < 0) break;
            os.write(buffer, 0, len);
        }
        is.close();
        os.close();
    }

    private File getTempFile(String libraryName) throws IOException {
        int suffixSeparator = libraryName.lastIndexOf(".");
        String suffix = null;
        String prefix = libraryName;
        if (suffixSeparator >= 0) {
            suffix = libraryName.substring(suffixSeparator);
            prefix = libraryName.substring(0, suffixSeparator - 1);
        }
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        return tempFile;
    }
}
