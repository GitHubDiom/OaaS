package org.hpcclab.msc.object.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hpcclab.msc.object.entity.object.OaasObject;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RootMscObjectCreating {
  String sourceUrl;
  OaasObject.Type type;
//  Map<String, MscFuncMetadata> functions = Map.of();
  List<String> functions = List.of();
}
