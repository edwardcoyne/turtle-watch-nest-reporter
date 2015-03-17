package com.islandturtlewatch.nest.reporter.transport;

import com.googlecode.objectify.annotation.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ReportProto;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodedReportRef {
  @Getter @Setter
  private String refEncoded;

  public static EncodedReportRef fromProto(ReportProto.ReportRef protoRef) {
    EncodedReportRef ref = new EncodedReportRef();
    ref.setRefEncoded(BaseEncoding.base64().encode(protoRef.toByteArray()));
    return ref;
  }

  public ReportProto.ReportRef toProto() throws InvalidProtocolBufferException {
    return ReportProto.ReportRef.parseFrom(BaseEncoding.base64().decode(this.refEncoded));
  }
}
