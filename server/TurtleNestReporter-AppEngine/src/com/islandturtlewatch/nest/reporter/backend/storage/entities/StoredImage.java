package com.islandturtlewatch.nest.reporter.backend.storage.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Builder;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;

@Entity
@Builder(fluent=false, chain=true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredImage {
  @Getter
  @Id
  @NonNull
  String key;

  @Getter
  @Load
  @Parent
  @NonNull
  Ref<StoredReport> report;

  @Getter
  String imageFileName;

  @Getter @Setter
  String cloudStorageObjectName;

  @Getter @Setter
  String blobKey;

  public static String toKey(long reportId, String filename) {
    return reportId + "/" + filename;
  }

  public static String toKey(ImageRef ref) {
    return toKey(ref.getReportId(), ref.getImageName());
  }
}
