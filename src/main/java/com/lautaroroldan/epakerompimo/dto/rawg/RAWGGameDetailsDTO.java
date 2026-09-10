package com.lautaroroldan.epakerompimo.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RAWGGameDetailsDTO(
        Integer id,
        String slug,
        String name,
        String name_original,
        String description,
        Integer metacritic,
        List<MetacriticPlatformDTO> metacritic_platforms,
        String released,
        boolean tba,
        String updated,
        String background_image,
        String background_image_additional,
        String website,
        Double rating,
        Integer rating_top,
        List<RatingDTO> ratings,
        Map<String, Integer> reactions,
        Integer added,
        Map<String, Integer> added_by_status,
        Integer playtime,
        Integer screenshots_count,
        Integer movies_count,
        Integer creators_count,
        Integer achievements_count,
        String parent_achievements_count,
        String reddit_url,
        String reddit_name,
        String reddit_description,
        String reddit_logo,
        Integer reddit_count,
        String twitch_count,
        String youtube_count,
        String reviews_text_count,
        Integer ratings_count,
        Integer suggestions_count,
        List<String> alternative_names,
        String metacritic_url,
        Integer parents_count,
        Integer additions_count,
        Integer game_series_count,
        EsrbRatingDTO esrb_rating,
        List<PlatformWrapperDTO> platforms
) {
}
