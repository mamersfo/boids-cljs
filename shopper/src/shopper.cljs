(ns boids.shopper
	(:require [boids.net :as net]
			  [boids.color :as color]
			  [boids.vector :as v]
			  [cljs.reader :as reader]))
	
(def pid (.-pid js/process))

(def ^:dynamic *radar-url* 
	{:host "localhost"
	 :port 1337
	 :path "/boids"})

(def ^:dynamic *radar-udp-port* 41234)

(def ^:dynamic *min-change* -0.25)
(def ^:dynamic *max-change* +0.25)

(def ^:dynamic *max-force* 0.05)
(def ^:dynamic *max-speed* 3.0)
(def ^:dynamic *wander-r* 16.0)
(def ^:dynamic *wander-d* 60.0)
(def ^:dynamic *radius* 4.0)
(def ^:dynamic *width* 400)
(def ^:dynamic *height* 400)

(defn random [lower upper]
	(+ lower (rand (- upper lower))))

(def boid (atom 
	{:pid (.-pid js/process)
	 :color (color/random)
	 :location {:x (/ *width* 2) :y (/ *height* 2)}
	 :velocity {:x (random -1 1) :y (random -1 1)}
	 :theta 0.0}))
	
(defn constrain [location radius width height]
	(let [x (location :x)
		  y (location :y)]
		{:x (Math/floor
				(cond
					(< x (- radius)) (+ width radius)
					(> x (+ width radius)) (- radius)
					:else x))
		 :y (Math/floor
				(cond
					(< y (- radius)) (+ height radius)
					(> y (+ height radius)) (- radius)
					:else y))}))

(defn steer [boid location]
	(let [desired (v/subtract location (boid :location))]
		(if (> (v/magnitude desired) 0)
			(-> desired
				(v/normalize)
				(v/multiply *max-speed*)
				(v/subtract (boid :velocity))
				(v/limit *max-force*))
			{:x 0 :y 0})))

(defn separate [boid boid-seq]
	(if (> (count boid-seq) 0)
		(let [boids (map #(assoc % :distance (v/distance (boid :location) (% :location))) boid-seq)
			  sum (reduce (fn [sum b]
						(v/add sum (-> (boid :location)
									   (v/subtract (b :location))
							   	   	   (v/normalize)
							  	   	   (v/divide (b :distance)))))
						{:x 0 :y 0} boids)]
			(v/divide sum (count boid-seq)))
		 {:x 0 :y 0}))

(defn wander [boid]
	(do
		(let [circle-loc (-> (boid :velocity)
							 (v/multiply *wander-d*)
							 (v/add (boid :location)))
			  circle-offset {:x (* *wander-r* (Math/cos (boid :theta)))
							 :y (* *wander-r* (Math/sin (boid :theta)))}]
			(steer boid (v/add circle-loc circle-offset)))))

(defn move [boid boid-seq]
	(let [velocity (-> (boid :velocity)
					   (v/add (wander boid))
					   (v/add (separate boid boid-seq))
					   (v/limit *max-speed*))
		  location (-> (boid :location)
					   (v/add velocity)
					   (constrain *radius* *width* *height*))]
		{:velocity velocity
		 :location location}))
				
;; TODO use radar to retrieve boid info, as in: (net/http-get *radar-url* (fn [data] etc.
(defn update []
		(if-let [boids (reader/read-string "{}")]
			(let [change (random *min-change* *max-change*)
				  boid-ref (swap! boid assoc :theta (+ (@boid :theta) change))
				  move (move boid-ref (vals (dissoc boids (boid-ref :pid))))]
				(do
					(swap! boid assoc 
						:velocity (move :velocity) 
						:location (move :location))
					(net/udp-send *radar-udp-port* @boid)))))

(defn -main [& _]
	(js/setInterval update 100))

(set! *main-cli-fn* -main)
