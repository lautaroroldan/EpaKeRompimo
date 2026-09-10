package com.lautaroroldan.epakerompimo.enums;

public enum EStore {
    STEAM("1"),
    GAMERSGATE("2"),
    GREENMANGAMING("3"),
    AMAZON("4"),
    GAMESTOP("5"),
    DIRECT2DRIVE("6"),
    GOG("7"),
    ORIGIN("8"),
    GET_GAMES("9"),
    SHINY_LOOT("10"),
    HUMBLE_STORE("11"),
    DESURA("12"),
    UPLAY("13"),
    INDIEGAMESTAND("14"),
    FANATICAL("15"),
    GAMESROCKET("16"),
    GAMES_REPUBLIC("17"),
    SILAGAMES("18"),
    PLAYFIELD("19"),
    IMPERIALGAMES("20"),
    WINGAMESTORE("21"),
    FUNSTOCKDIGITAL("22"),
    GAMEBILLET("23"),
    VOIDU("24"),
    EPIC_GAMES_STORE("25"),
    RAZER_GAME_STORE("26"),
    GAMESPLANET("27"),
    GAMESLOAD("28"),
    TWO_GAME("29"),
    INDIEGALA("30"),
    BLIZZARD_SHOP("31"),
    ALLYOUPLAY("32"),
    DLGAMER("33"),
    NOCTRE("34"),
    DREAMGAME("35");

    private final String storeID;

    EStore(String storeID) {
        this.storeID = storeID;
    }

    public String getStoreID() {
        return storeID;
    }
}
