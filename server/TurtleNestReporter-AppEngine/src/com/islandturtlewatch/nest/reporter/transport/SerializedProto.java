package com.islandturtlewatch.nest.reporter.transport;

import com.googlecode.objectify.annotation.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SerializedProto {
  @Getter @Setter
  private String serializedProto;

  public static SerializedProto fromProto(Message proto) {
    return SerializedProto.builder()
        .setSerializedProto(TextFormat.printToString(proto))
        .build();
  }
}
