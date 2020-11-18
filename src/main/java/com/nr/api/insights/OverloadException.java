package com.nr.api.insights;

import com.nr.logging.mparticle.utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OverloadException extends RetryableException {

    public OverloadException(Map<String, List<String>> headerFields) {
        super(headerFields);
    }
}