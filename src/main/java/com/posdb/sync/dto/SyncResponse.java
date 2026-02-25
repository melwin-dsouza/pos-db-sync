package com.posdb.sync.dto;

public class SyncResponse {
    public Integer totalRecords;
    public Integer successRecords;
    public Integer failedRecords;
    public String message;

    public SyncResponse() {
    }

    public SyncResponse(Integer totalRecords, Integer successRecords, Integer failedRecords, String message) {
        this.totalRecords = totalRecords;
        this.successRecords = successRecords;
        this.failedRecords = failedRecords;
        this.message = message;
    }
}

