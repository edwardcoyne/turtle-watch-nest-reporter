package com.islandturtlewatch.nest.reporter.backend.storage.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Builder;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredImage {
  @Getter
  @Id
  long imageId;

  @Getter
  @Load
  @Parent
  @NonNull
  Ref<StoredReport> report;

  @Getter
  String imageFileName;

  @Getter
  String cloudStorageFileName;

}
