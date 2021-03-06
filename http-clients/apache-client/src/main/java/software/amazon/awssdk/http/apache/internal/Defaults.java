/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.http.apache.internal;

import java.time.Duration;
import software.amazon.awssdk.annotation.ReviewBeforeRelease;

/**
 * Default configuration values.
 */
@ReviewBeforeRelease("These values")
public final class Defaults {

    public static final Duration MAX_IDLE_CONNECTION_TIME = Duration.ofSeconds(60);

    /**
     * A value of -1 means infinite TTL in Apache.
     */
    public static final Duration CONNECTION_POOL_TTL = Duration.ofMillis(-1);

    public static final Boolean EXPECT_CONTINUE_ENABLED = Boolean.TRUE;

    private Defaults() {
    }
}
