package com.islandturtlewatch.nest.reporter.backend.storage.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Root node of storage, parent to multiple {@link StoredReports}.
 */
@Entity
@ToString
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
  @Id
  @Getter
  public String id;

  @Getter @Setter
  public long highestReportId;

  public Key<User> getKey() {
    return Key.create(this);
  }
}
