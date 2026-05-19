package com.segurapass.models.credentials;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DecryptedCredentials {
    private List<DecryptedCredential> credentials;
}
