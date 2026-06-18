(ns components.rec-demo.data
  "Hardcoded demo content for the rec-redesign PROTOTYPE — v2 (SLU BSN exemplar).

   Source of truth: bryc_exemplar (SLU BSN) + the v2 Build Plan & Obney Spec.
   Field names mirror the Obney spec so production wiring is a direct map. The
   production app sources these from Notion / BOR / IPEDS / PSEO / LWC / IPUMS;
   the prototype hardcodes verified exemplar values.

   Transferability (Build Plan §5/§6): every discipline-specific field is OPTIONAL
   and omitted when nil (caveat, licensure-exam, PUMS for non-bachelor's, etc.),
   so the same components render any bachelor's pathway by swapping this data.

   Both previously-flagged items now follow the plan exactly:
     • SECTION DISPLAY ORDER — Build Plan §3 order, in `section-order`.
     • 4-YR GRAD-RATE NORMING — spec bands/thresholds, computed in `grad-rate-norm`."
  (:require [components.rec-demo.charts :as charts]))

;; ----------------------------------------------------------------------------
;; Student profile / intake
;; ----------------------------------------------------------------------------

(def student
  {:name "Arianna Johnson"
   :first-name "Arianna"
   :placeholder? true
   :act 20
   :gpa "3.1"
   :tops "Opportunity"
   :interests "Healthcare, wanting to help people"
   ;; Race-aware enrollment branch (Build Plan §3.5). Arianna is in a minority
   ;; racial group at SLU, so she sees own-group-vs-total. TODO(verify): student
   ;; race is inferred from the v7 exemplar framing, not an explicit intake field.
   :race-group "Black/African American"
   :minority? true})

;; ----------------------------------------------------------------------------
;; Advisor card — login-driven (Build Plan §2). Prototype = the logged-in creator.
;; Heading label follows the creator's role: advisor → "Advisor Messages".
;; ----------------------------------------------------------------------------

(def advisor
  {:messages-label "Advisor Messages"          ;; "Counselor Messages" when a Senior Counselor builds it
   :name "Tavidee Hoskins"
   ;; TODO(verify): exact title string from the BRYC Team sheet (col Title).
   :title "BRYC Senior Advisor"
   :email "tavidee@thebryc.org"
   :appointment-url "https://calendar.app.google/QMgRhuFJTedbwBub6"
   :headshot "assets/advisor-tavidee.png"      ;; Team Headshots_BRYC App → "Tavidee Hoskins"
   :note (str "Based on your interest in healthcare and wanting to make a direct "
              "impact on people's lives, this pathway is a strong fit for your "
              "profile. It leads to real career outcomes with strong financial "
              "returns in Louisiana. Review each section below, then let's talk "
              "through your questions at our next meeting.")})

;; ----------------------------------------------------------------------------
;; "Terms to Know" + Safety/Target/Reach legend (Build Plan §2)
;; ----------------------------------------------------------------------------

(def str-legend
  [{:key "safety" :label "Safety"  :color "#2f9e44"
    :def "Your GPA/ACT are above average for incoming students here."}
   {:key "target" :label "Target"  :color "#e8a93b"
    :def "Your GPA/ACT are similar to incoming students here."}
   {:key "reach"  :label "Reach"   :color "#c92a4a"
    :def "Your GPA/ACT are below average for incoming students here."}])

(def terms-to-know
  [{:term "Safety / Target / Reach"
    :def "How your academics compare to students this school usually admits — above average (Safety), similar (Target), or below (Reach)."}
   {:term "Median"
    :def "The middle value: half of people earn more, half earn less. A better gut-check than an average."}
   {:term "Living wage"
    :def "The income a single adult needs to cover basic costs (housing, food, transportation) in a given area — here, Baton Rouge."}])

;; ----------------------------------------------------------------------------
;; Safety/Target/Reach classifier (Obney spec — ACT vs school 25th/75th)
;; Safety = ACT ≥ 75th · Target = 25th–75th · Reach = < 25th · Open = no ACT.
;; ----------------------------------------------------------------------------

(defn classify-str [student-act act-25 act-75]
  (cond
    (nil? act-25)            {:key "open"   :label "Open Admission" :color "#676868"}
    (>= student-act act-75)  {:key "safety" :label "Safety" :color "#2f9e44"}
    (>= student-act act-25)  {:key "target" :label "Target" :color "#e8a93b"}
    :else                    {:key "reach"  :label "Reach"  :color "#c92a4a"}))

;; ----------------------------------------------------------------------------
;; 4-year grad-rate norming — Obney spec bands/thresholds, COMPUTED exactly.
;; Method: gr2024 bachelor's cohort (GRTYPE 8 adjusted cohort, GRTYPE 13 = completers
;; within 100% normal time) ÷ → 4-yr rate; UG enrollment via ef2024a (EFALEVEL=2,
;; LINE=99) sets the size band; band average is the mean 4-yr rate across LA public
;; 4-years (bor_ipeds_bridge) in the SAME band. Indicator: High ≥ +5 pts vs band
;; avg · Low ≤ −5 · Medium otherwise.
;;   SLU 4-yr 26.3% · band 10,000–20,000 {ULL 26.9, SLU 26.3, LA Tech 44.5} avg 32.6%
;;   → 26.3 − 32.6 = −6.3 pts → Low.
;; ----------------------------------------------------------------------------

(def grad-rate-norm
  {:status :final
   :size-bands ["<5,000" "5,000–10,000" "10,000–20,000" ">20,000"]
   :slu-band "10,000–20,000"           ;; SLU UG enrollment 13,192
   :slu-rate 26.3
   :band-average 32.6
   :band-members 3
   :delta -6.3
   :indicator "Low"                    ;; ≤ −5 pts vs band average
   :threshold "High ≥ +5 pts vs band avg · Low ≤ −5 pts · Medium otherwise"})

;; ----------------------------------------------------------------------------
;; Per-pathway SECTION DISPLAY ORDER — Build Plan §3 order (1–4). Sections 5 & 6
;; are school-anchored (render once per school, after the pathway-specific 1–4).
;; ----------------------------------------------------------------------------

(def section-order
  [:overview        ;; 1
   :whats-cool      ;; 2
   :salary          ;; 3
   :careers])       ;; 4  (school-anchored 5 About-This-School + 6 Costs follow)

;; ============================================================================
;; SCHOOL (school-anchored data — keyed by UNITID, shared across all pathways)
;; ============================================================================

(def school
  {:unitid "160612"
   :name "Southeastern Louisiana University"
   :short-name "Southeastern"
   :type "Public 4-Year"
   :city "Hammond" :state "LA"
   :location "Hammond, LA"
   :distance "~45 miles from Baton Rouge"
   :website "https://www.southeastern.edu"
   :website-label "southeastern.edu"
   ;; Verified working campus-life page (web, Jun 2026).
   :campus-life-url "https://www.southeastern.edu/campus_life/"
   ;; Official Southeastern wordmark (shield + name), fetched from Wikimedia.
   :logo "assets/slu-logo.png"

   ;; --- Admissions (Obney spec: louisiana_programs) ---
   :act-25 18 :act-75 24
   :acceptance "99%"                 ;; acceptance_rate 0.9898 (was mislabeled 94% in v1)

   ;; --- About This School: 3-figure callout (Build Plan §3.5) ---
   :grad-rate-4yr "26%"              ;; gr2024 · 100%-time bachelor's completers · UNITID 160612 (26.3%)
   :enrollment-total 13192          ;; ef2024a EFTOTLT
   :enrollment-group-label "Black/African American"
   :enrollment-group-count 2586     ;; ef2024a EFBKAAT (≈19.6% of total)
   :enrollment-group-pct "19.6%"

   ;; About-This-School narrative bullets
   :setting "a regional public university with a traditional, residential campus feel while staying close to home"
   :retention "71%"                 ;; ef2024d RET_PCF (shown as supporting context)

   ;; --- What It Costs You (school-anchored; recomputed on BOR FY26) ---
   :costs {:tuition 5777 :fees 3266 :tuition-fees 9043      ;; BOR FY26 (replaces IPEDS $8,373)
           :room-board-on 10240 :room-board-off 11812
           :books 1300
           :coa 20583                                       ;; 9043 + 10240 + 1300
           :tops 5652 :tops-name "TOPS Opportunity"         ;; LOSFA OPH (current-year)
           :pell 7395                                        ;; pell-per-sem 3697.5 × 2
           :net 7536                                         ;; 20583 − 5652 − 7395
           :aid-covered 13047                                ;; coa − net (TOPS + Pell)
           ;; Average debt at graduation = IPEDS institution-wide cumulative figure
           ;; (the standard "debt at graduation"). The Scorecard loan-average-amount
           ;; ($5,368) is a narrower per-borrower field and reads misleadingly low,
           ;; so it's NOT used. No peer cumulative-debt source exists for a comparison.
           :avg-debt 22113
           ;; SLU's dedicated Net Price Calculator page is currently 404 on their own
           ;; site (both /index.html and the directory). Link the WORKING cost &
           ;; financial-aid page instead. TODO(Cameron): swap to the live NPC tool
           ;; URL once SLU restores it (federal law requires every school have one).
           :npc-url "https://www.southeastern.edu/admin/fin_aid/cost/"}

   :pathways [:bsn]})

;; ============================================================================
;; PATHWAY — SLU BSN (everything discipline-specific is optional/conditional)
;; ============================================================================

(def bsn
  {:id "bsn"
   :name "Bachelor of Science in Nursing"
   :acronym "BSN"
   :track "Generic Baccalaureate Track"
   :credential-level "Bachelor's"
   :bachelors? true
   :cip "51.38"
   :primary-soc "29-1141"
   :tags ["Nursing" "Bachelor's" "Safety net: high LA demand"]
   ;; TODO(verify): exact BSN program page (School Addendum). The louisiana_programs
   ;; row that matched SLU was a DNP cert, not the BSN — do NOT use it. Linked to
   ;; the school site as a stand-in until the BSN program URL is confirmed.
   :program-url "https://www.southeastern.edu"
   :program-url-confirmed? false

   ;; Licensure/exam — CONDITIONAL (omit for pathways with none).
   :licensure-exam {:name "NCLEX-RN"
                    :url "https://www.nclex.com/"
                    :note "the national exam every new RN must pass to be licensed"}

   ;; --- Section 1: Overview — exactly 6 bullets ---
   :overview
   {:credential-line
    (str "The Bachelor of Science in Nursing (BSN) is a 4-year degree that prepares "
         "you to become a licensed Registered Nurse (RN) — the professional who "
         "provides direct patient care in hospitals, clinics, and surgical settings "
         "alongside doctors.")
    :student-connection
    (str "It lines up directly with your interest in healthcare and helping people: "
         "this is hands-on work supporting patients through the hardest moments of "
         "their lives, every day.")
    ;; CONDITIONAL caveat (Pathway Profile) — omit entirely if nil.
    :caveat
    (str "Heads up: being admitted to the university and being admitted to the "
         "nursing program are two separate steps. University admission is open, but "
         "the nursing major is competitive — your GPA matters from day one.")
    ;; Bullets 4–6: exactly 3 day-to-day tasks (O*NET top tasks by importance, SOC 29-1141).
    :day-to-day
    ["Record patients' medical histories and symptoms, and monitor their vital signs and condition."
     "Administer medications and treatments, then watch for and document patients' reactions."
     "Coordinate care with physicians and families, and explain what to do for recovery at home."]}

   ;; --- Section 2: What's Cool About This Program — ≤4 qualitative points ---
   ;; (School Addendum: CDS + official site only. No quantitative data here.)
   :whats-cool
   [{:tag "Pass Rate"
     :title "97% NCLEX-RN First-Attempt Pass Rate"
     :body (str "Above the Louisiana state average, and a 7× Louisiana Nightingale "
                "Award winner — including 2024 and 2025.")
     :accent "border-l-[#05a09c] bg-[#d0ecef]/60"}
    {:tag "Clinical Location"
     :title "Clinical Training in Your Hometown"
     :body (str "The final 3 semesters of nursing coursework happen at the SLU Baton "
                "Rouge Center on Essen Lane — not in Hammond — so your clinicals are "
                "close to home.")
     :accent "border-l-[#007f81] bg-[#b1e1e9]/50"}
    {:tag "Direct Admit"
     :title "EDGE Program — Direct Admission to Nursing"
     :body (str "Seniors with a 3.7+ GPA and 25+ ACT who apply by January 15 are "
                "guaranteed a nursing seat as long as they meet progression "
                "requirements.")
     :accent "border-l-[#40bfbb] bg-[#d0ecef]/60"}
    {:tag "Scholarships"
     :title "Program-Specific Nursing Scholarships"
     :body (str "Nursing students can apply for awards like the LaNeaf Lilburn "
                "\"Lee\" Jones Scholarship in Nursing and the Mary Seal & W.K. "
                "\"Bill\" Carlile Endowed Scholarship.")
     :accent "border-l-[#2a6465] bg-[#d0ecef]/40"}]

   ;; --- Section 3: Salary & Job Opportunities ---
   :salary
   {:chart-data
    [{:name "1 Year Out"  :value 70105 :label "$70,105" :fill charts/color-earnings}
     {:name "5 Years Out" :value 73162 :label "$73,162" :fill charts/color-earnings}
     {:name "10 Years Out" :value 85313 :label "$85,313" :fill charts/color-earnings}
     {:name "Living Wage —\nBaton Rouge\n(Single Adult)" :value 45496 :label "$45,496" :fill charts/color-col}
     {:name "Living Wage —\nLouisiana\n(Single Adult)"   :value 42370 :label "$42,370" :fill charts/color-col}]
    :chart-legend
    [{:color charts/color-earnings :label "Southeastern BSN graduate earnings (median)"}
     {:color charts/color-col :label "Living wage for a single adult"}]
    ;; Living-wage band (Obney spec): Y1 $70,105 vs BR $45,496 = +54% → Above (>+15%).
    :living-wage-band "Above"
    :living-wage-sentence
    (str "Based on first-year earnings, graduates of this program tend to earn "
         "Above a living wage in Baton Rouge.")
    :tiles
    [{:kind :stars :value 5 :label "Job Demand (LWC)"
      :detail "5-star = high demand — significantly more employers than qualified candidates in Louisiana."}
     {:kind :stat :value "+7.55%" :label "10-Year Growth"
      :detail "About 27,706 total openings projected statewide — steady, reliable hiring."}
     {:kind :summary :value "Strong & stable"
      :label "Bottom line"
      :detail "High demand, pay well above a living wage, and clear room to grow into higher-paying nursing roles."}]}

   ;; --- Section 4: Career Paths (O*NET-linked; advanced-cred flagged) ---
   :careers
   {:roles
    [{:title "Registered Nurse (RN)" :soc "29-1141.00"
      :desc "Provides direct patient care in hospitals, clinics, and surgical units. Requires passing the NCLEX-RN after graduation."}
     {:title "Operating Room Nurse" :soc "29-1141.00"
      :desc "Works inside the surgical suite — prepping patients, monitoring during procedures, and managing the sterile field with the team."}
     {:title "Intensive Care Unit (ICU) Nurse" :soc "29-1141.03"
      :desc "Cares for the most critically ill patients, including post-surgical recovery. High-stakes work needing close monitoring and fast judgment."}
     {:title "Nurse Practitioner (NP)" :soc "29-1171.00"
      :requirement "Requires an MSN or DNP after your BSN"
      :desc "Can diagnose, prescribe, and treat patients independently. Specialties include Family, Psychiatric-Mental Health, Pediatric, and Women's Health."}
     {:title "Certified Registered Nurse Anesthetist (CRNA)" :soc "29-1151.00"
      :requirement "Requires a doctoral degree after your BSN"
      :desc "Administers anesthesia in surgical settings — among the highest-earning nursing roles. Most programs want at least a year of ICU experience first."}]

    ;; PUMS "what degree-holders actually do" — bachelor's only (real IPUMS data).
    ;; degfieldd 6107 (Nursing), scope=Louisiana (total_in_field 38,185 ≥ 30),
    ;; top 5 by weighted_n. in-field (CIP→SOC) is dominated by RN (62.8%), so the
    ;; <50% callout does NOT fire here — but the flag is wired for other pathways.
    :pums
    {:scope "Louisiana"
     :total-in-field 38185
     :in-field-pct 63
     :under-50-callout? false
     ;; :onet = O*NET-SOC code (with .00) when a single occupation page exists;
     ;; nil for broad census buckets with no single O*NET page (rendered unlinked).
     :occupations
     [{:title "Registered Nurses" :onet "29-1141.00" :pct "62.8%" :median 70000 :in-field? true}
      {:title "Nurse Practitioners" :onet "29-1171.00"
       :pct "8.1%" :median 113232 :in-field? true
       :note "Census aggregate (SOC 29-11) — predominantly advanced-practice nurses; linked to the O*NET Nurse Practitioner profile."}
      {:title "Medical & Health Services Managers" :onet "11-9111.00" :pct "2.9%" :median 107184 :in-field? false}
      {:title "Postsecondary Teachers" :onet nil :pct "2.1%" :median 60000 :in-field? false}
      {:title "Nurse Anesthetists" :onet "29-1151.00" :pct "1.9%" :median 191164 :in-field? true}]}}})

;; A school may host multiple pathways; the demo has one. Sections 1–4 render per
;; pathway; sections 5–6 render once from `school`.
(def pathways [bsn])
