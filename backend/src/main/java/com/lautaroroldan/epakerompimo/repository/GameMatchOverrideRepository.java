package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameMatchOverrideRepository extends JpaRepository<GameMatchOverride, Long> {

    Optional<GameMatchOverride> findByTitleKey(String titleKey);
}
