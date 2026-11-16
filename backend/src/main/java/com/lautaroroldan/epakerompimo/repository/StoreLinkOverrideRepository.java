package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.StoreLinkOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreLinkOverrideRepository extends JpaRepository<StoreLinkOverride, Long> {

    List<StoreLinkOverride> findByTitleKey(String titleKey);

    Optional<StoreLinkOverride> findByTitleKeyAndStoreName(String titleKey, String storeName);

    void deleteByTitleKey(String titleKey);
}
