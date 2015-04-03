;; gorilla-repl.fileformat = 1

;; **
;;; # The Indian GPA Problem
;;; 
;;; This example was inspired by [Stuart Russell](https://www.cs.berkeley.edu/~russell/) who pointed out that most probabilistic programming systems, _Anglican currently included_, produce the "wrong" answer to this problem.  In short, the problem is: if you observe that a student GPA is exactly @@4.0@@ in a mix of transcripts of students from the USA (GPA's from @@0.0@@ to @@4.0@@ and India (GPA's from @@0.0@@ to @@10.0@@) what is the probability that the student is from India?  This problem gets at the heart of measure theoretic problems arising from combining distribution and density, problems not easy to automatically avoid in a probabilistic programming system.  As we know from statistics, given the mixture distribution and given the fact that his/her GPA is _exactly_ @@4.0@@,
;;; the probability that the student is American must be @@1.0@@ (i.e. zero probability that the student is from India).  What this really highlights is the definition of _is_.
;;; Does observing a GPA of @@4.0@@ mean exactly and only @@4.0@@?
;; **

;; @@
(ns indian-gpa
  (:require [gorilla-plot.core :as plot])
  (:use clojure.repl
        [mrepl core]
        [embang runtime emit]
        [anglib crp]
        [clojure.string :only (join split blank?)]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Partially discrete, partially continuous distributions
;;; 
;;; Let's model this anyway.  Anglican allows complex compositions of distributions conveniently.
;;; 
;;; For example, you can easily represent a partially discrete, partially continuous distribution.  Such a distribution could be used to represent the distribution over GPAs of American students, where their GPAs from the interval @@\left(0.0, 4.0\right)@@ are distributed via Beta distribution (parameters @@\alpha@@ and @@\beta@@),
;;; while some small fraction of students have _exactly_ 0.0 and 4.0 as their GPAs.
;;; These latter two points should have separate probability masses @@p_1@@ and @@p_2@@.
;;; 
;;; The density function of this distribution can be written as
;;; 
;;; $$ f\left(x\right) = p_1 \delta x + p_2 \delta \left( x - 4.0 \right) + \left( 1 - p_1 - p_2 \right) \frac{(^x/_4)^{\alpha-1}(1- (^x/_4) )^{\beta-1}} {Beta(\alpha,\beta)}\! $$
;;; 
;;; This distribution could be represented as the following simple probabilistic program (e.g. let @@p_1 = 0.02, p_2 = 0.08, \alpha = 7.0, \beta = 3.0@@ (to account for grade inflation).  We can also see what this looks like 
;; **

;; @@
(def american-gpa (fn [] (if (sample (flip 0.9)) 
                    (* 4 (sample (beta 7 3))) 
                    (* 4 (if (flip 0.8) 1.0 0.0)))))
(plot/histogram (repeatedly 10000 american-gpa) :bins 50)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"6df24841-840e-45d0-bd97-bf841951cf35","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"6df24841-840e-45d0-bd97-bf841951cf35","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"6df24841-840e-45d0-bd97-bf841951cf35"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"6df24841-840e-45d0-bd97-bf841951cf35","values":[{"x":0.5963715996338594,"y":0},{"x":0.6644441676411823,"y":1.0},{"x":0.7325167356485052,"y":2.0},{"x":0.800589303655828,"y":1.0},{"x":0.8686618716631509,"y":7.0},{"x":0.9367344396704738,"y":0.0},{"x":1.0048070076777966,"y":7.0},{"x":1.0728795756851195,"y":8.0},{"x":1.1409521436924424,"y":8.0},{"x":1.2090247116997652,"y":14.0},{"x":1.277097279707088,"y":21.0},{"x":1.345169847714411,"y":22.0},{"x":1.4132424157217338,"y":23.0},{"x":1.4813149837290567,"y":26.0},{"x":1.5493875517363795,"y":34.0},{"x":1.6174601197437024,"y":67.0},{"x":1.6855326877510253,"y":69.0},{"x":1.7536052557583481,"y":92.0},{"x":1.821677823765671,"y":79.0},{"x":1.8897503917729939,"y":99.0},{"x":1.9578229597803167,"y":150.0},{"x":2.0258955277876396,"y":156.0},{"x":2.0939680957949625,"y":180.0},{"x":2.1620406638022853,"y":201.0},{"x":2.230113231809608,"y":211.0},{"x":2.298185799816931,"y":236.0},{"x":2.366258367824254,"y":258.0},{"x":2.434330935831577,"y":287.0},{"x":2.5024035038388996,"y":328.0},{"x":2.5704760718462225,"y":362.0},{"x":2.6385486398535454,"y":393.0},{"x":2.7066212078608682,"y":399.0},{"x":2.774693775868191,"y":396.0},{"x":2.842766343875514,"y":425.0},{"x":2.910838911882837,"y":421.0},{"x":2.9789114798901597,"y":420.0},{"x":3.0469840478974826,"y":436.0},{"x":3.1150566159048054,"y":398.0},{"x":3.1831291839121283,"y":397.0},{"x":3.251201751919451,"y":407.0},{"x":3.319274319926774,"y":349.0},{"x":3.387346887934097,"y":347.0},{"x":3.4554194559414197,"y":328.0},{"x":3.5234920239487426,"y":271.0},{"x":3.5915645919560655,"y":232.0},{"x":3.6596371599633883,"y":196.0},{"x":3.727709727970711,"y":124.0},{"x":3.795782295978034,"y":84.0},{"x":3.863854863985357,"y":60.0},{"x":3.93192743199268,"y":26.0},{"x":4.000000000000003,"y":942.0},{"x":4.0680725680073255,"y":0}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"6df24841-840e-45d0-bd97-bf841951cf35\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"6df24841-840e-45d0-bd97-bf841951cf35\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"6df24841-840e-45d0-bd97-bf841951cf35\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"6df24841-840e-45d0-bd97-bf841951cf35\", :values ({:x 0.5963715996338594, :y 0} {:x 0.6644441676411823, :y 1.0} {:x 0.7325167356485052, :y 2.0} {:x 0.800589303655828, :y 1.0} {:x 0.8686618716631509, :y 7.0} {:x 0.9367344396704738, :y 0.0} {:x 1.0048070076777966, :y 7.0} {:x 1.0728795756851195, :y 8.0} {:x 1.1409521436924424, :y 8.0} {:x 1.2090247116997652, :y 14.0} {:x 1.277097279707088, :y 21.0} {:x 1.345169847714411, :y 22.0} {:x 1.4132424157217338, :y 23.0} {:x 1.4813149837290567, :y 26.0} {:x 1.5493875517363795, :y 34.0} {:x 1.6174601197437024, :y 67.0} {:x 1.6855326877510253, :y 69.0} {:x 1.7536052557583481, :y 92.0} {:x 1.821677823765671, :y 79.0} {:x 1.8897503917729939, :y 99.0} {:x 1.9578229597803167, :y 150.0} {:x 2.0258955277876396, :y 156.0} {:x 2.0939680957949625, :y 180.0} {:x 2.1620406638022853, :y 201.0} {:x 2.230113231809608, :y 211.0} {:x 2.298185799816931, :y 236.0} {:x 2.366258367824254, :y 258.0} {:x 2.434330935831577, :y 287.0} {:x 2.5024035038388996, :y 328.0} {:x 2.5704760718462225, :y 362.0} {:x 2.6385486398535454, :y 393.0} {:x 2.7066212078608682, :y 399.0} {:x 2.774693775868191, :y 396.0} {:x 2.842766343875514, :y 425.0} {:x 2.910838911882837, :y 421.0} {:x 2.9789114798901597, :y 420.0} {:x 3.0469840478974826, :y 436.0} {:x 3.1150566159048054, :y 398.0} {:x 3.1831291839121283, :y 397.0} {:x 3.251201751919451, :y 407.0} {:x 3.319274319926774, :y 349.0} {:x 3.387346887934097, :y 347.0} {:x 3.4554194559414197, :y 328.0} {:x 3.5234920239487426, :y 271.0} {:x 3.5915645919560655, :y 232.0} {:x 3.6596371599633883, :y 196.0} {:x 3.727709727970711, :y 124.0} {:x 3.795782295978034, :y 84.0} {:x 3.863854863985357, :y 60.0} {:x 3.93192743199268, :y 26.0} {:x 4.000000000000003, :y 942.0} {:x 4.0680725680073255, :y 0})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; In India, however, in most GPAs lie in the range @@[0.0, 10.0]@@, and for instance could be represented as follows:
;; **

;; @@
(def indian-gpa (fn [] (if (sample (flip 0.99)) 
                    (* 10 (sample (beta 5 5))) 
                    (* 10 (if (flip 0.5) 1.0 0.0)))))
(plot/histogram (repeatedly 10000 indian-gpa) :bins 50)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"411bf235-ae0d-47d9-81f4-d6a7dab95a4e","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"411bf235-ae0d-47d9-81f4-d6a7dab95a4e","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"411bf235-ae0d-47d9-81f4-d6a7dab95a4e"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"411bf235-ae0d-47d9-81f4-d6a7dab95a4e","values":[{"x":0.35445772782895746,"y":0},{"x":0.5473685732723783,"y":1.0},{"x":0.7402794187157992,"y":2.0},{"x":0.93319026415922,"y":2.0},{"x":1.126101109602641,"y":5.0},{"x":1.319011955046062,"y":18.0},{"x":1.511922800489483,"y":17.0},{"x":1.7048336459329039,"y":44.0},{"x":1.8977444913763248,"y":57.0},{"x":2.090655336819746,"y":84.0},{"x":2.2835661822631668,"y":119.0},{"x":2.4764770277065877,"y":143.0},{"x":2.6693878731500087,"y":163.0},{"x":2.8622987185934297,"y":184.0},{"x":3.0552095640368506,"y":228.0},{"x":3.2481204094802716,"y":260.0},{"x":3.4410312549236926,"y":316.0},{"x":3.6339421003671135,"y":328.0},{"x":3.8268529458105345,"y":381.0},{"x":4.019763791253955,"y":403.0},{"x":4.2126746366973755,"y":406.0},{"x":4.405585482140796,"y":426.0},{"x":4.598496327584217,"y":437.0},{"x":4.791407173027637,"y":477.0},{"x":4.984318018471058,"y":428.0},{"x":5.177228863914478,"y":445.0},{"x":5.370139709357899,"y":487.0},{"x":5.563050554801319,"y":429.0},{"x":5.75596140024474,"y":432.0},{"x":5.94887224568816,"y":376.0},{"x":6.141783091131581,"y":388.0},{"x":6.334693936575001,"y":417.0},{"x":6.527604782018422,"y":320.0},{"x":6.720515627461842,"y":299.0},{"x":6.913426472905263,"y":268.0},{"x":7.106337318348683,"y":229.0},{"x":7.299248163792104,"y":222.0},{"x":7.492159009235524,"y":150.0},{"x":7.685069854678945,"y":139.0},{"x":7.877980700122365,"y":106.0},{"x":8.070891545565786,"y":81.0},{"x":8.263802391009207,"y":70.0},{"x":8.456713236452629,"y":44.0},{"x":8.64962408189605,"y":18.0},{"x":8.842534927339472,"y":21.0},{"x":9.035445772782893,"y":16.0},{"x":9.228356618226314,"y":5.0},{"x":9.421267463669736,"y":1.0},{"x":9.614178309113157,"y":2.0},{"x":9.807089154556579,"y":0.0},{"x":10.0,"y":0.0},{"x":10.192910845443421,"y":106.0},{"x":10.385821690886843,"y":0}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"411bf235-ae0d-47d9-81f4-d6a7dab95a4e\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"411bf235-ae0d-47d9-81f4-d6a7dab95a4e\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"411bf235-ae0d-47d9-81f4-d6a7dab95a4e\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"411bf235-ae0d-47d9-81f4-d6a7dab95a4e\", :values ({:x 0.35445772782895746, :y 0} {:x 0.5473685732723783, :y 1.0} {:x 0.7402794187157992, :y 2.0} {:x 0.93319026415922, :y 2.0} {:x 1.126101109602641, :y 5.0} {:x 1.319011955046062, :y 18.0} {:x 1.511922800489483, :y 17.0} {:x 1.7048336459329039, :y 44.0} {:x 1.8977444913763248, :y 57.0} {:x 2.090655336819746, :y 84.0} {:x 2.2835661822631668, :y 119.0} {:x 2.4764770277065877, :y 143.0} {:x 2.6693878731500087, :y 163.0} {:x 2.8622987185934297, :y 184.0} {:x 3.0552095640368506, :y 228.0} {:x 3.2481204094802716, :y 260.0} {:x 3.4410312549236926, :y 316.0} {:x 3.6339421003671135, :y 328.0} {:x 3.8268529458105345, :y 381.0} {:x 4.019763791253955, :y 403.0} {:x 4.2126746366973755, :y 406.0} {:x 4.405585482140796, :y 426.0} {:x 4.598496327584217, :y 437.0} {:x 4.791407173027637, :y 477.0} {:x 4.984318018471058, :y 428.0} {:x 5.177228863914478, :y 445.0} {:x 5.370139709357899, :y 487.0} {:x 5.563050554801319, :y 429.0} {:x 5.75596140024474, :y 432.0} {:x 5.94887224568816, :y 376.0} {:x 6.141783091131581, :y 388.0} {:x 6.334693936575001, :y 417.0} {:x 6.527604782018422, :y 320.0} {:x 6.720515627461842, :y 299.0} {:x 6.913426472905263, :y 268.0} {:x 7.106337318348683, :y 229.0} {:x 7.299248163792104, :y 222.0} {:x 7.492159009235524, :y 150.0} {:x 7.685069854678945, :y 139.0} {:x 7.877980700122365, :y 106.0} {:x 8.070891545565786, :y 81.0} {:x 8.263802391009207, :y 70.0} {:x 8.456713236452629, :y 44.0} {:x 8.64962408189605, :y 18.0} {:x 8.842534927339472, :y 21.0} {:x 9.035445772782893, :y 16.0} {:x 9.228356618226314, :y 5.0} {:x 9.421267463669736, :y 1.0} {:x 9.614178309113157, :y 2.0} {:x 9.807089154556579, :y 0.0} {:x 10.0, :y 0.0} {:x 10.192910845443421, :y 106.0} {:x 10.385821690886843, :y 0})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; # Mixture of complex distributions
;;; 
;;; We can easily create the mixture of these two distributions (e.g., the student with the same probability @@0.5@@ either is from some US or Indian university):
;; **

;; @@
(defn student-gpa [] (if (sample (flip 0.5))
                             (american-gpa)
                             (indian-gpa)))
(plot/histogram (repeatedly 10000 student-gpa) :bins 50)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"c4370177-1302-4df2-b727-1def046713d7","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"c4370177-1302-4df2-b727-1def046713d7","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"c4370177-1302-4df2-b727-1def046713d7"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"c4370177-1302-4df2-b727-1def046713d7","values":[{"x":0.6639291418529296,"y":0},{"x":0.850650559015871,"y":8.0},{"x":1.0373719761788125,"y":4.0},{"x":1.224093393341754,"y":14.0},{"x":1.4108148105046954,"y":37.0},{"x":1.5975362276676368,"y":81.0},{"x":1.7842576448305782,"y":126.0},{"x":1.9709790619935197,"y":193.0},{"x":2.1577004791564613,"y":288.0},{"x":2.344421896319403,"y":403.0},{"x":2.5311433134823442,"y":499.0},{"x":2.7178647306452857,"y":585.0},{"x":2.904586147808227,"y":663.0},{"x":3.0913075649711685,"y":707.0},{"x":3.27802898213411,"y":694.0},{"x":3.4647503992970514,"y":628.0},{"x":3.651471816459993,"y":502.0},{"x":3.8381932336229343,"y":307.0},{"x":4.024914650785876,"y":716.0},{"x":4.211636067948818,"y":191.0},{"x":4.3983574851117595,"y":205.0},{"x":4.585078902274701,"y":183.0},{"x":4.771800319437643,"y":217.0},{"x":4.958521736600585,"y":228.0},{"x":5.145243153763527,"y":230.0},{"x":5.331964570926469,"y":230.0},{"x":5.518685988089411,"y":216.0},{"x":5.705407405252353,"y":225.0},{"x":5.892128822415295,"y":213.0},{"x":6.0788502395782364,"y":214.0},{"x":6.265571656741178,"y":164.0},{"x":6.45229307390412,"y":173.0},{"x":6.639014491067062,"y":145.0},{"x":6.825735908230004,"y":114.0},{"x":7.012457325392946,"y":100.0},{"x":7.199178742555888,"y":118.0},{"x":7.38590015971883,"y":82.0},{"x":7.5726215768817715,"y":64.0},{"x":7.759342994044713,"y":47.0},{"x":7.946064411207655,"y":43.0},{"x":8.132785828370597,"y":30.0},{"x":8.319507245533538,"y":32.0},{"x":8.50622866269648,"y":17.0},{"x":8.69295007985942,"y":6.0},{"x":8.879671497022361,"y":7.0},{"x":9.066392914185302,"y":2.0},{"x":9.253114331348243,"y":5.0},{"x":9.439835748511184,"y":0.0},{"x":9.626557165674125,"y":1.0},{"x":9.813278582837066,"y":0.0},{"x":10.000000000000007,"y":43.0},{"x":10.186721417162948,"y":0}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"c4370177-1302-4df2-b727-1def046713d7\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"c4370177-1302-4df2-b727-1def046713d7\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"c4370177-1302-4df2-b727-1def046713d7\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"c4370177-1302-4df2-b727-1def046713d7\", :values ({:x 0.6639291418529296, :y 0} {:x 0.850650559015871, :y 8.0} {:x 1.0373719761788125, :y 4.0} {:x 1.224093393341754, :y 14.0} {:x 1.4108148105046954, :y 37.0} {:x 1.5975362276676368, :y 81.0} {:x 1.7842576448305782, :y 126.0} {:x 1.9709790619935197, :y 193.0} {:x 2.1577004791564613, :y 288.0} {:x 2.344421896319403, :y 403.0} {:x 2.5311433134823442, :y 499.0} {:x 2.7178647306452857, :y 585.0} {:x 2.904586147808227, :y 663.0} {:x 3.0913075649711685, :y 707.0} {:x 3.27802898213411, :y 694.0} {:x 3.4647503992970514, :y 628.0} {:x 3.651471816459993, :y 502.0} {:x 3.8381932336229343, :y 307.0} {:x 4.024914650785876, :y 716.0} {:x 4.211636067948818, :y 191.0} {:x 4.3983574851117595, :y 205.0} {:x 4.585078902274701, :y 183.0} {:x 4.771800319437643, :y 217.0} {:x 4.958521736600585, :y 228.0} {:x 5.145243153763527, :y 230.0} {:x 5.331964570926469, :y 230.0} {:x 5.518685988089411, :y 216.0} {:x 5.705407405252353, :y 225.0} {:x 5.892128822415295, :y 213.0} {:x 6.0788502395782364, :y 214.0} {:x 6.265571656741178, :y 164.0} {:x 6.45229307390412, :y 173.0} {:x 6.639014491067062, :y 145.0} {:x 6.825735908230004, :y 114.0} {:x 7.012457325392946, :y 100.0} {:x 7.199178742555888, :y 118.0} {:x 7.38590015971883, :y 82.0} {:x 7.5726215768817715, :y 64.0} {:x 7.759342994044713, :y 47.0} {:x 7.946064411207655, :y 43.0} {:x 8.132785828370597, :y 30.0} {:x 8.319507245533538, :y 32.0} {:x 8.50622866269648, :y 17.0} {:x 8.69295007985942, :y 6.0} {:x 8.879671497022361, :y 7.0} {:x 9.066392914185302, :y 2.0} {:x 9.253114331348243, :y 5.0} {:x 9.439835748511184, :y 0.0} {:x 9.626557165674125, :y 1.0} {:x 9.813278582837066, :y 0.0} {:x 10.000000000000007, :y 43.0} {:x 10.186721417162948, :y 0})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; 
;;; # Inferring nationality from GPA example
;;; 
;;; We might like to condition the probabilistic program, inferring the probability of a student being American if his or her GPA is exactly @@4.0@@ but this is syntactically not allowed in Anglican natively (on purpose).
;;; 
;;; However, if we condition on the fact that the student's GPA is appoximately equal to @@4.0@@, so we have some uncertaintly about the full precision (which could be represented via Gaussian noise), the posterior will be different and now there will be some positive probability that student's university is located in India as well.
;; **

;; @@
  (with-primitive-procedures [student-gpa american-gpa indian-gpa] 
    (defquery which-nationality [observed-gpa tolerance]
    (let [nationality (sample (categorical [["USA" 0.6] ["India" 0.4] ]))  
          student_gpa (if (= nationality "USA") 
                          (american-gpa)
                          (indian-gpa))]
          (observe (normal student_gpa tolerance) observed-gpa)
          (predict :nationality nationality))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;indian-gpa/which-nationality</span>","value":"#'indian-gpa/which-nationality"}
;; <=

;; @@
(def N 1000)
(def tolerance 0.01)
(def sampler (doquery :pcascade which-nationality [4.0 tolerance]))
(def samples (drop 100 (take (+ N 100) sampler)))
(def num-usa (count (filter #(= % "USA") (map :nationality (map get-predicts samples)))))
["USA" (/ num-usa N) "India" (/ (- N num-usa) N)]
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;USA&quot;</span>","value":"\"USA\""},{"type":"html","content":"<span class='clj-ratio'>247/250</span>","value":"247/250"},{"type":"html","content":"<span class='clj-string'>&quot;India&quot;</span>","value":"\"India\""},{"type":"html","content":"<span class='clj-ratio'>3/250</span>","value":"3/250"}],"value":"[\"USA\" 247/250 \"India\" 3/250]"}
;; <=

;; **
;;; By playing with the tolerance we can get different behavior.  Try, for instance, ```(def tolerance 0.0001)```
;;; 
;;; We can add a Dirac distribution to the language easily enough, but in so doing we must be careful, particularly we must realize that inference will effectively default to rejection sampling in most cases, and rejection sampling can be exponentially slow.  In this case we should be OK.
;; **

;; @@
(defn dirac
  "Dirac distribution"
  [x]
    (reify
      distribution
      (sample [this] x)
      (observe [this value] (if (= x value) 0.0 (- (/ 1.0 0.0))))))

(with-primitive-procedures [student-gpa american-gpa indian-gpa dirac] 
    (defquery which-nationality-with-dirac [observed-gpa]
    (let [nationality (sample (categorical [["USA" 0.6] ["India" 0.4] ]))  
          student_gpa (if (= nationality "USA") 
                          (american-gpa)
                          (indian-gpa))]
          (observe (dirac student_gpa) observed-gpa)
          (predict :nationality nationality))))
(def N 1000)
(def sampler (doquery :smc which-nationality-with-dirac [4.0] :number-of-particles 1000))
(def samples (drop 100 (take (+ N 100) sampler)))
(def num-usa (count (filter #(= % "USA") (map :nationality (map get-predicts samples)))))
["USA" (float (/ num-usa N)) "India" (float (/ (- N num-usa) N)) "N" N]
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;USA&quot;</span>","value":"\"USA\""},{"type":"html","content":"<span class='clj-unkown'>0.644</span>","value":"0.644"},{"type":"html","content":"<span class='clj-string'>&quot;India&quot;</span>","value":"\"India\""},{"type":"html","content":"<span class='clj-unkown'>0.356</span>","value":"0.356"},{"type":"html","content":"<span class='clj-string'>&quot;N&quot;</span>","value":"\"N\""},{"type":"html","content":"<span class='clj-long'>1000</span>","value":"1000"}],"value":"[\"USA\" 0.644 \"India\" 0.356 \"N\" 1000]"}
;; <=

;; @@
;samples
;; @@
;; =>
;; <=

;; @@

;; @@