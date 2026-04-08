package com.segurapass.model.deletion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthorizedDeletionCompleteReq {
    private UUID deviceId;
    private String M1;
}
