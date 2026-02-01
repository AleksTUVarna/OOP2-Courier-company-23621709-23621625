package com.tu.courier.dto;

public class EnumCountRow {
    private final String key;
    private final long count;

    public EnumCountRow(String key, long count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() { return key; }
    public long getCount() { return count; }
}
