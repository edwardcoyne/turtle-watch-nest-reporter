package com.islandturtlewatch.nest.reporter.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
import com.islandturtlewatch.nest.data.ImageProto.ImageUploadRef;
import com.islandturtlewatch.nest.reporter.backend.ClientIds;
import com.islandturtlewatch.nest.reporter.backend.storage.ImageStore;
import com.islandturtlewatch.nest.reporter.transport.EncodedImageRef;
import com.islandturtlewatch.nest.reporter.transport.SerializedProto;


@Api(name = "imageEndpoint",
clientIds = {ClientIds.ANDROID_CLIENT_ID_DEV, ClientIds.ANDROID_CLIENT_ID_PROD},
audiences = ClientIds.CLIENT_ID,
scopes = {"https://www.googleapis.com/auth/userinfo.email"},
namespace = @ApiNamespace(ownerDomain = "islandturtlewatch.com",
                          ownerName = "islandturtlewatch.com",
                          packagePath = "nest.reporter.transport"))
public class ImageEndpoint {
  /**
   * Creates a reference for uploading a image.
   * @throws InvalidProtocolBufferException
   * @returns ImageUploadRef
   * @throws OAuthRequestException
   */
  @ApiMethod(name = "imageUpload")
  public SerializedProto imageUpload(User user, EncodedImageRef ref)
      throws InvalidProtocolBufferException {
    ImageRef imageRef = ImageRef.newBuilder()
        .mergeFrom(BaseEncoding.base64().decode(ref.getRefEncoded()))
        .build();
    return SerializedProto.fromProto(ImageUploadRef.newBuilder()
        .setUrl(ImageStore.getUploadUrl(imageRef))
        .build());
  }

  /**
   * Creates a reference for downloading a image.
   * @returns ImageDownloadRef
   */
  @ApiMethod(name = "imageDownload")
  public SerializedProto imageDownload(User user, EncodedImageRef ref) {
    return null;
  }
}
