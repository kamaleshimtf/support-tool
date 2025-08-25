package com.imtf.cstool.supporttool.model;


import java.util.HashMap;
import java.util.Map;

public class Product {

    private Map<String,FilterPaths> productFiles = new HashMap<String,FilterPaths>();

    public Product(Map<String,FilterPaths> productFiles) {
        this.productFiles = productFiles;
    }
}
