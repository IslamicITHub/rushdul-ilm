You are starting a new Android development project called **Rushd-ul-Ilm (رشد العلم)**. Before writing a single line of code, read these files in this exact order:

1. `AGENT_RULES.md` — master project rules, tech stack, Islamic content rules
2. `SPRINT_SYSTEM.md` — the complete micro-task breakdown for all phases
3. `activity-logs/ACTIVITY_LOG.md` — current sprint status and next task

All three files are in the current working directory. Read them completely. Do not summarize — read them fully so you understand:
- What the app does and who it is for
- What the complete technology stack is
- What the absolute prohibition rules are (especially the Islamic content rules)
- What micro-task to start with (find CURRENT_MICRO_TASK in ACTIVITY_LOG.md)

---

After reading all three files, follow these rules for this entire session and every future session:

**TEACHING RULE:** I am a complete beginner in Android Development and Android Studio. I have intermediate Python and basic Kotlin knowledge. Before showing any new concept in code, explain it in 2-4 sentences of plain English with a simple real-life analogy. Then show the code. Every single line of code must have an inline comment explaining what it does and why.

**SPRINT RULE:** Work on exactly ONE micro-task per response. State the micro-task ID at the start (e.g., "Starting micro-task P1.S1.SS1.MT1"). When the micro-task is done, state its DONE CONDITION clearly, verify it is met, then stop and ask me to confirm before moving to the next micro-task. Never combine multiple micro-tasks into one response.

**DOCUMENTATION RULE:** After completing each micro-task, append an entry to `activity-logs/ACTIVITY_LOG.md` with the sprint ID, micro-task ID, files created/modified, and the NEXT_MICRO_TASK field filled in.

**ANTI-HALLUCINATION RULE:** If you are unsure about any Android API, library version, or function signature — say so explicitly with: "⚠️ VERIFY IN DOCS:" followed by the official documentation URL. Never invent API methods.

**ISLAMIC CONTENT RULE:** Never write any code that generates Islamic fatwas, rulings, or Quranic interpretations from an LLM's own training data. All Islamic answers must come from the RAG pipeline with source URLs. This rule applies to every line of code in this project, forever.

---

Now confirm you have read all three files by answering these questions from memory (do not re-read to answer — this tests your comprehension):

1. What is the Android package name? (Hint: it is in AGENT_RULES.md)
2. What is the first micro-task ID? (from ACTIVITY_LOG.md or SPRINT_SYSTEM.md)
3. What are the three approved Islamic knowledge source websites?
4. What GPU does the developer's machine have and what is its VRAM limit?
5. What is the minimum font size allowed for any user-facing text in the app?

After answering these 5 questions correctly, begin micro-task P1.S1.SS1.MT1 from SPRINT_SYSTEM.md. Follow the micro-task instructions exactly. Do not add extra features or go beyond what the micro-task specifies.

## ═══ PROMPT BODY END ═══

## ═══ RESUME PROMPT (use this for every session AFTER the first) ═══

Paste this at the start of every new session after the first:

```
Read these files in order before doing anything else:
  1. AGENT_RULES.md
  2. activity-logs/ACTIVITY_LOG.md  ← find CURRENT_MICRO_TASK at the bottom
  3. SPRINT_SYSTEM.md  ← find that micro-task ID and read its instructions

After reading, tell me:
  - Which micro-task you are resuming (ID + one-sentence description)
  - What the previous session completed (from ACTIVITY_LOG.md last entry)
  - Whether there are any blockers noted

Then begin that micro-task. One micro-task per response. Explain every new concept before showing code. Comment every line.
```

## ═══ RESUME PROMPT END ═══

