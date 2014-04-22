package com.islandturtlewatch.nest.reporter.backend.endpoints;

import java.util.List;

import javax.annotation.Nullable;

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
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.ClientIds;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.transport.ReportRef;
import com.islandturtlewatch.nest.reporter.transport.ReportRequest;
import com.islandturtlewatch.nest.reporter.transport.ReportResponse;
import com.islandturtlewatch.nest.reporter.transport.ReportResponse.Code;

@Api(name = "reportEndpoint",
clientIds = ClientIds.ANDROID_CLIENT_ID,
scopes = {"https://www.googleapis.com/auth/userinfo.email"},
namespace = @ApiNamespace(ownerDomain = "islandturtlewatch.com",
                          ownerName = "islandturtlewatch.com",
                          packagePath = "nest.reporter.transport"))
public class ReportEndpoint {
  /**
   * This method returns refs to all the latest reports for the user.
   * @throws OAuthRequestException
   */
  @ApiMethod(name = "getLatestRefsForUser")
  public CollectionResponse<ReportRef> latestRefsForUser(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("Not authorized");
    }

    ReportStore store = new ReportStore();
    store.init();
    List<ReportWrapper> latestReportsForUser = store.getLatestReportsForUser(user.getUserId());
    return CollectionResponse.<ReportRef>builder().setItems(
        Lists.transform(latestReportsForUser, new Function<ReportWrapper, ReportRef>(){
          @Override
          @Nullable
          public ReportRef apply(@Nullable ReportWrapper wrapper) {
            return ReportRef.fromProto(wrapper.getRef());
          }}))
        .build();
  }

  /**
   * This method returns refs to all the latest reports for the user.
   * @throws OAuthRequestException
   */
  @ApiMethod(name = "createReport")
  public ReportResponse createReport(
      User user,
      ReportRequest request) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("Not authorized");
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

    ReportStore store = new ReportStore();
    store.init();
    ReportWrapper addedReport = store.addReport(user.getUserId(), report);
    return ReportResponse.builder()
        .setCode(Code.OK)
        .setReportRefEncoded(BaseEncoding.base64().encode(addedReport.toByteArray()))
        .build();
  }

  /**
   * This method returns refs to all the latest reports for the user.
   */
  @ApiMethod(name = "updateReport")
  public ReportResponse updateReport(
      User user,
      ReportRequest request) {
    //TODO(edcoyne): implement
    return ReportResponse.builder()
        .setCode(Code.OK)
        .build();
  }
}
