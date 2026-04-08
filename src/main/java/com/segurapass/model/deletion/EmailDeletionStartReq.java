package com.segurapass.model.deletion;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDeletionStartReq {
    private String email;
}
