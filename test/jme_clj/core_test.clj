(ns jme-clj.core-test
  (:require [clojure.test :refer :all]
            [jme-clj.core :refer :all])
  (:import (com.jme3.math Vector3f Ray ColorRGBA Vector2f Quaternion)))

(deftest quat-test
  (testing "Instantiation of Quaternions"
    (let [q (quat)]
      (let [expected true
            actual (instance? Quaternion q)]
        (is (= actual expected)))
      (let [expected 0.0
            actual (.getX q)]
        (is (= actual expected)))
      (let [expected 0.0
            actual (.getY q)]
        (is (= actual expected)))
      (let [expected 0.0
            actual (.getZ q)]
        (is (= actual expected)))
      (let [expected 1.0
            actual (.getW q)]
        (is (= actual expected))))
    (let [q  (quat 1 2 3)]
      (let [expected true
            actual (instance? Quaternion q)]
        (is (= actual expected)))
      (let [expected 1.0
            actual (.getX q)]
        (is (= actual expected)))
      (let [expected 2.0
            actual (.getY q)]
        (is (= actual expected)))
      (let [expected 3.0
            actual (.getZ q)]
        (is (= actual expected)))
      (let [expected 1.0
            actual (.getW q)]
        (is (= actual expected))))
    (let [q  (quat (quat 1 2 3) (quat 4 5 6) 1)]
      (let [expected true
            actual (instance? Quaternion q)]
        (is (= actual expected)))
      (let [expected 4.0
            actual (.getX q)]
        (is (= actual expected)))
      (let [expected 5.0
            actual (.getY q)]
        (is (= actual expected)))
      (let [expected 6.0
            actual (.getZ q)]
        (is (= actual expected)))
      (let [expected 1.0
            actual (.getW q)]
        (is (= actual expected))))
    (let [q (quat 1 2 3 4)]
      (let [expected true
            actual (instance? Quaternion q)]
        (is (= actual expected)))
      (let [expected 1.0
            actual (.getX q)]
        (is (= actual expected)))
      (let [expected 2.0
            actual (.getY q)]
        (is (= actual expected)))
      (let [expected 3.0
            actual (.getZ q)]
        (is (= actual expected)))
      (let [expected 4.0
            actual (.getW q)]
        (is (= actual expected))))
    (let [expected nil
          actual (quat :foo 2 3)]
      (is (= actual expected)))
    (let [expected nil
          actual (quat 1 :foo 2)]
      (is (= actual expected)))
    (let [expected nil
          actual (quat 1 2 :foo)]
      (is (= actual expected)))
    (let [expected nil
          actual (quat :foo (quat 1 2 3) 3)]
      (is (= actual expected)))
    (let [expected nil
          actual (quat (quat 1 2 3) :foo 3)]
      (is (= actual expected)))
    (let [expected nil
          actual (quat (quat 1 2 3) (quat 1 2 3) :foo)]
      (is (= actual expected)))))