package com.jezhumble.javasysmon;

public class MacOsXMonitor implements Monitor {

    public native float cpuUsage();

    static {
        System.loadLibrary("javasysmon.dylib");
    }
    
}
