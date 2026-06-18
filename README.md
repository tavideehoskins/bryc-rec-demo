# BRYC Recommendation UI — Prototype (v2)

A clickable prototype of the proposed recommendation output format (SLU BSN exemplar),
built to show the dev team the target student-facing UI.

**Live demo:** https://tavideehoskins.github.io/bryc-rec-demo/

v2 restructure: two-column layout (Advisor Messages · Pathway Recommendations),
BRYC teal branding, a school tile whose pathways unfurl individually, and
school-anchored **About This School** + **What It Costs You** sections. Content is
hardcoded but sourced to match the Obney data spec (PSEO/BOR/IPEDS/LWC + real IPUMS
PUMS outcomes) — no backend, no Notion calls. This is a static build of the standalone
`rec-demo` page from the `bryc-workshop` ClojureScript app (UIx + shadcn/ui + Tailwind
v4, charts via Recharts).

## Source & integration
- `source/` — the prototype ClojureScript source (UIx + shadcn/ui + Tailwind, Recharts charts), for browsing.
- `integration/` — a patch to apply the prototype to `ObneyAI/bryc-workshop`, plus run/build instructions.
