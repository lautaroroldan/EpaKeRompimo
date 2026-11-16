package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.StoreDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.entity.StoreLinkOverride;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.exceptions.InvalidMatchFixException;
import com.lautaroroldan.epakerompimo.repository.GameMatchOverrideRepository;
import com.lautaroroldan.epakerompimo.repository.StoreLinkOverrideRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GameMatchOverrideServiceManager implements GameMatchOverrideService {

    private final GameMatchOverrideRepository gameMatchOverrideRepository;
    private final StoreLinkOverrideRepository storeLinkOverrideRepository;
    private final UserRepository userRepository;

    public GameMatchOverrideServiceManager(GameMatchOverrideRepository gameMatchOverrideRepository,
                                           StoreLinkOverrideRepository storeLinkOverrideRepository,
                                           UserRepository userRepository) {
        this.gameMatchOverrideRepository = gameMatchOverrideRepository;
        this.storeLinkOverrideRepository = storeLinkOverrideRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<GameMatchOverride> findGameOverride(String title) {
        String key = titleKey(title);
        return key.isEmpty() ? Optional.empty() : gameMatchOverrideRepository.findByTitleKey(key);
    }

    @Override
    public List<StoreLinkOverride> findStoreOverrides(String title) {
        String key = titleKey(title);
        return key.isEmpty() ? List.of() : storeLinkOverrideRepository.findByTitleKey(key);
    }

    @Override
    public List<PriceDTO> applyStoreOverrides(String title, List<PriceDTO> offers) {
        if (offers == null || offers.isEmpty()) {
            return offers == null ? List.of() : offers;
        }

        Map<String, StoreLinkOverride> byStore = findStoreOverrides(title).stream()
                .collect(Collectors.toMap(StoreLinkOverride::getStoreName, Function.identity(),
                        (first, duplicate) -> first));

        if (byStore.isEmpty()) {
            return offers;
        }

        return offers.stream()
                .map(offer -> applyOverride(offer, byStore))
                .filter(offer -> offer != null)
                .toList();
    }

    private PriceDTO applyOverride(PriceDTO offer, Map<String, StoreLinkOverride> byStore) {
        StoreLinkOverride override = byStore.get(offer.store() != null ? offer.store().name() : null);
        if (override == null) {
            return offer;
        }
        if (override.isHidden()) {
            return null;
        }
        if (override.getUrl() == null || override.getUrl().isBlank()) {
            return offer;
        }
        return new PriceDTO(offer.salePrice(), offer.normalPrice(),
                new StoreDTO(offer.store().name(), override.getUrl(), offer.store().urlLogo()),
                offer.providerName(), offer.discountPercent(), offer.currency());
    }

    @Override
    @Transactional
    public GameMatchOverride saveGameOverride(String displayTitle, Long igdbGameId, String itadGameId,
                                              Long updatedByUserId) {
        String key = titleKey(displayTitle);
        if (key.isEmpty()) {
            throw new InvalidMatchFixException("El título del juego no puede quedar vacío");
        }
        if (igdbGameId == null && (itadGameId == null || itadGameId.isBlank())) {
            throw new InvalidMatchFixException("Indicá al menos un juego de IGDB o de ITAD");
        }

        GameMatchOverride override = gameMatchOverrideRepository.findByTitleKey(key)
                .orElseGet(GameMatchOverride::new);
        override.setTitleKey(key);
        override.setDisplayTitle(displayTitle.trim());
        override.setIgdbGameId(igdbGameId);
        override.setItadGameId(itadGameId == null || itadGameId.isBlank() ? null : itadGameId.trim());
        override.setUpdatedAt(Instant.now());
        override.setUpdatedBy(userRepository.findById(updatedByUserId).orElse(null));
        return gameMatchOverrideRepository.save(override);
    }

    @Override
    @Transactional
    public StoreLinkOverride saveStoreOverride(String displayTitle, String storeName, String url,
                                               boolean hidden, Long updatedByUserId) {
        String key = titleKey(displayTitle);
        if (key.isEmpty()) {
            throw new InvalidMatchFixException("El título del juego no puede quedar vacío");
        }
        if (storeName == null || storeName.isBlank()) {
            throw new InvalidMatchFixException("Indicá la tienda cuyo link hay que corregir");
        }

        String cleanUrl = url == null || url.isBlank() ? null : url.trim();
        if (!hidden && cleanUrl == null) {
            throw new InvalidMatchFixException("Pegá la URL correcta o marcá la oferta como oculta");
        }

        String cleanStore = storeName.trim();
        StoreLinkOverride override = storeLinkOverrideRepository
                .findByTitleKeyAndStoreName(key, cleanStore)
                .orElseGet(StoreLinkOverride::new);
        override.setTitleKey(key);
        override.setDisplayTitle(displayTitle.trim());
        override.setStoreName(cleanStore);
        override.setUrl(cleanUrl);
        override.setHidden(hidden);
        override.setUpdatedAt(Instant.now());
        override.setUpdatedBy(userRepository.findById(updatedByUserId).orElse(null));
        return storeLinkOverrideRepository.save(override);
    }

    @Override
    @Transactional
    public void clear(String title) {
        String key = titleKey(title);
        if (key.isEmpty()) {
            return;
        }
        storeLinkOverrideRepository.deleteByTitleKey(key);
        gameMatchOverrideRepository.findByTitleKey(key).ifPresent(gameMatchOverrideRepository::delete);
    }

    private static String titleKey(String title) {
        return TitleNormalizer.normalize(title);
    }
}
