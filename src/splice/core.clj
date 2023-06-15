(ns splice.core
  (:require [splice.param :as param]
            [splice.file :as file])
  (:gen-class))
(defn -main
  [& args]
  (-> args param/init file/validate file/tree-flatten))
