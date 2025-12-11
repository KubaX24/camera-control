package dev.chytac.camera.entity;

import lombok.Getter;

public enum ConfigValues {

    SHUTTER_SPEED("Shutter speed", "/main/capturesettings/shutterspeed"),
    ISO("ISO", "/main/imgsettings/iso"),
    APERTURE("Aperture", "/main/capturesettings/aperture"),
    WHITE_BALANCE("White balance","/main/imgsettings/whitebalance");


    @Getter
    private final String label;
    @Getter
    private final String config;

    ConfigValues(String label, String config) {
        this.label = label;
        this.config = config;
    }
}
