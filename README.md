# BRYC Recommendation UI — Prototype (v4)

A clickable prototype of the proposed recommendation output, built to show the dev team the
target student-facing UI.

**Live demo:** https://tavideehoskins.github.io/bryc-rec-demo/

**v3** adds a **three-tab menu** (Colleges · Short-Term · Scholarships), a **second college
(BRCC, 2-year/CTE)** alongside SLU, and six sourced sample records — an AAS, a technical
associate, a transfer associate, a technical diploma (LPN), an apprenticeship, and three scholarships
— each a transferable template. Builds on the v2 two-column layout, teal branding, and
school-anchored About/Costs sections. Content is hardcoded but **every field maps to a named
source** (PSEO/BOR/IPEDS/LWC/O*NET/IPUMS + Career-OneStop scholarships) — no backend, no Notion
calls. Static build of the standalone `rec-demo` page from the `bryc-workshop` ClojureScript app
(UIx + shadcn/ui + Tailwind v4, Recharts).

**v3.1** brings the 2-year college (BRCC) to **full parity** with the 4-year: BOR-sourced tuition,
a sector-normed graduation rate, IPEDS transfer-out, enrollment/retention, and a complete cost
waterfall — all rendered by the *same* data-driven components, so any recommended institution
populates the same way. See the "v3.1" section of the sourcing spec.

**v4** (per `BRYC_Prototype_Upgrade_Plan_July2026.md`, CEO/Lucas feedback) reworks the menu into
**Degree · Career-Technical · Scholarships** driven by a terminal-vs-transfer flag; fixes earnings
with the **PSEO CIP cascade** (Y1/Y5) and corrects the Process Tech occupation/name; adds **six
hero stat boxes**, a **"Making It Pay Off"** rules-of-the-game module, an on-time-actions **Time &
Completion** reframe, HD2024 net-price links, and Scorecard debt. Every field still traces to a
named source (see the **"v4"** section of the sourcing spec).

## Source & integration
- **`integration/V3_SPEC_AND_SOURCING.md`** — as-built field-by-field sourcing, data files added to the repo (+ iCloud link), acquisition items, and integration notes (**start here**).
- `source/` — the prototype ClojureScript source, for browsing.
- `integration/rec-ui-redesign.patch` + `integration/README.md` — apply the prototype to `ObneyAI/bryc-workshop`.
