package com.tu.courier.dto;

public class OfficeReportRow {

    private final String officeName;
    private final long shipmentsCount;
    private final Double revenue;

    public OfficeReportRow(String officeName, long shipmentsCount, Double revenue) {
        this.officeName = officeName;
        this.shipmentsCount = shipmentsCount;
        this.revenue = revenue;
    }

    public String getOfficeName() {
        return officeName;
    }

    public long getShipmentsCount() {
        return shipmentsCount;
    }

    public Double getRevenue() {
        return revenue;
    }
}
