/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mparticle.sdk.model.eventprocessing.UserIdentity;

import java.util.List;

// JSON helper class
public class UserIdentities {
    @JsonProperty
    List<UserIdentity> userIdentities;
}