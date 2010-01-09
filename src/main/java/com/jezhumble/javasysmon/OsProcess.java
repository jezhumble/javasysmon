package com.jezhumble.javasysmon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This object represents a node in the process tree. It knows
 * information about the process it represents, and also
 * what its children are.
 */
public class OsProcess {

    private final ArrayList children = new ArrayList();
    private final ProcessInfo processInfo;

    private OsProcess(ProcessInfo processInfo) {
        this.processInfo = processInfo;
    }

    /**
     * This method is the only way to create an OsProcess object.
     * It creates a graph of OsProcess objects from an array of
     * ProcessInfo objects representing the processes in the system.
     *
     * @param processTable An array of objects representing the processes in the system.
     * @return A graph of OsProcess objects.
     */
    public static OsProcess createTree(ProcessInfo[] processTable) {
        HashMap processes = new HashMap();
        OsProcess topLevelProcess = new OsProcess(null);
        for (int i = 0; i < processTable.length; i++) {
            OsProcess process = new OsProcess(processTable[i]);
            processes.put(new Integer(processTable[i].getPid()), process);
        }
        for (int i = 0; i < processTable.length; i++) {
            int pid = processTable[i].getPid();
            int ppid = processTable[i].getParentPid();
            OsProcess process = (OsProcess) processes.get(new Integer(pid));
            if (ppid == pid || !processes.containsKey(new Integer(ppid))) {
                topLevelProcess.children.add(process);
            } else {
                ((OsProcess) processes.get(new Integer(ppid))).children.add(process);
            }
        }
        return topLevelProcess;
    }

    /**
     * Gets the list of child processes of this object.
     *
     * @return The list of child processes of this object.
     */
    public List children() {
        return children;
    }

    /**
     * Information about this process.
     *
     * @return Information about this process.
     */
    public ProcessInfo processInfo() {
        return processInfo;
    }

    /**
     * Finds and returns a particular node in the process tree
     * by its id.
     *
     * @param pid the id of the process to find.
     * @return The process node in the tree.
     */
    public OsProcess find(int pid) {
        if (this.processInfo != null && this.processInfo.getPid() == pid) {
            return this;
        }
        for (Iterator it = children.iterator(); it.hasNext();) {
            final OsProcess found = ((OsProcess) it.next()).find(pid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Method to allow visiting the process tree. Use the convenience method
     * {@link JavaSysMon#visitProcessTree}
     *
     * @param processVisitor An instance of {@link ProcessVisitor}
     * @param level          The level currently being visited
     */
    public void accept(ProcessVisitor processVisitor, int level) {
        for (Iterator it = children.iterator(); it.hasNext();) {
            ((OsProcess) it.next()).accept(processVisitor, level + 1);
        }
        if (this.processInfo != null) {
            if (processVisitor.visit(this, level)) {
                new JavaSysMon().killProcess(processInfo.getPid());
            }
        }
    }
}
