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
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.transport.ReportRef;

@Api(name = "reportEndpoint",
  namespace = @ApiNamespace(ownerDomain = "islandturtlewatch.com", ownerName = "islandturtlewatch.com", packagePath = "nest.reporter.transport"))
public class ReportRefEndpoint {

  /**
   * This method returns refs to all the latest reports for the user.
   */
  @ApiMethod(name = "getLatestForUser")
  public CollectionResponse<ReportRef> latestForUser(@Named("user_id") Long userId) {
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
}
