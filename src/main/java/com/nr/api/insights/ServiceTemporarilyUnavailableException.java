package com.nr.api.insights;

import java.util.List;
import java.util.Map;

public class ServiceTemporarilyUnavailableException extends RetryableException {

    public ServiceTemporarilyUnavailableException(Map<String, List<String>> headerFields) {
        super(headerFields);
    }
}