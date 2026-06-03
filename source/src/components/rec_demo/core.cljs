(ns components.rec-demo.core
  "PROTOTYPE — Rec UI redesign demo page (route: /rec-demo).

   Demonstrates the proposed 3-level recommendation output for the dev team:
     L1  School card (collapsed)
     L2  Program list + school-level sections (Why This School Fits, Financial Aid)
     L3  Program sections as a one-at-a-time accordion
          (Overview · What's Cool About This Program [NEW] · Expected Outcomes
           · Career Summary · Career Paths)

   All content is hardcoded (components.rec-demo.data) from exemplar v7. No Notion
   calls, no live routes touched. Matches the app's UIx + shadcn/ui + Tailwind v4
   conventions and the student rec page's glassmorphism aesthetic."
  (:require [uix.core :as uix :refer [defui $ use-state]]
            [components.rec-demo.data :as data]
            [components.rec-demo.charts :as charts]
            ["/gen/shadcn/components/ui/card" :as card]
            ["/gen/shadcn/components/ui/badge" :as badge]
            ["/gen/shadcn/components/ui/accordion" :as accordion]))

;; ============================================================================
;; Small shared primitives
;; ============================================================================

(defui section-eyebrow
  "Uppercase label with the app's gradient dot, used for section headings."
  [{:keys [label]}]
  ($ :div {:class "flex items-center mb-4"}
     ($ :div {:class "w-2 h-2 bg-gradient-to-r from-blue-400 to-purple-500 rounded-full mr-3 opacity-70"})
     ($ :h3 {:class "text-sm font-semibold uppercase tracking-wide text-gray-600"} label)))

(defui stat-card
  [{:keys [label value sub]}]
  ($ :div {:class "bg-white/70 rounded-xl shadow-sm p-4 flex flex-col"}
     ($ :div {:class "text-2xl font-bold text-slate-800 leading-tight"} value)
     ($ :div {:class "text-xs font-medium text-gray-600 mt-1"} label)
     (when sub
       ($ :div {:class "text-xs text-gray-400 mt-0.5"} sub))))

(defui stat-grid
  [{:keys [stats]}]
  ($ :div {:class "grid grid-cols-2 md:grid-cols-3 gap-3"}
     (for [[i s] (map-indexed vector stats)]
       ($ stat-card (assoc s :key i)))))

(defui bullet-list
  [{:keys [bullets]}]
  ($ :ul {:class "space-y-3"}
     (for [[i b] (map-indexed vector bullets)]
       ($ :li {:key i :class "flex text-sm md:text-base text-gray-600 leading-relaxed"}
          ($ :span {:class "mr-3 text-blue-400 shrink-0"} "•")
          ($ :span b)))))

(defui callout-card
  "Highlighted differentiator card for the What's Cool section."
  [{:keys [tag title body link accent]}]
  ($ :div {:class (str "border-l-4 rounded-r-xl shadow-sm p-4 " accent)}
     ($ :div {:class "text-[11px] font-semibold uppercase tracking-wide text-gray-500 mb-1"} tag)
     ($ :div {:class "text-sm md:text-base font-semibold text-slate-800 mb-1"} title)
     ($ :p {:class "text-sm text-gray-600 leading-relaxed"} body)
     (when link
       ($ :a {:href (:href link)
              :class "inline-block mt-2 text-sm font-semibold text-primary hover:underline underline-offset-2"}
          (:label link)))))

;; ============================================================================
;; Level 3 — Program section accordion
;; ============================================================================

(defui section-item
  "One collapsible program section. The shadcn accordion (type=single) gives the
   one-at-a-time behavior Tavidee chose."
  [{:keys [value heading badge-text children]}]
  ($ accordion/AccordionItem
     {:value value
      :class "bg-white/60 backdrop-blur-sm rounded-xl shadow-sm border-0 px-4"}
     ($ accordion/AccordionTrigger {:class "hover:no-underline py-4"}
        ($ :div {:class "flex items-center gap-2 text-left"}
           ($ :span {:class "text-base font-semibold text-slate-700"} heading)
           (when badge-text
             ($ badge/Badge {:variant "secondary"
                             :class "text-[10px] bg-violet-100 text-violet-700"}
                badge-text))))
     ($ accordion/AccordionContent {:class "pb-5 pt-1"}
        children)))

(defui whats-cool-section []
  ($ :div {:class "space-y-5"}
     ($ charts/salary-vs-col-chart
        {:title "Median Salary Compared to Cost of Living"
         :legend-items data/salary-legend
         :source data/salary-source
         :data data/salary-data})
     ($ :div {:class "grid grid-cols-1 md:grid-cols-2 gap-3"}
        (for [[i c] (map-indexed vector data/whats-cool-callouts)]
          ($ callout-card (assoc c :key i))))))

(defui expected-outcomes-section []
  ($ :div {:class "space-y-4"}
     ($ :div {:class "flex items-center gap-2"}
        ($ badge/Badge {:class "bg-amber-100 text-amber-800 border-0"} "★ 5-Star LWC Occupation")
        ($ :span {:class "text-sm text-gray-600"}
           "High demand — significantly more employers than qualified candidates in Louisiana."))
     ($ stat-grid {:stats data/expected-outcomes-stats})
     ($ :p {:class "text-xs italic text-slate-500"} data/expected-outcomes-source)))

(defui career-paths-section []
  ($ :div {:class "space-y-3"}
     (for [[i {:keys [title requirement desc]}] (map-indexed vector data/career-paths)]
       ($ :div {:key i :class "bg-white/70 rounded-xl shadow-sm p-4"}
          ($ :div {:class "flex flex-wrap items-center gap-2 mb-1"}
             ($ :span {:class "font-semibold text-slate-800"} title)
             (when requirement
               ($ badge/Badge {:variant "outline" :class "text-[10px] text-gray-500"}
                  requirement)))
          ($ :p {:class "text-sm text-gray-600 leading-relaxed"} desc)))))

(defui program-sections
  "Level 3: all program sections as a one-at-a-time accordion (Overview open)."
  []
  ($ accordion/Accordion {:type "single" :collapsible true :default-value "overview"
                          :class "space-y-3"}
     ($ section-item {:value "overview" :heading "Overview"}
        ($ bullet-list {:bullets data/overview-bullets}))
     ($ section-item {:value "whats-cool" :heading "What's Cool About This Program"
                      :badge-text "NEW"}
        ($ whats-cool-section))
     ($ section-item {:value "outcomes" :heading "Expected Outcomes"}
        ($ expected-outcomes-section))
     ($ section-item {:value "career-summary" :heading "Career Summary"}
        ($ bullet-list {:bullets data/career-summary-bullets}))
     ($ section-item {:value "career-paths" :heading "Career Paths"}
        ($ career-paths-section))))

;; ============================================================================
;; Level 2 — School view (program rows + static school-level sections)
;; ============================================================================

(defui program-row
  [{:keys [program open? on-toggle]}]
  ($ :div {:class "bg-white/60 backdrop-blur-sm rounded-xl shadow-sm overflow-hidden"}
     ($ :button {:class "w-full flex items-center justify-between gap-4 p-4 text-left hover:bg-white/40 transition-colors"
                 :on-click on-toggle}
        ($ :div
           ($ :div {:class "font-semibold text-slate-800"} (:title program))
           ($ :div {:class "text-sm text-gray-500 mt-0.5"}
              (str (:track program) " · " (:credential program))))
        ($ :div {:class "flex items-center gap-3 shrink-0"}
           ($ badge/Badge {:variant "outline" :class "text-xs"} (:category program))
           ($ :span {:class "text-gray-400 text-sm"} (if open? "▲ Hide" "▼ View"))))
     (when open?
       ($ :div {:class "px-3 pb-4 pt-1 md:px-4"}
          ($ program-sections)))))

(defui why-this-school-fits []
  ($ :div {:class "space-y-4"}
     ($ section-eyebrow {:label "Why This School Fits"})
     ($ stat-grid {:stats data/why-fits-stats})
     ($ :div {:class "bg-white/60 backdrop-blur-sm rounded-2xl shadow-sm p-5"}
        ($ bullet-list {:bullets data/why-fits-bullets}))))

(defui financial-aid-section []
  ($ :div {:class "space-y-4"}
     ($ section-eyebrow {:label "Financial Aid"})
     ($ charts/financial-aid-waterfall
        {:title "Estimated Annual Cost at SLU — After Your Financial Aid"
         :legend-items data/waterfall-legend
         :source data/waterfall-source
         :data data/waterfall-data})
     ($ :div {:class "bg-white/60 backdrop-blur-sm rounded-2xl shadow-sm p-5 space-y-4"}
        ($ bullet-list {:bullets data/financial-aid-bullets})
        ($ :a {:href data/net-price-calculator-url
               :target "_blank" :rel "noopener noreferrer"
               :class "inline-block text-sm font-semibold text-primary hover:underline underline-offset-2"}
           "Personalized estimate — SLU Net Price Calculator →"))))

(defui school-view []
  (let [programs (:programs data/school)
        [open-id set-open!] (use-state (:id (first programs)))]
    ($ card/CardContent {:class "space-y-8 pt-0 px-6 pb-6"}
       ;; --- Programs (each opens Level 3) ---
       ($ :div {:class "space-y-4"}
          ($ section-eyebrow {:label (str (count programs) " Recommended Program"
                                          (when (> (count programs) 1) "s"))})
          (for [p programs]
            ($ program-row {:key (:id p)
                            :program p
                            :open? (= open-id (:id p))
                            :on-toggle #(set-open! (fn [cur] (when-not (= cur (:id p)) (:id p))))})))
       ($ :div {:class "h-px bg-gradient-to-r from-transparent via-gray-200 to-transparent"})
       ;; --- Static school-level sections ---
       ($ why-this-school-fits)
       ($ financial-aid-section))))

;; ============================================================================
;; Level 1 — School card
;; ============================================================================

(defui school-card []
  (let [[expanded? set-expanded!] (use-state false)
        s data/school]
    ($ card/Card
       {:class "relative transition-all duration-300 ease-in-out bg-white/60 backdrop-blur-sm rounded-2xl shadow-sm border-0"}
       ($ card/CardHeader {:class "p-6 cursor-pointer"
                           :on-click #(set-expanded! not)}
          ($ :div {:class "flex items-start justify-between gap-6"}
             ($ :div {:class "flex-1"}
                ($ card/CardTitle {:class "text-lg md:text-xl font-semibold text-gray-700 mb-2"}
                   (:name s))
                ($ :div {:class "flex flex-wrap items-center gap-x-2 gap-y-1 text-sm text-muted-foreground"}
                   ($ :span (:type s))
                   ($ :span "·") ($ :span (:location s))
                   ($ :span "·") ($ :span (:distance s))
                   ($ :span "·") ($ :span (str "Acceptance rate: " (:acceptance s))))
                ($ :div {:class "flex flex-wrap gap-2 mt-3"}
                   (for [c (:credentials s)]
                     ($ badge/Badge {:key c :variant "secondary" :class "text-xs"} c))))
             ($ :span {:class "text-sm font-medium text-gray-500 shrink-0 mt-1"}
                (if expanded? "Collapse" "Expand"))))
       (when expanded?
         ($ school-view)))))

;; ============================================================================
;; Page
;; ============================================================================

(defui rec-demo-page [_props]
  (let [s data/student]
    ($ :div {:class "min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50"}
       ;; Header
       ($ :div {:class "bg-white/40 backdrop-blur-sm shadow-sm px-6 md:px-8 py-5"}
          ($ :div {:class "max-w-4xl mx-auto flex items-center justify-between gap-4"}
             ($ :h1 {:class "text-xl md:text-2xl font-semibold text-gray-700"}
                "Your Personalized Recommendations")
             ($ badge/Badge {:variant "outline"
                             :class "text-[11px] text-amber-700 border-amber-300 bg-amber-50"}
                "PROTOTYPE")))

       ($ :div {:class "max-w-4xl mx-auto px-6 md:px-8 py-8 space-y-6"}
          ;; Student context
          ($ :div {:class "bg-white/60 backdrop-blur-sm rounded-2xl shadow-sm p-5"}
             ($ :div {:class "flex flex-wrap items-baseline gap-x-3 gap-y-1"}
                ($ :span {:class "text-lg font-semibold text-slate-800"} (:name s))
                (when (:placeholder? s)
                  ($ :span {:class "text-xs text-gray-400"} "(placeholder)")))
             ($ :div {:class "flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-600 mt-2"}
                ($ :span "ACT: " ($ :span {:class "font-medium text-slate-700"} (:act s)))
                ($ :span "GPA: " ($ :span {:class "font-medium text-slate-700"} (:gpa s)))
                ($ :span "TOPS: " ($ :span {:class "font-medium text-slate-700"} (:tops s)))
                ($ :span "Interests: " ($ :span {:class "font-medium text-slate-700"} (:interests s)))))

          ;; Advisor note (per-student, shown first)
          ($ :div
             ($ section-eyebrow {:label "From Your Advisor"})
             ($ :div {:class "bg-white/60 backdrop-blur-sm rounded-2xl shadow-sm p-6 border-l-4 border-l-blue-400"}
                ($ :p {:class "text-sm md:text-base text-gray-700 leading-relaxed"}
                   data/advisor-note)))

          ;; School card (Level 1 → 2 → 3)
          ($ school-card)))))
