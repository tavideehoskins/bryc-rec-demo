# BRYC Recommendation v3 — Data Sourcing Spec (AS BUILT) + Handoff

**For:** Daryl & Cameron (porting v3 into the production recommendation engine).
**Companion:** `BRYC_Rec_v3_Build_Plan.md`. **Live demo:** https://tavideehoskins.github.io/bryc-rec-demo/

Field-by-field sourcing for the v3 output **as actually built**, down to file / column, Notion
profile, or existing engine module — including every deviation from the original v3 spec.
**Rule (honored): every field traces to a named authoritative source in the repo or a Notion
profile. Nothing is guessed. If a field has no source it is omitted from the student view (no
placeholder) and flagged on the advisor side.**

All file references verified against the repo on **2026-06-29**; the **2-year parity refresh**
(§ "v3.1") verified **2026-06-30**. The prototype hardcodes values in `data.cljs`, but every field
name mirrors its source, so production wiring is field→source.

---

## What v3 built (deltas vs the v2 single-school Colleges view)

1. **Three-tab menu** — `Colleges` · `Short-Term` · `Scholarships` (in-page state; no routing).
2. **Second college in Colleges** — Baton Rouge Community College (BRCC, 2-year/CTE) alongside SLU.
3. **Six sourced sample records**: AAS (ADN), technical associate (Industrial Production Tech),
   transfer associate (Business LA Transfer), certificate (LPN), apprenticeship (BR Electrical JATC),
   and 3 scholarships.
4. **New per-type sections**: Time & Completion, Transfer Plan, Cost & Funding, Earn While You Learn.
5. **As-built deviations from the original spec (call-outs):**
   - **BRCC is not in PSEO** → all BRCC samples show the **LWC occupation wage** (labeled
     occupation-level), not a program-earnings chart.
   - **Scholarships** use the in-repo `career_onestop_louisiana_final_4036.csv.gz` (the spec's
     `scholarships_v2_…csv` lives in the Obney `python/` repo, not the data mount).
   - **Time-to-degree (CMPLTTD) is institution-level only** (no CIP column) → shown as a
     school-wide figure; **program-level completions added separately from CMPLRACE**.
   - **Apprenticeship wage progression IS in the repo** (PROG `starting`/`average-income-hourly`)
     — so it's shown, not deferred.
   - **Transfer→bachelor's completion is NOT shown** — no school-specific source in the repo
     (national NSC numbers aren't true for BRCC); kept as an acquisition item.
   - **Logos** are domain-derived (favicon) with curated overrides — consistently attainable for any school.

---

## v3.1 — 2-year (BRCC) brought to FULL parity with 4-year (2026-06-30)

The first v3 cut gave 2-year/CTE schools a **minimal** About + simple cost (STR, grad-norm, campus
life, and the cost waterfall were suppressed). Per advisor feedback, the 2-year **About This School**
and **What It Costs You** are now sourced to the **same depth as the 4-year**, and the SAME components
render both (no `cte?` branch). **Every value below is institution-agnostic** — pulled from the same
per-school datasets used for SLU, so any recommended 2-year populates the same way.

| # | Change | As built (BRCC, UNITID 437103) | Source |
|---|---|---|---|
| 1 | **Tuition now from BOR, not Scorecard** | Tuition & **fees $4,419** (resident) replaces Scorecard tuition-only $3,237 | **BORFEE** `SummaryUGrad` (calibrated: SLU col = known $9,043) |
| 2 | **PSEO-first made explicit** | Precedence is **PSEO whenever present, LWC only as fallback**; BRCC has no PSEO row → LWC occupation wage | code + PSEO scan (BRCC absent) |
| 3 | **Graduation rate, sector-normed** | **31%** (3-yr / 150%-time) vs **LA public 2-yr avg 30.3%** → **+0.7 → "Medium"** | **GR** `gr2024` 2-yr cohort: 299 completers ÷ 964 (GRTYPE 30/29); peers {BRCC 31.0, River Parishes 29.6} |
| 4 | **Transfer data in About This School** | **17%** transfer-out (≈167 of 964, within 3 yrs) | **GR** `gr2024` transfer-out (GRTYPE 35 / CHRTSTAT 22) |
| 5 | **Cost waterfall** (parity w/ 4-yr) | direct costs **$5,719** → after TOPS-Tech **$2,633** → **$0 out of pocket** (after Pell) | BORFEE T&F $4,419 + books $1,300; TOPS-Tech $3,086; Pell $7,395. **Off-campus living EXCLUDED** (an allowance, not a school charge) → aid ($10,481) covers all direct costs |
| 6 | **Enrollment + retention** | UG **11,182**, **Black/AfAm 56.9%** (6,359), **retention 53%** | **EF** `ef2024a` (`EFTOTLT`/`EFBKAAT`); `ef2024d` `RET_PCF` |
| 7 | **Transfer associate now shows TIME** | Business (LA Transfer) shows school-wide **≈4.6 yr** alongside its **56** completers | **TTD** institution-level (applies to the transfer associate too) |
| 8 | **Avg debt OMITTED for 2-yr** | no bullet — Scorecard per-borrower ($5,204) reads misleadingly low; no in-repo cumulative figure | acquisition (IPEDS cumulative debt for 2-yrs) |

**Grad-rate norm caveat (acquisition):** the in-repo LA roster (`PROG` + `BRIDGE`) resolves only **2**
public 2-years (BRCC, River Parishes), so the "statewide 2-yr average" is thin. Completing the LCTCS
roster (finish `bor_ipeds_bridge.csv`, or add the IPEDS HD directory for a LA-public-2-yr filter)
widens the peer set; the mechanism is unchanged.

---

## Source shorthand
Files in `components/recommendations/resources/recommendations/`. **★ = added to the repo
(not in the original ObneyAI checkout) — see "Resources added" + iCloud link below.**
- **PROG** = `louisiana_programs_with_embeddings.csv` (acceptance, ACT 25/75, tuition, room/board, Pell, NPC, distance, sector, HBCU, `award_level_name`, `award_category`, `program_url`, `starting/average-income-hourly`, `loan-average-amount`)
- **LWC** = `louisiana_occupation_wages.csv` (`star_rating`, `median_wage`, `growth_pct`, `total_openings`, `education_required`)
- **XWALK** = `cip_soc_crosswalk.csv` (CIP→SOC) · **ONET** = `onet_tasks_with_embeddings.csv.gz` (`top_tasks_json`), `onet_occupations_with_embeddings.csv.gz`
- **PSEO ★** = `pseo_la (1).xlsx` (program earnings; `agg_level_pseo=34`, `label_degree_level`, `y1/5/10_p50_earnings`, status)
- **PUMS ★** = `ipums_acs_degfield_outcomes.csv` + `ipums_degfieldd_cip_crosswalk.csv`
- **GR ★** = `ipeds_extracted/gr2024.csv` · **EF ★** = `ipeds_extracted/ef2024a.csv` · **BRIDGE ★** = `bor_ipeds_bridge.csv`
- **BORFEE ★** = `BOR-Fee_Detail_FY26-8-11-25-Revised.xlsx` (FY26 tuition+fees, LA public)
- **TTD ★** = `CMPLTTD.xlsx` (time-to-degree, **institution-level**) · **CMPL ★** = `CMPLRACE.xlsx` (completions **by CIP**)
- **SCHOL** = `career_onestop_louisiana_final_4036.csv.gz` (`louisiana_eligible`, `selection_criteria`, `award_amount_display`, `deadline_display`, `target_degree_levels`, `healthcare_focus`)
- **TOPS ★** = LOSFA award PDFs (OPH / TECH / Excellence) · **MIT** = MIT Living Wage · **NOTION** = Foundation / School Profile / School Addendum

---

## Page level

| Field | Source (as built) |
|---|---|
| Student first name, ACT (20), GPA, interests, race | student profile / intake |
| Advisor card (name, title, email, headshot, appointment) | BRYC Team sheet + "Team Headshots_BRYC App" + appointment-link doc (prototype: Tavidee / calendar.app.google) |
| School logo | curated asset if present, else **favicon from PROG website domain** (`icons.duckduckgo.com/ip3/{domain}.ico`) — consistent for any school |
| Safety/Target/Reach vs **Open Admission** | ACT vs PROG `act_composite_25th`/`_75th`; **no ACT → "Open Admission"** (all 2-year/CTE) |

## Colleges tab — 4-year (SLU BSN, UNITID 160612)  *(carried from v2)*

| Field | Source (as built) |
|---|---|
| Identity, STR badge | PROG (`acceptance_rate`, `act_composite_25th/75th` 18/24) |
| Overview (credential, connection, caveat) | NOTION Pathway Profile (caveat conditional) |
| Overview day-to-day (3) | ONET `top_tasks_json`, SOC 29-1141 via XWALK |
| What's Cool (≤4) | NOTION School Addendum (CDS + official site) |
| Earnings Y1/Y5/Y10 ($70,105/$73,162/$85,313) | PSEO `agg_level 34`, inst+`cipcode`+degree, status=1 |
| Living wage ($45,496 BR / $42,370 LA); band "Above" | MIT; band computed (Y1 vs BR: >+15% Above / ±15% Near / <−15% Below) |
| Demand (★5, +7.55%, 27,706) | LWC, SOC 29-1141 |
| Careers + O*NET links + advanced-cred flags | XWALK (51.38→SOCs) + ONET; `onetonline.org/link/summary/[SOC].00` |
| PUMS "what degree-holders do" (<50% callout) | PUMS: CIP→`degfieldd` 6107 → LA, top-5 `weighted_n`, `pct_in_occupation`, `median_wagp` |
| Acceptance 99% · 4-yr grad 26% | PROG `acceptance_rate`; GR (GRTYPE 8 cohort, GRTYPE 13 100%-time, UNITID 160612) |
| Grad-rate norm ("Low") | computed: LA-public-4yr (BRIDGE) UG size bands (EF) × band-avg GR; High ≥+5 / Low ≤−5 / Med |
| Enrollment, race-aware (Black 2,586/13,192) | EF `EFALEVEL=2, LINE=99` (`EFTOTLT`/`EFBKAAT`/…) |
| Tuition+fees ($9,043 = $5,777+$3,266) | **BORFEE** (replaces older IPEDS $8,373) |
| Room/board ($10,240) · books ($1,300) · Pell ($7,395) | PROG (`room_board_on_campus_annual`, `books_supplies_annual`, `pell-grant-amount-per-semester`×2) |
| TOPS Opportunity (−$5,652) · net ($7,536) | **TOPS** (LOSFA OPH); net computed (COA − TOPS − Pell) |
| Avg debt ($22,113) | IPEDS institution-wide (PROG `loan-average-amount` $5,368 Scorecard also exists — confirm which) |
| NPC / FAFSA / campus-life links | verified web (SLU cost page, studentaid.gov, SLU campus_life) |

## Colleges tab — Terminal / Technical Associate (AAS)  *(BRCC ADN; Industrial)*

| Field | Source (as built) |
|---|---|
| Program title, credential (AAS), CIP | PROG `program-title`, `award_level_name`, `cip-code` (ADN 51.3801 · Industrial 15.0699) |
| Career + day-to-day | XWALK CIP→SOC → ONET (RN 29-1141 · Ind. Eng. Tech 17-3026) |
| **Earnings** | **none — BRCC not in PSEO** → show **LWC occupation wage** (labeled occupation-level) |
| Demand (stars / growth / openings / median) | LWC, primary SOC (RN ★5 $76,636 +7.55% · Ind. ★5 $99,602) |
| Living-wage band | computed: LWC occupation median vs MIT BR single-adult |
| Time-to-credential (designed ~2 yr · typical ≈4.6 yr) | **TTD** — **institution-level** (BRCC FTIC-FT 4.635872; school-wide, NOT per-program) |
| **Completers / year (program-specific)** | **CMPL** — awards by CIP, BRCC 2024 (ADN **131** · Industrial **29**) |
| License (NCLEX-RN; pass rate omitted) | NOTION Addendum (pass rate = acquisition) |
| Cost / funding | **school-anchored** — see "2-year School-anchored" table below (now a full BOR/IPEDS waterfall, not minimal) |
| **Not shown (by data, not by type):** PUMS (non-bachelor's only) · campus life (no campus-life URL) | STR shows **"Open Admission"**; **grad-norm + transfer-out + cost waterfall ARE now shown** (see v3.1) |

## Colleges tab — Transfer Associate (BRCC Business, LA Transfer, CIP 52.0101)

| Field | Source (as built) |
|---|---|
| Program identity (associate, transfer-designed) | PROG `award_level_name` + Foundation (track=transfer) |
| **LA transfer guarantee (AALT/ASLT)** | LCTCS / BOR statewide transfer-degree policy |
| Careers at the destination bachelor's | XWALK/ONET/LWC at the BS CIP (Gen & Ops Mgrs 11-1021, LWC $101,556) |
| Completers / year (program-specific) | **CMPL** (CIP 52.0101, BRCC 2024 = **56**) |
| **Time to credential (designed ~2 yr · typical ≈4.6 yr)** *(v3.1)* | **TTD** institution-level — the transfer associate IS a BRCC associate, so the same school-wide figure applies (renders with the 56 completers) |
| Cost to bachelor's | PROG tuition (2-yr) + "continues at a 4-year" note (full cost depends on destination) |
| **Articulation + transfer→bachelor's completion** | **acquisition** — BOR Statewide Articulation; CCRC/NSC (shown as advisor-mapped, not a number). NB: the school-level **transfer-OUT rate** (17%) IS shown in About This School (GR), distinct from transfer-*completion* |

## Colleges tab — 2-year School-anchored: About This School + What It Costs You  *(v3.1; BRCC, UNITID 437103)*

Rendered ONCE per school by the **same** components as the 4-year. Mirrors the 4-year sourcing exactly.

| Field | Source (as built) |
|---|---|
| STR badge → **"Open Admission"** | no ACT in PROG → classifier returns Open Admission |
| **Graduation rate 31% (3-yr) + norm "Medium"** | **GR** `gr2024` 2-yr cohort 299/964 (GRTYPE 30/29); peer avg = mean across LA public 2-yrs {BRCC 31.0, River Parishes 29.6} = 30.3% (+0.7); High ≥+5 / Low ≤−5 / Med |
| **Transfer-out 17%** (≈167 of 964, within 3 yrs) | **GR** `gr2024` transfer-out (GRTYPE 35 / CHRTSTAT 22) ÷ adjusted cohort |
| Enrollment, race-aware (Black 6,359 / 11,182 = 56.9%) | **EF** `ef2024a` (`EFALEVEL=2, LINE=99`: `EFTOTLT`/`EFBKAAT`) |
| First-year retention (53%) | **EF** `ef2024d` `RET_PCF` |
| Setting / open-admission / commuter narrative | PROG `sector`, `distance_from_baton_rouge_miles=0`; open-admission from missing ACT |
| **Tuition & fees $4,419** (resident) | **BORFEE** `SummaryUGrad` — replaces Scorecard tuition-only $3,237 |
| Books $1,300 → **direct cost $5,719** (tuition + fees + books) | PROG `books_supplies_annual`. **Off-campus living EXCLUDED** — a commuter allowance, not a school charge (per advisor direction); COA shows school-charged direct costs only |
| **TOPS-Tech −$3,086** · Pell −$7,395 → **net $0 out of pocket** | **BORFEE** TOPS column; Pell = `pell-grant-amount-per-semester`×2; aid ($10,481) exceeds direct costs → net capped at $0 |
| Avg debt | **OMITTED** (no sound in-repo cumulative figure; Scorecard per-borrower $5,204 misleads) — acquisition |
| NPC / FAFSA links | PROG `net-price-calculator-url` (mybrcc.edu); studentaid.gov |

## Short-Term tab — Certificate (BRCC LPN, CIP 51.3901)

| Field | Source (as built) |
|---|---|
| Program title, certificate type, city | PROG `program-title`, `award_level_name`, `city` |
| Career + day-to-day | XWALK→ONET (LPN 29-2061) |
| **Earnings** | none (BRCC not in PSEO) → LWC occupation wage (★4 $49,997 +7.48%) |
| Completers / year | **CMPL** (CIP 51.3901, BRCC 2024 = **24**) |
| Funding (Pell / TOPS-Tech, commuter) | PROG `pell-grant-*`, `title-iv-status`; TOPS-Tech |
| License (NCLEX-PN; pass rate omitted) | NOTION Addendum (pass rate = acquisition) |
| **More-info link** | PROG `program_url` (mybrcc.edu practical-nursing page) |
| Length (weeks) / stackability | acquisition (institution / LCTCS) |

## Short-Term tab — Apprenticeship (Baton Rouge Electrical JATC, SOC 47-2111)

| Field | Source (as built) |
|---|---|
| Trade / occupation, sponsor | PROG (`award_category`=Apprenticeship) |
| Demand + median wage | LWC, SOC 47-2111 (★5 $59,259 +11.7%, HS-entry) |
| **Starting → average wage progression ($17.74 → $28.16/hr)** | **PROG `starting-income-hourly` / `average-income-hourly`** (in repo) |
| **More-info link** | verified `apprenticeshipla.com/apprenticeships/baton-rouge-electrical-jatc/` (PROG field's slug was mis-pointed to Alexandria — corrected) |
| Duration / journey credential | acquisition — DOL Apprenticeship.gov |
| **Dropped:** tuition/Pell/TOPS (you're paid), grad rate, ACT, campus life | — |

## Scholarships tab (reuse the 5-phase pipeline unchanged)

| Field | Source (as built) |
|---|---|
| Scholarship set (Louisiana-eligible) | **SCHOL** `louisiana_eligible=true` (3 rows: Jane Delano / Career Mobility / A Flight Away) |
| Name, sponsor, award, deadline, eligibility, criteria, levels, URL | SCHOL columns (verbatim from file) |
| Eligibility + relevance match | student profile × SCHOL (`selection_criteria`, `target_degree_levels`, `healthcare_focus`) |
| "Why it fits" + application tips | **DSPy `ExplainScholarshipMatch` (phase 5)** in production; grounded match notes in the prototype |

---

## Resources added to the repo (not in the original checkout)

All under `components/recommendations/resources/recommendations/`.
**iCloud folder with these materials:** https://www.icloud.com/iclouddrive/039xU8kYdhAFAeWju4VtVPfHQ#recommendations

| File (★ added) | Used for |
|---|---|
| `BOR-Fee_Detail_FY26-8-11-25-Revised.xlsx` | BOR FY26 tuition + fees (LA public) |
| `CMPLTTD.xlsx` (+ `CMPLTTD (1).xlsx` baccalaureate) | time-to-degree (institution-level) |
| `CMPLRACE.xlsx` | program completions by CIP (completers/year) |
| `bor_ipeds_bridge.csv` | UNITID ↔ BOR code bridge (grad-rate norm) |
| `ipeds_extracted/gr2024.csv` (from `gr2024.zip`) | IPEDS graduation rates (4-yr & **2-yr cohorts**) + **transfer-out** |
| `ipeds_extracted/ef2024a.csv` (from `ef2024a.zip`) | IPEDS enrollment by race |
| `ipeds_extracted/ef2024d.csv` (from `ef2024d.zip`) | IPEDS first-year **retention** (`RET_PCF`) — SLU 71%, BRCC 53% |
| `pseo_la (1).xlsx` | PSEO program-level earnings |
| `ipums_acs_degfield_outcomes.csv` + `ipums_degfieldd_cip_crosswalk.csv` (+ `_reference.csv`) | PUMS "what degree-holders do" |
| `TOPS OPH / TECH / Excellence Award Amounts.pdf` | LOSFA TOPS award amounts |

*Already in the repo (used, not added):* `louisiana_programs_with_embeddings.csv`,
`louisiana_occupation_wages.csv`, `cip_soc_crosswalk.csv`, `onet_tasks_*`/`onet_occupations_*`,
`career_onestop_louisiana_final_4036.csv.gz`.
*Present but unused by v3:* `STDMIGRRPT.xlsx` (regional migration, not transfer-completion),
`BRGRATERPT.xlsx`, `CRINACCR.xlsx`, `CRINPROGA.xlsx`, IPUMS raw `usa_*`.

---

## Transferability mechanisms (as built)

- **Per-record `:sections`** — each program declares which sections render; falls back to the 4-year default. Add/remove by data, not code.
- **Salary is PSEO-FIRST** — `:earnings` (PSEO program-level) is used **whenever present**; the `:occupation` LWC occupation-wage chart is the **fallback only** when PSEO isn't published for that school×program (labeled occupation-level). Institution-agnostic precedence.
- **School-anchored About + Cost are one data-driven path** *(v3.1)* — the SAME `about-school-section` / `costs-section` render 4-year and 2-year; what shows is driven by the fields the school record carries (acceptance/ACT when selective; transfer-out + open-admission copy otherwise). No school-type branch.
- **Grad-rate norm is sector-aware, per-school** *(v3.1)* — each school carries its own `:grad-rate {rate, normed-against, peer-average, delta, indicator}`. Mechanism = IPEDS rate vs mean of **same-sector** LA public peers (4-yr: size-banded; 2-yr: statewide). High ≥+5 / Low ≤−5 / Med.
- **Transfer-out** *(v3.1)* — `:transfer-out` (IPEDS GR) renders in About This School when present (community colleges); omitted otherwise.
- **Cost waterfall is generic** *(v3.1)* — reads `:tuition-fees` / **optional** `:living`+`:living-label` / `:books` / `:coa` / `:tops`+`:tops-name` / `:pell` / `:net` / optional `:avg-debt` / `:commuter?` / `:fully-covered?`; axis scales to COA. A residential school includes `:living` (on-campus room & board, a real charge); a **commuter school omits `:living`** so the COA is school-charged direct costs only. When aid ≥ COA the net shows **$0 out of pocket**. Same chart for both school types.
- **Chips** — derived per-program from `:field` (CIP title), `:credential-level` (award level), `:lwc-stars` (≥4 → "High demand").
- **Logos** — curated asset OR domain favicon (any school).
- **STR / Open Admission** — classifier on ACT vs 25/75; no ACT → "Open Admission".
- **Conditional fields** — every discipline-specific field omitted when `nil` (e.g. avg-debt for 2-yrs, PUMS below a bachelor's).

---

## Data we do NOT have in the repo — acquisition items + authoritative source

Omitted from the student view, flagged on the advisor side.

| Missing field | Tier(s) | Authoritative source |
|---|---|---|
| **Transfer→bachelor's *completion*, school-specific** | Transfer Associate | CCRC *Tracking Transfer* / NSC StudentTracker (national NSC is **not** school-specific — not shown). NB transfer-*out* rate (17%) **is** shown from IPEDS GR. |
| **Complete LA public 2-year roster** (for the grad-rate norm) *(v3.1)* | 2-year norm | finish `bor_ipeds_bridge.csv` UNITID mapping, or add IPEDS HD directory (filter LA + public + 2-yr). In-repo roster currently resolves only 2 colleges. |
| **Cumulative debt at graduation, 2-year** *(v3.1)* | 2-year cost | IPEDS / College Scorecard institution cumulative debt (Scorecard per-borrower loan-average is misleadingly low — debt bullet omitted for 2-yrs) |
| **Program-level time-to-degree** | All associate/cert | BOR SSPS unit-record / institution IR (CMPLTTD is institution-level only) |
| Licensure / cert pass rates (beyond NCLEX-RN) | AAS, Certificate | State boards (LA State Board of Nursing, Cosmetology, Contractors, AWS, OMV) |
| Program approval-for-licensure flag | AAS, Certificate | Same state boards |
| Job-placement / completer employment rate | AAS, Certificate | WIOA ETPL / TrainingProviderResults.gov; LCTCS; BOR workforce report |
| Articulation + credit applicability | Transfer Associate | BOR Statewide Articulation; LCTCS 2+2 |
| Apprenticeship duration / journey credential | Apprenticeship | DOL Apprenticeship.gov / RAPIDS |
| Program length (weeks) + format | Certificate, Apprenticeship | Institution pages (Addendum); LCTCS |
| Stackability / IBC mapping | Certificate | LA LWC/LDOE IBC catalog; Credential Engine; LCTCS |
| BSN-specific program URL | 4-year | NOTION School Addendum (the PROG row matching SLU was a DNP cert — not used) |
| Real school logos at scale | All | curated per-UNITID asset store (favicon fallback implemented) |

---

## Display & missing-data rules (student-facing)

1. **No placeholders.** Any field without a source is omitted entirely — no `[DATA NOT FOUND]`, no empty rows.
2. **Earnings chart degrades** — full Y1/Y5/Y10 only where PSEO has it; otherwise the **LWC occupation-wage** chart, labeled occupation-level (BRCC samples).
3. **Cost helper** ("school hasn't published pricing — contact them") is retained for unreported short-term tuition — content, not a placeholder.
4. **PUMS** never appears below a bachelor's.
5. **2-year ≠ suppressed** *(v3.1)* — 2-year schools get the SAME About + Cost depth as 4-year: STR → "Open Admission", but the **grad-rate norm (sector-aware), transfer-out, enrollment/retention, and the full cost waterfall ARE shown**. Only residential **campus life** (no URL) and **PUMS** (non-bachelor's) are absent — by data, not by school type.
6. **Time vs completions** — time-to-degree is school-wide (CMPLTTD; applies to the transfer associate too); completers/year is program-specific (CMPLRACE); each is labeled with its scope.
7. **Advisor side keeps every gap visible** (`[DATA NOT FOUND — Advisor Verify]` in the Notion draft + Advisor Review Notes) so omission is never silently lossy.

---

## Build & integration
```bash
cd ui/advising-hub
npx @tailwindcss/cli -i ./src/css/main.css -o ./rec-demo-site/main.css --minify
npx shadow-cljs -A:dev release rec-demo      # → rec-demo-site/js/main.js
```
Apply `integration/rec-ui-redesign.patch` (`git apply`) + copy `assets/` into `rec-demo-site/assets/`.
Field names in `data.cljs` mirror the sources above → production wiring is field→source, not a rewrite.
The Transfer Plan `:outcomes` render is built but dormant — drop in a per-UNITID transfer-outcomes
file and it lights up.
