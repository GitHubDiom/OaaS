package org.hpcclab.oaas.invoker;


import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import io.vertx.mutiny.kafka.client.producer.KafkaProducer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.hpcclab.oaas.invocation.TaskFactory;
import org.hpcclab.oaas.invocation.function.TaskSubmitter;
import org.hpcclab.oaas.model.TaskContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class KafkaTaskSubmitter implements TaskSubmitter {

  @Inject
  KafkaProducer<Buffer, Buffer> producer;
  @Inject
  TaskFactory taskFactory;
  @Inject
  InvokerConfig config;

  @Override
  public Uni<Void> submit(TaskContext context) {
    var task = taskFactory.genTask(context);
    var topic = selectTopic(context);
    var record = KafkaProducerRecord.create(
        topic,
        (Buffer) null,
        Json.encodeToBuffer(task)
      )
      .addHeaders(List.of(
        KafkaHeader.header("ce_id", task.getId()),
        KafkaHeader.header("ce_function", context.getFunction().getName())
      ));

    return producer.send(record)
      .replaceWithVoid();
  }

  public String selectTopic(TaskContext context) {
//    return config.topics().stream().findFirst().orElseThrow();
    return config.functionTopicPrefix() + context.getFunction().getName();
  }
}
