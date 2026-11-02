package com.example.warehouseApp.mapper;

import com.example.warehouseApp.model.LabeledEnum;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class LabeledEnums {

    private LabeledEnums() {
    }

    public static <E extends Enum<E> & LabeledEnum> E map(Class<E> type, String raw, String fieldName) {
        String value = raw.trim();
        for (E e : type.getEnumConstants()) {
            if (e.getLabel().equalsIgnoreCase(value) || e.name().equalsIgnoreCase(value)) {
                return e;
            }
        }
        String allowed = Arrays.stream(type.getEnumConstants())
                .map(LabeledEnum::getLabel)
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Неизвестное значение поля «" + fieldName
                + "»: «" + raw + "». Допустимые варианты: " + allowed);
    }
}