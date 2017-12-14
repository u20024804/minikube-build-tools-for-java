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

package com.google.cloud.tools.crepecake.hash;

import com.google.cloud.tools.crepecake.blob.BlobDescriptor;
import com.google.cloud.tools.crepecake.image.DescriptorDigest;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

/** A {@link DigestOutputStream} that also keeps track of the total number of bytes written. */
public class CountingDigestOutputStream extends DigestOutputStream {

  private static final String SHA_256_ALGORITHM = "SHA-256";

  /** Keeps track of the total number of bytes appended. */
  private long totalBytes = 0;

  public interface WriteFunction {

    void write(CountingDigestOutputStream countingDigestOutputStream) throws IOException;
  }

  public static BlobDescriptor decoratedWrite(OutputStream outputStream, WriteFunction writeFunction) throws IOException {
    CountingDigestOutputStream hashingOutputStream = new CountingDigestOutputStream(outputStream);

    writeFunction.write(hashingOutputStream);
    hashingOutputStream.flush();

    try {
      return hashingOutputStream.toBlobDescriptor();
    } catch (DigestException ex) {
      throw new IOException("BLOB hashing failed: " + ex.getMessage(), ex);
    }
  }

  /** Wraps the {@code outputStream}. */
  public CountingDigestOutputStream(OutputStream outputStream) {
    super(outputStream, null);
    try {
      setMessageDigest(MessageDigest.getInstance(SHA_256_ALGORITHM));
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(
          "SHA-256 algorithm implementation not found - might be a broken JVM");
    }
  }

  /** Builds a {@link BlobDescriptor} with the hash and size of the bytes written. */
  public BlobDescriptor toBlobDescriptor() throws DigestException {
    byte[] hashedBytes = digest.digest();

    // Encodes each hashed byte into 2-character hexadecimal representation.
    StringBuilder stringBuilder = new StringBuilder(2 * hashedBytes.length);
    for (byte b : hashedBytes) {
      stringBuilder.append(String.format("%02x", b));
    }
    String hash = stringBuilder.toString();

    DescriptorDigest digest = DescriptorDigest.fromHash(hash);
    return new BlobDescriptor(totalBytes, digest);
  }

  /** @return the total number of bytes that were hashed */
  public long getTotalBytes() {
    return totalBytes;
  }

  @Override
  public void write(byte[] data, int offset, int length) throws IOException {
    super.write(data, offset, length);
    totalBytes += length;
  }

  @Override
  public void write(int singleByte) throws IOException {
    super.write(singleByte);
    totalBytes++;
  }
}
