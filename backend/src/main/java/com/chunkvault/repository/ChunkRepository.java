package com.chunkvault.repository;

import com.chunkvault.entity.ChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<ChunkEntity, Long> {
    List<ChunkEntity> findByFileFileIdOrderBySequenceNumber(String fileId);
    List<ChunkEntity> findByStorageNode(String storageNode);
    void deleteByFileFileId(String fileId);
}
