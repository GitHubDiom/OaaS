package org.hpcclab.msc.object.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.NoStackTraceThrowable;
import org.hpcclab.msc.object.entity.MscFunction;
import org.hpcclab.msc.object.entity.object.FileState;
import org.hpcclab.msc.object.entity.object.MscObject;
import org.hpcclab.msc.object.entity.object.MscObjectOrigin;
import org.hpcclab.msc.object.model.RootMscObjectCreating;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class MscObjectRepository implements ReactivePanacheMongoRepository<MscObject> {

  public Uni<MscObject> createRootAndPersist(RootMscObjectCreating creating) {
    var object = new MscObject();
    object
//      .setType(creating.getType())
      .setState(new FileState()
        .setFileUrl(creating.getSourceUrl()))
      .setFunctions(creating.getFunctions())
      .setOrigin(new MscObjectOrigin().setRoot(true));
    return this.persist(object);
  }

  public Uni<MscObject> lazyFuncCall(MscObject mscObject,
                                     MscFunction function,
                                     Map<String, String> args) {
    var canInvoke = mscObject.getFunctions()
      .containsKey(function.getName());
    if (!canInvoke)
      return Uni.createFrom().failure(new NoStackTraceThrowable("Can not call this function"));
    var newObj = new MscObject()
      .setOrigin(new MscObjectOrigin()
        .setFuncName(function.getName())
        .setArgs(args)
        .setParentId(mscObject.getId())
        .setRootId(mscObject.getOrigin().getRootId())
      );
    return persist(newObj);
  }
}
