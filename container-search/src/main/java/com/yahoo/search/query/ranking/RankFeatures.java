// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.query.ranking;

import ai.vespa.searchlib.searchprotocol.protobuf.Search;
import com.yahoo.fs4.MapEncoder;
import com.yahoo.searchlib.protobuf.MapConverter;
import com.yahoo.tensor.Tensor;
import com.yahoo.text.JSON;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contains the rank features of a query.
 *
 * @author bratseth
 */
public class RankFeatures implements Cloneable {

    private final Map<String, Object> features;

    public RankFeatures() {
        this(new LinkedHashMap<>());
    }

    private RankFeatures(Map<String, Object> features) {
        this.features = features;
    }

    /** Sets a rank feature by full name to a value */
    public void put(String name, String value) {
        features.put(name, value);
    }

    /** Sets a tensor rank feature */
    public void put(String name, Tensor value) {
        features.put(name, value);
    }

    /** Returns a rank feature as a string by full name or null if not set */
    public String get(String name) {
        Object value = features.get(name);
        if (value == null) return null;
        return value.toString();
    }

    /** Returns this value as whatever type it was stored as. Returns null if the value is not set. */
    public Object getObject(String name) {
        return features.get(name);
    }

    /**
     * Returns a tensor rank feature, or empty if there is no value with this name.
     *
     * @throws IllegalArgumentException if the value is set but is not a tensor
     */
    public Optional<Tensor> getTensor(String name) {
        Object feature = features.get(name);
        if (feature == null) return Optional.empty();
        if (feature instanceof Tensor) return Optional.of((Tensor)feature);
        throw new IllegalArgumentException("Expected a tensor value of '" + name + "' but has " + feature);
    }

    /**
     * Returns the map holding the features of this.
     * This map may be modified to change the rank features of the query.
     */
    public Map<String, Object> asMap() { return features; }

    public boolean isEmpty() {
        return features.isEmpty();
    }

    /**
     * Prepares this for encoding, not for external use. See encode on Query for details.
     * <p>
     * If the query feature is found in the rank feature set,
     * remove all these entries and insert them into the rank property set instead.
     * We want to hide from the user that the query feature value is sent down as a rank property
     * and picked up by the query feature executor in the backend.
     */
    public void prepare(RankProperties rankProperties) {
        if (isEmpty()) return;

        List<String> featuresToRemove = new ArrayList<>();
        List<String> propertiesToInsert = new ArrayList<>();
        for (String key : features.keySet()) {
            if (key.startsWith("query(") && key.endsWith(")")) {
                featuresToRemove.add(key);
                propertiesToInsert.add(key.substring("query(".length(), key.length() - 1));
            } else if (key.startsWith("$")) {
                featuresToRemove.add(key);
                propertiesToInsert.add(key.substring(1));
            }
        }
        for (int i = 0; i < featuresToRemove.size(); ++i) {
            rankProperties.put(propertiesToInsert.get(i), features.remove(featuresToRemove.get(i)));
        }
    }

    public int encode(ByteBuffer buffer) {
        return MapEncoder.encodeMap("feature", features, buffer);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if ( ! (other instanceof RankFeatures)) return false;

        return this.features.equals(((RankFeatures)other).features);
    }

    @Override
    public int hashCode() {
        return features.hashCode();
    }

    @Override
    public RankFeatures clone() {
        return new RankFeatures(new LinkedHashMap<>(features));
    }

    @Override
    public String toString() {
        return JSON.encode(features);
    }

    public void addToProtobuf(Search.Request.Builder builder, boolean includeQueryData) {
        if (includeQueryData) {
            MapConverter.convertMapStrings(features, builder::addRankFeature);
            MapConverter.convertMapTensors(features, builder::addRankFeatureTensor);
        }
    }

}
