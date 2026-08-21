package io.akka.mem0.domain;

/** One conversation turn fed to extraction — SPEC-001 §2. */
public record Message(String role, String content) {}
