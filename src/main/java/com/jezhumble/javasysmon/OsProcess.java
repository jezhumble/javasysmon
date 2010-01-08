package com.jezhumble.javasysmon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class OsProcess {

    private final ArrayList children = new ArrayList();
    private final ProcessInfo processInfo;

    private OsProcess(ProcessInfo processInfo) {
        this.processInfo = processInfo;
    }

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

    public ArrayList children() {
        return children;
    }

    public ProcessInfo processInfo() {
        return processInfo;
    }

    public OsProcess find(int pid) {
        if (this.processInfo != null && this.processInfo.getPid() == pid) {
            return this;
        }
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            final OsProcess found = ((OsProcess) it.next()).find(pid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public void killTree(boolean descendantsOnly) {
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            ((OsProcess) it.next()).killTree(false);
        }
        if (!descendantsOnly) {
            new JavaSysMon().killProcess(processInfo.getPid());
        }
    }
}
