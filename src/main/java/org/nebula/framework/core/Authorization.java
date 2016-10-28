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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.utils.HashUtils;

import java.util.Map;
import java.util.TreeMap;

public class Authorization {

  private final static Log log = LogFactory.getLog(Authorization.class);

  private final static String ACCESSID_FLAG = "accessId";
  private final static String REGISTRATIONID_FLAG = "registrationId";
  private final static String INSTANCEID_FLAG = "instanceId";
  private final static String REQUEST_TYPE_FLAG = "requestType";
  private final static String TIMESTAMP_FLAG = "timestamp";
  private final static String SIGNATURE_FLAG = "signature";
  private final static String SALT_FLAG = "salt";

  private final static int SECRETKEY_MIN_LENGTH = 6;

  private Map<String, Object> fields = new TreeMap<String, Object>();

  private Authorization(){}

  public static Authorization create(){
    return new Authorization();
  }

  public static Authorization build(String signedSignature) {

    if(signedSignature==null) {
      throw new IllegalArgumentException("The signedSignature can't be null");
    }

    return Authorization.create().parse(signedSignature);
  }

  private Authorization parse(String signedSignature) {

    String[] signedFields = signedSignature.split("&");
    for(String signedField : signedFields) {

      String[] field = signedField.split("=");
      if(field.length!=2) {
        throw new IllegalArgumentException("The format of the signedSignature is illegal");
      }

      addField(field[0], field[1]);

    }

    return this;
  }

  public String getAccessId() {
    return getField(ACCESSID_FLAG);
  }

  public Authorization setAccessId(String accessId) {
    addField(ACCESSID_FLAG, accessId);
    return this;
  }

  public String getRequestType() {
    return getField(REQUEST_TYPE_FLAG);
  }

  public Authorization setRequestType(String requestType) {
    addField(REQUEST_TYPE_FLAG, requestType);
    return this;
  }

  public String getRegistrationId() {
    return getField(REGISTRATIONID_FLAG);
  }

  public Authorization setRegistrationId(String registrationId) {
    addField(REGISTRATIONID_FLAG, registrationId);
    return this;
  }

  public String getInstanceId() {
    return getField(INSTANCEID_FLAG);
  }

  public Authorization setInstanceId(String instanceId) {
    addField(INSTANCEID_FLAG, instanceId);
    return this;
  }

  public Authorization addTimestamp() {
    addField(TIMESTAMP_FLAG, System.currentTimeMillis());
    return this;
  }

  public String getTimestamp(){
     return getField(TIMESTAMP_FLAG);
  }

  private <T> T getField(String name) {
    return (T)fields.get(name);
  }

  public Authorization addField(String name, Object value) {
    if(name==null || value == null) {
      throw new IllegalArgumentException("The name or value can't be null");
    }

    fields.put(name, value.toString());
    return this;
  }

  public String toSignedSignature(String secretKey) {

    String stringToSign = toStringToSign(secretKey);

    String signature = hashWithSalt(stringToSign, secretKey);

    return new StringBuilder(stringToSign).append(SIGNATURE_FLAG).append("=").append(signature).toString();

  }

  public boolean authenticate(String secretKey){

    String accessId = getAccessId();

    if(accessId==null || accessId.trim().length()==0){
      throw new IllegalArgumentException("The accessId can't be blank");
    }

    String stringToSign = toStringToSign(secretKey);

    String expectedSignature = hashWithSalt(stringToSign, secretKey);

    String actualSignature = getField(SIGNATURE_FLAG);

    if(expectedSignature == null || actualSignature == null || !expectedSignature.equals(actualSignature)) {
      log.error("The expectedSignature " + expectedSignature + " and actualSignature " + actualSignature + " doesn't match");
      throw new SecurityException("The request signature is incorrect.");
    }

    return true;

  }

  private String hashWithSalt(String stringToSign, String secretKey){
    StringBuilder sb = new StringBuilder(stringToSign);
    sb.append(SALT_FLAG).append("=").append(secretKey);
    return HashUtils.md5(sb.toString());
  }

  private String toStringToSign(String secretKey){

    if(secretKey==null || secretKey.trim().length() < SECRETKEY_MIN_LENGTH) {
      throw new IllegalArgumentException("The secretKey must not less than " + SECRETKEY_MIN_LENGTH);
    }

    StringBuilder sb = new StringBuilder();

    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if(!SIGNATURE_FLAG.equals(key)) {
        sb.append(key).append("=").append(value).append("&");
      }
    }

    return sb.toString();
  }

}