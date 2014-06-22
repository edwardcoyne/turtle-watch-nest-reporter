package com.islandturtlewatch.nest.reporter.backend.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.islandturtlewatch.nest.reporter.backend.storage.ImageStore;

@Log
public class ImagePostUpload extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    log.info("Call to image-post-upload");
    ImageStore.handlePostUpoad(req);
  }
}
