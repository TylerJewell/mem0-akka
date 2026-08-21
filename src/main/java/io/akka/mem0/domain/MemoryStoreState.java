package io.akka.mem0.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One owner's memories — SPEC-001 rules 1-6.
 *
 * <p>All dedup and acceptance logic lives here as pure functions, so it is tested without a
 * runtime; {@code MemoryStoreEntity} decides only what to persist and what to answer, the same
 * split {@code TaskState}/{@code TaskEntity} already use in this harness.
 */
public record MemoryStoreState(List<Memory> memories) {

  public static MemoryStoreState empty() {
    return new MemoryStoreState(List.of());
  }

  /** Rule 1 — exact-content MD5, no trim or case fold, matching mem0-src/mem0/memory/main.py:1020. */
  public static String hashOf(String text) {
    try {
      var digest = MessageDigest.getInstance("MD5");
      var bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      var sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Rules 2, 3, 5 — which candidates survive, in order, given this state as the "existing
   * memories" they are checked against (open decision 1: the full current set, not a bounded
   * top-k). Does not mutate state; the entity persists one event per survivor.
   */
  public List<Candidate> accept(List<Candidate> candidates) {
    Set<String> seenHashes = new HashSet<>();
    for (Memory m : memories) seenHashes.add(m.hash());

    List<Candidate> accepted = new ArrayList<>();
    for (Candidate c : candidates) {
      if (c.text() == null || c.text().isBlank()) continue; // rule 3
      String hash = hashOf(c.text());
      if (seenHashes.contains(hash)) continue; // rule 2
      seenHashes.add(hash);
      accepted.add(c);
    }
    return accepted;
  }

  /** Rule 4 — id, hash, and createdAt assigned here; linkedMemoryIds copied verbatim. */
  public static Memory toMemory(Candidate candidate, Instant now) {
    return new Memory(
        UUID.randomUUID().toString(),
        candidate.text(),
        hashOf(candidate.text()),
        candidate.linkedMemoryIds(),
        now);
  }

  public MemoryStoreState apply(MemoryEvent event) {
    if (event instanceof MemoryEvent.MemoryAdded added) {
      var updated = new ArrayList<>(memories);
      updated.add(added.memory());
      return new MemoryStoreState(updated);
    }
    throw new IllegalStateException("Unknown event: " + event);
  }

  /** Rule 6 — most-recently added first. */
  public List<Memory> mostRecentFirst() {
    var ordered = new ArrayList<>(memories);
    ordered.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
    return List.copyOf(ordered);
  }
}
