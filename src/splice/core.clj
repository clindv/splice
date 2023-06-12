(ns splice.core
  (:require [splice.param :as param])
  (:gen-class))
(defn -main
  [& args]
  (-> args param/init))
