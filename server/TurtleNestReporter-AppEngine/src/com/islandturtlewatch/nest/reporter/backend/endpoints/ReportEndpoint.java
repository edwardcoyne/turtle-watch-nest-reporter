package com.islandturtlewatch.nest.reporter.backend.endpoints;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.transport.ReportRef;
import com.islandturtlewatch.nest.reporter.transport.ReportRequest;
import com.islandturtlewatch.nest.reporter.transport.ReportResponse;
import com.islandturtlewatch.nest.reporter.transport.ReportResponse.Code;

@Api(name = "reportEndpoint",
namespace = @ApiNamespace(ownerDomain = "islandturtlewatch.com", ownerName = "islandturtlewatch.com", packagePath = "nest.reporter.transport"))
public class ReportEndpoint {
  /**
   * This method returns refs to all the latest reports for the user.
   */
  @ApiMethod(name = "getLatestRefsForUser")
  public CollectionResponse<ReportRef> latestRefsForUser(@Named("user_id") Long userId) {
    ReportStore store = new ReportStore();
    store.init();
    List<ReportWrapper> latestReportsForUser = store.getLatestReportsForUser(userId);
    CollectionResponse.<ReportRef>builder().setItems(
        Lists.transform(latestReportsForUser, new Function<ReportWrapper, ReportRef>(){
          @Override
          @Nullable
          public ReportRef apply(@Nullable ReportWrapper wrapper) {
            return ReportRef.fromProto(wrapper.getRef());
          }}))
        .build();
    return null;
  }

  /**
   * This method returns refs to all the latest reports for the user.
   */
  @ApiMethod(name = "createReport")
  public ReportResponse createReport(
      @Named("user_id") Long userId,
      ReportRequest request) {
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
    ReportWrapper addedReport = store.addReport(userId, report);
    return ReportResponse.builder()
        .setCode(Code.OK)
        .setReportRefEncoded(BaseEncoding.base64().encode(addedReport.toByteArray()))
        .build();
  }

}
