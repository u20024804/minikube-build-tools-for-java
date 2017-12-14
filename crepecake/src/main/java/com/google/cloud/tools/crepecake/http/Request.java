/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.crepecake.http;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.cloud.tools.crepecake.blob.Blob;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.URL;
import javax.annotation.Nullable;

/** Holds an HTTP request. */
public class Request {

  /** The HTTP request headers. */
  private final HttpHeaders headers = new HttpHeaders();

  /** The request method; uses GET if null. */
  @Nullable private String method;

  /** The HTTP request body. */
  @Nullable private BlobHttpContent body;

  public HttpHeaders getHeaders() {
    return headers;
  }

  @Nullable
  public BlobHttpContent getBody() {
    return body;
  }

  /** Sets the {@code Content-Type} header. */
  public Request setContentType(String contentType) {
    headers.setContentType(contentType);
    return this;
  }

  public Request setBody(Blob body) {
    this.body = new BlobHttpContent(body);
    return this;
  }
}