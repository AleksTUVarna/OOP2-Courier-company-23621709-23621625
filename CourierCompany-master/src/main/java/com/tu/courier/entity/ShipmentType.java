package com.tu.courier.entity;

public enum ShipmentType {
    DOCUMENTS("Документи"),
    PACKAGE("Колет/Пакет"),
    PALLET("Палет");

    private final String label;

    ShipmentType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}