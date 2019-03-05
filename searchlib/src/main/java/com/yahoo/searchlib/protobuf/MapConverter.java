package com.yahoo.searchlib.protobuf;

import ai.vespa.searchlib.searchprotocol.protobuf.Searchprotocol.StringProperty;
import ai.vespa.searchlib.searchprotocol.protobuf.Searchprotocol.TensorProperty;
import com.google.protobuf.ByteString;
import com.yahoo.tensor.Tensor;
import com.yahoo.tensor.serialization.TypedBinaryFormat;

import java.util.List;
import java.util.Map;

public class MapConverter {
    @FunctionalInterface
    public interface PropertyInserter<T> {
        void add(T prop);
    }

    public static void convertMapTensors(Map<String, Object> map, PropertyInserter<TensorProperty.Builder> inserter) {
        map.forEach((k, v) -> {
            if(v instanceof Tensor) {
                byte[] tensor = TypedBinaryFormat.encode((Tensor) v);
                inserter.add(TensorProperty.newBuilder().setName(k).setValue(ByteString.copyFrom(tensor)));
            }
        });
    }

    public static void convertMapStrings(Map<String, Object> map, PropertyInserter<StringProperty.Builder> inserter) {
        map.forEach((k, v) -> {
            if(!(v instanceof Tensor)) {
                inserter.add(StringProperty.newBuilder().setName(k).setValue(v.toString()));
            }
        });
    }

    public static void convertStringMultiMap(Map<String, List<String>> map, PropertyInserter<StringProperty.Builder> inserter) {
        map.forEach((k, v) -> {
            if(v != null) {
                v.forEach(elem -> inserter.add(StringProperty.newBuilder().setName(k).setValue(elem)));
            }
        });
    }

    public static void convertMultiMap(Map<String, List<Object>> map, PropertyInserter<StringProperty.Builder> stringInserter,
            PropertyInserter<TensorProperty.Builder> tensorInserter) {
        map.forEach((k, values) -> {
            if(values != null) {
                values.forEach(v -> {
                    if(v != null) {
                        if(v instanceof Tensor) {
                            byte[] tensor = TypedBinaryFormat.encode((Tensor) v);
                            tensorInserter.add(TensorProperty.newBuilder().setName(k).setValue(ByteString.copyFrom(tensor)));
                        } else {
                            stringInserter.add(StringProperty.newBuilder().setName(k).setValue(v.toString()));
                        }
                    }
                });
            }
        });
    }
}
