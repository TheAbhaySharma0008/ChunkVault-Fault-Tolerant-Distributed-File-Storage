package com.chunkvault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "chunkvault")
public class StorageProperties {

    private long chunkSizeBytes = 1024 * 1024;
    private String storageRoot = "../storage";
    private List<String> nodes = List.of("nodeA", "nodeB", "nodeC");

    public long getChunkSizeBytes() { return chunkSizeBytes; }
    public void setChunkSizeBytes(long chunkSizeBytes) { this.chunkSizeBytes = chunkSizeBytes; }

    public String getStorageRoot() { return storageRoot; }
    public void setStorageRoot(String storageRoot) { this.storageRoot = storageRoot; }

    public List<String> getNodes() { return nodes; }
    public void setNodes(List<String> nodes) { this.nodes = nodes; }
}
