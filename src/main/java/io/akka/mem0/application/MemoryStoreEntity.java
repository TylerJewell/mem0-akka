package io.akka.mem0.application;

import akka.javasdk.annotations.Component;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import io.akka.mem0.domain.Candidate;
import io.akka.mem0.domain.Memory;
import io.akka.mem0.domain.MemoryEvent;
import io.akka.mem0.domain.MemoryStoreState;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * One owner's memories — SPEC-001 rules 1-6.
 *
 * <p>The entity id is the owner id (mem0's collapsed {@code user_id}/{@code agent_id}/
 * {@code run_id}, SPEC-001 §1). Extraction itself happens outside this entity
 * ({@link Extractor}, {@link MemoryService}); this entity is handed the already-extracted
 * candidates and owns only the deterministic part: hash dedup against its own current state,
 * and persisting the survivors.
 */
@Component(id = "memory-store")
public class MemoryStoreEntity extends EventSourcedEntity<MemoryStoreState, MemoryEvent> {

  public MemoryStoreEntity(EventSourcedEntityContext context) {}

  @Override
  public MemoryStoreState emptyState() {
    return MemoryStoreState.empty();
  }

  public record AcceptCandidates(List<Candidate> candidates) {}

  public Effect<List<Memory>> acceptCandidates(AcceptCandidates command) {
    Instant now = Instant.now();
    List<Candidate> accepted = currentState().accept(command.candidates());

    if (accepted.isEmpty()) {
      // Rule 5 -- nothing extracted/nothing survives dedup is a no-op, not an error.
      return effects().reply(List.of());
    }

    List<MemoryEvent> events = new ArrayList<>();
    List<Memory> written = new ArrayList<>();
    for (Candidate c : accepted) {
      Memory memory = MemoryStoreState.toMemory(c, now);
      events.add(new MemoryEvent.MemoryAdded(memory));
      written.add(memory);
    }

    return effects()
        .persistAll(events)
        .thenReply(state -> written);
  }

  public ReadOnlyEffect<List<Memory>> getMemories() {
    return effects().reply(currentState().mostRecentFirst());
  }

  @Override
  public MemoryStoreState applyEvent(MemoryEvent event) {
    return currentState().apply(event);
  }
}
