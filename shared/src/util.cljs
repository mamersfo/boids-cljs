(ns boids.util
	(:require [cljs.nodejs :as node]))
	
(def url (node/require "url"))
(def http (node/require "http"))
	
(defn parse-url [u]
	(let [raw (js->clj (.parse url u))]
		{:query (get raw "query")
		 :path (get raw "pathname")}))
	
