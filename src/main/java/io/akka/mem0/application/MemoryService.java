package io.akka.mem0.application;

import akka.javasdk.client.ComponentClient;
import io.akka.mem0.domain.Memory;
import io.akka.mem0.domain.Message;
import java.util.List;

/**
 * Orchestrates one add-messages request: read the owner's current memories, call the extractor,
 * hand the candidates to the entity to hash-dedup and persist (question-log #6).
 *
 * <p>This is the piece {@code TaskRunner} plays for intentkit's scheduled runs — plain code
 * outside any entity, calling one entity, an external step, then another entity call.
 */
public final class MemoryService {

  private final ComponentClient componentClient;
  private final Extractor extractor;

  public MemoryService(ComponentClient componentClient, Extractor extractor) {
    this.componentClient = componentClient;
    this.extractor = extractor;
  }

  public List<Memory> addMessages(String ownerId, List<Message> newMessages) {
    List<Memory> existing =
        componentClient
            .forEventSourcedEntity(ownerId)
            .method(MemoryStoreEntity::getMemories)
            .invoke();

    var candidates = extractor.extract(existing, newMessages);

    return componentClient
        .forEventSourcedEntity(ownerId)
        .method(MemoryStoreEntity::acceptCandidates)
        .invoke(new MemoryStoreEntity.AcceptCandidates(candidates));
  }
}
