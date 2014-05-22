package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.common.base.Optional;
import com.google.common.net.MediaType;
import com.islandturtlewatch.nest.reporter.backend.storage.ImageStore;
/**
 * Servlet to display stored images.
 *
 * path: ../report_id/file_name
 */
@Log
public class ImageServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Optional<UrlInfo> info = UrlInfo.parseFrom(request.getPathInfo());
    if (!info.isPresent()) {
      log.warning("Invalid image request structure: " + request.getPathInfo());
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    byte[] image = ImageStore.readImage(info.get().reportId, info.get().fileName);
    response.setContentType(MediaType.JPEG.toString());
    response.getOutputStream().write(image);
  }

  private static class UrlInfo{
    private static final Pattern urlPattern = Pattern.compile("/([0-9]*)/([^/]*)");

    public long reportId;
    public String fileName;
    public static Optional<UrlInfo> parseFrom(String url) {
      if (url == null) {
        return Optional.absent();
      }

      Matcher matcher = urlPattern.matcher(url);
      if (!matcher.matches() || matcher.groupCount() < 2) {
        return Optional.absent();
      }
      UrlInfo info = new UrlInfo();
      info.reportId = Long.parseLong(matcher.group(1));
      info.fileName = matcher.group(2);
      return Optional.of(info);
    }
  }
}
