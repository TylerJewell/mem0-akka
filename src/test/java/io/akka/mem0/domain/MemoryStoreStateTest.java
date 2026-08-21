package io.akka.mem0.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** SPEC-001 rules 1-6. */
public class MemoryStoreStateTest {

  private static final Instant T0 = Instant.parse("2026-08-20T12:00:00Z");

  @Test
  public void hashIsExactContentNoTrimOrCaseFold() {
    // question-log #1, run against the source's identical expression.
    assertThat(MemoryStoreState.hashOf("User likes tea"))
        .isNotEqualTo(MemoryStoreState.hashOf("  User likes tea "));
    assertThat(MemoryStoreState.hashOf("User likes tea"))
        .isNotEqualTo(MemoryStoreState.hashOf("user likes tea"));
    assertThat(MemoryStoreState.hashOf("User likes tea"))
        .isEqualTo(MemoryStoreState.hashOf("User likes tea"));
  }

  @Test
  public void blankTextIsRejectedBeforeHashing() {
    var state = MemoryStoreState.empty();
    var accepted = state.accept(List.of(new Candidate("", List.of()), new Candidate("   ", List.of())));
    assertThat(accepted).isEmpty();
  }

  @Test
  public void duplicateAgainstExistingIsRejected() {
    var existing = MemoryStoreState.toMemory(new Candidate("User has a dog named Poppy", List.of()), T0);
    var state = new MemoryStoreState(List.of(existing));

    var accepted = state.accept(List.of(new Candidate("User has a dog named Poppy", List.of())));

    assertThat(accepted).isEmpty();
  }

  @Test
  public void duplicateWithinTheSameBatchKeepsOnlyTheFirst() {
    var state = MemoryStoreState.empty();

    var accepted = state.accept(List.of(
        new Candidate("User likes tea", List.of()),
        new Candidate("User likes coffee", List.of()),
        new Candidate("User likes tea", List.of())));

    assertThat(accepted).hasSize(2);
    assertThat(accepted.get(0).text()).isEqualTo("User likes tea");
    assertThat(accepted.get(1).text()).isEqualTo("User likes coffee");
  }

  @Test
  public void aNonDuplicateSurvivesAlongsideAnUnrelatedExisting() {
    var existing = MemoryStoreState.toMemory(new Candidate("User has a dog named Poppy", List.of()), T0);
    var state = new MemoryStoreState(List.of(existing));

    var accepted = state.accept(List.of(new Candidate("User's dog Poppy learned to sit", List.of())));

    assertThat(accepted).hasSize(1);
  }

  @Test
  public void toMemoryAssignsIdHashLinksAndCreatedAt() {
    var candidate = new Candidate("User has a dog named Poppy", List.of("existing-id-1"));

    var memory = MemoryStoreState.toMemory(candidate, T0);

    assertThat(memory.id()).isNotBlank();
    assertThat(memory.text()).isEqualTo("User has a dog named Poppy");
    assertThat(memory.hash()).isEqualTo(MemoryStoreState.hashOf("User has a dog named Poppy"));
    assertThat(memory.linkedMemoryIds()).containsExactly("existing-id-1");
    assertThat(memory.createdAt()).isEqualTo(T0);
  }

  @Test
  public void linkedIdsAreStoredEvenWhenTheyDoNotResolveToAnExistingMemory() {
    // Rule 4 -- the source never validates a link target either.
    var candidate = new Candidate("User started a new hobby", List.of("no-such-memory"));

    var memory = MemoryStoreState.toMemory(candidate, T0);

    assertThat(memory.linkedMemoryIds()).containsExactly("no-such-memory");
  }

  @Test
  public void readsMostRecentlyAddedFirst() {
    var older = new Memory("1", "older", MemoryStoreState.hashOf("older"), List.of(), T0);
    var newer = new Memory("2", "newer", MemoryStoreState.hashOf("newer"), List.of(), T0.plusSeconds(60));
    var state = new MemoryStoreState(List.of(older, newer));

    assertThat(state.mostRecentFirst()).extracting(Memory::id).containsExactly("2", "1");
  }
}
