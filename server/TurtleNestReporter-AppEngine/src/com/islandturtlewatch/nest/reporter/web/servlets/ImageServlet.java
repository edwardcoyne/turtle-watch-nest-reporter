package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
import com.islandturtlewatch.nest.reporter.backend.storage.ImageStore;
/**
 * Servlet to display stored images.
 *
 * Expects base64 encoded ImageRef on ?ref= parameter.
 */
@Log
public class ImageServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ImageRef ref = ImageRef.newBuilder()
        .mergeFrom(BaseEncoding.base64().decode(request.getParameter("ref")))
        .build();
    log.info("Displaying image for: " + ref.toString());

    ImageStore.serveImage(ref, response);
  }
}
