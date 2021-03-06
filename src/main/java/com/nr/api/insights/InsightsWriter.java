/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.api.insights;

import com.nr.logging.mparticle.utils.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

/*
Insights limits the size and rate of custom events:

Attributes: 255 maximum per event
String attributes: 4KB maximum length
Batch total count: 1000 events per call
Batch total size: 1MB maximum per call
API calls exceeding 10 seconds will timeout
Payloads exceeding 100KB may see increased response times
 */
public class InsightsWriter {
    private static final Logger log = new Logger(InsightsWriter.class);

    private StringBuffer getConnectionStreamValue(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        bufferedReader.close();
        return response;
    }

    /*
     * gzip -c example_events.json | curl -X POST -H "Content-Type: application/json" -H "X-Insert-Key: YOUR_KEY_HERE" -H "Content-Encoding: gzip" https://insights-collector.newrelic.com/v1/accounts/YOUR_ACCOUNT_ID/events --data-binary @-
     */
    public StringBuffer post(String urlString, String apiKey, byte[] form) throws IOException {
        log.fine("post: url: %s insertKey: %s", urlString, apiKey);

        HttpsURLConnection connection = null;
        URL url = new URL(urlString);
        connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("X-Insert-Key", apiKey);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Encoding", "gzip");
        connection.setRequestMethod("POST");

        connection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.write(form);
        dataOutputStream.flush();
        dataOutputStream.close();

        int responseCode = connection.getResponseCode();
        log.fine("postJson: response Code: %s", responseCode);
        if (responseCode >= 400) {
            switch (responseCode) {
                case 400:
                    throw new ContentLengthException();
                case 403:
                    throw new APIKeyException();
                case 408:
                    throw new RequestTimeoutExeption();
                case 413:
                    throw new RequestTooLargeException();
                case 415:
                    throw new InvalidContentTypeException();
                case 429:
                    throw new OverloadException(connection.getHeaderFields());
                case 503:
                    throw new ServiceTemporarilyUnavailableException(connection.getHeaderFields());
                default:
                    StringBuffer error = getConnectionStreamValue(connection.getErrorStream());
                    throw new IOException(String.format("url: %s status: %s error: %s", connection.getURL(), responseCode, error.toString()));
                }
            }
        StringBuffer response = getConnectionStreamValue(connection.getInputStream());
        log.finer("postJson: response: %s", response.toString());
        return response;
    }
}
