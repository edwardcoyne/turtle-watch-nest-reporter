package com.islandturtlewatch.nest.reporter.transport;

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import com.google.common.io.BaseEncoding;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodedImage {
  @Getter @Setter
  private String imageEncoded;

  public static EncodedImage fromBytes(byte[] bytes) {
    return EncodedImage.builder()
        .setImageEncoded(BaseEncoding.base64().encode(bytes))
        .build();
  }
}
