package com.lautaroroldan.epakerompimo.provider.info;

public record RAWGGameDetailResponse(
        int id,
        String slug,
        String name,
        Boolean tba,
        String released,
        String background_image,
        int rating,
        int rating_top,
//        ratings[]
//        ratings_count,
        int reviews_text_count,
        int added,
//        object added_by_status,
        int metacritic,
        int playtime,
        int suggestions_count,
        String updated,
        String user_game,
        int reviews_count,
        String saturated_color,
        String dominant_color,
        String description
//         platforms y mas...
) {
}
