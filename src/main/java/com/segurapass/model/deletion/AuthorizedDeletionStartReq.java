package com.segurapass.model.deletion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthorizedDeletionStartReq {
    private UUID deviceId;
    private String A;
}
