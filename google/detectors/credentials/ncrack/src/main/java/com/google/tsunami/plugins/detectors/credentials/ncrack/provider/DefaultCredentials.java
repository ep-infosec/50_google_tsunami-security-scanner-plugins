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
package com.google.tsunami.plugins.detectors.credentials.ncrack.provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.tsunami.common.data.NetworkServiceUtils;
import com.google.tsunami.plugins.detectors.credentials.ncrack.proto.DefaultCredentialsData;
import com.google.tsunami.proto.NetworkService;
import java.util.Iterator;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Credential provider that provides the default username/password combination for popular web
 * services.
 */
public final class DefaultCredentials extends CredentialProvider {

  private final DefaultCredentialsData defaultCredentialsData;

  @Inject
  DefaultCredentials(DefaultCredentialsData defaultCredentialsData) {
    this.defaultCredentialsData = defaultCredentialsData;
  }

  @Override
  public String name() {
    return "DefaultCredentials";
  }

  @Override
  public String description() {
    return "For a given service, returns the default credentials";
  }

  @Override
  public Iterator<TestCredential> generateTestCredentials(NetworkService networkService) {
    String serviceName = NetworkServiceUtils.getServiceName(networkService);

    return defaultCredentialsData.getServiceDefaultCredentialsList().stream()
        .filter(
            serviceDefaultCredentials ->
                serviceDefaultCredentials.getServiceName().equals(serviceName))
        .flatMap(
            serviceDefaultCredentials ->
                Lists.cartesianProduct(
                    ImmutableList.of(
                        serviceDefaultCredentials.getDefaultUsernamesList(),
                        serviceDefaultCredentials.getDefaultPasswordsList()))
                    .stream())
        .map(
            usernamePassworkPair ->
                // Since
                // NcrackCredentialTester.generateTestCredentialsMapFromListOfCredentials
                // sets null passwords to empty string anyway, we just pass empty strings
                // directly to the Optional to simplify the logic here.
                TestCredential.create(
                    usernamePassworkPair.get(0), Optional.of(usernamePassworkPair.get(1))))
        .iterator();
  }
}
