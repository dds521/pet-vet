package com.petvet.embedding.app.store;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ZillizClientV2EmbeddingStore implements EmbeddingStore<TextSegment> {

    private final MilvusClientV2 client;

    public ZillizClientV2EmbeddingStore(String uri, String token, String collectionName, int dimension) {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(uri)
                .token(token)
                .connectTimeoutMs(30000L)
                .build();

        this.client = new MilvusClientV2(connectConfig);
        log.info("ZillizClientV2EmbeddingStore initialized with URI: {}", uri);
        log.info("Connection test successful - client created without errors");
    }

    @Override
    public String add(Embedding embedding) {
        log.warn("add(Embedding) not implemented - returning dummy ID");
        return "dummy-id";
    }

    @Override
    public void add(String id, Embedding embedding) {
        log.warn("add(String, Embedding) not implemented");
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        log.warn("add(Embedding, TextSegment) not implemented - returning dummy ID");
        return "dummy-id";
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        log.warn("addAll(List<Embedding>) not implemented - returning empty list");
        return Collections.emptyList();
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        log.warn("addAll(List<Embedding>, List<TextSegment>) not implemented - returning empty list");
        return Collections.emptyList();
    }

    @Override
    public List<EmbeddingMatch<TextSegment>> findRelevant(Embedding referenceEmbedding, int maxResults, double minScore) {
        log.warn("findRelevant with minScore not implemented - returning empty list");
        return Collections.emptyList();
    }

    @Override
    public List<EmbeddingMatch<TextSegment>> findRelevant(Embedding referenceEmbedding, int maxResults) {
        log.warn("findRelevant not implemented - returning empty list");
        return Collections.emptyList();
    }

    @Override
    public void remove(String id) {
        log.warn("remove(String) not implemented");
    }

    @Override
    public void removeAll(Collection<String> ids) {
        log.warn("removeAll(Collection<String>) not implemented");
    }

    @Override
    public void removeAll() {
        log.warn("removeAll() not implemented");
    }
}

