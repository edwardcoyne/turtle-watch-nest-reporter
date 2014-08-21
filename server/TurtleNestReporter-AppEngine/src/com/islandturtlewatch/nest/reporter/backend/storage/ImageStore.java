package com.islandturtlewatch.nest.reporter.backend.storage;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
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

  public void addOrUpdateImage(
      final ImageRef ref, final String imageCsObjectName, final BlobKey blobKey) {
    backend().transact(new Work<Void>(){
      @Override
      public Void run() {
        doAddOrUpdateImage(ref, imageCsObjectName, blobKey);
        return null;
      }});
  }

  public void doAddOrUpdateImage(ImageRef ref, String imageCsObjectName, BlobKey blobKey) {
    try {
      Optional<StoredImage> oldImage = tryLoadImage(ref);
      if (oldImage.isPresent()) {
        updateImage(oldImage.get(), imageCsObjectName, blobKey);
      } else {
        addImage(ref, imageCsObjectName, blobKey);
      }
    } catch (Exception ex) {
      // Try to cleanup this file, otherwise orphaned.
      deleteImage(imageCsObjectName);
      throw ex;
    }
  }

  public Optional<StoredImage> tryLoadImage(ImageRef ref) {
    Optional<StoredReport> report = reportStore.tryLoadReport(ref.getOwnerId(), ref.getReportId());
    Preconditions.checkArgument(report.isPresent(), "Report not found: " + ref.toString());

    return Optional.fromNullable(backend().load()
        .type(StoredImage.class).parent(report.get()).id(StoredImage.toKey(ref)).now());
  }

  private void updateImage(StoredImage image, String imageCsObjectName, BlobKey blobKey) {
    String oldObjectName = image.getCloudStorageObjectName();

    boolean deleted = deleteImage(oldObjectName);
    if (!deleted) {
      log.warning("Failed to delete: " + oldObjectName);
    }

    image.setCloudStorageObjectName(imageCsObjectName);
    image.setBlobKey(blobKey.getKeyString());
    backend().save().entity(image).now();
  }

  private void addImage(ImageRef ref, String imageCsObjectName, BlobKey blobKey) {
    Optional<StoredReport> report = reportStore.tryLoadReport(ref.getOwnerId(), ref.getReportId());
    Preconditions.checkArgument(report.isPresent(), "Report not found: " + ref.toString());

    StoredImage image = StoredImage.builder()
        .setKey(StoredImage.toKey(ref))
        .setReport(Ref.create(report.get()))
        .setImageFileName(ref.getImageName())
        .setCloudStorageObjectName(imageCsObjectName)
        .setBlobKey(blobKey.getKeyString())
        .build();
    backend().save().entity(image).now();
  }

  private static Objectify backend() {
    // Docs suggest never caching the result of this.
    return ObjectifyService.ofy();
  }

  private static String getBucket() {
    return AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName();
  }

  private static GcsFilename createOldGcsFileName(long reportId, String fileName) {
      return new GcsFilename(
          AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName(),
          String.format(Locale.US, "%d/%s", reportId, fileName));
  }

  private static BlobKey getBlobkKey(ImageRef ref) {
    ImageStore store = new ImageStore();
    store.init();

    Optional<StoredImage> image = store.tryLoadImage(ref);
    if (image.isPresent()) {
      log.info("Using blobkey: " + image.get().getBlobKey());
      return new BlobKey(image.get().getBlobKey());
    } else {
      GcsFilename oldGcsFileName = createOldGcsFileName(ref.getReportId(), ref.getImageName());
      log.info("Using old GCS fileName: " + oldGcsFileName.toString());

      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      // Does not work on dev server.
      return blobstoreService.createGsBlobKey("/gs/" + oldGcsFileName.getBucketName()
          + "/" + oldGcsFileName.getObjectName());
    }
  }

  public static String getUploadUrl(ImageRef ref) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    return blobstoreService.createUploadUrl(
        "/backend/image-post-upload?ref=" + BaseEncoding.base64().encode(ref.toByteArray()),
        UploadOptions.Builder.withGoogleStorageBucketName(getBucket()));
  }

  public static String getDownloadUrl(ImageRef ref) {
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    return imagesService.getServingUrl(ServingUrlOptions.Builder
        .withBlobKey(getBlobkKey(ref))
        .imageSize(ImagesService.SERVING_SIZES_LIMIT));
  }

  public static void serveImage(ImageRef ref, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    blobstoreService.serve(getBlobkKey(ref), response);
  }

  public static boolean deleteImage(String objectName) {
    try {
      GcsService gcsService =
          GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
      GcsFilename fileName = new GcsFilename(getBucket(), objectName);

      return gcsService.delete(fileName);
    } catch (IOException ex) {
      log.log(Level.WARNING, "Exception while deleting file", ex);
      return false;
    }
  }
}
