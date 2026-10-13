package com.chunkvault.repository;

import com.chunkvault.entity.StorageNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageNodeRepository extends JpaRepository<StorageNodeEntity, Long> {
    Optional<StorageNodeEntity> findByNodeName(String nodeName);
    boolean existsByNodeName(String nodeName);
}
