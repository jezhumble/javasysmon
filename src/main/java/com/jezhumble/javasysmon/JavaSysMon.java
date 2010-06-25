package com.jezhumble.javasysmon;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class provides the main API for JavaSysMon.
 * You must instantiate this class in order to use it,
 * but it stores no state, so there is zero overhead to
 * instantiating it as many times as you like, and
 * hence no need to cache it.
 * <p>
 * When instantiated for the first time, JavaSysMon
 * will discover which operating system it is running on
 * and attempt to load the appropriate OS-specific
 * extensions. If JavaSysMon doesn't support the OS
 * you're running on, all calls to the API will return
 * null or zero values. Probably the best one to test is
 * osName.
 * <p>
 * You can run JavaSysMon directly as a jar file, using
 * the command "java -jar javasysmon.jar", in which case
 * it will display output similar to the UNIX "top"
 * command. You can optionally specify a process id as an
 * argument, in which case JavaSysMon will attempt to
 * kill the process.
 *
 * @author Jez Humble
 */
public class JavaSysMon implements Monitor {

    private static Monitor monitor = null;
    private static ArrayList supported = new ArrayList();

    /**
     * Allows you to register your own implementation of {@link Monitor}.
     *
     * @param myMonitor An implementation of the Monitor interface that all API calls will be delegated to
     */
    public static void setMonitor(Monitor myMonitor) {
        if (monitor == null || monitor instanceof NullMonitor) {
            monitor = myMonitor;
        }
    }

    static void addSupportedConfig(String config) {
        supported.add(config);
    }

    static {
        new MacOsXMonitor();
        new LinuxMonitor();
        new WindowsMonitor();
        new SolarisMonitor();
        new NullMonitor(); // make sure the API never gives back a NPE
    }

    /**
     * Creates a new JavaSysMon object through which to access
     * the JavaSysMon API. All necessary state is kept statically
     * so there is zero overhead to instantiating this class.
     */
    public JavaSysMon() {}

    /**
     * This is the main entry point when running the jar directly.
     * It prints out some system performance metrics and the process table
     * in a format similar to the UNIX top command. Optionally you can
     * specify a process id as an argument, in which case JavaSysMon
     * will attempt to kill the process specified by that pid.
     */
    public static void main (String[] params) throws Exception {
        if (monitor instanceof NullMonitor) {
            System.err.println("Couldn't find an implementation for OS: " + System.getProperty("os.name"));
            System.err.println("Supported configurations:");
            for (Iterator iter = supported.iterator(); iter.hasNext(); ) {
                String config = (String) iter.next();
                System.err.println(config);
            }
        } else {
            if (params.length == 1) {
                System.out.println("Attempting to kill process id " + params[0]);
                monitor.killProcess(Integer.parseInt(params[0]));
            }
            CpuTimes initialTimes = monitor.cpuTimes();
            System.out.println("OS name: " + monitor.osName() +
                    "  Uptime: " + secsInDaysAndHours(monitor.uptimeInSeconds()) +
                    "  Current PID: " + monitor.currentPid());
            System.out.println("Number of CPUs: " + monitor.numCpus() +
                    "  CPU frequency: " + monitor.cpuFrequencyInHz() / (1000*1000) + " MHz");
            System.out.println("RAM " + monitor.physical() + "  SWAP " + monitor.swap());
            System.out.println("Sampling CPU usage...");
            Thread.sleep(500);
            System.out.println("CPU Usage: " + monitor.cpuTimes().getCpuUsage(initialTimes));
            System.out.println("\n" + ProcessInfo.header());
            ProcessInfo[] processes = monitor.processTable();
            for (int i = 0; i < processes.length; i++) {
                System.out.println(processes[i].toString());
            }
        }
    }

    /**
     * Whether or not JavaSysMon is running on a supported platform.
     *
     * @return <code>true</code> if the platform is supported,
     * <code>false</code> if it isn't.
     */
    public boolean supportedPlatform() {
        return !(monitor instanceof NullMonitor);
    }

    private static String secsInDaysAndHours(long seconds) {
        long days = seconds / (60 * 60 * 24);
        long hours = (seconds / (60 * 60)) - (days * 24);
        return days + " days " + hours + " hours";
    }

    // Following is the actual API

    /**
     * Get the operating system name.
     *
     * @return The operating system name.
     */
    public String osName() {
        return monitor.osName();
    }

    /**
     * Get the number of CPU cores.
     *
     * @return The number of CPU cores.
     */
    public int numCpus() {
        return monitor.numCpus();
    }

    /**
     * Get the CPU frequency in Hz
     *
     * @return the CPU frequency in Hz
     */
    public long cpuFrequencyInHz() {
        return monitor.cpuFrequencyInHz();
    }

    /**
     * How long the system has been up in seconds.
     * Doesn't generally include time that the system
     * has been hibernating or asleep.
     *
     * @return The time the system has been up in seconds.
     */
    public long uptimeInSeconds() {
        return monitor.uptimeInSeconds();
    }

    /**
     * Gets the pid of the process that is calling this method
     * (assuming it is running in the same process).
     *
     * @return The pid of the process calling this method.
     */
    public int currentPid() {
        return monitor.currentPid();
    }

    /**
     * Gets a snapshot which contains the total amount
     * of time the CPU has spent in user mode, kernel mode,
     * and idle. Given two snapshots, you can calculate
     * the CPU usage during that time. There is a convenience
     * method to perform this calculation in
     * {@link CpuTimes#getCpuUsage}
     *
     * @return An object containing the amount of time the
     * CPU has spent idle, in user mode and in kernel mode,
     * in milliseconds.
     */
    public CpuTimes cpuTimes() {
        return monitor.cpuTimes();
    }

    /**
     * Gets the physical memory installed, and the amount free.
     *
     * @return An object containing the amount of physical
     * memory installed, and the amount free.
     */
    public MemoryStats physical() {
        return monitor.physical();
    }

    /**
     * Gets the amount of swap available to the operating system,
     * and the amount that is free.
     *
     * @return An object containing the amount of swap available
     * to the system, and the amount free.
     */
    public MemoryStats swap() {
        return monitor.swap();
    }

    /**
     * Get the current process table. This call returns an array of
     * objects, each of which represents a single process. If you want
     * the objects in a tree structure, use {@link #processTree} instead.
     *
     * @return An array of objects, each of which represents a process.
     */
    public ProcessInfo[] processTable() {
        return monitor.processTable();
    }

    /**
     * Gets the current process table in the form of a process tree.
     * The object returned is a top-level container which doesn't actually
     * represent a process - its children are the top-level processes
     * running in the system. This is necessary because some operating systems
     * (Windows, for example) don't have a single top-level process (orphans
     * are literally orphaned), and because the process table snapshot
     * is not atomic. That means the process table thus returned can be
     * internally inconsistent.
     *
     * @return The current process table in the form of a process tree.
     */
    public OsProcess processTree() {
        return OsProcess.createTree(monitor.processTable());
    }

    /**
     * Attempts to kill the process identified by the integer id supplied.
     * This will silently fail if you don't have the authority to kill
     * that process. This method sends SIGTERM on the UNIX platform,
     * and kills the process using TerminateProcess on Windows.
     *
     * @param pid The id of the process to kill
     */
    public void killProcess(int pid) {
        monitor.killProcess(pid);
    }

    /**
     * Allows you to visit the process tree, starting at the node identified by pid.
     * The process tree is traversed depth-first.
     * 
     * @param pid The identifier of the node to start visiting
     * @param processVisitor The visitor
     */
    public void visitProcessTree(final int pid, final ProcessVisitor processVisitor) {
        final OsProcess process = processTree().find(pid);
        if (process != null) {
            process.accept(processVisitor, 0);
        }
    }

    /**
     * Kills the process tree starting at the process identified by pid. The
     * process tree is killed from the bottom up to ensure that orphans are
     * not generated.
     * <p>
     * This method uses {@link #visitProcessTree}.
     * 
     * @param pid The identifier of the process at which to start killing the tree.
     * @param descendantsOnly Whether or not to kill the process you start at,
     * or only its descendants
     */
    public void killProcessTree(final int pid, final boolean descendantsOnly) {
        visitProcessTree(pid, new ProcessVisitor() {
            public boolean visit(OsProcess process, int level) {
                return !descendantsOnly || (pid != process.processInfo().getPid());
            }
        });
    }

    /**
     * Attempts to kill all the descendants of the currently running process.
     */
    public void infanticide() {
        killProcessTree(currentPid(), true);
    }
}
