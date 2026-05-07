package com.dev.demo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    // @JsonValue: Khi Backend trả data về cho Frontend, nó sẽ in ra chữ "Nam" thay vì "MALE"
    @JsonValue
    public String getValue() {
        return value;
    }

    // @JsonCreator: Khi Frontend gửi chữ "Nam" hoặc "nam" lên, Spring sẽ tự động map thành MALE
    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value.trim())) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Giới tính không hợp lệ! Vui lòng chọn Nam, Nữ hoặc Khác.");
    }
}