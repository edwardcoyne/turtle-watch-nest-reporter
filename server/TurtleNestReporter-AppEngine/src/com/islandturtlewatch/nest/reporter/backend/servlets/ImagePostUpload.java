package com.islandturtlewatch.nest.reporter.backend.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
import com.islandturtlewatch.nest.reporter.backend.storage.ImageStore;

@Log
public class ImagePostUpload extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse resp)
      throws ServletException, IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    ImageStore store = new ImageStore();
    store.init();

    ImageRef ref = ImageRef.newBuilder()
        .mergeFrom(BaseEncoding.base64().decode(request.getParameter("ref")))
        .build();
    log.info("Post upoad for report: " + ref.toString());
    log.info("Ref-enc:" + request.getParameter("ref"));
    Map<String, List<BlobKey>> blobKeyMap = blobstoreService.getUploads(request);
    for (Entry<String, List<FileInfo>> fileInfoEntry
        : blobstoreService.getFileInfos(request).entrySet()) {
      List<FileInfo> fileInfos = fileInfoEntry.getValue();
      List<BlobKey> keys = blobKeyMap.get(fileInfoEntry.getKey());
      Preconditions.checkArgument(fileInfos.size() == 1);
      Preconditions.checkArgument(keys.size() == 1);

      store.addOrUpdateImage(ref, fileInfos.get(0).getGsObjectName(), keys.get(0));
/*
      for (FileInfo fileInfo : fileInfoEntry.getValue()) {
        log.info("Uploaded " + fileInfo.getFilename() + " :: " + fileInfo.getGsObjectName()
            + " :: " + fileInfo.getContentType());

        store.addOrUpdateImage(ref, fileInfo.getGsObjectName());
      }
      */
    }
  }
}
