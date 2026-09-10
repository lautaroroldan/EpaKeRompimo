package com.lautaroroldan.epakerompimo.dto.cheapshark;

import com.lautaroroldan.epakerompimo.enums.ESortByDeals;

import java.util.LinkedHashMap;
import java.util.Map;

public record CheapSharkDealFilterDTO(
        String title,
        String storeIDs,
        Integer exact,
        Integer pageNumber,
        Integer pageSize,
        ESortByDeals sortBy,
        Integer desc,
        Double lowerPrice,
        Double upperPrice,
        Integer metacritic,
        Double steamRating,
        Integer minimumReviewCount,
        Integer maxAge,
        String steamAppID,
        String AAA,
        String steamworks,
        Integer onSale
) {
    public CheapSharkDealFilterDTO {
        if (storeIDs == null) storeIDs = "1,2,3,7,8,11,13,15,21,23,24,25,27";
        if (exact == null) exact = 1;
    }

    public Map<String, Object> toParamMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("storeID", storeIDs);
        map.put("exact", exact);
        map.put("pageNumber", pageNumber);
        map.put("pageSize", pageSize);
        map.put("sortBy", sortBy);
        map.put("desc", desc);
        map.put("lowerPrice", lowerPrice);
        map.put("upperPrice", upperPrice);
        map.put("metacritic", metacritic);
        map.put("steamRating", steamRating);
        map.put("minimumReviewCount", minimumReviewCount);
        map.put("maxAge", maxAge);
        map.put("steamAppID", steamAppID);
        map.put("AAA", AAA);
        map.put("steamworks", steamworks);
        map.put("onSale", onSale);
        map.entrySet().removeIf(e -> e.getValue() == null);
        return map;
    }
}
