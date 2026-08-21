package io.akka.mem0.domain;

import akka.javasdk.annotations.TypeName;

/** SPEC-001 rule 5 — one event per accepted candidate, nothing else in this slice. */
public sealed interface MemoryEvent {
  @TypeName("memory-added")
  record MemoryAdded(Memory memory) implements MemoryEvent {}
}
