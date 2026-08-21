package io.akka.mem0.domain;

import java.time.Instant;
import java.util.List;

/** One durably stored fact — SPEC-001 §2. */
public record Memory(
    String id, String text, String hash, List<String> linkedMemoryIds, Instant createdAt) {}
