JavaSysMon
==========

JavaSysMon is designed to provide an OS-independent way to get live system information such as CPU and memory usage, distributed as a single jar file. It is written in C and Java. However the native binaries are hidden away inside the jar (thanks to [one-jar](http://one-jar.sourceforge.net/)), so you never need to worry about them.

Currently it supports Mac OS X, Linux, Windows, and Solaris. Ultimately we aim to support everything from AIX to Android.

If you’re interested in adding support for a new platform, check out the project wiki.

Download latest
---------------

The current version of JavaSysMon is 0.2.0, released December 28th. You can get it here: http://github.com/jezhumble/javasysmon/downloads

Run it with java -jar

Building and running
--------------------

Run ant, and then:

    java -jar target/javasysmon.jar

Using the library from code
---------------------------

Simply put the jar in your classpath, and use it like this:

    import com.jezhumble.javasysmon.JavaSysMon;
       
    JavaSysMon monitor =   new JavaSysMon();
    
    String osName =        monitor.osName();
    long uptimeInSeconds = monitor.uptimeInSeconds();
    int currentPid =       monitor.currentPid();
    CpuTimes cpuTimes =	   monitor.cpuTimes();
    MemoryStats physical = monitor.physical();
    MemoryStats swap =     monitor.swap();
    int numCpus =          monitor.numCpus();
    long cpuFrequency =    monitor.cpuFrequencyInHz();

Current support and limitations
-------------------------------

* Currently supports Mac OS X, Linux, Windows, and Solaris
* Solaris binary is compiled on x86 on OpenSolaris, so it won't work on SPARC, and may not work on SunOS < 5.11
* Solaris CPU usage only correctly reports usage for first CPU.
* Supports Java 1.4 and above
* CPU speed on Linux only reports correct values for Intel CPUs

License
-------

JavaSysMon uses the NetBSD (2-line) license.

Links
-----

* Code: http://github.com/jezhumble/javasysmon
* Bugs/Features: http://github.com/arya/javasysmon/issues
* Mailing List: http://groups.google.com/group/javasysmon
