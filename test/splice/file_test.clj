(ns splice.file-test
  (:require [clojure.test :refer :all]
            [splice.param :as param]
            [splice.file :as file]))
(defn- walkie [a x] (swap! a conj (type x)) (clojure.walk/walk (partial walkie a) identity x))
(deftest file-test
  (testing "validate without input files"
    (let [atom-empty (atom nil)
          atom-dot (atom nil)
          _ (walkie atom-empty (file/validate []))
          _ (walkie atom-dot (file/validate ["."]))]
      (is (= @atom-empty @atom-dot))))
  (testing "validate files not exists"
    (is (thrown-with-msg? AssertionError #"files: file-not-exists-.*? NOT exists."
                          (file/validate (repeatedly (inc (rand-int 10)) #(str "file-not-exists-" (rand-int 1e8))))))
    (is (thrown? AssertionError (file/validate (list "." (str "file-not-exists-" (rand-int 1e8))))) "partially"))
  (testing "file tree flatten"
    (reset! param/flat true)
    (is (= (file/tree-flatten {:a {:b 1 :c 2 :a 3} :c 4}) {:a 3 :b 1 :c 4}))
    (reset! param/flat false)))
