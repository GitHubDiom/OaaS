package org.hpcclab.oaas.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import org.hpcclab.oaas.arango.DataAccessException;
import org.hpcclab.oaas.controller.OcConfig;
import org.hpcclab.oaas.controller.service.FunctionProvisionPublisher;
import org.hpcclab.oaas.model.Views;
import org.hpcclab.oaas.model.cls.OaasClass;
import org.hpcclab.oaas.model.exception.NoStackException;
import org.hpcclab.oaas.model.function.OaasFunction;
import org.hpcclab.oaas.repository.ClassRepository;
import org.hpcclab.oaas.repository.FunctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestScoped
public class ModuleResource implements ModuleService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleResource.class);

  @Inject
  ClassRepository classRepo;
  @Inject
  FunctionRepository funcRepo;
  @Inject
  FunctionProvisionPublisher provisionPublisher;
  @Inject
  OcConfig config;

  ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

  @Override
  @JsonView(Views.Public.class)
  public Uni<Module> create(Boolean update,
                            Module batch) {
    var classes = batch.getClasses();
    var functions = batch.getFunctions();
    for (OaasClass cls : classes) {
      cls.validate();
    }
    for (OaasFunction function : functions) {
      function.validate();
    }

    var uni = Uni.createFrom().deferred(() -> {
        var clsMap = classes.stream()
          .collect(Collectors.toMap(OaasClass::getName, Function.identity()));
        var changedClasses = classRepo.resolveInheritance(clsMap);
        var partitioned = changedClasses.values()
          .stream()
          .collect(Collectors.partitioningBy(cls -> cls.getRev() == null));
        var newClasses = partitioned.get(true);
        var oldClasses = partitioned.get(false);
      return classRepo
        .persistWithPreconditionAsync(oldClasses)
        .flatMap(__ -> classRepo.persistAsync(newClasses))
        .flatMap(__ -> funcRepo.persistAsync(functions));
      })
      .onFailure(DataAccessException.class)
      .retry().atMost(3);
    if (config.kafkaEnabled()) {
      return uni.call(__ ->
          provisionPublisher.submitNewFunction(batch.getFunctions().stream()))
        .replaceWith(batch);
    } else {
      return uni.replaceWith(batch);
    }
  }

  @Override
  @JsonView(Views.Public.class)
  public Uni<Module> createByYaml(Boolean update,
                                  String body) {
    try {
      var batch = yamlMapper.readValue(body, Module.class);
      return create(update, batch);
    } catch (JsonProcessingException e) {
      throw new NoStackException(e.getMessage(), 400);
    }
  }
}
