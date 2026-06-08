package xyz.segurapass.sdk.exception;

import com.segurapass.exception.ApiException;

public class SegurapassSdkException extends RuntimeException {

    private final int statusCode;
    private final String httpMethod;
    private final String endpoint;

    public SegurapassSdkException(int statusCode, String httpMethod, String message, String endpoint) {
        super(message);
        this.statusCode = statusCode;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
    }

    public SegurapassSdkException(ApiException apiException) {
        super(apiException.getMessage());
        this.statusCode = apiException.getStatusCode();
        this.httpMethod = apiException.getHttpMethod();
        this.endpoint = apiException.getEndpoint();
    }
}
