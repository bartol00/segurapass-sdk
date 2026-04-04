package com.segurapass.model.deletion;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthorizedDeletionCompleteReq {
    private UUID deviceId;
    private String M1;
}
