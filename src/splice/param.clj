(ns splice.param
  (:gen-class))
(def parameter {(def names (atom nil)) []
                (def number (atom nil)) []
                (def null (atom nil)) [""]
                (def help (atom nil)) ["help" "h"]
                (def testing (atom true)) ["test" "t"]
                (def flat (atom nil)) ["flat" "f"]
                (def real (atom nil)) ["real" "r"]})
(def help-info (flatten (map (partial map (partial str "-")) (vals parameter))))
(def image-suffix (apply into (map var-get [(def real-suffix ["bmp" "png"]) (def unreal-suffix ["jpg" "jpeg"])])))
(def output-folder (java.io.File. (str "out_" (Long/toString (System/currentTimeMillis) 36))))
(defn init [args]
  (doseq [vars (keys parameter)] (reset! (var-get vars) false))
  (let [{params true, values false} (group-by #(clojure.string/starts-with? % "-")
                                              (map (partial clojure.string/lower-case) args))]
    (doseq [param (map #(clojure.string/replace % #"^(\-+)" "") params)]
      (if (not-every? false? (map (partial = param) (map first (vals parameter))))
        (doseq [[flag [elaboration abbreviation]] parameter
                :when (= param elaboration)]
          (reset! (var-get flag) true))
        (let [parings (reduce #(clojure.string/replace %1 (if (nil? %2) "" %2) "")
                              param (map second (vals parameter)))
              parings-read (try (read-string parings) (catch Exception e ""))]
          (if (empty? parings)
            (doseq [[flag [elaboration abbreviation]] parameter
                    :when (and (not-empty abbreviation) (.contains param abbreviation))]
              (reset! (var-get flag) true))
            (if (number? parings-read)
              (reset! number parings-read)
              (throw (AssertionError. (str "-" (if (= parings param) nil (str parings " in -"))
                                           param " is invalid, -h for help"))))))))
    (if @help (println help-info))
    (reset! names values)))
