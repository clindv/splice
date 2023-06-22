(ns splice.image
  (:import (java.io File)
           (java.awt image.BufferedImage)
           (javax.imageio ImageIO))
  (:require [splice.param :as param])
  (:gen-class))
(defn- dice [{cuts :cuts cutted :cutted plats :plats transpose-flag :transpose-flag}]
  (if (empty? cuts)
    cutted
    (let [[width height] [(.getWidth (:buff (first cuts))) (.getHeight (:buff (first cuts)))]
          not-fit (take-while #(or (> width (- (:border-x %) (:offset-x %)))
                                   (> height (- (:border-y %) (:offset-y %)))) plats)
          behind-cut (nthrest plats (inc (count not-fit)))
          {offset-x :offset-x offset-y :offset-y border-x :border-x border-y :border-y} (nth plats (count not-fit))
          new-plats (list
                     (if transpose-flag
                       {:offset-x offset-x :offset-y (+ offset-y height)
                        :border-x (+ offset-x width) :border-y border-y}
                       {:offset-x (+ offset-x width) :offset-y offset-y
                        :border-x border-x :border-y (+ offset-y height)})
                     (if transpose-flag
                       {:offset-x (+ offset-x width) :offset-y offset-y
                        :border-x border-x :border-y border-y}
                       {:offset-x offset-x :offset-y (+ offset-y height)
                        :border-x border-x :border-y border-y}))]
      (recur {:cuts (rest cuts)
              :cutted (conj cutted (into (first cuts) {:offset-x offset-x
                                                       :offset-y offset-y
                                                       :border-x (+ offset-x width)
                                                       :border-y (+ offset-y height)}))
              :plats (concat not-fit new-plats behind-cut)
              :transpose-flag transpose-flag}))))
(defn- outline [images-file-buff]
  (let [images (map (comp (partial apply hash-map) (partial interleave [:file :buff])) images-file-buff)
        size-sqrt (inc (long (Math/sqrt (apply + (map #(* (.getWidth (:buff %)) (.getHeight (:buff %))) images)))))
        width-max (apply max (map #(.getWidth (:buff %)) images))
        height-max (apply max (map #(.getHeight (:buff %)) images))
        transpose-flag (not (< height-max size-sqrt))
        border-proximal (if transpose-flag (max height-max size-sqrt) (max width-max size-sqrt))]
    {:cuts (sort-by (if transpose-flag #(.getWidth (:buff %)) #(.getHeight (:buff %))) > images)
     :cutted []
     :plats [{:offset-x 0
              :offset-y 0
              :border-x (if transpose-flag (apply + (map #(.getWidth (:buff %)) images)) border-proximal)
              :border-y (if transpose-flag border-proximal (apply + (map #(.getHeight (:buff %)) images)))}]
     :transpose-flag transpose-flag}))
(defn- log [images folder]
  (spit (.getPath (File. folder "image-list.txt"))
        (apply conj (for [image images]
                      {(.getPath (:file image)) (vec ((juxt :offset-x :offset-y :border-x :border-y) image))}))))
(defn- draw [images ^File folder suffix]
  (let [[size-x size-y] (apply (partial map max) (map (juxt :border-x :border-y) images))
        image-file (File. folder (str (.getName param/output-folder) "." suffix))
        buff (BufferedImage. size-x size-y BufferedImage/TYPE_INT_ARGB)
        graphics (.createGraphics buff)]
    (doseq [image images]
      (.drawImage graphics (:buff image) ^Long (:offset-x image) ^Long (:offset-y image) nil))
    (ImageIO/write buff suffix image-file))
  images)
(defn suture [images-file-buff ^File folder suffix]
  (.mkdir folder)
  (if (not (empty? images-file-buff))
    (-> images-file-buff outline dice (draw folder suffix) (log folder))))
(defn sunder [file-tree]
  (let [{folders true, files false} (group-by (comp map? val) (first (vals file-tree)))
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
    (doseq [folder folders] (sunder (apply hash-map folder)))))
