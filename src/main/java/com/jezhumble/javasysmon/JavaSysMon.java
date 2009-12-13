package com.jezhumble.javasysmon;

public class JavaSysMon {

    public static void main (String[] params) {
        Monitor monitor = new MacOsXMonitor();
        System.out.println("CPU Usage: " + monitor.cpuUsage());
    }
    
}