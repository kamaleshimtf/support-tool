package org.imtf.siron.supporttool.model;

public class ErrorResponse {

    private String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getError() {
        return errorMessage;
    }

    public ErrorResponse() {
    }
}
