(ns splice.image
  (:import (java.io File
                    FileFilter)
           (javax.imageio ImageIO))
  (:require [splice.param :as param])
  (:gen-class))
(defn suture [folder images suffix]
  ())
(defn sunder [file-tree]
  (let [{folders true, files false} (group-by (comp map? val) file-tree)
        {real true, lossy false}
        (group-by #(not-every? false? (map (fn [suffix] (.endsWith (.getName (key %)) suffix))
                                           param/real-suffix)) files)]
    (case [(boolean @param/real) (boolean @param/lossy)]
      [false false] (do (suture (ffirst file-tree) real (first param/real-suffix))
                        (suture (ffirst file-tree) lossy (first param/lossy-suffix)))
      [true false] (suture (ffirst file-tree) files (first param/real-suffix))
      [false true] (suture (ffirst file-tree) files (first param/lossy-suffix))
      [true true] (do (suture (ffirst file-tree) files (first param/real-suffix))
                      (suture (ffirst file-tree) files (first param/lossy-suffix))))
    (if (empty? folders) nil (recur (val (first folders))))))
