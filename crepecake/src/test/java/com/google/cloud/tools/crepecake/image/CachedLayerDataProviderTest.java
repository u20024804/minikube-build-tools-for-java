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

package com.google.cloud.tools.crepecake.image;

import com.google.cloud.tools.crepecake.blob.BlobDescriptor;
import com.google.cloud.tools.crepecake.blob.BlobStream;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link CachedLayerDataProvider}. */
public class CachedLayerDataProviderTest {

  @Mock private BlobDescriptor mockBlobDescriptor;

  @Mock private DescriptorDigest mockDiffId;

  @Before
  public void setUpMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetBlobStream()
      throws URISyntaxException, NoSuchAlgorithmException, IOException, DigestException {
    File fileA = new File(Resources.getResource("fileA").toURI());
    String expectedFileAString = new String(Files.readAllBytes(fileA.toPath()), Charsets.UTF_8);

    CachedLayerDataProvider cachedLayerDataProvider =
        new CachedLayerDataProvider(fileA, mockBlobDescriptor, mockDiffId);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BlobStream fileBlobStream = cachedLayerDataProvider.getBlobStream();
    fileBlobStream.writeTo(outputStream);

    Assert.assertEquals(
        expectedFileAString, new String(outputStream.toByteArray(), Charsets.UTF_8));
    Assert.assertEquals(mockBlobDescriptor, cachedLayerDataProvider.getBlobDescriptor());
    Assert.assertEquals(mockDiffId, cachedLayerDataProvider.getDiffId());
  }
}