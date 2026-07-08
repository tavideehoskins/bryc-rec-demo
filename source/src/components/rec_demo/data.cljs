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
     • GRAD-RATE NORMING — sector-aware, carried per-school under each record's :grad-rate
       (see the norming note below); renders identically for 4-year and 2-year schools."
  ;; Pure data namespace — no presentation deps; the components assemble all display
  ;; strings (chart names, legends, copy) from these fields.
  )

;; ----------------------------------------------------------------------------
;; BRYC home region — the single source for the living-wage benchmark + "distance
;; from" framing. One place to change; every metro/state reference derives from it.
;; ----------------------------------------------------------------------------

(def home-metro "Baton Rouge")
(def home-state "Louisiana")
;; MIT Living Wage — single adult (shared benchmark; drives the cost-of-living bars).
(def living-wage-area-amount 45496)   ;; Baton Rouge MSA
(def living-wage-state-amount 42370)  ;; Louisiana

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
  [{:term "Median"
    :def "The middle value: half of people earn more, half earn less. A better gut-check than an average."}
   {:term "Living wage"
    :def (str "The income a single adult needs to cover basic costs (housing, food, "
              "transportation) in a given area — here, " home-metro ".")}
   {:term "Net cost"
    :def "What you actually pay after grants and scholarships — not the sticker price."}
   {:term "TOPS"
    :def "Louisiana's state scholarship that helps cover tuition for eligible in-state students (TOPS-Tech for 2-year/technical programs)."}
   {:term "Pell Grant"
    :def "A federal grant for students with financial need — money you don't repay."}])

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
;; GRAD-RATE NORMING — sector-aware and INSTITUTION-AGNOSTIC. The norm now lives on
;; each school record under :grad-rate, so every recommended school (4-year OR 2-year)
;; carries its own sourced rate + peer comparison; the component renders whatever is
;; present (no per-school branch). The mechanism is identical for any institution:
;;
;;   rate  = IPEDS gr2024 completers-within-150%-of-normal-time ÷ adjusted cohort
;;           (4-year cohort GRTYPE 8/13 → 4-yr rate; 2-year cohort GRTYPE 29/30 → 3-yr rate)
;;   peers = mean rate across SAME-SECTOR Louisiana public institutions
;;           (4-year: size-banded by ef2024a UG enrollment; 2-year: statewide)
;;   indicator = High ≥ +5 pts vs peer avg · Low ≤ −5 · Medium otherwise
;;
;; Worked values (see each school's :grad-rate):
;;   SLU  26.3% · band 10,000–20,000 {ULL 26.9, SLU 26.3, LA Tech 44.5} avg 32.6% → −6.3 → Low
;;   BRCC 31.0% (299/964) · LA public 2-yr {BRCC 31.0, River Parishes 29.6} avg 30.3% → +0.7 → Medium
;; CAVEAT (acquisition): the in-repo LA roster resolves only 2 public 2-years; completing
;; the LCTCS roster (bor_ipeds_bridge or IPEDS HD directory) widens the 2-year peer set.
;; ----------------------------------------------------------------------------

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
   :distance (str "~45 miles from " home-metro)   ;; "~45 miles" is per-school; home-metro is shared
   :living-wage-area home-metro                    ;; MIT living-wage benchmark metro (drives salary copy)
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
   ;; Grad-rate norm carried on the school (sector-aware; see norming note above).
   :grad-rate {:rate "26%" :rate-num 26.3
               :label "4-Year Graduation Rate"
               :normed-against "similar-size LA public 4-years"
               :peer-average "32.6" :peer-n 3 :delta "−6.3" :indicator "Low"
               :method (str "IPEDS gr2024 bachelor's 100%-time completers ÷ adjusted cohort (UNITID 160612 = 26.3%). "
                            "Peer avg = mean 4-yr rate across LA public 4-years in the same UG-enrollment band "
                            "(10,000–20,000: ULL 26.9, SLU 26.3, LA Tech 44.5 → 32.6). "
                            "Indicator: High ≥ +5 pts · Low ≤ −5 · Medium otherwise.")}
   :enrollment-total 13192          ;; ef2024a EFTOTLT
   ;; Dynamic enrollment box (Upgrade §4.6): ALL race counts carried; the box keys to the
   ;; STUDENT's own race, falling back to total enrollment for non-minority / race-not-given.
   :enrollment-races {"Black/African American"           {:count 2589 :pct "19.6%"}
                      "Hispanic"                         {:count 895  :pct "6.8%"}
                      "White"                            {:count 8311 :pct "63.0%"}
                      "Asian"                            {:count 240  :pct "1.8%"}
                      "American Indian/Alaska Native"    {:count 31   :pct "0.2%"}
                      "Native Hawaiian/Pacific Islander" {:count 5    :pct "0.0%"}
                      "Two or more races"                {:count 576  :pct "4.4%"}}

   ;; About-This-School narrative bullets
   :setting "a regional public university with a traditional, residential campus feel while staying close to home"
   :retention "71%"                 ;; ef2024d RET_PCF (shown as supporting context)

   ;; --- What It Costs You (school-anchored; recomputed on BOR FY26) ---
   :costs {:tuition 5777 :fees 3266 :tuition-fees 9043      ;; BOR FY26 (replaces IPEDS $8,373)
           :room-board-on 10240 :room-board-off 11812
           :living 10240 :living-label "Room & board"       ;; generic cost-of-attendance line (on-campus)
           :books 1300
           :coa 20583                                       ;; 9043 + 10240 + 1300
           :tops 5652 :tops-name "TOPS Opportunity"         ;; LOSFA OPH (current-year)
           :pell 7395                                        ;; pell-per-sem 3697.5 × 2
           :net 7536                                         ;; 20583 − 5652 − 7395
           :aid-covered 13047                                ;; coa − net (TOPS + Pell)
           ;; Average debt at graduation — College Scorecard MEDIAN DEBT OF COMPLETERS
           ;; (GRAD_DEBT_MDN): UNITID 160612 = $22,113 (from 5,945 completers). This is the
           ;; field the current $22,113 actually traces to — the old comment mislabeled it
           ;; "IPEDS" and it wasn't in any extracted repo file (only the Scorecard per-borrower
           ;; loan-average $5,368 was, which reads misleadingly low). Wiring: add GRAD_DEBT_MDN
           ;; to the Scorecard extract, keyed by UNITID (Upgrade §1 / debt-box decision).
           :avg-debt 22113
           :avg-debt-source "College Scorecard — aid.median_debt.completers.overall (GRAD_DEBT_MDN), UNITID 160612"
           ;; Net-price + financial-aid links from HD2024 (Upgrade §4.7) — the authoritative
           ;; IPEDS directory fields NPRICURL / FAIDURL (already https for SLU).
           :npc-url "https://www.southeastern.edu/admin/fin_aid/cost/net_price_calculator/index.html"  ;; HD2024 NPRICURL
           :faid-url "https://www.southeastern.edu/admin/fin_aid/"}                                     ;; HD2024 FAIDURL

   :pathways [:bsn]})

;; ============================================================================
;; PATHWAY — SLU BSN (everything discipline-specific is optional/conditional)
;; ============================================================================

(def bsn
  {:id "bsn"
   :name "Bachelor of Science in Nursing"
   :acronym "BSN"
   :track "Generic Baccalaureate Track"
   :credential-level "Bachelor's"      ;; chip 2 — award_level_name (Scorecard/IPEDS)
   :bachelors? true
   :cip "51.38"
   :primary-soc "29-1141"
   :field "Nursing"                     ;; chip 1 — short field from the CIP title (CIP 51.38 = Registered Nursing)
   :lwc-stars 5                         ;; chip 3 source — LWC demand rating for the primary SOC (louisiana_occupation_wages)
   ;; Chips are DERIVED per-pathway from the three fields above (see core/pathway-chips),
   ;; so a school with several programs shows each program's own field/credential/demand.
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
   ;; Raw sourced values only. The chart bar names, legend, banded sentence, and the
   ;; three tiles are ASSEMBLED in core/salary-section from these + school fields, so
   ;; nothing is hardwired to this school/metro/discipline.
   :salary
   {:earnings [{:label "1 Year Out"  :value 70105}     ;; PSEO 51.38 Baccalaureate (SLU), Y1 p50 (status 1)
               {:label "5 Years Out" :value 73162}]    ;; Y5 p50 — Upgrade §4.2: Y1 + Y5 (dropped Y10)
    :earnings-source "PSEO — 51.38 Baccalaureate, SLU (Y1/Y5 median, status=1)"
    :living-wage-area-value 45496       ;; MIT Living Wage — single adult, home metro
    :living-wage-state-value 42370      ;; MIT Living Wage — single adult, home state
    :living-wage-band "Above"           ;; computed: Y1 70,105 vs BR 45,496 = +54% → Above (>+15%)
    ;; Growth vs openings split (Upgrade §4.8), SOC 29-1141 (LWC):
    :growth-rate "+7.55%"               ;; growth_pct
    :growth-net-new "3,300"             ;; growth_10yr — NET NEW jobs
    :growth-openings "27,706"}          ;; total_openings — incl. replacement/turnover

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

;; ============================================================================
;; v3 — CTE / Short-Term / Scholarships samples (ADDITIVE; nothing above changed).
;; Every value sourced from the repo: louisiana_programs, louisiana_occupation_wages
;; (LWC), cip_soc_crosswalk, O*NET tasks, BOR CMPLTTD, career_onestop scholarships.
;; Unsourced fields are omitted (never placeheld). Per-record :sections drives which
;; pathway sections render; :salary uses :occupation (LWC) when there's no PSEO.
;; ============================================================================

;; --- Baton Rouge Community College (2-year / CTE home for the samples) --------
;; BRCC is sourced to PARITY with the 4-year school — every field below comes from the
;; same institution-agnostic datasets used for SLU (BOR FY26 fee detail, IPEDS gr2024 /
;; ef2024a / ef2024d), so the SAME About/Cost components render any 2-year the same way.
(def brcc
  {:unitid "437103"
   :name "Baton Rouge Community College"
   :short-name "BRCC"
   :type "Public 2-Year"
   :kind :two-year                     ;; open-admission (no ACT) → "Open Admission" badge; commuter cost copy
   :city "Baton Rouge" :state "LA"
   :location "Baton Rouge, LA"
   :distance "In Baton Rouge"          ;; distance_from_baton_rouge_miles = 0
   :sector "Public, 2-year"
   :setting "an open-admission community college with a commuter campus"
   :logo "assets/brcc-logo.jpg"        ;; official BRCC wordmark (Wikimedia)
   :website "https://www.mybrcc.edu"
   :website-label "mybrcc.edu"

   ;; --- About This School: 3-figure callout (grad rate · transfer-out · enrollment) ---
   ;; Grad-rate norm — sector-aware, computed exactly like the 4-year (see norming note).
   :grad-rate {:rate "31%" :rate-num 31.0
               :label "Graduation Rate (3-yr)"
               :normed-against "Louisiana public 2-year colleges"
               :peer-average "30.3" :peer-n 2 :delta "+0.7" :indicator "Medium"
               :method (str "IPEDS gr2024 2-year cohort: 299 completers within 150% of normal time (3 yrs) "
                            "÷ 964 adjusted cohort (UNITID 437103 = 31.0%). Peer avg = mean across LA public "
                            "2-year colleges {BRCC 31.0, River Parishes 29.6} = 30.3%. Indicator: High ≥ +5 pts "
                            "· Low ≤ −5 · Medium otherwise. Acquisition: complete the LCTCS roster to widen the peer set.")}
   ;; School-specific TRANSFER data — IPEDS gr2024 transfer-out (community-college mission).
   :transfer-out {:rate "17%" :rate-num 17.3 :count 167 :cohort 964
                  :timeframe "within 3 years"
                  :sub "≈167 of 964 first-time students transfer to continue elsewhere (within 3 yrs)"
                  :source "IPEDS Graduation Rates — transfer-out, 2024"}
   :enrollment-total 11182             ;; ef2024a EFTOTLT (UG)
   :enrollment-races {"Black/African American"           {:count 6359 :pct "56.9%"}
                      "Hispanic"                         {:count 684  :pct "6.1%"}
                      "White"                            {:count 3389 :pct "30.3%"}
                      "Asian"                            {:count 168  :pct "1.5%"}
                      "American Indian/Alaska Native"    {:count 42   :pct "0.4%"}
                      "Native Hawaiian/Pacific Islander" {:count 12   :pct "0.1%"}
                      "Two or more races"                {:count 313  :pct "2.8%"}}
   :retention "53%"                    ;; ef2024d RET_PCF

   ;; --- What It Costs You — waterfall, sourced from BOR/IPEDS (parity w/ 4-year) ---
   ;; Tuition & fees from BOR FY26 (authoritative, tuition+fees) — NOT Scorecard tuition-only.
   ;; Commuter school: OFF-CAMPUS LIVING IS EXCLUDED (an allowance, not a school charge), so
   ;; the COA shows school-charged DIRECT costs only (tuition, fees, books). TOPS-Tech + Pell
   ;; ($10,481) exceed those direct costs → estimated $0 out of pocket (:fully-covered?).
   :costs {:tuition-fees 4419 :tuition-fees-source "BOR FY26 (resident, tuition & fees)"
           :books 1300                                         ;; IPEDS/Scorecard books & supplies
           :coa 5719                                           ;; 4419 + 1300 (no living/room-board)
           :tops 3086 :tops-name "TOPS-Tech"                   ;; BOR FY26 TOPS column
           :pell 7395                                          ;; pell-per-sem 3697.5 × 2
           :net 0                                              ;; aid ($10,481) > direct costs → $0 out of pocket
           :aid-covered 5719                                   ;; aid covers all direct costs (capped at COA)
           :fully-covered? true                                ;; aid ≥ COA → "$0 direct cost" framing
           ;; Average debt at graduation — College Scorecard MEDIAN DEBT OF COMPLETERS
           ;; (GRAD_DEBT_MDN): UNITID 437103 = $12,450 (4,853 completers). SAME authoritative
           ;; source as the 4-year, so the debt box now renders for 2-years too (resolves the v3
           ;; omission — the Scorecard per-borrower loan-average $5,204 was the misleading field).
           :avg-debt 12450
           :avg-debt-source "College Scorecard — aid.median_debt.completers.overall (GRAD_DEBT_MDN), UNITID 437103"
           :commuter? true
           ;; Net-price + FAID links from HD2024 (Upgrade §4.7). BRCC's NPRICURL lacked a scheme
           ;; ("www.mybrcc.edu/...") — https:// PREPENDED per §4.7 (the defect that broke old links).
           :npc-url "https://www.mybrcc.edu/financial-aid/npc/index.html"     ;; HD2024 NPRICURL (scheme-normalized)
           :faid-url "https://www.mybrcc.edu/financial-aid/index.php"}})      ;; HD2024 FAIDURL (scheme-normalized)

;; --- Colleges tab · 2-year associate: BRCC Associate of Science in Nursing (ASN) ---
;; Name + acronym from BRCC's OWN catalog (mybrcc.edu ASN program page, ACEN-accredited) —
;; the IPEDS/Scorecard source only carries the generic CIP 51.3801 title ("Registered
;; Nursing/Registered Nurse") at award level "Associate's degree", so the institution's
;; actual degree name is used (BRCC confers an Associate of SCIENCE in Nursing, not an
;; "ADN"/Applied-Science). "ADN" is the generic profession-wide term, not BRCC's conferral.
(def asn
  {:id "asn" :school :brcc :type :as
   :name "Associate of Science in Nursing" :acronym "ASN"
   :track "Registered Nursing/Registered Nurse" :credential-level "Associate's"
   :cip "51.3801" :field "Nursing" :primary-soc "29-1141"
   :lwc-stars 5
   :completions {:per-year 131 :year "2024"}   ;; program-specific (BOR CMPLRACE, CIP 51.3801, BRCC)
   :sections [:overview :salary :careers :time-to-credential]
   :overview
   {:credential-line
    (str "The Associate of Science in Nursing (ASN) is a ~2-year community-college degree that "
         "qualifies you to sit for the NCLEX-RN and become a licensed Registered Nurse — "
         "the same licensure exam as the 4-year BSN.")
    :student-connection
    "Like the BSN, it leads straight into hands-on patient care — a faster, lower-cost route into nursing."
    ;; CONDITIONAL caveat — open-admission COLLEGE ≠ admission to the NURSING PROGRAM (a
    ;; separate competitive gate), the 2-year parallel to the SLU BSN caveat. Sourced from
    ;; BRCC's ASN admission requirements (mybrcc.edu ASN page: ~2.8 GPA, C+ in 16 prereq
    ;; credit hours, competitive — "meeting minimum admission requirements does not
    ;; guarantee acceptance"). Acquisition for production: per-program admission criteria.
    :caveat
    (str "Heads up: getting into BRCC and getting into its nursing program are two separate "
         "steps. BRCC is open-admission, but the ASN program is competitive — it expects about "
         "a 2.8 GPA and a C or better in 16 credit hours of prerequisites, and meeting the "
         "minimums doesn't guarantee a seat. Your grades count from day one.")
    :day-to-day
    ["Record patients' medical histories and symptoms and monitor their vital signs and condition."
     "Administer medications and treatments, then watch for and document patients' reactions."
     "Coordinate care with physicians and families and explain at-home recovery steps."]}
   :licensure-exam {:name "NCLEX-RN" :url "https://www.nclex.com/"
                    :note "the national exam every new RN must pass to be licensed (pass rate not in repo — advisor verify)"}
   ;; PSEO cascade (Upgrade §4.2): 6-digit 51.3801 absent → 4-digit 51.38 Associates at BRCC
   ;; IS published (Y1 $66,466 / Y5 $73,832, status 1). Program-level grad earnings are PREFERRED
   ;; over the LWC occupation wage — this corrects the v3 "BRCC not in PSEO" assumption (it was
   ;; only absent at the 6-digit level; the 4-digit cascade surfaces it).
   :salary {:earnings [{:label "1 Year Out"  :value 66466}
                       {:label "5 Years Out" :value 73832}]
            :earnings-source "PSEO — 51.38 Associates, BRCC (4-digit cascade; Y1/Y5 median, status=1)"
            :living-wage-area-value 45496 :living-wage-state-value 42370
            :living-wage-band "Above"           ;; $66,466 Y1 vs BR $45,496 = +46% → Above
            :growth-rate "+7.55%" :growth-net-new "3,300" :growth-openings "27,706"}   ;; SOC 29-1141 (LWC)
   :careers {:roles
             [{:title "Registered Nurse (RN)" :soc "29-1141.00"
               :desc "Provides direct patient care in hospitals, clinics, and surgical units. Requires passing the NCLEX-RN after graduation."}
              {:title "Operating Room Nurse" :soc "29-1141.00"
               :desc "Works in the surgical suite — prepping patients, monitoring during procedures, and managing the sterile field."}
              {:title "Intensive Care Unit (ICU) Nurse" :soc "29-1141.03"
               :desc "Cares for the most critically ill patients — high-stakes work needing close monitoring and fast judgment."}
              {:title "Nurse Practitioner (NP)" :soc "29-1171.00"
               :requirement "Requires a BSN, then an MSN or DNP"
               :desc "Diagnoses, prescribes, and treats patients. From the ASN, bridge to a BSN first, then graduate study."}
              {:title "Certified Registered Nurse Anesthetist (CRNA)" :soc "29-1151.00"
               :requirement "Requires a BSN, then a doctoral degree"
               :desc "Administers anesthesia — among the highest-earning nursing roles. Bridge to a BSN, then doctoral study."}]}
   :time-to-credential
   {:designed "~2 years full-time"
    :actual "≈4.6 years"
    :actual-note "School-wide average across ALL of BRCC's associate programs (program-specific timing isn't published) — first-time, full-time completers. Actual pace runs longer than the 2-year design because students often attend part-load or stop out. Source: LA Board of Regents time-to-degree (CMPLTTD), 2024-25."}})

;; --- Colleges tab · technical associate: BRCC Process Technology (CIP 15.0699) -
;; NAME OVERRIDE (Upgrade §4.3): the raw CIP title is "Industrial Production Technologies";
;; BRCC's catalog/profile name is "Process Technology" — the profile is the authoritative
;; display-name override (kept in :cip-title as the fallback). CROSSWALK CORRECTION (§4.5):
;; 15.0699 maps raw ONLY to Industrial Engineering Technologists (17-3026, $99,602, just 588
;; openings) — WRONG for this program, which feeds plant/process OPERATORS. Career Paths and
;; the growth/openings stat use the corrected, profile-verified target SOCs (51-8091/8093/9011).
(def process-tech
  {:id "process-tech" :school :brcc :type :aas
   :name "Process Technology"                          ;; §4.3 catalog/profile display override
   :cip-title "Industrial Production Technologies"      ;; raw CIP 15.0699 title (fallback when no override)
   :acronym "PTEC"
   :track "Industrial Production Technologies/Technicians" :credential-level "Associate's"
   :cip "15.0699" :field "Process Technology" :primary-soc "51-8091"
   :lwc-stars 5
   :completions {:per-year 29 :year "2024"}    ;; program-specific (BOR CMPLRACE, CIP 15.0699 Associate, BRCC)
   :sections [:overview :salary :careers :time-to-credential]
   :overview
   {:credential-line
    (str "A ~2-year associate that trains you to run and monitor the automated systems in "
         "chemical plants and refineries — the process operators who keep production running "
         "safely. A core feeder program for Louisiana's plants along the river corridor.")
    :student-connection nil :caveat nil
    :day-to-day
    ["Monitor and control continuous plant processes from a control room and in the field."
     "Adjust equipment, valves, and pumps to keep temperature, pressure, and flow in spec."
     "Inspect equipment and run safety and quality checks, logging readings each shift."]}
   ;; PSEO cascade (§4.2): 6-digit 15.0699 absent → 4-digit 15.06 Associates at BRCC IS published
   ;; (Y1 $75,398 / Y5 $124,426, status 1). Program earnings PREFERRED — and far higher than the
   ;; mis-mapped 17-3026 wage ($99,602) the old occupation lookup showed.
   :salary {:earnings [{:label "1 Year Out"  :value 75398}
                       {:label "5 Years Out" :value 124426}]
            :earnings-source "PSEO — 15.06 Associates, BRCC (4-digit cascade; Y1/Y5 median, status=1)"
            :living-wage-area-value 45496 :living-wage-state-value 42370
            :living-wage-band "Above"           ;; $75,398 Y1 vs BR $45,496 = +66% → Above
            ;; Demand/growth/openings from the CORRECTED target SOC 51-8091 (§4.5, §4.8):
            :growth-rate "+10.6%" :growth-net-new "342" :growth-openings "3,420"}
   ;; CORRECTED occupation targets (§4.5): plant/process operators, not the raw 17-3026 mapping.
   :careers {:roles [{:title "Chemical Plant & System Operators" :soc "51-8091.00"
                      :desc "Run and monitor the equipment that turns raw materials into chemicals — median $95,124 in Louisiana."}
                     {:title "Petroleum Pump System & Refinery Operators" :soc "51-8093.00"
                      :desc "Operate the units that refine crude oil into fuels and products — median $95,552."}
                     {:title "Chemical Equipment Operators & Tenders" :soc "51-9011.00"
                      :desc "Operate equipment that mixes, reacts, or processes chemicals — median $80,840."}]}
   :time-to-credential
   {:designed "~2 years full-time"
    :actual "≈4.6 years"
    :actual-note "School-wide average across ALL of BRCC's associate programs (program-specific timing isn't published) — first-time, full-time completers. Actual pace runs longer than the 2-year design because students often attend part-load or stop out. Source: LA Board of Regents time-to-degree (CMPLTTD), 2024-25."}})

;; --- Colleges tab · transfer associate: BRCC Business (Louisiana Transfer) ----
(def business-transfer
  {:id "business-transfer" :school :brcc :type :transfer
   :name "Business (Louisiana Transfer)" :acronym nil
   :track "Business/Commerce, General" :credential-level "Associate's (transfer-designed)"
   :cip "52.0101" :field "Business"
   :completions {:per-year 56 :year "2024"}    ;; program-specific (BOR CMPLRACE, CIP 52.0101 Associate, BRCC)
   :sections [:overview :transfer-plan :time-to-credential]
   :overview
   {:credential-line
    (str "A transfer-designed associate built to move toward a Business Administration "
         "bachelor's — you complete the first two years at community-college cost, then "
         "transfer to a 4-year.")
    :student-connection nil :caveat nil
    :day-to-day nil}
   :transfer-plan
   {:destination "Business Administration (BS)"
    :framing "2 + 2: finish the associate at BRCC, then transfer toward the bachelor's."
    ;; Louisiana statewide transfer-degree guarantee (LCTCS / BOR) — real statewide policy.
    :la-guarantee "Louisiana's statewide transfer degree (AALT/ASLT) guarantees your lower-division general-education credits transfer to any Louisiana public university."
    ;; Transfer→bachelor's COMPLETION outcomes are intentionally NOT shown: a school-specific
    ;; figure for BRCC isn't in the repo (national NSC numbers aren't true for this school).
    ;; To surface this, add a per-UNITID transfer-outcomes file (CCRC Tracking Transfer
    ;; institutional data / NSC StudentTracker) and the :outcomes render lights up. Acquisition.
    :articulation-note "Course articulation, credit-applicability, and transfer-completion rates aren't in the repo yet — your advisor maps them (BOR Statewide Articulation; CCRC/NSC transfer outcomes)."
    :cost-note "BRCC tuition now (~$3,237/yr); the bachelor's portion continues at a 4-year — full cost-to-degree depends on the destination you choose."
    :destination-careers
    [{:title "General & Operations Managers" :soc "11-1021.00" :median 101556 :stars 5
      :desc "Plan, direct, and coordinate the operations of a business or department. Shown at the bachelor's level (the transfer destination)."}]}
   ;; School-level time-to-degree applies to the transfer associate too (it IS a BRCC
   ;; associate) — same CMPLTTD institution figure as ASN/Industrial. Answers "why is
   ;; there no time under Time & Completion": the time cards now render with the 56 completions.
   :time-to-credential
   {:designed "~2 years full-time"
    :actual "≈4.6 years"
    :actual-note "School-wide average across ALL of BRCC's associate programs (program-specific timing isn't published) — first-time, full-time completers. Actual pace runs longer than the 2-year design because students often attend part-load or stop out. Source: LA Board of Regents time-to-degree (CMPLTTD), 2024-25."}})

;; --- Short-Term tab · Technical Diploma: BRCC Practical Nursing (LPN) ----------
;; BRCC confers a Technical Diploma (TD) in Practical Nursing — a five-semester, 59-credit
;; program (mybrcc.edu catalog), NCLEX-PN eligible → Licensed Practical Nurse. NOT a "certificate".
(def lpn
  {:id "lpn" :type :technical-diploma :provider "Baton Rouge Community College"
   :name "Licensed Practical/Vocational Nurse" :acronym "LPN"
   :credential-level "Technical Diploma" :location "Baton Rouge, LA"
   :cip "51.3901" :field "Nursing" :primary-soc "29-2061"
   :lwc-stars 4
   :info-url "https://mybrcc.edu/academics/nursing-and-allied-health/tdpracticalnursing.php"  ;; program_url
   :completions {:per-year 24 :year "2024"}    ;; program-specific (BOR CMPLRACE, CIP 51.3901, BRCC)
   ;; Designed (nominal) length only — a typical ACTUAL time-to-completion isn't in the repo for
   ;; sub-associate credentials: CMPLTTD (time-to-degree) has only Associate + Baccalaureate sheets.
   ;; Length sourced from BRCC's catalog (five-semester, 59-credit TD in Practical Nursing).
   :time-to-credential
   {:designed "5 semesters"
    :actual-note (str "Designed length is the nominal full-time program (BRCC catalog: a five-semester, "
                      "59-credit Technical Diploma in Practical Nursing). A typical actual time-to-completion "
                      "isn't shown — the state time-to-degree data (CMPLTTD) covers only associate and "
                      "bachelor's degrees, not certificate/diploma programs.")}
   :sections [:overview :salary :careers :time-to-credential :funding]
   :overview
   {:credential-line
    (str "A five-semester Technical Diploma that qualifies you to sit for the NCLEX-PN and work "
         "as a Licensed Practical Nurse — the fastest licensed-nursing entry point.")
    :student-connection
    "A quick, lower-cost way into hands-on patient care — and credits can later build toward an RN."
    :day-to-day
    ["Observe patients and chart and report changes such as adverse reactions to medication or treatment."
     "Measure and record vital signs — height, weight, temperature, blood pressure, pulse, respiration."
     "Administer prescribed medications or start IV fluids, noting times and amounts on patients' charts."]}
   :licensure-exam {:name "NCLEX-PN" :url "https://www.nclex.com/"
                    :note "the licensing exam for practical nurses (pass rate not in repo — advisor verify)"}
   ;; PSEO cascade (§4.2): 51.3901 → 51.39 Certificate 1-2yr at BRCC IS published
   ;; (Y1 $39,496 / Y5 $51,576, status 1). Program earnings preferred over the LWC wage.
   :salary {:earnings [{:label "1 Year Out"  :value 39496}
                       {:label "5 Years Out" :value 51576}]
            :earnings-source "PSEO — 51.39 Certificate 1-2yr, BRCC (4-digit cascade; Y1/Y5 median, status=1)"
            :living-wage-area-value 45496 :living-wage-state-value 42370
            :living-wage-band "Near"            ;; $39,496 Y1 vs BR $45,496 = −13% → Near (within ±15%)
            :growth-rate "+7.48%" :growth-net-new "1,439" :growth-openings "16,532"}   ;; SOC 29-2061
   :careers {:roles [{:title "Licensed Practical Nurse (LPN)" :soc "29-2061.00"
                      :desc "Provides basic nursing care under RNs and physicians. Requires passing the NCLEX-PN."}]}
   :funding {:tuition 4419 :tuition-flag "BOR FY26, tuition & fees"   ;; BOR FY26 (was Scorecard tuition-only)
             :pell 7395 :tops "TOPS-Tech eligible" :commuter? true}})

;; --- Short-Term tab · apprenticeship: Electrical (Baton Rouge Electrical JATC) -
(def electrical-apprenticeship
  {:id "electrical-appr" :type :apprenticeship :provider "Baton Rouge Electrical JATC"
   :name "Electrical Apprenticeship" :location "Baton Rouge, LA"
   :field "Electrical" :primary-soc "47-2111"     ;; CIP 99.0 (unmapped) → SOC from the trade
   :credential-level "Registered Apprenticeship" :lwc-stars 5
   ;; Verified BR Electrical JATC page (the data field's slug pointed to Alexandria).
   :info-url "https://apprenticeshipla.com/apprenticeships/baton-rouge-electrical-jatc/"
   ;; Apprenticeship wage progression IS in louisiana_programs for this JATC.
   :wage-progression {:start "$17.74/hr" :average "$28.16/hr"}
   :sections [:overview :salary :careers :earn]
   :overview
   {:credential-line
    (str "A registered apprenticeship: you're paid to work alongside licensed electricians "
         "while you train — earn while you learn, no tuition debt.")
    :student-connection nil
    :day-to-day
    ["Read blueprints and sketches to plan the layout of wiring and equipment to code."
     "Install conduit and pull insulated wiring through walls and concealed spaces."
     "Install, maintain, and repair electrical systems from ladders, scaffolds, and roofs."]}
   ;; No PSEO for a JATC (not a degree program) → LWC occupation median, labeled "median" (§4.4).
   :salary {:occupation {:soc-title "Electricians" :soc "47-2111"
                         :median 59259 :stars 5 :growth-pct "+11.7%" :net-new "1,384" :openings "12,294"
                         :education "High school diploma or equivalent (train on the job)"}
            :living-wage-band "Above"}   ;; $59,259 vs BR $45,496 = +30%
   :careers {:roles [{:title "Electrician" :soc "47-2111.00"
                      :desc "Installs, maintains, and repairs electrical systems in homes, businesses, and industrial sites."}]}
   :earn-while-learn? true
   ;; Wage progression, duration, and journey credential not in repo (DOL Apprenticeship.gov — advisor verify).
   })

;; --- Scholarships tab · one real row (career_onestop, LA-eligible, healthcare) -
;; In production the "why it fits" + tips come from the DSPy 5-phase pipeline
;; (ExplainScholarshipMatch); here they're a grounded match note over the real row.
(def jane-delano-scholarship
  {:id "jane-delano" :type :scholarship
   :name "Jane Delano Student Nurse Scholarships"
   :sponsor "American National Red Cross"
   :award "$3,000"
   :deadline "November 4"
   :target-levels "Bachelor, Master"
   :url "http://www.redcross.org"
   :eligibility "Red Cross volunteer or employee within the past 5 years; at least one year of college credits completed; currently enrolled in an accredited U.S. nursing program."
   :selection "Selection by the scholarship committee; preference to student-nurse volunteers."
   :why-fits "Matches your nursing pathway and healthcare interest — it's a nursing-specific award open to BSN students. Plan ahead: it expects Red Cross volunteer/employee experience and at least a year of college, so line up volunteering now."
   :tips ["Start (or log) Red Cross volunteer hours before you apply."
          "Apply once you've completed your first year in an accredited nursing program."
          "Lead with your patient-care and community-service experience."]})

(def career-mobility-scholarship
  {:id "career-mobility" :type :scholarship
   :name "Career Mobility Scholarships"
   :sponsor "Foundation of the National Student Nurses Association"
   :award "$1,000–$7,500"
   :deadline "January 24"
   :target-levels "Associate, Bachelor, Master"
   :url "http://www.forevernursing.org"
   :eligibility "Enrolled in a state-approved nursing program leading to an associate, baccalaureate, diploma, or direct-entry master's — or an RN-to-BSN completion program."
   :selection "Based on academic achievement, financial need, and involvement in nursing student organizations and community health activities."
   :why-fits "Works for any nursing route you choose — it explicitly covers associate-degree nursing, so it fits whether you start with the ASN or go straight to the BSN."
   :tips ["Note your financial need and any nursing-club or community-health involvement."
          "You're eligible once you're enrolled and matriculated in an approved nursing program."]})

(def flight-away-scholarship
  {:id "flight-away" :type :scholarship
   :name "A Flight Away"
   :sponsor "A Leap Ahead Foundation"
   :award "$2,000"
   :deadline "October 31"
   :target-levels "Associate, Bachelor, Master, Professional"
   :url "https://aleapaheadfoundation.org/"
   :eligibility "U.S. residents, 18+, enrolled or pursuing enrollment at a U.S. university in an associate, bachelor's, master's, or professional program."
   :selection "Evaluated on an essay response."
   :why-fits "A broad, no-GPA award open to any major and any level — a good catch-all to apply for alongside the nursing-specific scholarships."
   :tips ["Open to any field — apply regardless of your major."
          "It's decided on an essay, so put real effort into a strong personal statement."]})

;; --- Tab registry -------------------------------------------------------------
;; Colleges = schools (each with pathways). Short-Term = standalone programs.
;; Scholarships = scholarship cards.
(def colleges-schools [school brcc])
(def college-pathways {"160612" pathways                       ;; SLU
                       "437103" [asn process-tech business-transfer]})  ;; BRCC
(def short-term-programs [lpn electrical-apprenticeship])
(def scholarship-list [jane-delano-scholarship career-mobility-scholarship flight-away-scholarship])
