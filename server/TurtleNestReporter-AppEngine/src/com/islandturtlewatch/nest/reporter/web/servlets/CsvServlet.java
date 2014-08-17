package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.common.base.Joiner;
import com.google.common.net.MediaType;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;

/**
 * Will generate csv of all active reports.
 */
@Log
public class CsvServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  static Joiner pathJoiner = Joiner.on(".");

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("Generating Csv report, no auth.");
    ReportStore store = new ReportStore();
    store.init();

    response.setContentType(MediaType.CSV_UTF_8.toString());
    ReportCsvGenerator generator = new ReportCsvGenerator();
    generator.addAllRows(store.getActiveReports());

    response.setHeader("content-disposition",
        "inline; filename=\"nest_reporter_reports_" + new Date().toString() + ".csv\"");
    response.setCharacterEncoding("UTF-8");
    ServletOutputStream outputStream = response.getOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
    writer.write('\ufeff');
    writer.flush();
    generator.write(writer);
  }
}
