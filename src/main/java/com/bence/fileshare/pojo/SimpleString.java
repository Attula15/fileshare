package com.bence.fileshare.pojo;

import lombok.Data;

@Data
public class SimpleString {
    String simpleString;

    public SimpleString(String simpleString) {
        this.simpleString = simpleString;
    }
}
