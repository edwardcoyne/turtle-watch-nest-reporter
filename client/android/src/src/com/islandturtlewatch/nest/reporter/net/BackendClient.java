package com.islandturtlewatch.nest.reporter.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.reporter.CloudEndpointUtils;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.CollectionResponseReportRef;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportRef;
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
      CollectionResponseReportRef refs = endpoint.getLatestForUser(USER_ID).execute();
      List<ReportProto.ReportRef> outputRefs = new ArrayList<>();
      for (ReportRef ref : refs.getItems()) {
        outputRefs.add(ReportProto.ReportRef.parseFrom(ref.getRef().getBytes()));
      }
      return outputRefs;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
