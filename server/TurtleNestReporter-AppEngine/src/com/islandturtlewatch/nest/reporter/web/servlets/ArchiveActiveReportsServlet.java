package com.islandturtlewatch.nest.reporter.web.servlets;

import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

/**
 * Created by ReverendCode on 4/29/16.
 */

@Log
public class ArchiveActiveReportsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ReportStore store = new ReportStore();
        store.init();
        store.markAllActiveReportsInactive();

    }
}
