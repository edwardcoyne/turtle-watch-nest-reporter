package com.islandturtlewatch.nest.reporter.backend.endpoints;

import java.io.IOException;
import java.util.List;

import lombok.extern.java.Log;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.ClientIds;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.backend.util.UserUtil;
import com.islandturtlewatch.nest.reporter.transport.EncodedReport;
import com.islandturtlewatch.nest.reporter.transport.EncodedReportRef;
import com.islandturtlewatch.nest.reporter.transport.ReportRequest;
import com.islandturtlewatch.nest.reporter.transport.ReportResponse;
import com.islandturtlewatch.nest.reporter.transport.ReportResponse.Code;

@Api(name = "reportEndpoint",
clientIds = {ClientIds.ANDROID_CLIENT_ID_DEV, ClientIds.ANDROID_CLIENT_ID_PROD},
audiences = ClientIds.CLIENT_ID,
scopes = {"https://www.googleapis.com/auth/userinfo.email"},
namespace = @ApiNamespace(ownerDomain = "islandturtlewatch.com",
                          ownerName = "islandturtlewatch.com",
                          packagePath = "nest.reporter.transport"))
@Log
public class ReportEndpoint {
  private final ReportStore store;

  public ReportEndpoint() {
    store = new ReportStore();
    store.init();
  }

  /**
   * This method returns refs to all the latest reports for the user.
   * @throws OAuthRequestException
   */
  @ApiMethod(name = "getLatestRefsForUser", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<EncodedReportRef> latestRefsForUser(User user) throws OAuthRequestException {
    log.info("get latest for user: " + user );
    if (user == null) {
      throw new OAuthRequestException("Not authorized");
    }

    List<ReportWrapper> latestReportsForUser =
        store.getLatestReportsForUser(UserUtil.getUserId(user));
    return CollectionResponse.<EncodedReportRef>builder().setItems(
        Lists.transform(latestReportsForUser, new Function<ReportWrapper, EncodedReportRef>(){
          @Override
          public EncodedReportRef apply(ReportWrapper wrapper) {
            return EncodedReportRef.fromProto(wrapper.getRef());
          }}))
        .build();
  }

  /**
   * Get the requested report.
   * @throws OAuthRequestException
   * @throws IOException
   */
  @ApiMethod(name = "fetchReport")
  public EncodedReport fetchReport(User user, EncodedReportRef encodedRef)
      throws OAuthRequestException, IOException {
    if (user == null) {
      throw new OAuthRequestException("Not authorized");
    }
    ReportRef ref = encodedRef.toProto();
    ReportWrapper wrapper =
        store.getReportLatestVersion(UserUtil.getUserId(user), ref.getReportId());

    long startTimestamp = System.currentTimeMillis();
    try {
      return EncodedReport.fromProto(wrapper.getReport());
    } finally {
      log.info(String.format("Encoded report in %f s.",
          (System.currentTimeMillis() - startTimestamp) / 1000.0));
    }
  }

  /**
   * This method returns refs to all the latest reports for the user.
   * @throws OAuthRequestException
   * @throws IOException If we can't write image.
   */
  @ApiMethod(name = "createReport")
  public ReportResponse createReport(
      User user,
      ReportRequest request) throws OAuthRequestException, IOException {
    log.info("CreateUser: " + user + " :: " + request);
    if (user == null) {
      return ReportResponse.builder().setCode(Code.AUTHENTICATION_FAILURE).build();
    }

    Report report;
    try {
      report = Report.parseFrom(BaseEncoding.base64().decode(request.getReportEncoded()));
    } catch (InvalidProtocolBufferException e) {
      return ReportResponse.builder()
          .setCode(Code.INVALID_REQUEST)
          .setErrorMessage("Report is not valid protobuf")
          .build();
    }
    ReportWrapper addedReport = store.addReport(UserUtil.getUserId(user), report);
    return ReportResponse.builder()
        .setCode(Code.OK)
        .setReportRefEncoded(BaseEncoding.base64().encode(addedReport.getRef().toByteArray()))
        .build();
  }

  /**
   * This method returns refs to all the latest reports for the user.
   * @throws IOException if we can't write image.
   */
  @ApiMethod(name = "updateReport")
  public ReportResponse updateReport(
      User user,
      ReportRequest request) throws IOException {
    log.info("UpdateReport: " + user + " :: " + request);
    if (user == null) {
      return ReportResponse.builder().setCode(Code.AUTHENTICATION_FAILURE).build();
    }

    Report report;
    try {
      report = Report.parseFrom(BaseEncoding.base64().decode(request.getReportEncoded()));
    } catch (InvalidProtocolBufferException e) {
      return ReportResponse.builder()
          .setCode(Code.INVALID_REQUEST)
          .setErrorMessage("Report is not valid protobuf")
          .build();
    }

    ReportRef ref;
    try {
      ref = ReportRef.parseFrom(BaseEncoding.base64().decode(request.getReportRefEncoded()));
    } catch (InvalidProtocolBufferException e) {
      return ReportResponse.builder()
          .setCode(Code.INVALID_REQUEST)
          .setErrorMessage("ReportRef is not valid protobuf")
          .build();
    }
    // Add user to ref.
    ref = ref.toBuilder().setOwnerId(UserUtil.getUserId(user)).build();

    ReportWrapper wrapper = ReportWrapper.newBuilder().setRef(ref).setReport(report).build();
    ReportWrapper updatedWrapper = store.updateReport(wrapper);

    return ReportResponse.builder()
        .setCode(Code.OK)
        .setReportRefEncoded(BaseEncoding.base64().encode(updatedWrapper.getRef().toByteArray()))
        .build();
  }
}
