JavaSysMon
==========

JavaSysMon is designed to provide an OS-independent way to manage OS processes and get live system performance information such as CPU and memory usage, distributed as a single jar file. It is written in C and Java. However the native binaries are hidden away inside the jar, so you never need to worry about them.

Currently it supports Mac OS X, Linux, Windows, and Solaris. Ultimately we aim to support everything from AIX to Android.

If you’re interested in adding support for a new platform, check out the [project wiki](http://wiki.github.com/jezhumble/javasysmon).

Download latest
---------------

The current version of JavaSysMon is 0.3.0, released January 8th. You can get it here: http://github.com/jezhumble/javasysmon/downloads

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
    etc...

For full details of the API, consult the [JavaDoc](http://jezhumble.github.com/javasysmon/)

Current support and limitations
-------------------------------

* Currently supports Mac OS X, Linux, Windows, and Solaris
* Solaris binary is compiled on x86 on OpenSolaris, so it won't work on SPARC, and has not been tested on SunOS < 5.11
* Solaris CPU usage only correctly reports usage for first CPU.
* Supports Java 1.4 and above
* CPU speed on Linux only reports correct values for Intel CPUs

Source code
-----------

The Java source code sits under src/main/java. The C source code is in src/main/c, with a subdirectory for each platform supported by JavaSysMon. The compiled binaries are stored in lib/native, and it is these that are used to build the jar when you run ant. So if you change the c source, you'll need to compile and copy the binary to lib/native before running ant in order to test your changes.

License
-------

JavaSysMon uses the NetBSD (2-line) license.

Links
-----

* [Source code](http://github.com/jezhumble/javasysmon)
* [Wiki](http://wiki.github.com/jezhumble/javasysmon)
* [JavaDoc](http://jezhumble.github.com/javasysmon/)
* [Bugs/Features](http://github.com/arya/javasysmon/issues)
* [Mailing List](http://groups.google.com/group/javasysmon)
