package com.islandturtlewatch.nest.reporter.transport;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ReportProto;

@Entity
public class ReportRef {
  private static final int MODIFIER = 50_000;

  @Id
  long id;

  byte[] ref;

  public ReportRef() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public byte[] getRef() {
    return ref;
  }

  public void setRef(byte[] ref) {
    this.ref = ref;
  }

  public static ReportRef fromProto(ReportProto.ReportRef protoRef) {
    ReportRef ref = new ReportRef();
    // Pack all ids together.
    ref.setId((protoRef.getOwnerId() * MODIFIER)
        + (protoRef.getReportId() * MODIFIER)
        + (protoRef.getVersion() * MODIFIER));
    ref.setRef(protoRef.toByteArray());
    return ref;
  }

  public ReportProto.ReportRef toProto() throws InvalidProtocolBufferException {
    return ReportProto.ReportRef.parseFrom(this.ref);
  }
}
