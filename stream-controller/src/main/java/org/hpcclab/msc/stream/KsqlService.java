package org.hpcclab.msc.stream;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class KsqlService {
  private static final Logger LOGGER = LoggerFactory.getLogger( KsqlService.class );
  @Inject
  WebClient client;
  JsonArray executeStatement(String sql) {
    return executeStatement(sql, Map.of());
  }

  JsonArray executeStatement(String sql, Map<String, String> props) {
    var res = client.post("/ksql")
      .sendJsonObject(new JsonObject().put("ksql", sql).put("streamsProperties", props))
      .await().indefinitely();
    if (res.statusCode() != 200)  {
      LOGGER.error("status is not 200\n===BODY===\n {}", res.bodyAsJsonObject().encodePrettily());
      throw new RuntimeException("status is not 200");
    }
    return res.bodyAsJsonArray();
  }
}
