/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nebula.framework.core;

import org.junit.Before;
import org.junit.Test;
import org.nebula.framework.utils.HashUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthorizationTest {

  private String accessId = "nebula";
  private String registrationId = "domain-nebula-a40f1523-6a84-482f-a227-ec8cd5715833";
  private String instanceId = "domain-nebula-569bdf4c-1e73-4565-b29f-b9741e68e2f4";

  private String secretKey = "nebula-secret-key";

  private Authorization authorization;

  @Before
  public void setUp() {
    authorization =
        Authorization.create().setAccessId(accessId).addTimestamp()
            .addField("registrationId", registrationId).addField("instanceId", instanceId);

  }


  @Test
  public void testToSignedSignature() {
    String actualSignature = authorization.toSignedSignature(secretKey);

    String expectedSignature = getSignedSignature();

    assertEquals(expectedSignature, actualSignature);

  }

  @Test
  public void testSuccessfulAuthentication() {
    String actualSignature = authorization.toSignedSignature(secretKey);

    Authorization actualAuthorization = Authorization.build(actualSignature);

    assertEquals(accessId, actualAuthorization.getAccessId());
    assertEquals(authorization.getTimestamp(), actualAuthorization.getTimestamp());

    assertTrue(actualAuthorization.authenticate(secretKey));

  }

  private String getSignedSignature() {

    String
        stringToSign =
        String.format("accessId=%s&instanceId=%s&registrationId=%s&timestamp=%s", accessId,
                      instanceId, registrationId, authorization.getTimestamp());

    String
        signature =
        HashUtils.md5(String.format("%s&salt=%s", stringToSign, secretKey));

    return String.format("%s&signature=%s", stringToSign, signature);
  }


}