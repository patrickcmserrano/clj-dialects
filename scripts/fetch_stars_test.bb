(ns fetch-stars-test
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [clojure.edn :as edn]))

;; Simular a lógica de enriquecimento do script original
(defn enrich-dialeto [d]
  (if (:repo d)
    (assoc d :stars 100) ;; Mocking stars
    d))

(deftest test-enrich-dialeto
  (testing "Dialeto com repo deve ganhar campo :stars"
    (let [d {:nome "Test" :repo "user/repo"}
          result (enrich-dialeto d)]
      (is (contains? result :stars))
      (is (= 100 (:stars result)))))

  (testing "Dialeto sem repo deve permanecer inalterado"
    (let [d {:nome "Lisp"}
          result (enrich-dialeto d)]
      (is (not (contains? result :stars)))
      (is (= d result)))))

(let [summary (run-tests)]
  (when (pos? (+ (:fail summary) (:error summary)))
    (System/exit 1)))
