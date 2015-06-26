;; gorilla-repl.fileformat = 1

;; **
;;; # Simple Branching (AISTATS)
;;; 
;;; This is the simple branching benchmark from the 2014 AISTATS paper, with enumerated posterior.
;; **

;; @@
 (ns aistats-examples
  (:require [gorilla-plot.core :as plot]
            [clojure.core.matrix :as m])
  (:use clojure.repl
        [anglican 
          core runtime emit 
          [state :only [get-predicts get-log-weight]]]))
 
 (defn empirical-frequencies
  "applies f to each sample and returns a map of weighted 
  counts for each unique value"
  [f samples]
  (let [log-Z (- (reduce 
                   log-sum-exp
                   (map get-log-weight samples))
                 (log (count samples)))]
    (reduce (fn [freqs s]
              (let [v (f s)]
                (assoc freqs
                  v (+ (get freqs v 0.0)
                       (exp (- (get-log-weight s)
                               log-Z))))))
            {}
            samples)))
 
(defn normalize
  "divides each element in a collection by the sum of the elements"
  [coll]
  (let [norm (reduce + coll)]
    (map #(/ % norm) coll)))

(defn kl-categorical
  "KL divergence between two categorical distributions"
  [p-categories q-categories]
  (let [q (zipmap (map first q-categories)
                  (normalize (map second 
                                  q-categories)))]
    (reduce + 
            (map (fn [c prob]
                   (if (> prob 0.0)
                     (* prob 
                        (log (/ (double prob)
                                (double (get q c 0.0)))))
                     0.0))
                 (map first p-categories)
                 (normalize (map second p-categories))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/kl-categorical</span>","value":"#'aistats-examples/kl-categorical"}
;; <=

;; **
;;; Define model
;; **

;; @@
(def fib-seq 
  "Lazily constructed Fibonacci sequence"
  ((fn rfib [a b] 
     (lazy-seq (cons a (rfib b (+ a b)))))
   0 1))

(defquery branching
    "A simple example illustrating flow control with
    dependence on random choices"
    []
    (let [count-prior (poisson 4)
          r (sample count-prior)
          l (if (< 4 r)
              6
              (+ (nth fib-seq (* 3 r))
                 (sample count-prior)))]
      (observe (poisson l) 6)
      (predict :r r)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/branching</span>","value":"#'aistats-examples/branching"}
;; <=

;; **
;;; Posterior
;; **

;; @@
(def -inf (/ -1.0 0.0))

(def posterior 
  "posterior on r (ranged 0 ... 15), calculated by enumeration"
  (zipmap (range 15)
          (mapv exp 
                [-3.9095 -2.1104 -2.6806 -inf -inf -1.1045 
                 -1.5051 -2.0530 -2.7665 -3.5635 -4.4786 
                 -5.5249 -6.5592 -7.8998 -8.7471])))

;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/posterior</span>","value":"#'aistats-examples/posterior"}
;; <=

;; **
;;; Run inference
;; **

;; @@
(def number-of-samples 1000000)

(def samples
  (->> (doquery :importance branching [])
       (take number-of-samples)
       doall
       time))
;; @@
;; ->
;;; &quot;Elapsed time: 14733.852 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/samples</span>","value":"#'aistats-examples/samples"}
;; <=

;; **
;;; Calculate KL error relative to true posterior as a function of number of samples
;; **

;; @@
(def num-sample-range (mapv (partial * number-of-samples)
                            [1e-2 2e-2 5e-2 1e-1 2e-1 5e-1 1]))
  
(def KL-errors
  (map (fn [n]
         (->> (take n samples)
              (empirical-frequencies 
                (comp :r get-predicts))
              (kl-categorical posterior)))
       num-sample-range))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/KL-errors</span>","value":"#'aistats-examples/KL-errors"}
;; <=

;; @@
(plot/list-plot (map vector 
                     (map #(/ (log %) 
                              (log 10)) 
                          num-sample-range)
                     (map #(/ (log %) 
                              (log 10)) 
                          KL-errors))
                :joined true
                :color "#05A"
                :x-title "log number of samples"
                :y-title "log KL divergence"
                :plot-range [:all [-5 -2]])
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"7fef8551-0e9b-41b1-aa37-2d123370eb4f","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":[-5,-2]}],"marks":[{"type":"line","from":{"data":"7fef8551-0e9b-41b1-aa37-2d123370eb4f"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"stroke":{"value":"#05A"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"7fef8551-0e9b-41b1-aa37-2d123370eb4f","values":[{"x":4.0,"y":-3.116243729270428},{"x":4.30102999566398,"y":-3.0891159890729964},{"x":4.698970004336019,"y":-3.436757518196397},{"x":5.0,"y":-3.5949353662374413},{"x":5.301029995663981,"y":-3.788400055444913},{"x":5.698970004336018,"y":-3.827573298427538},{"x":5.999999999999999,"y":-3.9241344405330656}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"7fef8551-0e9b-41b1-aa37-2d123370eb4f\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain [-5 -2]}], :marks [{:type \"line\", :from {:data \"7fef8551-0e9b-41b1-aa37-2d123370eb4f\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :stroke {:value \"#05A\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"7fef8551-0e9b-41b1-aa37-2d123370eb4f\", :values ({:x 4.0, :y -3.116243729270428} {:x 4.30102999566398, :y -3.0891159890729964} {:x 4.698970004336019, :y -3.436757518196397} {:x 5.0, :y -3.5949353662374413} {:x 5.301029995663981, :y -3.788400055444913} {:x 5.698970004336018, :y -3.827573298427538} {:x 5.999999999999999, :y -3.9241344405330656})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=
