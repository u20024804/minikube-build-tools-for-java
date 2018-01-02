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

package com.google.cloud.tools.crepecake.registry;

import com.google.cloud.tools.crepecake.blob.Blob;
import com.google.cloud.tools.crepecake.blob.BlobDescriptor;
import com.google.cloud.tools.crepecake.image.DescriptorDigest;
import com.google.cloud.tools.crepecake.image.json.ManifestTemplate;
import com.google.cloud.tools.crepecake.image.json.V21ManifestTemplate;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestException;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class BlobPullerIntegrationTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @BeforeClass
  public static void startLocalRegistry() throws IOException, InterruptedException {
    String runRegistryCommand =
        "docker run -d -p 5000:5000 --restart=always --name registry registry:2";
    Runtime.getRuntime().exec(runRegistryCommand).waitFor();

    String pullImageCommand = "docker pull busybox";
    Runtime.getRuntime().exec(pullImageCommand).waitFor();

    String tagImageCommand = "docker tag busybox localhost:5000/busybox";
    Runtime.getRuntime().exec(tagImageCommand).waitFor();

    String pushImageCommand = "docker push localhost:5000/busybox";
    Runtime.getRuntime().exec(pushImageCommand).waitFor();
  }

  @AfterClass
  public static void stopLocalRegistry() throws IOException, InterruptedException {
    String stopRegistryCommand = "docker stop registry";
    Runtime.getRuntime().exec(stopRegistryCommand).waitFor();

    String removeRegistryContainerCommand = "docker rm -v registry";
    Runtime.getRuntime().exec(removeRegistryContainerCommand).waitFor();
  }

  @Test
  public void testPull() throws IOException, RegistryException, DigestException {
    // Pulls the busybox image.
    RegistryClient registryClient = new RegistryClient(null, "localhost:5000", "busybox");
    ManifestTemplate manifestTemplate = registryClient.pullManifest("latest");

    V21ManifestTemplate v21ManifestTemplate = (V21ManifestTemplate) manifestTemplate;
    DescriptorDigest realDigest = v21ManifestTemplate.getLayerDigests().get(0);

    // Pulls a layer BLOB of the busybox image.
    File destFile = temporaryFolder.newFile();
    File checkBlobFile = temporaryFolder.newFile();

    Blob blob = registryClient.pullBlob(realDigest, destFile.toPath());

    try (OutputStream outputStream =
        new BufferedOutputStream(new FileOutputStream(checkBlobFile))) {
      BlobDescriptor blobDescriptor = blob.writeTo(outputStream);
      Assert.assertEquals(realDigest, blobDescriptor.getDigest());
    }

    Assert.assertArrayEquals(
        Files.readAllBytes(destFile.toPath()), Files.readAllBytes(checkBlobFile.toPath()));
  }

  @Test
  public void testPull_unknownBlob() throws RegistryException, IOException, DigestException {
    DescriptorDigest nonexistentDigest =
        DescriptorDigest.fromHash(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    try {
      RegistryClient registryClient = new RegistryClient(null, "localhost:5000", "busybox");
      registryClient.pullBlob(nonexistentDigest, Mockito.mock(Path.class));
      Assert.fail("Trying to pull nonexistent blob should have errored");

    } catch (RegistryErrorException ex) {
      Assert.assertThat(
          ex.getMessage(),
          CoreMatchers.containsString(
              "pull BLOB for localhost:5000/busybox with digest " + nonexistentDigest));
    }
  }
}