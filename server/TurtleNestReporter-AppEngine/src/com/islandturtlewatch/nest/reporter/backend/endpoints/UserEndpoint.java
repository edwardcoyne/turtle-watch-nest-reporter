package com.islandturtlewatch.nest.reporter.backend.endpoints;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.transport.CreateUserResponse;

@Api(name = "userEndpoint",
  namespace = @ApiNamespace(ownerDomain = "islandturtlewatch.com", ownerName = "islandturtlewatch.com", packagePath = "nest.reporter.transport"))
public class UserEndpoint {

  @ApiMethod(name = "createUser")
  public CreateUserResponse createUser(@Named("user_id") Long userId) {
    ReportStore store = new ReportStore();
    store.init();
    if (store.hasUser(userId)) {
      return CreateUserResponse.builder()
          .setCode(CreateUserResponse.Code.FAILED)
          .setErrorMessage("Duplicate user")
          .build();
    }

    store.addUser(userId);
    return CreateUserResponse.builder().setCode(CreateUserResponse.Code.OK).build();
  }

}
