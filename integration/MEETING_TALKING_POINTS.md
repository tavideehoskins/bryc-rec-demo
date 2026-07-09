# Prototype Walkthrough — Talking Points

### For your meeting with Daryl & Cameron

**Live demo:** https://tavideehoskins.github.io/bryc-rec-demo/
**Full field-by-field spec:** [`V3_SPEC_AND_SOURCING.md`](./V3_SPEC_AND_SOURCING.md) · **Apply to the app:** [`rec-ui-redesign.patch`](./rec-ui-redesign.patch) · **Data files:** [iCloud folder](https://www.icloud.com/iclouddrive/039xU8kYdhAFAeWju4VtVPfHQ#recommendations)

> **Open with this:** *"It's a clickable prototype of the student-facing recommendation. The content is hardcoded, but every field maps to a real, named source — so porting it is a **field → source wiring job, not a redesign**. And nothing is hardcoded per-program: each field derives from a source, so it works for **any** school × program the engine recommends."*

---

## Contents
1. [The big idea (say first)](#1-the-big-idea--say-this-first)
2. [What's on the screen](#2-whats-on-the-screen--30-second-orientation)
3. [Sourcing map — section by section](#3-sourcing-map--section-by-section) ← the core
4. [The data files](#4-the-data-files-whats-in-the-repo)
5. [How it wires into the engine](#5-how-it-wires-into-the-engine-the-to-do-for-daryl--cameron)
6. [Open decisions & missing data](#6-open-decisions--missing-data-need-your-input)
7. [Reference](#7-reference)

---

## 1. The big idea — say this first

- The prototype is two things at once: the **target UI** *and* a **worked example of the sourcing rules**.
- **Transferable, not hardcoded.** Every value traces to a named source (IPEDS · BOR · PSEO · LWC · O\*NET · College Scorecard · Career-OneStop · Notion profiles). Swap the data and any school × program renders the same way.
- **Honest by rule.** If a field can't be sourced, it is **omitted and advisor-verify-flagged** — never faked or estimated into a student-facing field.
- Built additively **v2 → v3 → v4**; **v4** is the July upgrade from the CEO/Lucas feedback + the LAHS Chapter 3 pathway articles.

## 2. What's on the screen — 30-second orientation

- **Left column:** advisor note + advisor card (login-driven).
- **Right column:** "Check these out" → **3 tabs: `Bachelor's` · `Career-Technical` · `Scholarships`.**
- In a tab: **school tile** → *Learn More* → **recommended pathways** (each expands to its own sections) → then **About This School** + **What It Costs You** rendered once per school.
- **Collapsed by default** — the snapshot view; the student opens what they want.
- A school can appear in **both** program tabs with different pathways (e.g. BRCC: Business-Transfer under Bachelor's, ASN/Process-Tech/LPN under Career-Technical).

---

## 3. Sourcing map — section by section

*(Data-file shorthand at the bottom of this section.)*

### Tabs & classification — the backbone
| What you see | How it's sourced / decided |
|---|---|
| Which **tab** a program lands in | `:category` from the **terminal-vs-transfer flag**: award type + **BOR Louisiana Transfer** designation. Terminal (ASN, AAS, certificate) → *Career-Technical*; bachelor's + transfer-designed associate → *Bachelor's*. **Driven by classification, not length.** |
| **Tags** — `Terminal` / `Transfer-designed` / `Louisiana Transfer` | same flag (`award_level_name` + BOR LT registry) |
| The ASN is **Terminal** (not transfer) | resolved on the Lucas call — you finish it, sit the NCLEX, work as an RN |

### Program header — the chips (each deterministic for any program)
| Chip | Source |
|---|---|
| **Field** (Nursing, Business…) | **CIP → short label**: 4-digit CIP series (`51.38 → Nursing`), falling back to 2-digit family (`51 → Health`). Deterministic from the CIP. A profile may override (e.g. catalog name "Process Technology"). |
| **Credential** (Bachelor's, Associate's…) | `award_level_name` — IPEDS/Scorecard controlled vocabulary |
| **Demand** (High / Steady) | **LWC `star_rating`** of the program's primary SOC |

### Overview
| Piece | Source |
|---|---|
| Credential line, "why it fits," caveat | **Notion Pathway Profile / School Addendum** (caveat is conditional — e.g. "getting into the college ≠ getting into the nursing program") |
| The 3 day-to-day tasks | **O\*NET** top tasks for the primary SOC (SOC via the CIP→SOC crosswalk) |
| Licensure note (NCLEX, etc.) | Notion Addendum |
| "See the full program page →" | **`program_url`** — a *column* in `louisiana_programs` (sourced upstream from College Scorecard). ~66% of programs have one → link omitted when blank. URLs can go stale, so **liveness-check on ingest** (same as HD2024). |

### Salary & Job Opportunities
| Piece | Source |
|---|---|
| Earnings bars (Year-1 + Year-5) | **PSEO** — program-level graduate earnings, using a **CIP cascade** (6-digit → 4-digit → 2-digit) so we catch data the exact code misses. Preferred whenever published. |
| Fallback wage (if no PSEO row) | **LWC** occupation median for the primary SOC, **labeled "median, occupation-level"** — used only when PSEO is absent (e.g. the apprenticeship) |
| Living-wage comparison bars | MIT Living Wage (single adult, metro + state) |
| Demand ★ / 10-yr growth / openings | **LWC** on the primary SOC — growth % + **net-new jobs** shown *separately* from **total openings** (they're different measures) |

### Career Paths
| Piece | Source |
|---|---|
| The role list + O\*NET links | **CIP→SOC crosswalk**, *curated*: noise SOCs (e.g. "postsecondary teacher of…", generic managers) filtered out; the **profile-verified target SOC** governs |
| "What degree-holders actually do" | **IPUMS ACS** field-of-degree data (bachelor's-level only) |

### Making It Pay Off  *(new in v4)*
> **This copy now lives in Notion** — the **🧭 Rules of the Game** database in *BRYC App — Knowledge Base*, one record per **Key**. The engine reads it **live by Key**; advisors edit it there (no code deploy). Copy authored from LAHS Chapter 3.

| Piece | Source (Notion Key) |
|---|---|
| Plain-language **Terminal** definition *or* the **transfer-risk** heads-up | `terminal-definition` / `transfer-risk` (chosen by the terminal/transfer flag) |
| **Rules of the game** (per pathway type) | Key = `:rules-type` → `transfer-associate` / `aas` / `certificate` / `apprenticeship` |
| Universal on-time actions *(shown in Time & Completion)* | `on-time-actions` |

### Time & Completion  *(reframed in v4)*
| Piece | Source |
|---|---|
| **Intended full-time length** ("2 years, full-time") | program design (profile/catalog) — replaces the old, misleading "≈4.6-yr average" |
| **Universal on-time actions** | **Notion Rules of the Game** (Key `on-time-actions`) — authored from LAHS Chapter 3 |

### Transfer Plan  *(transfer associates)*
| Piece | Source |
|---|---|
| Destination + 2+2 framing | Notion profile |
| **Louisiana Transfer guarantee (AALT/ASLT)** | BOR / LCTCS statewide transfer policy |
| "Where it leads" (career at the bachelor's) | crosswalk/O\*NET/LWC at the destination CIP |

### Earn While You Learn  *(apprenticeship)*
| Piece | Source |
|---|---|
| Starting → average wage progression | `louisiana_programs` (JATC record) |
| Duration / journey credential | **acquisition** — DOL Apprenticeship.gov (advisor-verify) |

### About This School — six hero stat boxes  *(v4)*
| Box | Source |
|---|---|
| **Acceptance Rate** (selective) *or* **Transfer-Out Rate** (open-admission) | `louisiana_programs acceptance_rate` · **IPEDS `gr2024`** transfer-out |
| **Graduation Rate** + norm vs. peers | **IPEDS `gr2024`** ÷ adjusted cohort; normed vs. same-sector LA public schools |
| **Enrollment** — *keyed to the student's own race* | **IPEDS `ef2024a`** (all race columns); falls back to total for non-minority |
| **Avg Debt at Graduation** | **College Scorecard** median debt of completers (`GRAD_DEBT_MDN`) |
| **First-Year Retention** | **IPEDS `ef2024d`** |
| **Campus** (Residential/Commuter + location) | profile; distance shown only when relevant |

### What It Costs You  *(one shared cost bar for every program type)*
| Piece | Source |
|---|---|
| Cost waterfall: Cost of Attendance → after TOPS → net after Pell | **BOR FY26** tuition & fees · books + living (IPEDS/Scorecard) · **TOPS** (LOSFA/BOR) · Pell |
| **Net-Price Calculator + Financial-Aid links** | **IPEDS `HD2024`** (`NPRICURL`/`FAIDURL`) — scheme-normalized + **liveness-checked** (some school URLs are stale) |
| "Lower your net cost: live at home…" | shown **only for a commutable school** (in/near the student's metro) — omitted otherwise |
| Estimate-only / FAFSA note | always shown |

### Scholarships
| Piece | Source |
|---|---|
| The scholarship set | **Career-OneStop** (Louisiana-eligible) — **confirmed the authoritative source** |
| Card fields (award, deadline, eligibility, criteria) | Career-OneStop columns, verbatim |
| "Why it fits" + tips | production: the **DSPy 5-phase pipeline** (`ExplainScholarshipMatch`); prototype: grounded match notes |
| Expandable + "advisor-verified" | advisors keep the list current and can add their own |

**Shorthand:** `louisiana_programs` (Scorecard-derived: acceptance, CIP, award level, program_url, tuition, apprenticeship wages) · `louisiana_occupation_wages` = **LWC** (median, growth, openings, stars, education) · **PSEO** = census program earnings · **IPEDS** `gr2024`/`ef2024a`/`ef2024d`/`HD2024` · **BOR** fee + TOPS files · **Scorecard** `GRAD_DEBT_MDN` · **Career-OneStop** scholarships · **Notion** = the reusable profiles.

---

## 4. The data files (what's in the repo)

All under `components/recommendations/resources/recommendations/` — **the added ones are in the [iCloud folder](https://www.icloud.com/iclouddrive/039xU8kYdhAFAeWju4VtVPfHQ#recommendations).**

- **Already there:** `louisiana_programs_with_embeddings.csv`, `louisiana_occupation_wages.csv`, `cip_soc_crosswalk.csv`, `onet_tasks_*`, `career_onestop_*` (scholarships).
- **Added for this build:** `pseo_la.xlsx` (PSEO earnings), `HD2024.csv` (net-price/FAID URLs), IPEDS `gr2024` + `ef2024a` + `ef2024d`, BOR fee-detail + TOPS, `bor_ipeds_bridge.csv`, IPUMS ACS + crosswalk.
- **Sourced live (not a repo file yet):** College Scorecard `GRAD_DEBT_MDN` (debt box) — add it to the Scorecard extract, keyed by UNITID.

---

## 5. How it wires into the engine (the to-do for Daryl & Cameron)

**The port is field → source.** `data.cljs` field names mirror the sources, so each field is a direct wire. The mechanisms the engine implements:

- **Notion profiles** (Foundation / School / Addendum — via your existing `bryc-build-profiles`) supply the **qualitative** content: overview copy, "what's cool," caveats, catalog-name override, and the **verified primary SOC**.
- **Notion "🧭 Rules of the Game"** (new database in the Knowledge Base) supplies the "Making It Pay Off" copy + on-time actions — keyed by pathway type: resolve `:rules-type` (or the terminal/transfer flag) → **Key** → read that record's body. Advisor-editable; no code deploy.
- **Joins:** by **UNITID** (school data), **CIP** (program data), **SOC** (occupation data).
- **PSEO cascade** — 6→4→2-digit CIP, `status=1`, show Y1 + Y5.
- **Crosswalk curation** — drop noise SOCs; use the profile-verified target SOC for careers + growth/openings. *(This is the biggest reliability lever — flag it.)*
- **CIP → field dictionary** — load the full **NCES CIP-2020** list (the prototype only seeds it).
- **HD2024 join** — strip the BOM, prepend `https://`, and **liveness-check** URLs on ingest.
- **Scorecard debt** — add `GRAD_DEBT_MDN` to the extract.
- **Dynamic enrollment** — pick the student's own race column from `ef2024a`.
- **Per-record drivers** — `:category` (tab), terminal/transfer flag (classification + rules module), `:sections` (which sections render), `:commutable?` (cost advice) — all data-driven, not code branches.
- **Scholarships** — Career-OneStop set + the DSPy explain pipeline.

---

## 6. Open decisions & missing data (need your input)

| Item | Status / ask |
|---|---|
| **CIP→SOC crosswalk accuracy** | Raw crosswalk mis-maps many sub-baccalaureate programs → careers/demand can be wrong. **Needs the profile-verified target SOC.** Biggest dependency. |
| **Full NCES CIP dictionary** for field chips | prototype seeds ~30 entries; load the complete 4-digit list |
| **Program-level time-to-degree**, **licensure pass rates**, **transfer→bachelor's completion**, **apprenticeship duration** | not in any current source → **acquisition** (BOR IR, state boards, NSC/CCRC, DOL) |
| **HD2024 URL liveness** | some school NPC pages 404 (e.g. Southeastern's) — validate on ingest, fall back to the aid page |
| **Complete LCTCS roster** | the 2-year grad-rate peer set resolves only 2 colleges in-repo — widen it |
| **Per-UNITID logo store** | prototype uses a favicon fallback; production wants curated logos |
| **`:commutable?`** | prototype is Baton-Rouge-relative — production computes it from the **student's** location vs. the school |
| **Scholarships source** | ✅ decided — Career-OneStop is authoritative (curated list dropped) |

---

## 7. Reference

- **Live demo:** https://tavideehoskins.github.io/bryc-rec-demo/
- **Full spec (field → source, every deviation, acquisition items):** [`V3_SPEC_AND_SOURCING.md`](./V3_SPEC_AND_SOURCING.md)
- **Apply to `bryc-workshop`:** [`rec-ui-redesign.patch`](./rec-ui-redesign.patch) (`git apply`) + [`README.md`](./README.md)
- **Browse the source:** `source/` in this repo
- **Data files:** [iCloud folder](https://www.icloud.com/iclouddrive/039xU8kYdhAFAeWju4VtVPfHQ#recommendations)
