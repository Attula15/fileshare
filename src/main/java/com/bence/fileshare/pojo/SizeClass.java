package com.bence.fileshare.pojo;

import lombok.Data;

@Data
public class SizeClass {
    private double fileSize;
    private String unit;

    public SizeClass(){}

    public SizeClass(double fileSize, String unit) {
        this.fileSize = fileSize;
        this.unit = unit;
    }
}
