# BRYC Recommendation UI — Prototype

A clickable prototype of the proposed recommendation output format (SLU BSN exemplar),
built to show the dev team the target student-facing UI.

**Live demo:** https://tavideehoskins.github.io/bryc-rec-demo/

All data is hardcoded from exemplar v7 — no backend, no Notion calls. This is a static
build of the standalone `rec-demo` page from the `bryc-workshop` ClojureScript app
(UIx + shadcn/ui + Tailwind v4, charts via Recharts).

## Source & integration
- `source/` — the prototype ClojureScript source (UIx + shadcn/ui + Tailwind, Recharts charts), for browsing.
- `integration/` — a patch to apply the prototype to `ObneyAI/bryc-workshop`, plus run/build instructions.
