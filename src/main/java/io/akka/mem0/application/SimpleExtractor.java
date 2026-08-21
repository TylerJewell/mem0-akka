package io.akka.mem0.application;

import io.akka.mem0.domain.Candidate;
import io.akka.mem0.domain.Memory;
import io.akka.mem0.domain.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * The shipped stand-in for mem0's LLM extraction call.
 *
 * <p>The source's linguistic judgment — turning "I just got a dog named Poppy" into a
 * self-contained, temporally-grounded sentence — is the model's own work, explicitly out of
 * scope for direct reimplementation (SPEC-001 §1). What this slice ports is the pipeline around
 * that call: search, dedup, link, persist. This stand-in emits one candidate per non-blank
 * user/assistant message, verbatim, so the pipeline has real (if unrefined) text to run its
 * dedup and linking rules against.
 */
public final class SimpleExtractor implements Extractor {

  @Override
  public List<Candidate> extract(List<Memory> existing, List<Message> newMessages) {
    List<Candidate> candidates = new ArrayList<>();
    for (Message m : newMessages) {
      if (m.role() == null || "system".equals(m.role())) continue;
      if (m.content() == null || m.content().isBlank()) continue;
      candidates.add(new Candidate(m.content(), List.of()));
    }
    return candidates;
  }
}
