package org.imtf.siron.supporttool.model;

import java.util.List;

public class ProductInfo {

    private String productName;
    private List<String> clientIds;

    public List<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
