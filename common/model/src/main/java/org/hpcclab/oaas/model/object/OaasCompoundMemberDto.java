package org.hpcclab.oaas.model.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class OaasCompoundMemberDto {
  String name;
  UUID object;

  public OaasCompoundMemberDto() {
  }

  @ProtoFactory
  public OaasCompoundMemberDto(String name, UUID object) {
    this.name = name;
    this.object = object;
  }

  @ProtoField(1)
  public String getName() {
    return name;
  }

  @ProtoField(2)
  public UUID getObject() {
    return object;
  }
}
