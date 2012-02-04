(ns boids.net
	(:require [cljs.nodejs :as node]
			  [cljs.reader :as reader]))
	
(def socket (. (node/require "dgram") (createSocket "udp4")))
		
(defn udp-send [port data]
	(let [buf (js/Buffer. (pr-str data))]
		(. socket (send buf 0 (.-length buf) port "127.0.0.1"))))
		
(defn udp-listen [port callback]
	(. socket (on "message" 
		(fn [msg rinfo]
			(callback (reader/read-string (.toString msg "utf8"))))))
	(. socket (bind port))
	(.-port (. socket (address))))

(defn http-get [opts callback]
	(let [options (.-strobj { 
			"host" (opts :host) 
			"port" (opts :port)
			"path" (opts :path) })]
		(. (node/require "http") (get options (fn [response]
			(. response (on "data" (fn [data] (callback (.toString data "utf8"))))))))))
