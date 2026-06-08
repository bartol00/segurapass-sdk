package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DecryptedCredentials {
    private List<DecryptedCredential> credentials;
}
