package org.hpcclab.oaas.handler;

import io.smallrye.mutiny.Uni;
import org.hpcclab.oaas.entity.FunctionExecContext;
import org.hpcclab.oaas.repository.IfnpOaasObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class LogicalFunctionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger( LogicalFunctionHandler.class );
  @Inject
  IfnpOaasObjectRepository objectRepo;


  public Uni<FunctionExecContext> call(FunctionExecContext context) {
    if (context.getFunction().getName().equals("builtin.logical.copy")) {
      LOGGER.debug("Call function 'copy' {}", context.getMain().getId());
      var o = context.getMain().copy();
      o.setOrigin(context.createOrigin());
      o.setId(UUID.randomUUID());
      return objectRepo.persistAsync(o)
        .map(context::setOutput);
    } else {
      return null;
    }
  }

  public void validate(FunctionExecContext context) {

  }
}
