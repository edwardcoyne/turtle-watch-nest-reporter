package com.islandturtlewatch.nest.reporter.transport;

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ReportProto.Report;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodedReport {
  @Getter @Setter
  private String reportEncoded;

  public static EncodedReport fromProto(Report proto) {
    EncodedReport encoded = new EncodedReport();
    encoded.setReportEncoded(BaseEncoding.base64().encode(proto.toByteArray()));
    return encoded;
  }
}
