package com.chunkvault.controller;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.repository.ChunkRepository;
import com.chunkvault.storage.StorageNode;
import com.chunkvault.storage.StorageNodeRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
@CrossOrigin(origins = "*")
public class NodeController {

    private final StorageNodeRouter router;
    private final ChunkRepository chunkRepository;
    private final StorageProperties props;

    public NodeController(StorageNodeRouter router, ChunkRepository chunkRepository, StorageProperties props) {
        this.router = router;
        this.chunkRepository = chunkRepository;
        this.props = props;
    }

    @GetMapping
    public ResponseEntity<?> listNodes() {
        List<StorageNode> nodes = router.allNodes();
        Path root = Path.of(props.getStorageRoot()).toAbsolutePath().normalize();

        List<Map<String, Object>> nodeInfo = nodes.stream().map(node -> {
            Map<String, Object> info = new HashMap<>();
            info.put("nodeName", node.getName());
            info.put("status", "ACTIVE");
            info.put("storagePath", root.resolve(node.getName()).toString());

            long chunkCount = chunkRepository.findByStorageNode(node.getName()).size();
            info.put("chunkCount", chunkCount);

            try {
                long storageUsed = Files.walk(root.resolve(node.getName()))
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (Exception e) {
                                return 0;
                            }
                        })
                        .sum();
                info.put("storageUsed", storageUsed);
            } catch (Exception e) {
                info.put("storageUsed", 0L);
            }

            info.put("health", "HEALTHY");
            return info;
        }).toList();

        return ResponseEntity.ok(nodeInfo);
    }

    @GetMapping("/{nodeName}/health")
    public ResponseEntity<?> getNodeHealth(@PathVariable String nodeName) {
        StorageNode node = router.nodeByName(nodeName);
        Map<String, Object> health = new HashMap<>();
        health.put("nodeName", node.getName());
        health.put("status", "ACTIVE");
        health.put("health", "HEALTHY");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
}
