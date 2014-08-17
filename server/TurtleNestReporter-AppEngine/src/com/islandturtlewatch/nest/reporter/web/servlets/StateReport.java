package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.common.base.Joiner;

@Log
public class StateReport extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Joiner csvJoiner = Joiner.on(";");

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("Generating Csv report, no auth.");

  }
}