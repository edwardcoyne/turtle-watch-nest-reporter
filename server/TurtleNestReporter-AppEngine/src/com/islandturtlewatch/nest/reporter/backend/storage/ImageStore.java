package com.islandturtlewatch.nest.reporter.backend.storage;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import lombok.Cleanup;
import lombok.extern.java.Log;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;

@Log
public class ImageStore {
  public static String getUploadUrl(ImageRef ref) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    return blobstoreService.createUploadUrl("/backend/image-post-upload",
        UploadOptions.Builder.withGoogleStorageBucketName(
            AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName()));
  }

  public static void handlePostUpoad(HttpServletRequest req) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    for (Entry<String, List<FileInfo>> fileInfoEntry
        : blobstoreService.getFileInfos(req).entrySet()) {
      for (FileInfo fileInfo : fileInfoEntry.getValue()) {
        log.warning("" + fileInfo.getFilename() + " :: " + fileInfo.getGsObjectName()
            + " :: " + fileInfo.getContentType());
      }
    }

    //Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
    //lobKey blobKey = blobs.get("myFile");
  }

  private static GcsFilename createGcsFileName(long reportId, String fileName) {
    return new GcsFilename(
        AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName(),
        String.format(Locale.US, "%d/%s", reportId, fileName));
  }

  public static void writeImage(
      long reportId, String fileName, ByteString image) throws IOException {
    GcsFilename gcsFileName = createGcsFileName(reportId, fileName);
    GcsService gcsService =
        GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

    @Cleanup GcsOutputChannel outputChannel =
        gcsService.createOrReplace(gcsFileName, GcsFileOptions.getDefaultInstance());
    outputChannel.write(image.asReadOnlyByteBuffer());
    log.info("Wrote image to datastore: " + gcsFileName + " bytes: " + image.size());
  }

  public static byte[] readImage(long reportId, String fileName) throws IOException {
    GcsFilename gcsFileName = createGcsFileName(reportId, fileName);
    GcsService gcsService =
        GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
    GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0);
    return ByteStreams.toByteArray(Channels.newInputStream(readChannel));
  }

  public static Report stripAndWriteEmbeddedImages(
      ReportRef ref, Report report) throws IOException {
    Report.Builder builder = report.toBuilder();
    for (Image.Builder image : builder.getImageBuilderList()) {
      if (image.hasRawData()) {
        writeImage(ref.getReportId(), image.getFileName(), image.getRawData());
        image.clearRawData();
      }
    }
    return builder.build();
  }

  public static void writeImages(ReportRef ref, List<Image> images) throws IOException {
    for (Image image : images) {
      if (image.hasRawData()) {
        writeImage(ref.getReportId(), image.getFileName(), image.getRawData());
      }
    }
  }

  public static Report stripEmbeddedImages(Report report) {
    Report.Builder builder = report.toBuilder();
    for (Image.Builder image : builder.getImageBuilderList()) {
      image.clearRawData();
    }
    return builder.build();
  }

  public static Report embedImages(ReportWrapper wrapper) throws IOException {
    Report.Builder builder = wrapper.getReport().toBuilder();
    long startTimestamp = System.currentTimeMillis();
    for (Image.Builder image : builder.getImageBuilderList()) {
      byte[] bytes = readImage(wrapper.getRef().getReportId(), image.getFileName());
      image.setRawData(ByteString.copyFrom(bytes));
    }
    log.info(String.format("Embedded %d images in %f s.",
        builder.getImageCount(),
        (System.currentTimeMillis() - startTimestamp) / 1000.0));
    return builder.build();
  }
}
