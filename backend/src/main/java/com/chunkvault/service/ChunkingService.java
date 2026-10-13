package com.chunkvault.service;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.model.ChunkMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Splits a byte array into fixed-size chunks and produces chunk metadata.
 * Chunk size is read from configuration (never hardcoded).
 */
@Service
public class ChunkingService {

    private final StorageProperties props;

    public ChunkingService(StorageProperties props) {
        this.props = props;
    }

    public long chunkSize() {
        return props.getChunkSizeBytes();
    }

    /**
     * Splits data into chunks and returns raw chunk bytes in sequence order.
     */
    public List<byte[]> split(byte[] data) {
        long size = chunkSize();
        if (size <= 0) {
            throw new IllegalStateException("chunk-size-bytes must be > 0");
        }
        int chunkSize = (int) Math.min(size, Integer.MAX_VALUE);
        List<byte[]> chunks = new ArrayList<>();
        if (data.length == 0) {
            // Empty file -> single empty chunk so reconstruction works uniformly
            chunks.add(new byte[0]);
            return chunks;
        }
        for (int offset = 0; offset < data.length; offset += chunkSize) {
            int end = Math.min(offset + chunkSize, data.length);
            chunks.add(Arrays.copyOfRange(data, offset, end));
        }
        return chunks;
    }

    /**
     * Builds metadata for a list of chunks belonging to a file.
     * sequenceNumber is index-based starting at 0.
     */
    public List<ChunkMetadata> buildMetadata(String fileId, List<byte[]> chunks,
                                             java.util.function.BiFunction<Integer, byte[], String> nodePicker) {
        List<ChunkMetadata> metas = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            byte[] data = chunks.get(i);
            String chunkId = fileId + "-C" + String.format("%04d", i);
            String node = nodePicker.apply(i, data);
            metas.add(new ChunkMetadata(chunkId, fileId, i, node, data.length));
        }
        return metas;
    }
}
