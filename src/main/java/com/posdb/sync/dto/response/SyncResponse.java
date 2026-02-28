package com.posdb.sync.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SyncResponse {
    private Integer totalRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private String failureDetails;

}

