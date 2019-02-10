package org.anhcraft.enc.api.gem;

/**
 * Result of gem-merging.
 */
public enum MergeResult{
    /**
     * A gem was merged into a whole stack of items successfully.
     */
    SUCCESS,
    /**
     * Failed to marge a gem into a stack of items. The stack was not destroyed.
     */
    FAILURE,
    /**
     * Failed to marge a gem into a stack of items. The stack was destroyed.
     */
    DESTRUCTION
}
