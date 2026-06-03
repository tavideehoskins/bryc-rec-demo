(ns components.rec-demo.data
  "Hardcoded demo content for the rec-redesign PROTOTYPE.

   Source of truth: bryc_exemplar_v7.docx (SLU BSN). Do NOT invent, summarize, or
   paraphrase — every value below is copied from the exemplar / redesign plan.
   The production app sources this from Notion (Credential Foundation, School
   Addendum, School Profile); the prototype does not call Notion."
  (:require [components.rec-demo.charts :as charts]))

;; ----------------------------------------------------------------------------
;; Student context + advisor note (per-student, shown first)
;; ----------------------------------------------------------------------------

(def student
  {:name "Arianna Johnson"
   :placeholder? true
   :act "20"
   :gpa "3.1"
   :tops "Opportunity"
   :interests "Healthcare, wanting to help people"})

(def advisor-note
  (str "Based on your interest in healthcare and wanting to make a direct impact "
       "on people's lives, these programs are strong fits for your profile. Each "
       "one leads to real career outcomes with strong financial returns in "
       "Louisiana. Review each program, then let's talk through your questions at "
       "our next meeting."))

;; ----------------------------------------------------------------------------
;; Chart data
;; ----------------------------------------------------------------------------

(def salary-data
  [{:name "1 Year Out"                          :value 70105 :label "$70,105" :fill charts/color-brand}
   {:name "5 Years Out"                         :value 73162 :label "$73,162" :fill charts/color-brand}
   {:name "10 Years Out"                        :value 85313 :label "$85,313" :fill charts/color-brand}
   {:name "Cost to Live in\nBaton Rouge\n(Single Adult)" :value 45496 :label "$45,496" :fill charts/color-brand}
   {:name "Cost to Live in\nLouisiana\n(Single Adult)"   :value 42370 :label "$42,370" :fill charts/color-brand}])

(def salary-source
  (str "Sources: PSEO 2025Q4, U.S. Census Bureau (CIP 51.38, Registered Nursing, "
       "Baccalaureate, All Cohorts, 2,887 graduates) — lehd.ces.census.gov/data/pseo  |  "
       "MIT Living Wage Calculator, Feb 2026 — Baton Rouge $45,496/yr; Louisiana $42,370/yr "
       "(livingwage.mit.edu)"))

(def salary-legend
  [{:color charts/color-brand
    :label "Southeastern BSN graduate earnings (PSEO 2025Q4 — CIP 51.38, All Cohorts)"}])

;; Step-down waterfall as solid descending running-total bars (matches reference
;; PNG). NOTE: the "After Scholarships" step from the PNG is intentionally OMITTED
;; per the plan — it is illustrative (CareerOneStop), not real per-student data.
;;   Total CoA 19,913 → After TOPS 14,261 → After Pell 6,866 → Net 5,366
(def waterfall-data
  [{:name "Total Cost\nof Attendance" :value 19913 :label "$19,913" :fill charts/color-brand}
   {:name "After TOPS\nOpportunity"   :value 14261 :label "$14,261" :fill charts/color-brand}
   {:name "After\nPell Grant"         :value 6866  :label "$6,866"  :fill charts/color-brand}
   {:name "Your Estimated\nNet Cost"  :value 5366  :label "$5,366"  :fill charts/color-net}])

(def waterfall-legend
  [{:color charts/color-brand
    :label "Tuition & Fees $8,373 + Room & Board $10,240 + Books $1,300"}
   {:color charts/color-net
    :label "Your estimated net cost after all aid"}])

(def waterfall-source
  "IPEDS 2024-25 · LOSFA/SLU 2025-26 · Pell estimate (max annual)")

;; ----------------------------------------------------------------------------
;; Expected Outcomes — stat grid (LWC, SOC 29-1141 Registered Nurses)
;; ----------------------------------------------------------------------------

(def expected-outcomes-stats
  [{:label "Louisiana Median Wage"        :value "$76,636"  :sub "per year"}
   {:label "Starting Range (lowest 10%)"  :value "$60,722"  :sub "per year"}
   {:label "Top Earners (highest 10%)"    :value "$101,650" :sub "per year"}
   {:label "RNs Employed in Louisiana"    :value "43,736"   :sub "current"}
   {:label "Projected RNs by 2032"        :value "47,036"   :sub "+3,300 new jobs"}
   {:label "10-Year Growth Rate"          :value "7.55%"    :sub "27,706 total openings"}])

(def expected-outcomes-source
  "Louisiana Workforce Commission, SOC 29-1141")

;; ----------------------------------------------------------------------------
;; What's Cool About This Program — differentiator callouts (NEW section)
;; ----------------------------------------------------------------------------

(def whats-cool-callouts
  [{:tag "Pass Rate"
    :title "97% NCLEX-RN First-Attempt Pass Rate"
    :body (str "Above the Louisiana state average. 7× Louisiana Nightingale Award "
               "winner, including 2024 and 2025.")
    :accent "border-l-emerald-400 bg-emerald-50/60"}
   {:tag "Clinical Location"
    :title "Clinical Training in Your Hometown"
    :body (str "Final 3 semesters of nursing coursework at the SLU Baton Rouge Center "
               "on Essen Lane — not in Hammond. Clinical training happens in your hometown.")
    :accent "border-l-blue-400 bg-blue-50/60"}
   {:tag "Fast-Track"
    :title "EDGE Program — Direct Admission to Nursing"
    :body (str "Seniors with a 3.7+ GPA and 25+ ACT who apply by January 15 are "
               "guaranteed a nursing seat if they meet progression requirements.")
    :accent "border-l-violet-400 bg-violet-50/60"}
   {:tag "Scholarships"
    :title "Program Scholarships"
    :body (str "LaNeaf — Lilburn \"Lee\" Jones Scholarship in Nursing  ·  "
               "LaNeaf — Mary Seal and W.K. \"Bill\" Carlile Endowed Scholarship.")
    :link {:label "See Scholarships tab for full list →" :href "#"}
    :accent "border-l-amber-400 bg-amber-50/60"}])

;; ----------------------------------------------------------------------------
;; Program-level prose sections
;; ----------------------------------------------------------------------------

(def overview-bullets
  ["4-year degree that prepares you to become a licensed Registered Nurse (RN) — the professional who provides direct patient care in hospitals, clinics, and surgical settings alongside doctors."
   "If you want a career where you are genuinely helping people through the hardest moments of their lives, this is a direct path to that work every day."
   "The BSN alone does not license you to practice. You also pass the NCLEX-RN — the national exam every new RN must clear to become licensed. Passing is what makes you a registered nurse."])

(def career-summary-bullets
  ["Being an RN is a qualification, not a single job — your day depends entirely on where you practice."
   "Day-to-day: Record vitals and patient info. Administer medications and monitor reactions. Track and report changes in condition. Coordinate care with physicians and families."
   "Operating Room Nurse: Inside the surgical suite — prep patients before surgery, monitor during procedures, manage the sterile field alongside the team."
   "ICU Nurse: Care for the most critically ill patients including post-surgical recovery. High-stakes, high-judgment work with close, constant monitoring."
   "Important: Getting into SLU and getting into the nursing program are two separate steps. University admission is open — the nursing major is competitive. GPA matters from day one."
   "A BSN opens doors to advanced practice with additional degrees: Nurse Practitioner (NP), Certified Registered Nurse Anesthetist (CRNA), Clinical Nurse Specialist, Nurse Midwife."])

(def career-paths
  [{:title "Registered Nurse (RN)"
    :desc "Licensed to provide direct patient care in hospitals, clinics, and surgical units. Requires passing the NCLEX-RN after graduation."}
   {:title "Operating Room Nurse"
    :desc "Works inside the surgical suite preparing patients, monitoring during procedures, and maintaining the sterile field alongside the surgical team."}
   {:title "Intensive Care Unit (ICU) Nurse"
    :desc "Cares for the most critically ill patients, including post-surgical recovery. High-stakes clinical environment requiring close monitoring and fast judgment."}
   {:title "Nurse Practitioner (NP)"
    :requirement "Requires MSN or DNP after BSN"
    :desc "Can diagnose, prescribe, and treat patients independently. Specialties include Family, Psychiatric-Mental Health, Pediatric, and Women's Health."}
   {:title "Certified Registered Nurse Anesthetist (CRNA)"
    :requirement "Requires doctoral degree after BSN"
    :desc "Administers anesthesia in surgical and procedural settings. Among the highest-earning professions in nursing. Most programs require at least one year of ICU experience to apply."}])

;; ----------------------------------------------------------------------------
;; School-level sections (static across every program at the school)
;; ----------------------------------------------------------------------------

(def why-fits-stats
  [{:label "Acceptance Rate"            :value "94%"}
   {:label "4-Year Grad Rate"           :value "25%"}
   {:label "6-Year Grad Rate"           :value "44%"}
   {:label "6-Yr Grad · Black students" :value "33%"}
   {:label "First-Year Return"          :value "71%"}
   {:label "Black Enrollment"           :value "19.6%"}])

(def why-fits-bullets
  ["Located about 45 miles from Baton Rouge — a regional public university offering a distinct campus setting while staying close to home."
   "ACT of 20 is above their 25th percentile (18) and below the 75th (24) — admission is extremely likely."
   "Total enrollment: 13,192 undergraduates. Black/African American students make up 19.6% of the student body — about 1 in 5."])

(def financial-aid-bullets
  ["Annual tuition and fees: $8,373"
   "TOPS Opportunity covers $5,652 → remaining tuition: $2,721"
   "Pell Grant (estimated max $7,395) covers remaining $2,721 tuition; $4,674 applied toward housing"
   "Net tuition after TOPS + Pell: $0"
   "On-campus room and board: $10,240/yr · Books and supplies: $1,300/yr"
   "Estimated total annual net cost: ~$5,366 (primarily room, board, and books after aid)"
   "Average student debt at graduation: $22,113"])

(def net-price-calculator-url
  "https://www.southeastern.edu/admin/fin_aid/cost/net_price_calculator/index.html")

;; ----------------------------------------------------------------------------
;; School + program shape (one school, one program for the demo; structured as
;; collections so the hierarchy renders identically with more)
;; ----------------------------------------------------------------------------

(def school
  {:name "Southeastern Louisiana University"
   :type "Public 4-Year"
   :location "Hammond, LA"
   :distance "~45 miles from Baton Rouge"
   :acceptance "94%"
   :credentials ["BSN"]
   :programs
   [{:id "bsn"
     :title "Bachelor of Science in Nursing (BSN)"
     :track "Generic Baccalaureate Track"
     :credential "Bachelor's"
     :category "Safety"}]})
