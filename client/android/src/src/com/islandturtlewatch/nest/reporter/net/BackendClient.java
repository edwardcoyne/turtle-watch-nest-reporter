package com.islandturtlewatch.nest.reporter.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.CloudEndpointUtils;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.CollectionResponseReportRef;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportRef;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportRequest;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportResponse;
import com.islandturtlewatch.nest.reporter.transport.userEndpoint.UserEndpoint;
import com.islandturtlewatch.nest.reporter.transport.userEndpoint.model.CreateUserResponse;

public class BackendClient {
  private static final long USER_ID = 7357L;

  public BackendClient() {
  }

  public static boolean addUser() {
    UserEndpoint.Builder endpointBuilder = new UserEndpoint.Builder(
        AndroidHttp.newCompatibleTransport(),
        new JacksonFactory(),
        new HttpRequestInitializer() {
          @Override
          public void initialize(HttpRequest arg0) throws IOException {
          }
        });

    UserEndpoint endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
    try {
      CreateUserResponse response = endpoint.createUser(USER_ID).execute();
      return (response.getCode() != "OK");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static List<ReportProto.ReportRef> getLatestRefs() {
    ReportEndpoint.Builder endpointBuilder = new ReportEndpoint.Builder(
        AndroidHttp.newCompatibleTransport(),
        new JacksonFactory(),
        new HttpRequestInitializer() {
          @Override
          public void initialize(HttpRequest arg0) throws IOException {
          }
        });

    ReportEndpoint endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();

    try {
      CollectionResponseReportRef refs = endpoint.getLatestRefsForUser(USER_ID).execute();
      List<ReportProto.ReportRef> outputRefs = new ArrayList<>();
      if (refs == null) {
        return outputRefs;
      }
      for (ReportRef ref : refs.getItems()) {
        outputRefs.add(ReportProto.ReportRef.parseFrom(
            BaseEncoding.base64().decode(ref.getRefEncoded())));
      }
      return outputRefs;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Optional<ReportProto.ReportRef> addReport(Report report) {
    ReportEndpoint.Builder endpointBuilder = new ReportEndpoint.Builder(
        AndroidHttp.newCompatibleTransport(),
        new JacksonFactory(),
        new HttpRequestInitializer() {
          @Override
          public void initialize(HttpRequest arg0) throws IOException {
          }
        });

    ReportEndpoint endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();

    try {
      ReportRequest request = new ReportRequest();
      request.setReportEncoded(BaseEncoding.base64().encode(report.toByteArray()));
      ReportResponse response = endpoint.createReport(USER_ID, request).execute();
      if (response == null) {
        return Optional.absent();
      }
      Log.e("ERROR", "code: " + response.getCode()
          + " msg:" + response.getErrorMessage()
          + " ref:" + response.getReportRefEncoded());
      if (!response.getCode().equals("OK")) {
        return Optional.absent();
      }
      return Optional.of(ReportProto.ReportRef
          .parseFrom(BaseEncoding.base64().decode(response.getReportRefEncoded())));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Optional.absent();
  }
}
