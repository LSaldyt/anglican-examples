;; gorilla-repl.fileformat = 1

;; **
;;; # CRP Gaussian Mixture (AISTATS)
;;; 
;;; This is the CRP Gaussian mixture benchmark from the 2014 AISTATS paper, with 10 observations and a fully enumerated ground truth posterior.
;; **

;; @@
 (ns crp-aistats
  (:require [gorilla-plot.core :as plot]
            [clojure.core.matrix :as m]
            [anglican.stat :as s]
            :reload)
  (:use clojure.repl
        [anglican 
          core runtime emit 
          [state :only [get-predicts get-log-weight]]
          [inference :only [collect-by]]]))
 
(defn kl-categorical
  "KL divergence between two categorical distributions"
  [p-categories q-categories]
  (let [p-norm (reduce + (map second p-categories))
        q-norm (reduce + (map second q-categories))
        q (into {} (for [[c w] q-categories] 
                     [c (/ w q-norm)]))]
    (reduce + 
            (for [[c w] p-categories]
                   (if (> w 0.0)
                     (* (/ w p-norm)
                        (log (/ (double (/ w p-norm))
                                (double (get q c 0.0)))))
                     0.0)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;crp-aistats/kl-categorical</span>","value":"#'crp-aistats/kl-categorical"}
;; <=

;; **
;;; ## Define model
;; **

;; @@
(defquery crp-mixture
  "CRP gaussian mixture model"
  [observations alpha mu beta a b]
  (let [precision-prior (gamma a b)]
    (loop [observations observations
           state-proc (CRP alpha)
           obs-dists {}
           states []]
      (if (empty? observations)
        (do 
          (predict :states states)
          (predict :num-clusters (count obs-dists)))
        (let [state (sample (produce state-proc))
              obs-dist (get obs-dists
                            state
                            (let [l (sample precision-prior)
                                  s (sqrt (/ (* beta l)))
                                  m (sample (normal mu s))]
                              (normal m (sqrt (/ l)))))]
          (observe obs-dist (first observations))
          (recur (rest observations)
                 (absorb state-proc state)
                 (assoc obs-dists state obs-dist)
                 (conj states state)))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;crp-aistats/crp-mixture</span>","value":"#'crp-aistats/crp-mixture"}
;; <=

;; **
;;; ## Define data and true posterior
;; **

;; @@
(def data 
  "observation sequence length 10"
  [10 11 12 -100 -150 -200 0.001 0.01 0.005 0.0])
 
(def posterior 
  "posterior on number of states, calculated by enumeration"
  (zipmap 
    (range 1 11)
    (mapv exp 
        [-11.4681 -1.0437 -0.9126 -1.6553 -3.0348 
         -4.9985 -7.5829 -10.9459 -15.6461 -21.6521])))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;crp-aistats/posterior</span>","value":"#'crp-aistats/posterior"}
;; <=

;; **
;;; ## Run inference
;; **

;; @@
(def number-of-particles 10)
(def number-of-samples 1000)

(def samples
  (->> (doquery :pgas crp-mixture
                [data 1.72 0.0 100.0 1.0 10.0]
                :number-of-particles number-of-particles)
       (take number-of-samples)
       doall
       time))
;; @@
;; ->
;;; &quot;Elapsed time: 2566.540115 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;crp-aistats/samples</span>","value":"#'crp-aistats/samples"}
;; <=

;; **
;;; ## Plot Empirical and True Posterior
;; **

;; @@
(def empirical-posterior 
  (->> samples
       (collect-by :num-clusters)
       s/empirical-distribution
  	   (into (sorted-map))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;crp-aistats/empirical-posterior</span>","value":"#'crp-aistats/empirical-posterior"}
;; <=

;; **
;;; **Empirical Posterior**
;; **

;; @@
(plot/bar-chart (sort (keys posterior))
                (map #(get empirical-posterior % 0.0) 
                     (sort (keys posterior)))) 
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"099dda0b-b9e3-407a-9903-5a486d9679db","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"099dda0b-b9e3-407a-9903-5a486d9679db","field":"data.y"}}],"marks":[{"type":"rect","from":{"data":"099dda0b-b9e3-407a-9903-5a486d9679db"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":1}},"hover":{"fill":{"value":"#FF29D2"}}}}],"data":[{"name":"099dda0b-b9e3-407a-9903-5a486d9679db","values":[{"x":1,"y":0.0},{"x":2,"y":0.04100000000000007},{"x":3,"y":0.2929999999999999},{"x":4,"y":0.44199999999999995},{"x":5,"y":0.18800000000000006},{"x":6,"y":0.03600000000000007},{"x":7,"y":0.0},{"x":8,"y":0.0},{"x":9,"y":0.0},{"x":10,"y":0.0}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":40,"top":10,"right":10,"left":55}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"099dda0b-b9e3-407a-9903-5a486d9679db\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"099dda0b-b9e3-407a-9903-5a486d9679db\", :field \"data.y\"}}], :marks [{:type \"rect\", :from {:data \"099dda0b-b9e3-407a-9903-5a486d9679db\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 1}}, :hover {:fill {:value \"#FF29D2\"}}}}], :data [{:name \"099dda0b-b9e3-407a-9903-5a486d9679db\", :values ({:x 1, :y 0.0} {:x 2, :y 0.04100000000000007} {:x 3, :y 0.2929999999999999} {:x 4, :y 0.44199999999999995} {:x 5, :y 0.18800000000000006} {:x 6, :y 0.03600000000000007} {:x 7, :y 0.0} {:x 8, :y 0.0} {:x 9, :y 0.0} {:x 10, :y 0.0})}], :width 400, :height 247.2188, :padding {:bottom 40, :top 10, :right 10, :left 55}}}"}
;; <=

;; **
;;; **True Posterior**
;; **

;; @@
(let [p (into (sorted-map) posterior)]
	(plot/bar-chart (keys p)
    	            (vals p)))
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"9e3732fc-626d-44e4-ab29-341fb12f975a","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"9e3732fc-626d-44e4-ab29-341fb12f975a","field":"data.y"}}],"marks":[{"type":"rect","from":{"data":"9e3732fc-626d-44e4-ab29-341fb12f975a"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":1}},"hover":{"fill":{"value":"#FF29D2"}}}}],"data":[{"name":"9e3732fc-626d-44e4-ab29-341fb12f975a","values":[{"x":1,"y":1.0458453073364188E-5},{"x":2,"y":0.3521493160516654},{"x":3,"y":0.40147902040466416},{"x":4,"y":0.19103473668703566},{"x":5,"y":0.04808427876972766},{"x":6,"y":0.006748061503565986},{"x":7,"y":5.090827403037801E-4},{"x":8,"y":1.7630150940851895E-5},{"x":9,"y":1.6031904113694912E-7},{"x":10,"y":3.9501396390265077E-10}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":40,"top":10,"right":10,"left":55}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"9e3732fc-626d-44e4-ab29-341fb12f975a\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"9e3732fc-626d-44e4-ab29-341fb12f975a\", :field \"data.y\"}}], :marks [{:type \"rect\", :from {:data \"9e3732fc-626d-44e4-ab29-341fb12f975a\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 1}}, :hover {:fill {:value \"#FF29D2\"}}}}], :data [{:name \"9e3732fc-626d-44e4-ab29-341fb12f975a\", :values ({:x 1, :y 1.0458453073364188E-5} {:x 2, :y 0.3521493160516654} {:x 3, :y 0.40147902040466416} {:x 4, :y 0.19103473668703566} {:x 5, :y 0.04808427876972766} {:x 6, :y 0.006748061503565986} {:x 7, :y 5.090827403037801E-4} {:x 8, :y 1.7630150940851895E-5} {:x 9, :y 1.6031904113694912E-7} {:x 10, :y 3.9501396390265077E-10})}], :width 400, :height 247.2188, :padding {:bottom 40, :top 10, :right 10, :left 55}}}"}
;; <=

;; **
;;; ## Plot L2 error relative to true posterior as a function of number of samples
;; **

;; @@
(def num-sample-range (mapv (partial * number-of-samples)
                            [1e-2 2e-2 5e-2 1e-1 2e-1 5e-1 1]))
  
(def KL-errors
  (map (fn [n]
         (->> (take n samples)
              (collect-by :num-clusters)
              s/empirical-distribution
              (#(kl-categorical % posterior))))
       num-sample-range))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;crp-aistats/KL-errors</span>","value":"#'crp-aistats/KL-errors"}
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
                :y-title "log L2 error")
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"titleOffset":30,"title":"log number of samples","scale":"x","type":"x"},{"titleOffset":45,"title":"log L2 error","scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"07ea76a8-c15f-4ebc-8339-9ba96046bfd6","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"07ea76a8-c15f-4ebc-8339-9ba96046bfd6","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"07ea76a8-c15f-4ebc-8339-9ba96046bfd6"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"stroke":{"value":"#05A"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"07ea76a8-c15f-4ebc-8339-9ba96046bfd6","values":[{"x":1.0,"y":0.15558813491381174},{"x":1.301029995663981,"y":-0.25589095319799965},{"x":1.6989700043360185,"y":-0.18221887154272073},{"x":2.0,"y":-0.15774195149804776},{"x":2.301029995663981,"y":-0.2939549269677069},{"x":2.6989700043360183,"y":-0.23637474430033834},{"x":2.9999999999999996,"y":-0.2950299605917628}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":40,"top":10,"right":10,"left":55}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:titleOffset 30, :title \"log number of samples\", :scale \"x\", :type \"x\"} {:titleOffset 45, :title \"log L2 error\", :scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"07ea76a8-c15f-4ebc-8339-9ba96046bfd6\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"07ea76a8-c15f-4ebc-8339-9ba96046bfd6\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"07ea76a8-c15f-4ebc-8339-9ba96046bfd6\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :stroke {:value \"#05A\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"07ea76a8-c15f-4ebc-8339-9ba96046bfd6\", :values ({:x 1.0, :y 0.15558813491381174} {:x 1.301029995663981, :y -0.25589095319799965} {:x 1.6989700043360185, :y -0.18221887154272073} {:x 2.0, :y -0.15774195149804776} {:x 2.301029995663981, :y -0.2939549269677069} {:x 2.6989700043360183, :y -0.23637474430033834} {:x 2.9999999999999996, :y -0.2950299605917628})}], :width 400, :height 247.2188, :padding {:bottom 40, :top 10, :right 10, :left 55}}}"}
;; <=
