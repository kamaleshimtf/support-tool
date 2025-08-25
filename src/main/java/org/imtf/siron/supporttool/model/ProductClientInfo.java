package org.imtf.siron.supporttool.model;

import java.util.List;

public class ProductClientInfo {

    private String rootPath;
    private List<String> clientIds;

    public ProductClientInfo(String rootPath, List<String> clientIds) {
        this.rootPath = rootPath;
        this.clientIds = clientIds;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public List<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }
}
