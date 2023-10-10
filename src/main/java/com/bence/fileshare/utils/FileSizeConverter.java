package com.bence.fileshare.utils;

import com.bence.fileshare.pojo.SizeClass;

public class FileSizeConverter {
    public static SizeClass convert(double sizeInByte){
        //gb
        if(sizeInByte >= 1000000000){
            return new SizeClass(sizeInByte / 1000000000, "gb");
        }
        //mb
        else if(sizeInByte >= 1000000){
            return new SizeClass(sizeInByte / 1000000, "mb");
        }
        //kb
        else if(sizeInByte >= 1000){
            return new SizeClass(sizeInByte / 1000, "kb");
        }
        else{
            return new SizeClass(sizeInByte, "b") ;
        }
    }
}
