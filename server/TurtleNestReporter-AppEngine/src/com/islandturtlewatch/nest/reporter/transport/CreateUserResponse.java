package com.islandturtlewatch.nest.reporter.transport;

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

@Entity
@ToString
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateUserResponse {
  public enum Code {
    OK,
    FAILED
  };

  @Getter @Setter
  private Code code;

  @Getter @Setter
  private String errorMessage;
}
