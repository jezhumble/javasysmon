package com.jezhumble.javasysmon;

/**
 * Allows you to visit the process tree using {@link JavaSysMon#visitProcessTree}
 */
public interface ProcessVisitor {
    /**
     * Called on every node. The process tree is traversed depth-first
     * @param process The current process being visited
     * @param level How many levels beneath the initial node visited you are (0-indexed)
     * @return Whether or not to kill the process being visited
     */
    boolean visit(OsProcess process, int level);
}
