// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.config.provision.internal;

import com.google.common.collect.ImmutableList;
import com.yahoo.config.provision.Flavor;
import com.yahoo.config.provision.FlavorType;
import com.yahoo.config.provision.NodeFlavors;
import com.yahoo.config.provisioning.FlavorsConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * A host flavor (type). This is a value object where the identity is the name.
 * Use {@link NodeFlavors} to create a flavor.
 *
 * @author bratseth
 */
public class NodeFlavor implements Flavor {

    private final String name;
    private final int cost;
    private final boolean isStock;
    private final FlavorType type;
    private final double minCpuCores;
    private final double minMainMemoryAvailableGb;
    private final double minDiskAvailableGb;
    private final boolean fastDisk;
    private final double bandwidth;
    private final String description;
    private final boolean retired;
    private List<Flavor> replacesFlavors;
    private int idealHeadroom; // Note: Not used after Vespa 6.282

    /**
     * Creates a Flavor, but does not set the replacesFlavors.
     * @param flavorConfig config to be used for Flavor.
     */
    public NodeFlavor(FlavorsConfig.Flavor flavorConfig) {
        this.name = flavorConfig.name();
        this.replacesFlavors = new ArrayList<>();
        this.cost = flavorConfig.cost();
        this.isStock = flavorConfig.stock();
        this.type = FlavorType.valueOf(flavorConfig.environment());
        this.minCpuCores = flavorConfig.minCpuCores();
        this.minMainMemoryAvailableGb = flavorConfig.minMainMemoryAvailableGb();
        this.minDiskAvailableGb = flavorConfig.minDiskAvailableGb();
        this.fastDisk = flavorConfig.fastDisk();
        this.bandwidth = flavorConfig.bandwidth();
        this.description = flavorConfig.description();
        this.retired = flavorConfig.retired();
        this.idealHeadroom = flavorConfig.idealHeadroom();
    }

    @Override
    public String flavorName() { return name; }

    @Override
    public String getDescription() { return description; }

    @Override
    public int cost() { return cost; }

    @Override
    public boolean isStock() { return isStock; }

    @Override
    public boolean hasFastDisk() { return fastDisk; }

    @Override
    public double getMinCpuCores() { return minCpuCores; }

    @Override
    public double getMinMainMemoryAvailableGb() { return minMainMemoryAvailableGb; }

    @Override
    public double getMinDiskAvailableGb() { return minDiskAvailableGb; }

    @Override
    public double getBandwidth() { return bandwidth; }

    @Override
    public boolean isRetired() {
        return retired;
    }

    @Override
    public FlavorType getType() { return type; }

    @Override
    public int getIdealHeadroom() {
        return idealHeadroom;
    }

    @Override
    public List<Flavor> replaces() { return replacesFlavors; }

    /** Irreversibly freezes the content of this */
    public void freeze() {
        replacesFlavors = ImmutableList.copyOf(replacesFlavors);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if ( ! (other instanceof NodeFlavor)) return false;
        return ((NodeFlavor)other).name.equals(this.name);
    }

    @Override
    public String toString() { return "flavor '" + name + "'"; }
}
