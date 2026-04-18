package com.segurapass.service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.http.HttpHeaders;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final T body;
    private final HttpHeaders headers;
    private final int statusCode;
}
