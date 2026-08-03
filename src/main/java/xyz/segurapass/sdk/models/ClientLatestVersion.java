package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientLatestVersion {
    private String latestVersion;
    private Integer protocolVersion;
    private List<ClientVersion> versions;
}
