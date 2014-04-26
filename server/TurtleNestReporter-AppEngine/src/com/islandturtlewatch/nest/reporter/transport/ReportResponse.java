package com.islandturtlewatch.nest.reporter.transport;

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportResponse {
  public enum Code {
    OK,
    INVALID_REQUEST,
    AUTHENTICATION_FAILURE,
    FAILED
  };
  @Getter @Setter
  private Code code;

  @Getter @Setter
  private String errorMessage;

  @Getter @Setter
  private String reportRefEncoded;
}
