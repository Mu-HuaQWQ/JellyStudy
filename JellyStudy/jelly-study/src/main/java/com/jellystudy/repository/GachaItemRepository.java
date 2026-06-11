package com.jellystudy.repository;

import com.jellystudy.entity.GachaItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GachaItemRepository extends MongoRepository<GachaItem, String> {
    List<GachaItem> findByEnabledTrue();
    List<GachaItem> findByType(String type);
}
