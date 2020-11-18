/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// JSON helper class
public class UserAttributes {
    @JsonProperty
    Map<String, String> userAttributes;
}
