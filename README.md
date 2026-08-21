# mem0-akka

Turns new conversation messages into stored facts, skipping any that exactly repeat a
fact already known and linking a new fact to the older ones it relates to.

A port of [mem0ai/mem0](https://github.com/mem0ai/mem0) onto **Akka**, built with
**Akka Specify**.

---

## Where it came from

mem0 is a memory layer that lets an AI assistant remember facts about a person across
conversations. It was ported to derive a specification format precise enough to
regenerate a system on a different stack — the port is the vehicle, the specification
is the deliverable.

The specifications the port was generated from are in
[TylerJewell/akka-specify-harness](https://github.com/TylerJewell/akka-specify-harness)
under `mem0-port/`.

---

## mem0 → this port

📉 173 Python lines (scope-matched) → **117 Java lines**<br>
📁 2 files → **10 files**<br>
🧾 7/7 dedup hashes → **7/7 agree byte-for-byte**

Full method and the numbers that did *not* make this list:
[`bench/REPORT.md`](https://github.com/TylerJewell/akka-specify-harness/blob/main/mem0-port/bench/REPORT.md).

---

## What it took to build

⏱️ **0.3 hours** from the first command to the published repository, **0.3** of them active<br>
💬 **226** exchanges with the model<br>
✍️ **94,777** tokens written by the model, **32,842,290** counting everything sent and re-sent<br>
🙋 **0** questions to a human<br>
🧪 **11** tests

```bash
python toolkit/tokens.py --port mem0    # turns, tokens, elapsed and active time
```

The record of every question, and where the time went, is in
[`port-log/`](https://github.com/TylerJewell/akka-specify-harness/tree/main/port-log).

---

## What it does

From the specification:

- **A repeated fact is silently dropped, not overwritten.** Two facts with the exact
  same text never both get stored, and storing the second one is not an error — it is
  a normal, silent no-op.
- **A new fact may point at older facts it relates to, without changing them.** The
  link is stored alongside the new fact; the fact it points at is never rewritten.
- **Nothing about a stored fact can be revised or removed by this slice.** Once
  written, a fact's text is permanent here — updating or deleting one, if ever needed,
  is a separate capability this port does not include.

---

## Design decisions

**Exact-match dedup, not similarity dedup.** Two facts are treated as the same only
when their text is identical, character for character. This is simple to check and
predict, and it means a fact is never silently merged away just because it sounds
similar to one already stored.

**The fact-writing decision lives outside the model call.** The step that decides
what a new sentence means is kept separate from the step that decides whether to
store it. That way the storing rule can be tested and trusted on its own, without
needing a language model running to check it.

**One owner, one place its facts live.** Every fact belongs to exactly one owner, and
all of that owner's facts are kept together. Looking a fact up, or checking whether a
new one already exists, never has to search anywhere else.

---

## Running it — the short path

You do not need Java, Maven, or the Akka CLI installed. Akka Specify installs them for you.

**1. Install Akka Specify** in Claude Code:

```
/plugin marketplace add akka/ai-marketplace
/plugin install akka@akka-ai-marketplace
```

Restart Claude Code when it asks.

**2. Give it this prompt:**

> Clone https://github.com/TylerJewell/mem0-akka into a new directory and open it.
> Then run /akka:setup to install everything this project needs, and /akka:build to
> compile it, run the tests, and start it locally.

**3. Open** http://localhost:9034.

---

## Running it — the developer path

### Requirements

- Java 21 or newer
- Maven 3.9 or newer
- An Akka download token — run `akka code token` once

No model provider key is required. The step that would call a language model
(turning conversation text into candidate facts) is a small, swappable stand-in in
this port rather than a live call — see "Where it differs from mem0" below.

### Start the service

```bash
mvn compile
akka local run
```

The service starts on **port 9034**.

### Try it

```bash
curl -X POST http://localhost:9034/owners/alice/memories \
  -H "Content-Type: application/json" \
  -d '{"messages": [{"role": "user", "content": "I just got a dog named Poppy"}]}'

curl http://localhost:9034/owners/alice/memories
```

---

## Configuration

| Variable | Default | Notes |
|---|---|---|
| `akka.javasdk.dev-mode.http-port` | `9034` | set in `src/main/resources/application.conf` |

---

## Where it differs from mem0

Everything not listed here behaves the same way on purpose, including the parts that
look like mistakes.

- **What "existing memories" means for dedup and linking.** mem0 checks a new fact
  against only the top 10 nearest results of an embedding similarity search. This
  port checks against every fact the owner currently has. mem0's bound exists to keep
  one search call cheap; this port has no search call to bound, since it is not
  reimplementing similarity search (a fair stand-in, per this project's own rule that
  the model and the search index it depends on are swappable). The result is that
  this port catches an exact-duplicate that mem0's own top-10 bound could miss.
- **How a new fact's text is produced.** mem0 uses a language model to turn raw
  conversation text into a polished, self-contained sentence — resolving pronouns,
  grounding dates, and choosing what is worth remembering. This port's shipped
  extractor is a plain stand-in: it stores each message's text as given. The
  `Extractor` interface it implements is the seam where a real model call would go;
  swapping one in does not require any change to the dedup or storage logic, since
  those never look at how the text was produced.
- **Linking a fact to an id that does not exist.** mem0 never checks that a linked
  id actually names a fact the owner has. This port matches that: a link to a
  made-up id is stored as given, not rejected.

---

## Licence

mem0ai/mem0 is Apache License 2.0, © the mem0 project. This port reimplements the
behaviour described above without copying source; see `ACKNOWLEDGEMENTS.md`.
