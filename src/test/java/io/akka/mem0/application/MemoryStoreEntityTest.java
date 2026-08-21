package io.akka.mem0.application;

import static org.assertj.core.api.Assertions.assertThat;

import akka.javasdk.testkit.EventSourcedTestKit;
import io.akka.mem0.domain.Candidate;
import io.akka.mem0.domain.MemoryEvent;
import io.akka.mem0.domain.MemoryStoreState;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * SPEC-001 rules 2, 5 as a caller sees them — {@code MemoryStoreStateTest} checks the same
 * dedup rules on the pure state; this checks that acceptance persists exactly one event per
 * survivor and a no-op batch persists nothing.
 */
public class MemoryStoreEntityTest {

  private EventSourcedTestKit<MemoryStoreState, MemoryEvent, MemoryStoreEntity> store() {
    return EventSourcedTestKit.of("owner-1", MemoryStoreEntity::new);
  }

  @Test
  public void acceptingTwoDistinctCandidatesPersistsTwoEvents() {
    var kit = store();

    var result = kit.method(MemoryStoreEntity::acceptCandidates)
        .invoke(new MemoryStoreEntity.AcceptCandidates(List.of(
            new Candidate("User likes tea", List.of()),
            new Candidate("User likes coffee", List.of()))));

    assertThat(result.getReply()).hasSize(2);
    assertThat(result.getAllEvents()).hasSize(2);
    assertThat(result.getAllEvents()).allMatch(e -> e instanceof MemoryEvent.MemoryAdded);
  }

  @Test
  public void reAddingTheSameFactPersistsNothing() {
    var kit = store();
    kit.method(MemoryStoreEntity::acceptCandidates)
        .invoke(new MemoryStoreEntity.AcceptCandidates(
            List.of(new Candidate("User likes tea", List.of()))));

    var result = kit.method(MemoryStoreEntity::acceptCandidates)
        .invoke(new MemoryStoreEntity.AcceptCandidates(
            List.of(new Candidate("User likes tea", List.of()))));

    assertThat(result.getReply()).isEmpty();
    assertThat(result.getAllEvents()).isEmpty();
  }

  @Test
  public void getMemoriesReturnsWhatWasAccepted() {
    var kit = store();
    kit.method(MemoryStoreEntity::acceptCandidates)
        .invoke(new MemoryStoreEntity.AcceptCandidates(
            List.of(new Candidate("User has a dog named Poppy", List.of()))));

    var memories = kit.method(MemoryStoreEntity::getMemories).invoke();

    assertThat(memories.getReply()).hasSize(1);
    assertThat(memories.getReply().get(0).text()).isEqualTo("User has a dog named Poppy");
  }
}
