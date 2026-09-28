package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientLatestVersion {
    private String latestVersion;
    private Integer protocolVersion;
    private String downloadUrl;
    private UUID nonce;
    private List<ClientVersion> versions;
}
