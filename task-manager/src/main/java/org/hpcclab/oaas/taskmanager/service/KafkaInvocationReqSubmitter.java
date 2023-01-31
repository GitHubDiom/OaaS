package org.hpcclab.oaas.taskmanager.service;


import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hpcclab.oaas.invocation.InvocationQueueSender;
import org.hpcclab.oaas.model.invocation.InvocationRequest;
import org.hpcclab.oaas.model.task.TaskContext;
import org.hpcclab.oaas.taskmanager.TaskManagerConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KafkaInvocationReqSubmitter implements InvocationQueueSender {

  @Channel("tasks")
  MutinyEmitter<InvocationRequest> taskEmitter;
  @Inject
  TaskManagerConfig config;

  @Override
  public Uni<Void> send(InvocationRequest request) {
    var metaBuilder = OutgoingKafkaRecordMetadata.builder()
      .withTopic(selectTopic(request))
      .withHeaders(new RecordHeaders()
        .add("ce_id", request.invId().getBytes())
        .add("ce_type", InvocationRequest.CE_TYPE.getBytes())
      );
    if (!request.immutable())
      metaBuilder = metaBuilder.withKey(request.partKey());
    var message = Message.of(request)
      .addMetadata(metaBuilder.build());
    return taskEmitter.sendMessage(message);
  }

  public String selectTopic(InvocationRequest request) {
    return config.functionTopicPrefix() + request.function();
  }
}
