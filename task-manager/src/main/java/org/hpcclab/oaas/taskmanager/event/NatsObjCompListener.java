package org.hpcclab.oaas.taskmanager.event;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.mutiny.Uni;
import org.hpcclab.oaas.repository.event.ObjectCompletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class NatsObjCompListener implements ObjectCompletionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger( NatsObjCompListener.class );
  @Inject
  Connection nc;
  private ThreadLocal<Dispatcher> localDispatcher;

  @PostConstruct
  void setup() {
    localDispatcher = ThreadLocal.withInitial(() -> nc.createDispatcher());
  }

  public void onShutdown(@Observes ShutdownEvent event) {
    cleanup();
  }

  @Override
  public void cleanup() {
    try {
      nc.close();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Uni<String> wait(String id, Integer timeout) {
    var subject = "objects/" + id;
    return Uni.createFrom().emitter(emitter -> {
      var dispatcher = localDispatcher.get();
      LOGGER.debug("start subscribe to {}", id);
      dispatcher.subscribe(subject, msg -> {
        LOGGER.debug("receive event from {}", id);
        emitter.complete(id);
      });
      emitter.onTermination(() -> dispatcher.unsubscribe(subject));
    });
  }

  @Override
  public boolean healthcheck() {
    if (nc.getStatus() == Connection.Status.CONNECTED)
      return true;
    LOGGER.error("NATS client status is {}", nc.getStatus());
    return false;
  }
}
