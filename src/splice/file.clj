(ns splice.file
  (:import (java.io File
                    FileFilter)
           (javax.imageio ImageIO))
  (:require [splice.param :as param])
  (:gen-class))
(defn- list-files [^File file]
  (.listFiles file (reify FileFilter
                     (accept [this f]
                       (let [file-name (clojure.string/lower-case (.getName f))]
                         (not-every? false? (cons (.isDirectory f)
                                                  (for [suffix param/image-suffix]
                                                    (.endsWith file-name (str "." suffix))))))))))
(defn- tree [^File file]
  (if (.isDirectory file)
    {(File. (str (.getPath param/output-folder) "/" (.getPath file)))
     (reduce (partial conj {}) (for [f (list-files file)] (tree f)))}
    {file (ImageIO/read file)}))
(defn validate [file-names]
  (let [{files true, files-not-exists false} (group-by #(.exists %) (map #(File. %) file-names))]
    (if (pos? (count files-not-exists))
      (throw (AssertionError. (str "files: " (apply str (interpose " " files-not-exists)) " NOT exists.")))
      {param/output-folder (apply conj (map tree (case (count files)
                                                   0 (list-files (File. "."))
                                                   1 (list-files (first files))
                                                   files)))})))
(defn tree-flatten [m]
  (if @param/flat
    (reduce (partial conj {}) (filter #(and (map-entry? %) ((comp not map? val) %)) (tree-seq coll? identity m)))
    m))
