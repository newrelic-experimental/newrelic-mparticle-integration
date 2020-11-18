package com.nr.api.insights;

import com.nr.logging.mparticle.utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RetryableException extends IOException {
    private static final Logger log = new Logger(OverloadException.class);
    private Map<String, List<String>> headers;
    private RetryableException() {
    }

    public RetryableException(Map<String, List<String>> headerFields) {
        headers = headerFields;
    }
    private int getValue(String key) {
        int value = 0;
        if (headers.containsKey(key)) {
            List<String> list = headers.get(key);
            switch (list.size()) {
                case 0:
                    log.severe("No header fields available for key: %s", key);
                    break;
                case 1:
                    String s = list.get(0);
                    try {
                        value = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        log.severe("NumberFormatException parsing value: %s for key: %s", s, key);
                    }
                    break;
                default:
                    log.warning("Multiple header fields available for key: %s value: %s", key, list);
                    break;
            }
        }
        return value;
    }

    private int rateLimitReset() {
        final String key = "X-RateLimit-Reset";
        return getValue(key);
    }

    private int retryAfter() {
        final String key = "Retry-After";
        return getValue(key);
    }

    public int secondsUntilRetry() {
        return (rateLimitReset() + retryAfter());
    }
}
