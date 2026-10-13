package com.chunkvault.storage;

import com.chunkvault.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple round-robin router across a fixed set of local filesystem storage nodes.
 */
@Component
public class StorageNodeRouter {

    private final StorageProperties props;
    private final Map<String, StorageNode> nodes = new LinkedHashMap<>();
    private final List<StorageNode> orderedNodes = new ArrayList<>();

    public StorageNodeRouter(StorageProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        Path root = Paths.get(props.getStorageRoot()).toAbsolutePath().normalize();
        for (String nodeName : props.getNodes()) {
            Path nodeDir = root.resolve(nodeName);
            StorageNode node = new LocalFileSystemStorageNode(nodeName, nodeDir);
            nodes.put(nodeName, node);
            orderedNodes.add(node);
        }
    }

    public List<StorageNode> allNodes() {
        return List.copyOf(orderedNodes);
    }

    public StorageNode nodeByName(String name) {
        StorageNode n = nodes.get(name);
        if (n == null) {
            throw new IllegalArgumentException("Unknown storage node: " + name);
        }
        return n;
    }

    /** Round-robin: chunk 0 -> node0, chunk 1 -> node1, ... */
    public StorageNode nodeForSequence(int sequenceNumber) {
        if (orderedNodes.isEmpty()) {
            throw new IllegalStateException("No storage nodes configured");
        }
        int idx = Math.floorMod(sequenceNumber, orderedNodes.size());
        return orderedNodes.get(idx);
    }
}
