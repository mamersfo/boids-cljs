(ns boids.vector)

(defn magnitude [v]
	(Math/sqrt (+ (* (v :x) (v :x)) (* (v :y) (v :y)))))
	
(defn distance [v1 v2]
	(let [dx (- (v1 :x) (v2 :x))
		  dy (- (v1 :y) (v2 :y))]
		(Math/sqrt (+ (* dx dx) (* dy dy)))))
		
(defn add [v1 v2]
	{:x (+ (v1 :x) (v2 :x))
	 :y (+ (v1 :y) (v2 :y))})

(defn subtract [v1 v2]
	{:x (- (v1 :x) (v2 :x))
	 :y (- (v1 :y) (v2 :y))})
	
(defn multiply [v n]
	{:x (* (v :x) n)
	 :y (* (v :y) n)})
	
(defn divide [v n]
	{:x (/ (v :x) n)
	 :y (/ (v :y) n)})
	
(defn limit [v max]
	(let [m (magnitude v)]
		(if (> m max) (multiply (normalize v) max) v)))
		
(defn normalize [v]
	(let [m (magnitude v)]
		(if (> m 0) (divide v m) v)))

