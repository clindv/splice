(ns splice.param-test
  (:require [clojure.test :refer :all]
            [splice.param :as param]))
(deftest param-test
  (testing "init normal"
    (is (= (param/init ["-test" "null"]) ["null"]) "init test without hyphen of null")
    (is (= true @param/testing) "avoid conflict var of test")
    (is (= false @param/null) "without hyphen of null counts as name")
    (is (= ["null"] @param/names) "names"))
 (testing "init twice"
    (is (thrown? AssertionError (param/init ["-" "-nonexistent"])) "test nonexistent parameter throw")
    (is (= true @param/null) "refresh params before throw err")
    (is (= false @param/number) "not triggered")
    (is (= false @param/names) "refresh names"))
  (testing "init number"
    (is (thrown-with-msg? AssertionError #"-13-- in -13-t\- is invalid, -h for help"
                          (param/init ["-11t" "--12" "---" "---13-t-" "radix"])) "test invalid number throw")
    (is (= 12 @param/number) "number duplicate init")
    (is (= true @param/null) "null hyphen init")
    (is (= false @param/names) "invalid number thrown before init names"))
  (testing "init help"
    (is (= (with-out-str (param/init ["----h"]))
           "(- -help -h -test -t -flat -f -real -r -unreal -u)\n") "multi hyphen to escape from lein")))
