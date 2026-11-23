package com.chunkvault.controller;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.exception.FileNotFoundException;
import com.chunkvault.storage.StorageNode;
import com.chunkvault.storage.StorageNodeRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/nodes")
@CrossOrigin(origins = "*")
public class NodeController {

    private final StorageNodeRouter router;
    private final StorageProperties props;

    public NodeController(StorageNodeRouter router, StorageProperties props) {
        this.router = router;
        this.props = props;
    }

    @GetMapping
    public ResponseEntity<?> listNodes() {
        Path root = Path.of(props.getStorageRoot()).toAbsolutePath().normalize();

        List<Map<String, Object>> nodeInfo = router.allNodes().stream().map(node -> {
            Map<String, Object> info = new HashMap<>();
            info.put("nodeName", node.getName());
            info.put("status", node.isAvailable() ? "ACTIVE" : "DOWN");
            info.put("health", node.isAvailable() ? "HEALTHY" : "UNREACHABLE");
            info.put("storagePath", "storage/" + node.getName());

            Path nodeDir = root.resolve(node.getName());
            try (Stream<Path> walk = Files.walk(nodeDir)) {
                List<Path> chunkFiles = walk.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".chunk"))
                        .toList();
                info.put("chunkCount", (long) chunkFiles.size());
                info.put("storageUsed", chunkFiles.stream()
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .sum());
            } catch (IOException e) {
                info.put("chunkCount", 0L);
                info.put("storageUsed", 0L);
            }

            return info;
        }).toList();

        return ResponseEntity.ok(nodeInfo);
    }

    @GetMapping("/{nodeName}/health")
    public ResponseEntity<?> getNodeHealth(@PathVariable String nodeName) {
        StorageNode node = router.nodeByName(nodeName);
        Map<String, Object> health = new HashMap<>();
        health.put("nodeName", node.getName());
        health.put("status", node.isAvailable() ? "ACTIVE" : "DOWN");
        health.put("health", node.isAvailable() ? "HEALTHY" : "UNREACHABLE");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * Simulates a node outage/recovery without deleting any data.
     * PUT /api/nodes/{nodeName}/availability?available=false
     */
    @PutMapping("/{nodeName}/availability")
    public ResponseEntity<?> setAvailability(@PathVariable String nodeName,
                                             @RequestParam("available") boolean available) {
        StorageNode node;
        try {
            node = router.nodeByName(nodeName);
        } catch (IllegalArgumentException e) {
            throw new FileNotFoundException("Unknown storage node: " + nodeName);
        }
        node.setAvailable(available);

        Map<String, Object> body = new HashMap<>();
        body.put("nodeName", node.getName());
        body.put("status", available ? "ACTIVE" : "DOWN");
        body.put("message", available
                ? "Node " + nodeName + " marked as available"
                : "Node " + nodeName + " marked as unavailable (simulated outage)");
        return ResponseEntity.ok(body);
    }
}
