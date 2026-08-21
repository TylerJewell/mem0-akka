package io.akka.mem0.application;

import io.akka.mem0.domain.Candidate;
import io.akka.mem0.domain.Memory;
import io.akka.mem0.domain.Message;
import java.util.List;

/**
 * The LLM call that turns new messages into candidate facts (SPEC-001 §1: "a stand-in call —
 * the model is on the method's own list of fair stand-ins").
 *
 * <p>Runs outside the entity: an Event Sourced Entity's command handler cannot block on an
 * external call, so {@code MemoryService} reads the entity's current memories, calls this, then
 * sends the result back to the entity to hash-dedup and persist (question-log #6, open decision
 * 2).
 */
public interface Extractor {
  List<Candidate> extract(List<Memory> existing, List<Message> newMessages);
}
