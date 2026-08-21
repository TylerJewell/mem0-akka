package io.akka.mem0.domain;

import java.util.List;

/**
 * One fact proposed by the extractor, not yet accepted or hashed — SPEC-001 §2.
 *
 * <p>{@code linkedMemoryIds} is copied verbatim onto the accepted {@link Memory} without
 * validation against the owner's current memories (SPEC-001 rule 4).
 */
public record Candidate(String text, List<String> linkedMemoryIds) {

  public Candidate {
    linkedMemoryIds = linkedMemoryIds == null ? List.of() : List.copyOf(linkedMemoryIds);
  }
}
