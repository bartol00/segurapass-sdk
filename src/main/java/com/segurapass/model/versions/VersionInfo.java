package com.segurapass.model.versions;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VersionInfo {
    private String versionNumber, versionDescription, downloadUrl;
    private LocalDate versionDate;
}
