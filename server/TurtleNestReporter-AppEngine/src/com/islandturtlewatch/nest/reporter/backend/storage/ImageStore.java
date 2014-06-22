package com.islandturtlewatch.nest.reporter.backend.storage;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;

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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.storage.entities.StoredImage;
import com.islandturtlewatch.nest.reporter.backend.storage.entities.StoredReport;
import com.islandturtlewatch.nest.reporter.backend.storage.entities.User;

@Log
public class ImageStore {
  ReportStore reportStore;
  public void init() {
    reportStore = new ReportStore();
    reportStore.init();

    ObjectifyService.register(User.class);
    ObjectifyService.register(StoredReport.class);
    ObjectifyService.register(StoredImage.class);
  }

  public void addOrUpdateImage(ImageRef ref, String imageCsObjectName) {
    try {
      Optional<StoredImage> oldImage = tryLoadImage(ref);
      if (oldImage.isPresent()) {
        updateImage(oldImage.get(), imageCsObjectName);
      } else {
        addImage(ref, imageCsObjectName);
      }
    } catch (Exception ex) {
      // Try to cleanup this file, otherwise orphaned.
      deleteImage(imageCsObjectName);
      throw ex;
    }
  }

  private Optional<StoredImage> tryLoadImage(ImageRef ref) {
    return Optional.fromNullable(
        backend().load().type(StoredImage.class).id(StoredImage.toKey(ref)).now());
  }

  private void updateImage(StoredImage image, String imageCsObjectName) {
    String oldObjectName = image.getCloudStorageObjectName();

    boolean deleted = deleteImage(oldObjectName);
    if (!deleted) {
      log.warning("Failed to delete: " + oldObjectName);
    }

    image.setCloudStorageObjectName(imageCsObjectName);
    backend().save().entity(image).now();
  }

  private void addImage(ImageRef ref, String imageCsObjectName) {
    Optional<User> user = reportStore.tryLoadUser(ref.getOwnerId());
    Preconditions.checkArgument(user.isPresent(), "User not found:" + ref.toString());
    StoredReport report = reportStore.loadReport(user.get(), ref.getReportId());

    StoredImage image = StoredImage.builder()
        .setKey(StoredImage.toKey(ref))
        .setReport(Ref.create(report))
        .setImageFileName(ref.getImageName())
        .setCloudStorageObjectName(imageCsObjectName)
        .build();
    backend().save().entity(image).now();
  }

  private static Objectify backend() {
    // Docs suggest never caching the result of this.
    return ObjectifyService.ofy();
  }

  public static String getUploadUrl(ImageRef ref) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    return blobstoreService.createUploadUrl(
        "/backend/image-post-upload?ref=" + BaseEncoding.base64().encode(ref.toByteArray()),
        UploadOptions.Builder.withGoogleStorageBucketName(
            AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName()));
  }

  public static void handlePostUpoad(HttpServletRequest req) throws InvalidProtocolBufferException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    ImageStore store = new ImageStore();
    store.init();

    ImageRef ref = ImageRef.newBuilder()
        .mergeFrom(BaseEncoding.base64().decode(req.getParameter("ref")))
        .build();
    log.info("Post upoad for report: " + ref.toString());
    for (Entry<String, List<FileInfo>> fileInfoEntry
        : blobstoreService.getFileInfos(req).entrySet()) {
      for (FileInfo fileInfo : fileInfoEntry.getValue()) {
        log.info("Uploaded " + fileInfo.getFilename() + " :: " + fileInfo.getGsObjectName()
            + " :: " + fileInfo.getContentType());
        store.addOrUpdateImage(ref, fileInfo.getGsObjectName());
      }
    }
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

  public static boolean deleteImage(String objectName) {
    try {
      GcsService gcsService =
          GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
      GcsFilename fileName = new GcsFilename(
          AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName(),
          objectName);

      return gcsService.delete(fileName);
    } catch (IOException ex) {
      log.log(Level.WARNING, "Exception while deleting file", ex);
      return false;
    }
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

  public static Report embeddedImages(ReportWrapper wrapper) throws IOException {
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
