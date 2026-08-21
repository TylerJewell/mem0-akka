# Acknowledgements

This project is a port of **[mem0ai/mem0](https://github.com/mem0ai/mem0)** — the
memory extraction and consolidation slice: what is written, merged, or superseded.

## Licence and copyright

`mem0ai/mem0` is licensed under the **Apache License, Version 2.0** — read directly
from `mem0-src/LICENSE` in the cloned source. Apache-2.0 is a permissive licence with
no copyleft/network clause: a derivative work may be licensed differently, provided
the original copyright and licence notice are retained where the original work's own
files are redistributed.

## Was anything copied verbatim?

No. Every file in `mem0-akka/src/` is original Java written from scratch against the
Akka SDK. No source file, prompt string, or code fragment from `mem0-src/mem0/` was
copy-pasted. `ADDITIVE_EXTRACTION_PROMPT` (`mem0-src/mem0/configs/prompts.py`) was read
to understand what the extraction step is meant to produce, but the port's own
`SimpleExtractor` (SPEC-001 §4 decision 2) is a deliberately unrefined, deterministic
stand-in with its own independently-written logic — one candidate per message,
verbatim — not a translation of that prompt or the model behavior it elicits.

## Is behaviour derived even where no text was copied?

Yes. This is a behavior port: the exact-content (unnormalized) MD5 hash used for
dedup, the rule that a duplicate is silently dropped rather than erroring, and the
`linkedMemoryIds` association carried alongside a new memory are all read directly
from `mem0ai/mem0`'s behavior (`memory/main.py`'s `_add_to_vector_store`) and
reproduced deliberately — see `mem0-port/specs/SPEC-001-mem0.md` and
`mem0-port/docs/question-log.md` for what was checked and how. The one part of the
source's actual behavior not reproduced is the LLM's linguistic judgment inside
extraction itself (stood in for, per SPEC-001 §4 decision 2) and the bounded top-10
vector-similarity search used to build mem0's "existing memories" candidate set
(SPEC-001 §4 decision 1 — this port uses the owner's full current memory set instead).

## What licence does that force on this project?

Apache-2.0 places no restriction on licensing a behavior-derived rebuild differently,
and no file from `mem0ai/mem0` is redistributed here. Consistent with PIPELINE.md
step f's default, this repository is published **private** — a decision about
whether to make it public is separate from whether it is legally permitted, and is
left to a deliberate choice rather than a side effect of backing work up.

## Also used

- Akka (the Agentic Systems Platform) — Java SDK, `io.akka:akka-javasdk-parent`.
