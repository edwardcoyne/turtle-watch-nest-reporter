package com.islandturtlewatch.nest.reporter.transport;

import com.googlecode.objectify.annotation.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodedImageRef {
  @Getter @Setter
  private String refEncoded;

  public static EncodedImageRef fromProto(ImageRef proto) {
    EncodedImageRef encoded = new EncodedImageRef();
    encoded.setRefEncoded(BaseEncoding.base64().encode(proto.toByteArray()));
    return encoded;
  }
}
