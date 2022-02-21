package org.hpcclab.oaas.model.object;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

@Data
@Accessors(chain = true)
public class ObjectConstruction {
  String cls;
  @JsonRawValue
  String embeddedRecord;
  Set<String> labels = Set.of();
  Set<String> keys = Set.of();
  Map<String, String> overrideUrls;
  Set<OaasCompoundMember> members;

  @JsonRawValue
  public String getEmbeddedRecord() {
    return embeddedRecord;
  }

  public void setEmbeddedRecord(JsonNode val) {
    this.embeddedRecord = val.toString();
  }
}
