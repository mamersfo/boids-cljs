(ns boids.radar
	(:require [cljs.nodejs :as node]
			  [boids.net :as net]))
	
(def url (node/require "url"))
(def http (node/require "http"))
(def fs (node/require "fs"))

(def boids (atom {}))	
	
(defn receive [boid]
	(swap! boids assoc (boid :pid) boid))
	
(defn parse-url [u]
	(let [raw (js->clj (.parse url u))]
		{:query (get raw "query")
		 :path (get raw "pathname")}))

(defn handler [req res]
	(let [path (:path (parse-url (.-url req)))]
		(cond
			(= path "/boids")
				(do
					(. res (writeHead 200))
					(. res (end (pr-str @boids))))
			:else
				(. fs (readFile (subs path 1)
					(fn [_ data]
						(do
							(. res (writeHead 200))
							(. res (end data)))))))))
					
(defn start [& _]
	(let [server (. http (createServer handler))
		  host "localhost"
		  port 1337]
		(net/udp-listen 41234 receive)
		(. server (listen port host))
		(println (str "Server running at http://"host":"port))))

(set! *main-cli-fn* start)