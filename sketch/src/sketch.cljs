(ns boids.sketch
	(:require [cljs.reader :as reader]
			  [goog.net.XhrIo :as xhr]))
			
(defn run [p]
	(set! (.-setup p)
		(fn []
			(.size p 400 400)
			(.colorMode p (.-RGB p), 255, 255, 255, 100)
			(. p (smooth))))
	(set! (.-draw p)
		(fn []
			(xhr/send "http://localhost:1337/boids" 
				(fn [msg]
					(.background p 80)
					(let [r 4.0
						  boids (reader/read-string (. msg/target (getResponseText)))]
						(dorun 
							(map 
								(fn [b]
									(let [vel (b :velocity)
										  loc (b :location)
										  angle (Math/atan2 (- (vel :y)) (vel :x))
										  theta (+ (* -1 angle) (.radians p 90))
										  color (+ 0xFF000000 (js/parseInt (b :color) 16))]
										(. p (pushMatrix))
										(.translate p (loc :x) (loc :y))
										(.stroke p 255)
										(.fill p 255)
										(.text p (b :pid) (* r 2) (* r -2))
										(.rotate p theta)
										(.beginShape p (.-TRIANGLES p))
										(.fill p color)
										(.vertex p 0 (* r -2))
										(.vertex p (- r) (* r 2))
										(.vertex p r (* r 2))
										(. p (endShape))
										(. p (popMatrix))))
								(vals boids)))))))))
							