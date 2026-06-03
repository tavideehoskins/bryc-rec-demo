(ns components.rec-demo.charts
  "Recharts-based charts for the rec-redesign prototype.

   PROTOTYPE ONLY. Data is hardcoded from the SLU BSN exemplar v7 — see
   components.rec-demo.data. Recharts (^2.15.4, already in package.json) is the
   app's charting library per the redesign plan; this is the first Recharts
   interop in the CLJS app.

   Styling matches the two reference PNGs Tavidee supplied (bold centered title,
   boxed legend, light dashed gridlines, dark bold value labels, italic source
   caption)."
  (:require [uix.core :as uix :refer [defui $]]
            [clojure.string :as str]
            ["recharts" :refer [ResponsiveContainer BarChart Bar
                                XAxis YAxis CartesianGrid Tooltip
                                Cell LabelList]]))

;; ============================================================================
;; Palette
;; ============================================================================

(def color-brand "#4f6fc4")      ;; earnings / cost-of-attendance bars (cornflower blue)
(def color-muted "#94a3b8")      ;; neutral (available if CoL bars should be distinguished)
(def color-net "#6fa84f")        ;; highlighted "your net cost" (green, matches PNG)
(def color-deduction "#cbd5e1")
(def color-grid "#e5e7eb")
(def color-axis "#6b7280")
(def color-label "#1f2937")

;; ============================================================================
;; Helpers
;; ============================================================================

(defn usd [n]
  (str "$" (.toLocaleString (js/Math.round n) "en-US")))

(defn ^js multiline-x-tick
  "Custom XAxis tick that renders names containing newlines across <tspan>s,
   so long category labels wrap like the reference charts."
  [^js props]
  (let [x (.-x props)
        y (.-y props)
        value (.. props -payload -value)
        lines (str/split (str value) #"\n")]
    ($ :g {:transform (str "translate(" x "," y ")")}
       (for [[i line] (map-indexed vector lines)]
         ($ :text {:key i
                   :x 0
                   :y 0
                   :dy (+ 16 (* i 15))
                   :textAnchor "middle"
                   :fill color-axis
                   :fontSize 12}
            line)))))

;; ============================================================================
;; Chart container chrome (title + boxed legend + source caption)
;; ============================================================================

(defui chart-frame
  "legend-items: optional vector of {:color :label} rows rendered in a bordered box."
  [{:keys [title legend-items source children]}]
  ($ :div {:class "bg-white/70 rounded-2xl shadow-sm p-5 md:p-6"}
     (when title
       ($ :h4 {:class "text-base md:text-lg font-bold text-slate-700 text-center mb-3"}
          title))
     (when (seq legend-items)
       ($ :div {:class "flex justify-center mb-2"}
          ($ :div {:class "inline-flex flex-col gap-1 border border-slate-200 rounded-md px-3 py-2"}
             (for [[i {:keys [color label]}] (map-indexed vector legend-items)]
               ($ :div {:key i :class "flex items-center gap-2"}
                  ($ :span {:class "inline-block w-3.5 h-3.5 rounded-sm shrink-0"
                            :style {:background-color color}})
                  ($ :span {:class "text-xs md:text-sm text-slate-600"} label))))))
     children
     (when source
       ($ :p {:class "text-xs italic text-slate-500 mt-3 leading-relaxed"} source))))

;; ============================================================================
;; Chart 1 — Salary vs. Cost of Living (vertical bars)
;; ============================================================================

(defui salary-vs-col-chart
  [{:keys [data title legend-items source]}]
  ($ chart-frame
     {:title title :legend-items legend-items :source source}
     ($ :div {:class "w-full" :style {:height 360}}
        ($ ResponsiveContainer {:width "100%" :height "100%"}
           ($ BarChart {:data (clj->js data)
                        :margin #js {:top 30 :right 16 :left 8 :bottom 48}}
              ($ CartesianGrid {:strokeDasharray "4 4" :vertical false :stroke color-grid})
              ($ XAxis {:dataKey "name"
                        :tickLine false
                        :axisLine #js {:stroke "#d1d5db"}
                        :interval 0
                        :tick multiline-x-tick
                        :height 60})
              ($ YAxis {:domain #js [0 100000]
                        :ticks #js [0 20000 40000 60000 80000 100000]
                        :tickFormatter usd
                        :tickLine false
                        :axisLine false
                        :fontSize 13
                        :stroke color-axis
                        :width 64})
              ($ Tooltip {:formatter (fn [v] (usd v))
                          :cursor #js {:fill "rgba(79,111,196,0.06)"}})
              ($ Bar {:dataKey "value" :radius #js [4 4 0 0] :isAnimationActive false}
                 (for [[i d] (map-indexed vector data)]
                   ($ Cell {:key i :fill (:fill d color-brand)}))
                 ($ LabelList {:dataKey "label"
                               :position "top"
                               :fill color-label
                               :fontSize 14
                               :fontWeight "bold"})))))))

;; ============================================================================
;; Chart 3 — Financial Aid step-down waterfall
;;
;; Matches the reference PNG: solid descending running-total bars, blue, with the
;; final "Net Cost" bar green. NOTE: per the redesign plan we deliberately OMIT
;; the "After Scholarships" step shown in the PNG — that deduction is illustrative
;; (CareerOneStop), not real per-student data, and would misrepresent net cost.
;; ============================================================================

(defui financial-aid-waterfall
  [{:keys [data title legend-items source]}]
  ($ chart-frame
     {:title title :legend-items legend-items :source source}
     ($ :div {:class "w-full" :style {:height 360}}
        ($ ResponsiveContainer {:width "100%" :height "100%"}
           ($ BarChart {:data (clj->js data)
                        :margin #js {:top 30 :right 16 :left 8 :bottom 48}}
              ($ CartesianGrid {:strokeDasharray "4 4" :vertical false :stroke color-grid})
              ($ XAxis {:dataKey "name"
                        :tickLine false
                        :axisLine #js {:stroke "#d1d5db"}
                        :interval 0
                        :tick multiline-x-tick
                        :height 60})
              ($ YAxis {:domain #js [0 25000]
                        :ticks #js [0 5000 10000 15000 20000 25000]
                        :tickFormatter usd
                        :tickLine false
                        :axisLine false
                        :fontSize 13
                        :stroke color-axis
                        :width 64})
              ($ Tooltip {:formatter (fn [v] (usd v))
                          :cursor #js {:fill "rgba(79,111,196,0.06)"}})
              ($ Bar {:dataKey "value" :radius #js [4 4 0 0] :isAnimationActive false}
                 (for [[i d] (map-indexed vector data)]
                   ($ Cell {:key i :fill (:fill d color-brand)}))
                 ($ LabelList {:dataKey "label"
                               :position "top"
                               :fill color-label
                               :fontSize 14
                               :fontWeight "bold"})))))))
