(ns components.rec-demo.core
  "PROTOTYPE — Rec UI redesign demo page, v2 (route: /rec-demo).

   v2 restructure (per BRYC_Rec_v2_Build_Plan.md):
     • Two-column page: LEFT = Advisor Messages (note + login-driven advisor card);
       RIGHT = 'Check these out:' (Safety/Target/Reach legend, Pathway
       Recommendations, school tiles, Terms to Know).
     • School tile → 'Learn More' → per-pathway sections as a one-at-a-time
       accordion (Overview · What's Cool · Salary & Job Opportunities · Career
       Paths) followed by the SCHOOL-anchored sections (About This School · What
       It Costs You) rendered once per school.
     • TEAL branding; 'Pathway' (not 'Program') throughout; no 'new' labels.

   All content is hardcoded (components.rec-demo.data). Every discipline-specific
   field is conditional so the same components generalize to any bachelor's
   pathway. Section display order and grad-rate norming both follow the plan
   exactly (see data.cljs `section-order` and `grad-rate-norm`)."
  (:require [uix.core :as uix :refer [defui $ use-state]]
            [clojure.string :as str]
            [components.rec-demo.data :as data]
            [components.rec-demo.charts :as charts]
            ["/gen/shadcn/components/ui/card" :as card]
            ["/gen/shadcn/components/ui/badge" :as badge]
            ["/gen/shadcn/components/ui/accordion" :as accordion]))

;; ============================================================================
;; Small shared primitives
;; ============================================================================

(defn onet-url [soc] (str "https://www.onetonline.org/link/summary/" soc))

;; School logo — CONSISTENTLY ATTAINABLE for any school: a curated asset when one
;; exists (:logo), otherwise a favicon derived from the school's website domain
;; (works for every school with a domain — no per-school hand-fetching).
;; Production-grade path: a curated per-UNITID logo store; this is the fallback.
(defn favicon-url [website]
  (when (seq website)
    (let [domain (-> website (str/replace #"^https?://" "") (str/replace #"^www\." "")
                     (str/split #"/") first)]
      (str "https://icons.duckduckgo.com/ip3/" domain ".ico"))))

(defui ext-link
  "Inline external link: hover tooltip (title), opens in a new tab (Build Plan §4)."
  [{:keys [href label title class]}]
  ($ :a {:href href :target "_blank" :rel "noopener noreferrer"
         :title (or title href)
         :class (str "text-[#007f81] font-semibold hover:underline underline-offset-2 "
                     "decoration-[#40bfbb] " class)}
     label))

(defui section-eyebrow
  [{:keys [label]}]
  ($ :div {:class "flex items-center mb-4"}
     ($ :div {:class "w-2 h-2 bg-[#05a09c] rounded-full mr-3"})
     ($ :h3 {:class "text-sm font-semibold uppercase tracking-wide text-[#2a6465] font-head"} label)))

(defui bullet-list
  [{:keys [bullets dot]}]
  ($ :ul {:class "space-y-3"}
     (for [[i b] (map-indexed vector (remove nil? bullets))]
       ($ :li {:key i :class "flex text-sm md:text-base text-[#313335] leading-relaxed"}
          ($ :span {:class "mr-3 text-[#05a09c] shrink-0"} (or dot "•"))
          ($ :span b)))))

(defui stars
  [{:keys [n size color]}]
  ($ :div {:class (str "flex items-center gap-0.5 " (or size "text-xl"))}
     (for [i (range 5)]
       ($ :span {:key i :class (if (< i n) (or color "text-[#05a09c]") "text-white/40")} "★"))))

(defui str-badge
  [{:keys [classification]}]
  ($ :span {:class "inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold text-white"
            :style {:background-color (:color classification)}}
     (:label classification)))

(defui read-more
  "Shows `head` items, then a Read More / Read Less toggle revealing `tail`."
  [{:keys [head tail label-more label-less]}]
  (let [[open? set-open!] (use-state false)]
    ($ :div
       head
       (when open? tail)
       (when tail
         ($ :button {:class "mt-3 text-sm font-semibold text-[#007f81] hover:underline underline-offset-2"
                     :on-click #(set-open! not)}
            (if open? (or label-less "Read Less ▲") (or label-more "Read More ▼")))))))

;; ============================================================================
;; Section 1 — Overview (exactly 6 bullets; caveat conditional)
;; ============================================================================

(defui overview-section [{:keys [pathway]}]
  (let [{:keys [credential-line student-connection caveat day-to-day]} (:overview pathway)
        lic (:licensure-exam pathway)]
    ($ :div {:class "space-y-4"}
       ($ bullet-list
          {:bullets (concat [credential-line student-connection caveat] day-to-day)})
       (when lic
         ($ :p {:class "text-sm text-[#313335] leading-relaxed bg-[#d0ecef]/50 rounded-lg p-3"}
            "The degree alone doesn't license you — you also pass the "
            ($ ext-link {:href (:url lic) :label (:name lic)
                         :title (str (:name lic) " — " (:note lic))})
            ", " (:note lic) ".")))))

;; ============================================================================
;; Section 2 — What's Cool About This Pathway (≤4 qualitative callouts)
;; ============================================================================

(defui callout-card [{:keys [tag title body accent]}]
  ($ :div {:class (str "border-l-4 rounded-r-xl shadow-sm p-4 " accent)}
     ($ :div {:class "text-[11px] font-semibold uppercase tracking-wide text-[#676868] mb-1"} tag)
     ($ :div {:class "text-sm md:text-base font-semibold text-[#2a6465] mb-1 font-head"} title)
     ($ :p {:class "text-sm text-[#313335] leading-relaxed"} body)))

(defui whats-cool-section [{:keys [pathway]}]
  ($ :div {:class "grid grid-cols-1 md:grid-cols-2 gap-3"}
     (for [[i c] (map-indexed vector (take 4 (:whats-cool pathway)))]
       ($ callout-card (assoc c :key i)))))

;; ============================================================================
;; Section 3 — Salary & Job Opportunities (chart + banded sentence + 3 tiles)
;; ============================================================================

(defui salary-tile [{:keys [kind value label detail]}]
  (let [hot? (= kind :stars)]
    ($ :div {:class (str "rounded-xl shadow-sm p-4 flex flex-col gap-1.5 "
                         (if hot? "bg-[#05a09c] ring-1 ring-[#05a09c]"
                                  "bg-[#f3fafb] ring-1 ring-[#cdeaee]"))}
       (if hot?
         ($ stars {:n value :size "text-3xl" :color "text-white"})
         ($ :div {:class "text-2xl font-bold text-[#2a6465] font-head"} value))
       ($ :div {:class (str "text-xs font-semibold uppercase tracking-wide "
                            (if hot? "text-white" "text-[#676868]"))} label)
       ($ :div {:class (str "text-xs leading-relaxed " (if hot? "text-white/90" "text-[#313335]"))} detail))))

(defn- demand-word [stars]
  (cond (>= stars 4) "High demand" (= stars 3) "Steady demand" :else "Some demand"))

(defui salary-section
  "Two modes: PSEO program-earnings chart when :earnings is present (e.g. SLU BSN);
   otherwise an LWC occupation-wage view, clearly labeled (CTE programs with no PSEO)."
  [{:keys [pathway school]}]
  (let [s (:salary pathway)
        area (or (:living-wage-area school) data/home-metro)
        usd charts/usd
        band (:living-wage-band s)
        band-color (case band "Above" "#2f9e44" "Below" "#c92a4a" "#e8a93b")]
    (if (:earnings s)
      ;; ---- PSEO mode (program-level earnings) ----
      (let [stars (or (:lwc-stars pathway) 0)
            chart-data
            (concat
              (for [e (:earnings s)]
                {:name (:label e) :value (:value e) :label (usd (:value e)) :fill charts/color-earnings})
              [{:name (str "Living Wage —\n" area "\n(Single Adult)")
                :value (:living-wage-area-value s) :label (usd (:living-wage-area-value s)) :fill charts/color-col}
               {:name (str "Living Wage —\n" data/home-state "\n(Single Adult)")
                :value (:living-wage-state-value s) :label (usd (:living-wage-state-value s)) :fill charts/color-col}])
            demand  (demand-word stars)
            wage    (case band "Above" "earn above a living wage" "Near" "earn near a living wage"
                               "Below" "earn below a living wage" "have competitive earnings")
            verdict (cond (and (>= stars 4) (= band "Above")) "Strong & stable"
                          (>= stars 3) "Solid" :else "Mixed")
            tiles [{:kind :stars :value stars :label "Job Demand (LWC)"
                    :detail (str "LWC rates this " stars " of 5 — " (.toLowerCase demand) " in " data/home-state ".")}
                   {:kind :stat :value (:growth-rate s) :label "10-Year Growth"
                    :detail (str "About " (:growth-openings s) " openings projected statewide — steady, reliable hiring.")}
                   {:kind :summary :value verdict :label "Bottom line"
                    :detail (str demand ". Graduates " wage ", with clear room to grow into higher-paying roles.")}]]
        ($ :div {:class "space-y-5"}
           ($ charts/salary-vs-col-chart
              {:title "Median Earnings vs. Cost of Living"
               :legend-items [{:color charts/color-earnings
                               :label (str (:short-name school) " " (:acronym pathway) " graduate earnings (median)")}
                              {:color charts/color-col :label "Living wage for a single adult"}]
               :y-max 100000
               :data chart-data})
           ($ :div {:class "rounded-xl bg-[#d0ecef]/50 p-4 text-sm md:text-base text-[#313335] leading-relaxed"}
              "Based on first-year earnings, graduates of this pathway tend to earn "
              ($ :span {:class "font-bold" :style {:color band-color}} band)
              (str " a living wage in " area "."))
           ($ :div {:class "grid grid-cols-1 md:grid-cols-3 gap-3"}
              (for [[i t] (map-indexed vector tiles)]
                ($ salary-tile (assoc t :key i))))))
      ;; ---- LWC occupation mode (no PSEO for this school) ----
      ;; Earnings chart degrades to a single occupation-wage bar vs. the living wage
      ;; (Build Plan §5.2), clearly labeled occupation-level.
      (let [o (:occupation s)
            stars (or (:stars o) 0)
            demand (demand-word stars)
            chart-data
            ;; short bar name (full occupation title is in the legend) to avoid x-axis overlap
            [{:name "Typical Wage" :value (:median o) :label (usd (:median o)) :fill charts/color-earnings}
             {:name (str "Living Wage —\n" area "\n(Single Adult)")
              :value data/living-wage-area-amount :label (usd data/living-wage-area-amount) :fill charts/color-col}
             {:name (str "Living Wage —\n" data/home-state "\n(Single Adult)")
              :value data/living-wage-state-amount :label (usd data/living-wage-state-amount) :fill charts/color-col}]]
        ($ :div {:class "space-y-4"}
           ($ :div {:class "text-xs italic text-[#676868]"}
              (str "Occupation-level wage (Louisiana Workforce Commission) — program-level "
                   "(PSEO) earnings aren't published for this school, so this shows the typical "
                   "statewide wage for " (:soc-title o) " (" (:education o) ")."))
           ($ charts/salary-vs-col-chart
              {:title "Typical Wage vs. Cost of Living"
               :legend-items [{:color charts/color-earnings :label (str (:soc-title o) " — typical Louisiana wage")}
                              {:color charts/color-col :label "Living wage for a single adult"}]
               :y-max 110000
               :data chart-data})
           ($ :div {:class "rounded-xl bg-[#d0ecef]/50 p-4 text-sm md:text-base text-[#313335] leading-relaxed"}
              "Based on the typical Louisiana wage for this occupation, workers tend to earn "
              ($ :span {:class "font-bold" :style {:color band-color}} band)
              (str " a living wage in " area "."))
           ($ :div {:class "grid grid-cols-1 md:grid-cols-2 gap-3"}
              ($ salary-tile {:key 0 :kind :stars :value stars :label "Job Demand (LWC)"
                              :detail (str "LWC rates this " stars " of 5 — " (.toLowerCase demand) " in " data/home-state ".")})
              ($ salary-tile {:key 1 :kind :stat :value (:growth-pct o) :label "10-Year Growth"
                              :detail (str "About " (:openings o) " openings projected statewide.")})))))))

;; ============================================================================
;; Section 4 — Career Paths (O*NET-linked + advanced-cred flags + PUMS)
;; ============================================================================

(defui career-row [{:keys [title soc requirement desc]}]
  ($ :div {:class "bg-[#f3fafb] rounded-xl shadow-sm ring-1 ring-[#cdeaee] p-4"}
     ($ :div {:class "flex flex-wrap items-center gap-2 mb-1"}
        ($ ext-link {:href (onet-url soc) :label title
                     :title (str "O*NET career profile — " title)
                     :class "text-base font-head"})
        (when requirement
          ($ :span {:class "inline-block rounded-full border border-[#e8a93b] bg-[#fff7e6] text-[#9a6a00] text-[10px] font-semibold px-2 py-0.5"}
             requirement)))
     ($ :p {:class "text-sm text-[#313335] leading-relaxed"} desc)))

(defui pums-subsection [{:keys [pums field]}]
  ($ :div {:class "bg-[#f2f3f4] rounded-xl p-4 space-y-3"}
     ($ :div
        ($ :div {:class "text-sm font-semibold text-[#2a6465] font-head"}
           "What degree-holders actually do")
        ($ :p {:class "text-xs text-[#676868]"}
           (str "Where " (:scope pums) " " (.toLowerCase (or field "")) "-degree holders actually work "
                "(field-of-degree data, not just this school).")))
     (when (:under-50-callout? pums)
       ($ :div {:class "rounded-lg bg-[#fff7e6] border border-[#e8a93b] p-3 text-sm text-[#9a6a00]"}
          "Heads up: fewer than half of graduates in this field work in a directly-related job — worth talking through what that means for your plan."))
     ($ :div {:class "space-y-2"}
        (for [[i o] (map-indexed vector (:occupations pums))]
          ($ :div {:key i :class "flex items-center justify-between gap-3 bg-white rounded-lg px-3 py-2 ring-1 ring-[#cdeaee]"}
             ($ :div {:class "min-w-0"}
                (if (:onet o)
                  ($ ext-link {:href (onet-url (:onet o)) :label (:title o)
                               :title (str "O*NET — " (:title o)) :class "text-sm"})
                  ($ :span {:class "font-semibold text-sm text-[#313335]"} (:title o)))
                (when (:note o)
                  ($ :div {:class "text-[11px] text-[#676868]"} (:note o))))
             ($ :div {:class "text-right shrink-0"}
                ($ :div {:class "text-sm font-bold text-[#2a6465] font-head"} (:pct o))
                ($ :div {:class "text-[11px] text-[#676868]"}
                   (str "~$" (.toLocaleString (:median o) "en-US") "/yr"))))))))

(defui careers-section [{:keys [pathway]}]
  (let [roles (:roles (:careers pathway))
        pums (:pums (:careers pathway))
        base (vec roles)
        head-roles (subvec base 0 (min 3 (count base)))
        tail-roles (when (> (count base) 3) (subvec base 3))]
    ($ :div {:class "space-y-4"}
       ($ read-more
          {:head ($ :div {:class "space-y-3"}
                    (for [[i r] (map-indexed vector head-roles)] ($ career-row (assoc r :key i))))
           :tail (when tail-roles
                   ($ :div {:class "space-y-3 mt-3"}
                      (for [[i r] (map-indexed vector tail-roles)] ($ career-row (assoc r :key (+ 100 i))))))
           :label-more "Show advanced-practice paths ▼"
           :label-less "Show fewer ▲"})
       ;; PUMS — bachelor's only (Build Plan §4 / spec)
       (when (and (:bachelors? pathway) pums)
         ($ pums-subsection {:pums pums :field (:field pathway)})))))

;; ============================================================================
;; Section 5 — About This School (school-anchored; 3-figure callout)
;; ============================================================================

(defui callout-figure
  "indicator = optional {:label :color} pill (e.g. grad-rate norm High/Medium/Low)."
  [{:keys [value label sub indicator]}]
  ($ :div {:class "bg-white rounded-xl shadow-sm ring-1 ring-[#bcdfe5] p-4 text-center"}
     ($ :div {:class "text-2xl md:text-3xl font-bold text-[#2a6465] font-head"} value)
     ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mt-1"} label)
     (when indicator
       ($ :div {:class "mt-2 inline-block rounded-full px-2 py-0.5 text-[10px] font-bold"
                :style {:background-color (str (:color indicator) "1f")
                        :color (:color indicator)}}
          (:label indicator)))
     (when sub ($ :div {:class "text-xs text-[#676868] mt-1 leading-snug"} sub))))

(defui about-school-section [{:keys [school student]}]
  (let [cls (data/classify-str (:act student) (:act-25 school) (:act-75 school))
        gnorm data/grad-rate-norm
        gcolor (case (:indicator gnorm) "High" "#2f9e44" "Low" "#c92a4a" "#676868")
        total (.toLocaleString (:enrollment-total school) "en-US")
        enroll-fig (if (:minority? student)
                     {:value (:enrollment-group-pct school)
                      :label (str (:enrollment-group-label school) " enrollment")
                      :sub (str (.toLocaleString (:enrollment-group-count school) "en-US")
                                " of " total " students")}
                     {:value total :label "Total enrollment"})]
    ($ :div {:class "space-y-4"}
       ($ :div {:class "grid grid-cols-1 md:grid-cols-3 gap-3"}
          ($ callout-figure {:value (:acceptance school) :label "Acceptance Rate"})
          ($ callout-figure {:value (:grad-rate-4yr school) :label "4-Year Graduation Rate"
                             :indicator {:label (:indicator gnorm) :color gcolor}
                             :sub (str (:delta gnorm) " pts vs. similar-size LA publics (avg " (:band-average gnorm) "%)")})
          ($ callout-figure enroll-fig))
       ($ bullet-list
          {:bullets
           [($ :span nil
               (str "Located " (:distance school) " — " (:setting school) ". See ")
               ($ ext-link {:href (:campus-life-url school) :label "campus life"
                            :title (str (:short-name school) " — campus life")})
               ".")
            (str "Your ACT of " (:act student) " falls in this school's middle 50% (25th–75th: "
                 (:act-25 school) "–" (:act-75 school) "), so you're a " (:label cls)
                 " here — admission is very likely and a good academic match.")]}))))

;; ============================================================================
;; Section 6 — What It Costs You (school-anchored; recomputed waterfall)
;; ============================================================================

(defui costs-section [{:keys [school]}]
  (let [c (:costs school)
        usd (fn [n] (str "$" (.toLocaleString n "en-US")))]
    ($ :div {:class "space-y-4"}
       ($ charts/financial-aid-waterfall
          {:title "Your Estimated Annual Cost — After Financial Aid"
           :legend-items [{:color charts/color-earnings :label (str "Tuition & fees " (usd (:tuition-fees c)) " + Room & board " (usd (:room-board-on c)) " + Books " (usd (:books c)))}
                          {:color charts/color-net :label "Your estimated net cost after TOPS + Pell"}]
           :y-max 22000
           :data [{:name "Total Cost\nof Attendance" :value (:coa c) :label (usd (:coa c)) :fill charts/color-earnings}
                  {:name (str "After\n" (:tops-name c)) :value (- (:coa c) (:tops c)) :label (usd (- (:coa c) (:tops c))) :fill charts/color-earnings}
                  {:name "Your Net Cost\n(after Pell)" :value (:net c) :label (usd (:net c)) :fill charts/color-net}]})
       ($ bullet-list
          {:bullets
           [(str "The full cost to attend for one year is about " (usd (:coa c))
                 " — tuition, fees, housing, and books.")
            (str (:tops-name c) " plus the Pell Grant cover about " (usd (:aid-covered c)) " of that.")
            (str "That leaves an estimated net cost of about " (usd (:net c)) " for the year.")
            (str "Average student debt at graduation is about " (usd (:avg-debt c)) ".")]})
       ($ :div {:class "rounded-xl bg-[#fff7e6] border border-[#e8a93b] p-4"}
          ($ :p {:class "text-sm text-[#9a6a00] leading-relaxed"}
             ($ :span {:class "font-bold"} "This is an estimate only. ")
             "You must complete the "
             ($ ext-link {:href "https://studentaid.gov" :label "FAFSA"
                          :title "studentaid.gov — complete the FAFSA"})
             " and receive your award letter to know your true cost."))
       ($ ext-link {:href (:npc-url c) :label (str "See " (:short-name school) "'s cost & financial aid info →")
                    :title (str (:short-name school) " — Cost & Financial Aid")}))))

;; ============================================================================
;; v3 per-type section renderers (CTE)
;; ============================================================================

(defui time-to-credential-section [{:keys [pathway]}]
  ;; School-level time-to-degree (BOR CMPLTTD, when available) + PROGRAM-level
  ;; completions/year (BOR CMPLRACE, by CIP). Each card only shows when its data exists.
  (let [t (:time-to-credential pathway)
        c (:completions pathway)
        cards (cond-> []
                t (into [[(:designed t) "Designed length"] [(:actual t) "Typical actual"]])
                c (conj [(str (:per-year c)) "Graduates / year"]))
        cols (case (count cards) 1 "grid-cols-1" 2 "grid-cols-2" "grid-cols-3")]
    ($ :div {:class "space-y-3"}
       ($ :div {:class (str "grid gap-3 " cols)}
          (for [[i [v lbl]] (map-indexed vector cards)]
            ($ :div {:key i :class "bg-[#f3fafb] rounded-xl shadow-sm ring-1 ring-[#cdeaee] p-4 text-center"}
               ($ :div {:class "text-2xl font-bold text-[#2a6465] font-head"} v)
               ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mt-1"} lbl))))
       (when (:actual-note t)
         ($ :p {:class "text-xs text-[#676868] leading-relaxed"} (:actual-note t)))
       (when c
         ($ :p {:class "text-xs text-[#676868] leading-relaxed"}
            (str "Graduates / year is program-specific — " (:per-year c) " completers in " (:year c)
                 " (BOR completions by CIP, CMPLRACE)."))))))

(defui transfer-plan-section [{:keys [pathway]}]
  (let [tp (:transfer-plan pathway)
        o (:outcomes tp)]
    ($ :div {:class "space-y-4"}
       ($ :div {:class "rounded-xl bg-[#d0ecef]/50 p-4"}
          ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mb-1"} "Transfer destination")
          ($ :div {:class "text-base font-semibold text-[#2a6465] font-head"} (:destination tp))
          ($ :p {:class "text-sm text-[#313335] mt-1 leading-relaxed"} (:framing tp)))
       (when (:la-guarantee tp)
         ($ :div {:class "rounded-xl bg-[#f3fafb] ring-1 ring-[#cdeaee] p-3 text-sm text-[#313335] leading-relaxed"}
            ($ :span {:class "font-semibold text-[#2a6465]"} "Louisiana guarantee: ") (:la-guarantee tp)))
       ;; Transfer→bachelor's outcomes (national; sourced & annotated)
       (when o
         ($ :div {:class "space-y-2"}
            ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868]"}
               "How transfer students do (national)")
            ($ :div {:class "grid grid-cols-3 gap-2"}
               (for [[i [v note]] (map-indexed vector [[(:transfer-rate o) (:transfer-rate-note o)]
                                                       [(:transfer-bachelors o) (:transfer-bachelors-note o)]
                                                       [(:overall o) (:overall-note o)]])]
                 ($ :div {:key i :class "bg-[#f3fafb] rounded-xl shadow-sm ring-1 ring-[#cdeaee] p-3 text-center"}
                    ($ :div {:class "text-xl font-bold text-[#2a6465] font-head"} v)
                    ($ :div {:class "text-[11px] text-[#676868] mt-1 leading-snug"} note))))
            ($ :p {:class "text-[11px] italic text-[#676868] leading-relaxed"} (:source o))))
       ($ bullet-list {:bullets [(:cost-note tp) (:articulation-note tp)]})
       (when (seq (:destination-careers tp))
         ($ :div {:class "space-y-2"}
            ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868]"}
               "Where it leads (at the bachelor's)")
            (for [[i r] (map-indexed vector (:destination-careers tp))]
              ($ career-row (assoc r :key i))))))))

(defui funding-section [{:keys [pathway]}]
  (let [f (:funding pathway)
        usd (fn [n] (str "$" (.toLocaleString n "en-US")))]
    ($ :div {:class "space-y-3"}
       ($ bullet-list
          {:bullets
           [(str "Tuition: about " (usd (:tuition f)) "/year (" (:tuition-flag f) ").")
            (str "Pell Grant: up to " (usd (:pell f)) "/year for eligible students.")
            (str (:tops f) (when (:commuter? f) "; commuter — no room & board in this estimate."))]})
       ($ :div {:class "rounded-xl bg-[#fff7e6] border border-[#e8a93b] p-4"}
          ($ :p {:class "text-sm text-[#9a6a00] leading-relaxed"}
             ($ :span {:class "font-bold"} "Estimate only. ")
             "Complete the "
             ($ ext-link {:href "https://studentaid.gov" :label "FAFSA"
                          :title "studentaid.gov — complete the FAFSA"})
             " to confirm your aid.")))))

(defui earn-section [{:keys [pathway]}]
  (let [w (:wage-progression pathway)]
    ($ :div {:class "space-y-3"}
       ($ :div {:class "rounded-xl bg-[#d0ecef]/50 p-4 text-sm md:text-base text-[#313335] leading-relaxed"}
          ($ :span {:class "font-semibold text-[#2a6465]"} "Earn while you learn. ")
          "You're paid as an apprentice from day one — no tuition debt.")
       (when w
         ($ :div {:class "grid grid-cols-2 gap-3"}
            ($ :div {:class "bg-[#f3fafb] rounded-xl shadow-sm ring-1 ring-[#cdeaee] p-4 text-center"}
               ($ :div {:class "text-2xl font-bold text-[#2a6465] font-head"} (:start w))
               ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mt-1"} "Starting wage"))
            ($ :div {:class "bg-[#f3fafb] rounded-xl shadow-sm ring-1 ring-[#cdeaee] p-4 text-center"}
               ($ :div {:class "text-2xl font-bold text-[#2a6465] font-head"} (:average w))
               ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mt-1"} "Average wage"))))
       ($ :p {:class "text-xs text-[#676868] leading-relaxed"}
          "Program length and the journey credential depend on the sponsor (your advisor can confirm via DOL Apprenticeship.gov)."))))

;; ============================================================================
;; Per-pathway accordion (one-at-a-time) + school-anchored sections
;; ============================================================================

(defui section-item [{:keys [value heading children tint?]}]
  ;; tint? = soft teal wash for SCHOOL-anchored sections, to set them apart from
  ;; the white program/pathway sections.
  ($ accordion/AccordionItem
     {:value value
      :class (str "rounded-xl shadow-sm ring-1 border-0 px-4 "
                  (if tint? "bg-[#dceef1] ring-[#a9d8de]" "bg-white ring-[#bcdfe5]"))}
     ($ accordion/AccordionTrigger {:class "hover:no-underline py-4"}
        ($ :span {:class "text-base font-semibold text-[#2a6465] text-left font-head"} heading))
     ;; px-1.5 so inner box rings/shadows clear AccordionContent's overflow-hidden
     ;; (shadcn applies overflow-hidden for the open/close animation).
     ($ accordion/AccordionContent {:class "pb-5 pt-1 px-1.5"} children)))

(def pathway-section-meta
  {:overview           {:heading "Overview"                      :render overview-section}
   :whats-cool         {:heading "What's Cool About This Pathway" :render whats-cool-section}
   :salary             {:heading "Salary & Job Opportunities"    :render salary-section}
   :careers            {:heading "Career Paths"                  :render careers-section}
   :time-to-credential {:heading "Time & Completion"            :render time-to-credential-section}
   :transfer-plan      {:heading "Transfer Plan"                 :render transfer-plan-section}
   :funding            {:heading "Cost & Funding"               :render funding-section}
   :earn               {:heading "Earn While You Learn"         :render earn-section}})

(defui pathway-sections
  "The pathway-specific sections (1–4) as a one-at-a-time accordion, Build Plan §3 order.
   school/student are threaded through so school-aware sections (e.g. salary copy) can
   self-assemble their strings rather than hardcode them."
  [{:keys [pathway school student]}]
  ;; Per-pathway :sections drives which sections show (CTE programs differ from the
  ;; 4-year); falls back to the global section-order so the existing SLU BSN is unchanged.
  ($ accordion/Accordion {:type "single" :collapsible true :default-value "overview"
                          :class "space-y-3"}
     (for [k (or (:sections pathway) data/section-order)
           :let [{:keys [heading render]} (pathway-section-meta k)]]
       ($ section-item {:key (name k) :value (name k) :heading heading}
          ($ render {:pathway pathway :school school :student student})))))

(defn pathway-chips
  "Chips DERIVED per-pathway from sourced fields, so a school with several programs
   shows each program's own descriptors (Build Plan §6 transferability):
     • field        — short discipline from the program CIP title
     • credential   — award_level_name (Bachelor's / Associate / Certificate)
     • demand       — CONDITIONAL on the primary SOC's LWC star rating
   The demand chip varies program-to-program (different CIP → SOC → rating) and is
   omitted when demand is unremarkable."
  [{:keys [field credential-level lwc-stars]}]
  (->> [field
        credential-level
        (cond
          (>= (or lwc-stars 0) 4) "High demand"
          (= lwc-stars 3)         "Steady demand"
          :else                   nil)]
       (remove nil?)))

(defui pathway-row
  "A recommended pathway that unfurls its own details when clicked (v1 model).
   Multiple pathways can sit under one school; each unfurls independently, while
   About This School + What It Costs You are anchored once to the school below."
  [{:keys [pathway school student default-open?]}]
  (let [[open? set-open!] (use-state (boolean default-open?))]
    ($ :div {:class "bg-white rounded-xl shadow-sm ring-1 ring-[#bcdfe5] overflow-hidden"}
       ($ :button {:class "w-full flex items-center justify-between gap-3 p-4 text-left hover:bg-[#f3fafb] transition-colors"
                   :on-click #(set-open! not)}
          ($ :div {:class "min-w-0"}
             ($ :div {:class "font-semibold text-[#2a6465] font-head"}
                (str (:name pathway) (when (:acronym pathway) (str " (" (:acronym pathway) ")"))))
             ($ :div {:class "text-sm text-[#676868] mt-0.5"} (:track pathway))
             ;; Per-pathway chips (field · credential · demand) — derived, not hardcoded.
             ($ :div {:class "flex flex-wrap gap-2 mt-2"}
                (for [t (pathway-chips pathway)]
                  ($ :span {:key t :class "rounded-full bg-[#d0ecef] text-[#007f81] text-xs font-medium px-2.5 py-0.5"} t))))
          ($ :span {:class "shrink-0 text-sm font-semibold text-[#007f81]"}
             (if open? "Hide ▲" "View pathway ▼")))
       (when open?
         ($ :div {:class "px-3 md:px-4 pb-4 pt-2 bg-[#f2f3f4]"}
            ($ pathway-sections {:pathway pathway :school school :student student}))))))

;; CTE schools (2-year) get a MINIMAL About + simple cost — no STR/grad/campus (§5.4).
(defui cte-about-section [{:keys [school]}]
  ($ bullet-list
     {:bullets
      [(str (:name school) " is a " (:sector school) " in " (:location school) ".")
       (str "Open-admission and commuter-friendly — " (:distance school) ".")]}))

(defui cte-costs-section [{:keys [school]}]
  (let [c (:costs school)
        usd (fn [n] (str "$" (.toLocaleString n "en-US")))]
    ($ :div {:class "space-y-3"}
       ($ bullet-list
          {:bullets
           [(str "Tuition: about " (usd (:tuition c)) "/year (" (:tuition-flag c) ").")
            (str "Pell Grant: up to " (usd (:pell c)) "/year for eligible students.")
            (str (:tops c) "; as a Title IV school, federal aid applies.")
            "Commuter program — no room & board in this estimate."]})
       ($ :div {:class "rounded-xl bg-[#fff7e6] border border-[#e8a93b] p-4"}
          ($ :p {:class "text-sm text-[#9a6a00] leading-relaxed"}
             ($ :span {:class "font-bold"} "Estimate only. ")
             "Complete the "
             ($ ext-link {:href "https://studentaid.gov" :label "FAFSA"
                          :title "studentaid.gov — complete the FAFSA"})
             " to confirm your aid and net cost.")))))

(defui school-sections
  "School-anchored sections — rendered ONCE per school. CTE (2-year) gets minimal
   About + simple cost; universities get the full callout + cost waterfall."
  [{:keys [school student]}]
  (let [cte? (= (:kind school) :cte)]
    ($ accordion/Accordion {:type "multiple" :default-value #js ["about-school" "costs"]
                            :class "space-y-3"}
       ($ section-item {:value "about-school" :heading "About This School" :tint? true}
          (if cte? ($ cte-about-section {:school school})
                   ($ about-school-section {:school school :student student})))
       ($ section-item {:value "costs" :heading "What It Costs You" :tint? true}
          (if cte? ($ cte-costs-section {:school school})
                   ($ costs-section {:school school}))))))

(defui school-expanded
  "What unfurls under a school tile: the recommended pathways (each unfurls its own
   sections) followed by the school-anchored About This School + What It Costs You."
  [{:keys [school student]}]
  (let [ps (get data/college-pathways (:unitid school) [])]
    ($ :div {:class "space-y-5"}
       ($ :div {:class "space-y-3"}
          ($ section-eyebrow {:label (str (count ps) " Recommended Pathway"
                                          (when (> (count ps) 1) "s"))})
          ;; Pathways start COLLAPSED — the student clicks one to unfurl its details.
          (for [p ps]
            ($ pathway-row {:key (:id p) :pathway p :school school :student student :default-open? false})))
       ($ school-sections {:school school :student student}))))

;; ============================================================================
;; School tile (Pathway Recommendations) — Learn More expander
;; ============================================================================

(defui school-tile [{:keys [school student]}]
  (let [[open? set-open!] (use-state false)
        cls (data/classify-str (:act student) (:act-25 school) (:act-75 school))]
    ($ card/Card {:class "bg-white rounded-2xl shadow-sm ring-1 ring-[#d0ecef] border-0 overflow-hidden"}
       ($ :div {:class "p-5 space-y-3"}
          ;; Logo + Safety/Target/Reach badge — vertically centered together.
          ($ :div {:class "flex items-center justify-between gap-4"}
             ;; Curated wordmark (includes the name) when available; otherwise a
             ;; domain-derived favicon + the school name — consistent for any school.
             (if (:logo school)
               ($ :img {:src (:logo school) :alt (:name school)
                        :class "h-9 md:h-10 w-auto object-contain"})
               ($ :div {:class "flex items-center gap-2 min-w-0"}
                  (when-let [f (favicon-url (:website school))]
                    ($ :img {:src f :alt "" :class "w-7 h-7 rounded shrink-0"}))
                  ($ :h3 {:class "text-lg font-semibold text-[#2a6465] font-head"} (:name school))))
             ;; Selective schools show Safety/Target/Reach; open-admission schools
             ;; (no ACT — e.g. 2-year/CTE) show a neutral "Open Admission" chip.
             ($ str-badge {:classification cls}))
          ($ :div {:class "flex flex-wrap items-center gap-x-2 gap-y-1 text-sm text-[#676868]"}
             ($ :span (str (:city school) ", " (:state school)))
             ($ :span "·") ($ :span (:type school))
             ($ :span "·")
             ($ ext-link {:href (:website school) :label (:website-label school)
                          :title (:website school) :class "text-sm"}))
          ;; School-level chips removed — descriptors are per-pathway now (see pathway-row),
          ;; since programs at one school vary in field, credential, and demand.
          ($ :button {:class "mt-1 w-full rounded-xl bg-[#05a09c] hover:bg-[#007f81] text-white font-semibold py-2.5 transition-colors"
                      :on-click #(set-open! not)}
             (if open? "Show Less ▲" "Learn More ▼")))
       (when open?
         ($ :div {:class "px-3 md:px-5 pb-5 pt-1 bg-[#f2f3f4]"}
            ($ :div {:class "pt-4"}
               ($ school-expanded {:school school :student student})))))))

;; ============================================================================
;; Left column — Advisor Messages
;; ============================================================================

(defui advisor-card [{:keys [advisor]}]
  ($ :div {:class "bg-white rounded-2xl shadow-sm ring-1 ring-[#d0ecef] p-5"}
     ($ :div {:class "flex items-center gap-4"}
        ($ :img {:src (:headshot advisor)
                 :alt (:name advisor)
                 :class "w-16 h-16 rounded-full object-cover ring-2 ring-[#d0ecef]"})
        ($ :div
           ($ :div {:class "font-semibold text-[#2a6465] font-head"} (:name advisor))
           ($ :div {:class "text-sm text-[#676868]"} (:title advisor))))
     ($ :div {:class "mt-4 space-y-1.5 text-sm"}
        ($ :div "Email me: "
           ($ ext-link {:href (str "mailto:" (:email advisor)) :label (:email advisor)
                        :title (str "Email " (:name advisor))}))
        ($ :div
           ($ ext-link {:href (:appointment-url advisor) :label "Schedule an appointment →"
                        :title "Book time on my Google Calendar"})))))

(defui advisor-messages [{:keys [advisor]}]
  ($ :div {:class "space-y-4"}
     ($ section-eyebrow {:label (:messages-label advisor)})
     ($ :div {:class "bg-white rounded-2xl shadow-sm ring-1 ring-[#d0ecef] p-5 border-l-4 border-l-[#05a09c]"}
        ($ :p {:class "text-sm md:text-base text-[#313335] leading-relaxed"} (:note advisor)))
     ($ advisor-card {:advisor advisor})))

;; ============================================================================
;; Right column — "Check these out:"
;; ============================================================================

(defui str-legend []
  ($ :div {:class "bg-white rounded-2xl shadow-sm ring-1 ring-[#d0ecef] p-4"}
     ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mb-2"}
        "Safety · Target · Reach")
     ($ :div {:class "space-y-1.5"}
        (for [{:keys [key label def color]} data/str-legend]
          ($ :div {:key key :class "flex items-start gap-2 text-sm"}
             ($ :span {:class "mt-1.5 w-2.5 h-2.5 rounded-full shrink-0" :style {:background-color color}})
             ($ :span ($ :span {:class "font-semibold text-[#313335]"} (str label ": ")) def))))))

(defui terms-to-know []
  ($ :div {:class "bg-[#d0ecef]/40 rounded-2xl p-4"}
     ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#2a6465] mb-2 font-head"}
        "Terms to Know")
     ($ :dl {:class "space-y-2"}
        (for [{:keys [term def]} data/terms-to-know]
          ($ :div {:key term :class "text-sm"}
             ($ :dt {:class "font-semibold text-[#313335] inline"} (str term ": "))
             ($ :dd {:class "inline text-[#676868]"} def))))))

;; --- Short-Term program card (certificate / apprenticeship) — standalone, reuses
;;     the same section system as a school's pathway, with the provider shown. ---
(defui short-term-card [{:keys [program student default-open?]}]
  (let [[open? set-open!] (use-state (boolean default-open?))]
    ($ :div {:class "bg-white rounded-xl shadow-sm ring-1 ring-[#bcdfe5] overflow-hidden"}
       ($ :button {:class "w-full flex items-center justify-between gap-3 p-4 text-left hover:bg-[#f3fafb] transition-colors"
                   :on-click #(set-open! not)}
          ($ :div {:class "min-w-0"}
             ($ :div {:class "font-semibold text-[#2a6465] font-head"}
                (str (:name program) (when (:acronym program) (str " (" (:acronym program) ")"))))
             ($ :div {:class "text-sm text-[#676868] mt-0.5"}
                (str (:provider program) " · " (:credential-level program)))
             ($ :div {:class "flex flex-wrap gap-2 mt-2"}
                (for [t (pathway-chips program)]
                  ($ :span {:key t :class "rounded-full bg-[#d0ecef] text-[#007f81] text-xs font-medium px-2.5 py-0.5"} t))))
          ($ :span {:class "shrink-0 text-sm font-semibold text-[#007f81]"}
             (if open? "Hide ▲" "View ▼")))
       (when open?
         ($ :div {:class "px-3 md:px-4 pb-4 pt-2 bg-[#f2f3f4] space-y-3"}
            ($ pathway-sections {:pathway program :school nil :student student})
            (when (:info-url program)
              ($ :div {:class "px-1"}
                 ($ ext-link {:href (:info-url program)
                              :label "Learn more about this program →"
                              :title (:provider program)}))))))))

;; --- Scholarship card (mirrors the production card shape) ---
(defui scholarship-card [{:keys [s]}]
  ($ :div {:class "bg-white rounded-2xl shadow-sm ring-1 ring-[#bcdfe5] p-5 space-y-3"}
     ($ :div {:class "flex items-start justify-between gap-3"}
        ($ :div {:class "min-w-0"}
           ($ :div {:class "font-semibold text-[#2a6465] font-head text-base"} (:name s))
           ($ :div {:class "text-sm text-[#676868]"} (:sponsor s)))
        ($ :div {:class "text-right shrink-0"}
           ($ :div {:class "text-xl font-bold text-[#05a09c] font-head"} (:award s))
           ($ :div {:class "text-xs text-[#676868]"} (str "Due " (:deadline s)))))
     ($ :div {:class "rounded-xl bg-[#d0ecef]/50 p-3 text-sm text-[#313335] leading-relaxed"}
        ($ :span {:class "font-semibold text-[#2a6465]"} "Why it fits: ") (:why-fits s))
     ($ bullet-list {:bullets [(str "Eligibility: " (:eligibility s))
                               (str "Open to: " (:target-levels s) " students")
                               (:selection s)]})
     ($ :div
        ($ :div {:class "text-xs font-semibold uppercase tracking-wide text-[#676868] mb-1"} "Application tips")
        ($ bullet-list {:bullets (:tips s)}))
     ($ ext-link {:href (:url s) :label "Apply / learn more →" :title (:name s)})))

;; --- Tabs ---
(defui tab-button [{:keys [label active? on-click]}]
  ($ :button {:class (str "flex-1 px-3 py-2 text-sm font-semibold rounded-lg transition-colors "
                          (if active? "bg-[#05a09c] text-white" "text-[#2a6465] hover:bg-[#d0ecef]/60"))
              :on-click on-click}
     label))

(defui colleges-tab [{:keys [student]}]
  ($ :div {:class "space-y-4"}
     ($ str-legend)
     ($ :div {:class "space-y-3"}
        ($ section-eyebrow {:label "Pathway Recommendations"})
        (for [sch data/colleges-schools]
          ($ school-tile {:key (:unitid sch) :school sch :student student})))
     ($ terms-to-know)))

(defui short-term-tab [{:keys [student]}]
  ($ :div {:class "space-y-3"}
     ($ section-eyebrow {:label "Short-Term Training"})
     (for [p data/short-term-programs]
       ($ short-term-card {:key (:id p) :program p :student student}))))

(defui scholarships-tab []
  ($ :div {:class "space-y-3"}
     ($ section-eyebrow {:label "Scholarships"})
     (for [s data/scholarship-list]
       ($ scholarship-card {:key (:id s) :s s}))))

(defui recommendations-column [{:keys [student]}]
  (let [[tab set-tab!] (use-state :colleges)]
    ($ :div {:class "space-y-4"}
       ($ :h2 {:class "text-xl font-semibold text-[#2a6465] font-head"} "Check these out:")
       ($ :div {:class "flex gap-1 bg-white rounded-xl ring-1 ring-[#d0ecef] p-1"}
          ($ tab-button {:label "Colleges"     :active? (= tab :colleges)     :on-click #(set-tab! :colleges)})
          ($ tab-button {:label "Short-Term"   :active? (= tab :short-term)   :on-click #(set-tab! :short-term)})
          ($ tab-button {:label "Scholarships" :active? (= tab :scholarships) :on-click #(set-tab! :scholarships)}))
       (case tab
         :colleges     ($ colleges-tab {:student student})
         :short-term   ($ short-term-tab {:student student})
         :scholarships ($ scholarships-tab)))))

;; ============================================================================
;; Page
;; ============================================================================

(defui rec-demo-page [_props]
  (let [s data/student]
    ($ :div {:class "min-h-screen bg-[#f2f3f4]"}
       ;; Header bar — dark teal, BRYC white-circle logo + first-name title
       ($ :div {:class "bg-[#2a6465] shadow-sm px-6 md:px-8 py-4"}
          ($ :div {:class "max-w-6xl mx-auto flex items-center gap-3"}
             ($ :img {:src "assets/bryc-logo-white.png" :alt "BRYC"
                      :class "w-10 h-10 rounded-full"})
             ($ :h1 {:class "text-lg md:text-2xl font-bold text-white font-head"}
                (str (:first-name s) "'s Personalized Recommendations"))))

       ;; Two-column body
       ($ :div {:class "max-w-6xl mx-auto px-4 md:px-8 py-8"}
          ($ :div {:class "grid grid-cols-1 lg:grid-cols-12 gap-6 items-start"}
             ($ :div {:class "lg:col-span-4 lg:sticky lg:top-6"}
                ($ advisor-messages {:advisor data/advisor}))
             ($ :div {:class "lg:col-span-8"}
                ($ recommendations-column {:student s})))))))
