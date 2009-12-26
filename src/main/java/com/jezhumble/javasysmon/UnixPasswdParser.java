package com.jezhumble.javasysmon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class UnixPasswdParser {

    public HashMap parse() {
        try {
            HashMap users = new HashMap();
            final FileInputStream passwdFile = new FileInputStream("/etc/passwd");
            BufferedReader reader = new BufferedReader(new InputStreamReader(passwdFile, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(":");
                users.put(fields[2], fields[0]);
            }
            return users;
        } catch (Exception e) {
            System.err.println("Error reading password file: " + e.getMessage());
            return new HashMap();
        }
    }
}
