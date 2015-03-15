/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-06-09 16:41:44 UTC)
 * on 2014-06-25 at 05:40:13 UTC 
 * Modify at your own risk.
 */

package com.islandturtlewatch.nest.reporter.transport.imageEndpoint.model;

/**
 * Model definition for SerializedProto.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the imageEndpoint. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class SerializedProto extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String serializedProto;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getSerializedProto() {
    return serializedProto;
  }

  /**
   * @param serializedProto serializedProto or {@code null} for none
   */
  public SerializedProto setSerializedProto(java.lang.String serializedProto) {
    this.serializedProto = serializedProto;
    return this;
  }

  @Override
  public SerializedProto set(String fieldName, Object value) {
    return (SerializedProto) super.set(fieldName, value);
  }

  @Override
  public SerializedProto clone() {
    return (SerializedProto) super.clone();
  }

}