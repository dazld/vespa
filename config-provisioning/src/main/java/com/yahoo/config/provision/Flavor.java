// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.config.provision;

import java.util.List;

/**
 * A host flavor (type).
 *
 * @author bratseth
 */
public interface Flavor {

    /** Returns the unique identity of this flavor */
    String flavorName();

    String getDescription();

    /** Returns the cost associated with usage of this flavor */
    int cost();

    /**
     * A stock flavor is any flavor we expect more of in the future.
     * Stock flavors are assigned to applications by cost priority.
     *
     * Non-stock flavors are used for nodes for which a fixed amount has already been added
     * to the system for some historical reason. These nodes are assigned to applications
     * when available by exact match and ignoring cost.
     */
    boolean isStock();

    boolean hasFastDisk();

    double getMinCpuCores();

    double getMinMainMemoryAvailableGb();

    double getMinDiskAvailableGb();

    double getBandwidth();

    /** Returns whether the flavor is retired (should no longer be allocated) */
    boolean isRetired();

    FlavorType getType();
    
    /** Convenience, returns getType() == Type.DOCKER_CONTAINER */
    default boolean isDocker() { return getType() == FlavorType.DOCKER_CONTAINER; }

    /** The free capacity we would like to preserve for this flavor */
    int getIdealHeadroom();

    /**
     * Returns the canonical name of this flavor - which is the name which should be used as an interface to users.
     * The canonical name of this flavor is:
     * <ul>
     *   <li>If it replaces one flavor, the canonical name of the flavor it replaces
     *   <li>If it replaces multiple or no flavors - itself
     * </ul>
     *
     * The logic is that we can use this to capture the gritty details of configurations in exact flavor names
     * but also encourage users to refer to them by a common name by letting such flavor variants declare that they
     * replace the canonical name we want. However, if a node replaces multiple names, we have no basis for choosing one
     * of them as the canonical, so we return the current as canonical.
     */
    default String canonicalName() {
        return isCanonical() ? flavorName() : replaces().get(0).canonicalName();
    }
    
    /** Returns whether this is a canonical flavor */
    default boolean isCanonical() {
        return replaces().size() != 1;
    }

    /**
     * The flavors this (directly) replaces.
     * This is immutable if this is frozen, and a mutable list otherwise.
     */
    List<Flavor> replaces();

    /**
     * Returns whether this flavor satisfies the requested flavor, either directly
     * (by being the same), or by directly or indirectly replacing it
     */
    default boolean satisfies(Flavor flavor) {
        if (equals(flavor)) {
            return true;
        }
        if (isRetired()) {
            return false;
        }
        for (Flavor replaces : replaces())
            if (replaces.satisfies(flavor))
                return true;
        return false;
    }
    
    /** Returns whether this flavor has at least as much as each hardware resource as the given flavor */
    default boolean isLargerThan(Flavor other) {
        return this.getMinCpuCores() >= other.getMinCpuCores() &&
               this.getMinDiskAvailableGb() >= other.getMinDiskAvailableGb() &&
               this.getMinMainMemoryAvailableGb() >= other.getMinMainMemoryAvailableGb() &&
               this.hasFastDisk() || ! other.hasFastDisk();
    }

}
