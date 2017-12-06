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
import java.io.File;

/**
 * Represents a layer in an image.
 *
 * <p>An image layer consists of:
 *
 * <ul>
 *   <li>Content BLOB
 *   <li>
 *       <ul>
 *         <li>The compressed archive (tarball gzip) of the partial filesystem changeset.
 *       </ul>
 *
 *   <li>Content Digest
 *   <li>
 *       <ul>
 *         <li>The SHA-256 hash of the content BLOB.
 *       </ul>
 *
 *   <li>Content Size
 *   <li>
 *       <ul>
 *         <li>The size (in bytes) of the content BLOB.
 *       </ul>
 *
 *   <li>Diff ID
 *   <li>
 *       <ul>
 *         <li>The SHA-256 hash of the uncompressed archive (tarball) of the partial filesystem
 *             changeset.
 *       </ul>
 *
 * </ul>
 */
public class Layer {

  private LayerType type;
  private LayerDataProvider layerDataProvider;

  /** Instantiate a new {@code UNWRITTEN} {@link Layer}. */
  public static Layer newUnwritten(
      BlobStream compressedBlobStream, BlobStream uncompressedBlobStream) {
    return new Layer(
        LayerType.UNWRITTEN,
        new UnwrittenLayerDataProvider(compressedBlobStream, uncompressedBlobStream));
  }

  /** Instantiate a new {@code CACHED} {@link Layer}. */
  public static Layer newCached(File file, BlobDescriptor blobDescriptor, DescriptorDigest diffId) {
    return new Layer(LayerType.CACHED, new CachedLayerDataProvider(file, blobDescriptor, diffId));
  }

  /** Instantiate a new {@code REFERENCE} {@link Layer}. */
  public static Layer newReference(BlobDescriptor blobDescriptor, DescriptorDigest diffId) {
    return new Layer(LayerType.REFERENCE, new ReferenceLayerDataProvider(blobDescriptor, diffId));
  }

  /** Instantiate a new {@code REFERENCE_NO_DIFF_ID} {@link Layer}. */
  public static Layer newReferenceNoDiffId(BlobDescriptor blobDescriptor) {
    return new Layer(
        LayerType.REFERENCE_NO_DIFF_ID, new ReferenceNoDiffIdLayerDataProvider(blobDescriptor));
  }

  private Layer(LayerType type, LayerDataProvider layerDataProvider) {
    this.type = type;
    this.layerDataProvider = layerDataProvider;
  }

  public LayerType getType() {
    return type;
  }

  public BlobDescriptor getBlobDescriptor() throws LayerException {
    return layerDataProvider.getBlobDescriptor();
  }

  public DescriptorDigest getDiffId() throws LayerException {
    return layerDataProvider.getDiffId();
  }
}