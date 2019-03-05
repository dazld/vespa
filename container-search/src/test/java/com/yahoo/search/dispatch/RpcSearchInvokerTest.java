package com.yahoo.search.dispatch;

import com.google.common.collect.ImmutableMap;
import com.yahoo.compress.CompressionType;
import com.yahoo.fs4.QueryPacket;
import com.yahoo.prelude.fastsearch.CacheKey;
import com.yahoo.prelude.fastsearch.FastHit;
import com.yahoo.prelude.fastsearch.VespaBackEndSearcher;
import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.dispatch.RpcFillInvoker.GetDocsumsResponseReceiver;
import com.yahoo.search.dispatch.searchcluster.Node;
import com.yahoo.search.searchchain.Execution;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RpcSearchInvokerTest {
    @SuppressWarnings("resource")
    @Test
    public void testProtobufSerialization() throws IOException {
        Query q = new Query("search/?query=test&hits=10&offset=3");
        var holder = new AtomicReference<byte[]>();

        Client.NodeConnection mockNode = () -> {};
        var mockClient = new Client() {
            @Override
            public void search(NodeConnection node, CompressionType compression, int uncompressedLength, byte[] compressedPayload,
                    RpcSearchInvoker responseReceiver, double timeoutSeconds) {
                holder.set(compressedPayload);
            }

            @Override
            public void getDocsums(List<FastHit> hits, NodeConnection node, CompressionType compression, int uncompressedLength,
                    byte[] compressedSlime, GetDocsumsResponseReceiver responseReceiver, double timeoutSeconds) {
                fail("Unexpected call");

            }

            @Override
            public NodeConnection createConnection(String hostname, int port) {
                fail("Unexpected call");
                return null;
            }
        };
        var mockSearcher = new VespaBackEndSearcher() {
            @Override
            protected Result doSearch2(Query query, QueryPacket queryPacket, CacheKey cacheKey, Execution execution) {
                fail("Unexpected call");
                return null;
            }

            @Override
            protected void doPartialFill(Result result, String summaryClass) {
                fail("Unexpected call");
            }
        };
        var mockPool = new RpcResourcePool(mockClient, ImmutableMap.of(7, mockNode));
        var invoker = new RpcSearchInvoker(mockSearcher, new Node(7, "seven", 77, 1), mockPool);
        invoker.sendSearchRequest(q, null);

        assertThat(holder.get().length, equalTo(72));
    }
}
