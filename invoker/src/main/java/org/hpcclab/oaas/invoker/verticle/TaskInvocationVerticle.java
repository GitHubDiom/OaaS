package org.hpcclab.oaas.invoker.verticle;

import io.micrometer.core.instrument.MeterRegistry;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.common.KafkaClientOptions;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import org.hpcclab.oaas.invocation.SyncInvoker;
import org.hpcclab.oaas.invocation.InvocationExecutor;
import org.hpcclab.oaas.invoker.TaskConsumer;
import org.hpcclab.oaas.repository.FunctionRepository;
import org.hpcclab.oaas.repository.event.ObjectCompletionPublisher;

import javax.inject.Inject;
import java.util.Set;

//@Dependent
@Deprecated(forRemoval = true)
public class TaskInvocationVerticle extends AbstractVerticle {

  @Inject
  SyncInvoker invoker;
  @Inject
  FunctionRepository funcRepo;
  @Inject
  InvocationExecutor graphExecutor;
  @Inject
  ObjectCompletionPublisher objCompPublisher;
  @Inject
  KafkaClientOptions options;
  @Inject
  MeterRegistry registry;
  Set<String> topics;

  private KafkaConsumer<String, Buffer> kafkaConsumer;
  private TaskConsumer taskMessageConsumer;

  @Override
  public void init(Vertx vertx, Context context) {
    if (topics == null || topics.isEmpty()) {
      throw new IllegalStateException("topics must not be null or empty");
    }
    kafkaConsumer = KafkaConsumer.create(
      io.vertx.mutiny.core.Vertx.newInstance(vertx),
      options);
    taskMessageConsumer = new TaskConsumer(
      invoker,
      funcRepo,
      graphExecutor,
      objCompPublisher,
      kafkaConsumer,
      topics,
      registry
    );
  }
  public void  setTopics(Set<String> topics) {
    this.topics = topics;
  }

  @Override
  public Uni<Void> asyncStart() {
    return taskMessageConsumer.start();
  }

  @Override
  public Uni<Void> asyncStop() {
    return taskMessageConsumer.cleanup();
  }
}
