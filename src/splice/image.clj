(ns splice.image
  (:import (java.io File)
           (java.awt image.BufferedImage)
           (javax.imageio ImageIO))
  (:require [splice.param :as param])
  (:gen-class))
(defn suture [images suffix ^File folder]
  ())
(defn sunder [file-tree]
  (let [{folders true, files false} (group-by (comp map? val) file-tree)
        {real true, lossy false}
        (group-by #(not-every? false? (map (fn [suffix] (.endsWith (.getName (key %)) suffix))
                                           param/real-suffix)) files)]
    (case [(boolean @param/real) (boolean @param/lossy)]
      [false false] (do (suture real (ffirst file-tree) (first param/real-suffix))
                        (suture lossy (ffirst file-tree) (first param/lossy-suffix)))
      [true false] (suture files (ffirst file-tree) (first param/real-suffix))
      [false true] (suture files (ffirst file-tree) (first param/lossy-suffix))
      [true true] (do (suture files (ffirst file-tree) (first param/real-suffix))
                      (suture files (ffirst file-tree) (first param/lossy-suffix))))
    (if (empty? folders) nil (recur (val (first folders))))))