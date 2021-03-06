(ns nines.tests
  (:require [cljs.test :refer-macros [deftest is testing run-tests use-fixtures]]
            [nines.logic :refer      [generate-move-events
                                      is-in-direction-from-position
                                      is-before-col-according-to-direction
                                      is-not-on-same-row-according-to-direction
                                      tiles-in-direction-from-position]]
            [nines.util :refer       [next-id!]]
            [nines.entities :refer   [add-tile new-tile new-board new-tile-appearance-event new-tile-slide-event]]
            [nines.drawing :as draw]
            ))

(enable-console-print!)

(def empty-board (new-board 4 4))

(def tiles-at-left-board
  (-> (new-board 4 4)
      (add-tile (new-tile :testId1 30 0 0))))

(def board1
  (-> (new-board 4 4)
      (add-tile (new-tile :testId1 30 0 3))
      (add-tile (new-tile :testId2 31 1 2))
      (add-tile (new-tile :testId3 32 1 3))
      ))

(defn tile-with-id [id board]
  (first (filter #(= id (:id %)) (vals (:tiles board)))))

; generate-move-events
; generate-move-events
; generate-move-events

(deftest no-events-for-empty-board
  (is (= [] (generate-move-events {:board empty-board} :left))))

(deftest no-events-if-tile-already-at-edge
  (is (= [] (generate-move-events {:board tiles-at-left-board} :left))))

(deftest events-for-sliding-tiles
  (is (=
       [(new-tile-slide-event {:id :testId1} {:x 3 :y 0} nil)]
       (generate-move-events {:board tiles-at-left-board} :right))))


; ---
; ---
; tiles-in-dir

(deftest tiles-in-direction-from-position--success
  (is (=
       [(tile-with-id :testId1 tiles-at-left-board)]
       (tiles-in-direction-from-position (vals (:tiles tiles-at-left-board)) :left {:x 3 :y 0}))))

(deftest tiles-in-direction-from-position--success-down-empty
  (is (=
       []
       (tiles-in-direction-from-position (vals (:tiles tiles-at-left-board)) :down {:x 2 :y 0}))))

(deftest tiles-in-direction-from-position--success-down
  (is (=
       [(tile-with-id :testId2 board1) (tile-with-id :testId3 board1)]
       (tiles-in-direction-from-position (vals (:tiles board1)) :down {:x 1 :y 0}))))


; ---
; ---
; is-before-col-according-to-direction

(deftest is-before-col-according-to-direction--success-horizontal
  (is (=
       true
       (is-before-col-according-to-direction {:x 0 :y 0} {:x 1 :y 0} :left))))

(deftest is-before-col-according-to-direction--error-horizontal
  (is (=
       false
       (is-before-col-according-to-direction {:x 2 :y 0} {:x 1 :y 0} :left))))

(deftest is-before-col-according-to-direction--error-same
  (is (=
       false
       (is-before-col-according-to-direction {:x 1 :y 0} {:x 1 :y 0} :left))))

(deftest is-before-col-according-to-direction--success-vertical
  (is (=
       true
       (is-before-col-according-to-direction {:x 0 :y 2} {:x 0 :y 1} :down))))

(deftest is-before-col-according-to-direction--error-vertical
  (is (=
       false
       (is-before-col-according-to-direction {:x 0 :y 2} {:x 0 :y 3} :down))))

(deftest is-before-col-according-to-direction--success-vertical-up
  (is (=
       true
       (is-before-col-according-to-direction {:x 0 :y 2} {:x 0 :y 3} :up))))


; ---
; ---
; is-not-on-same-row-according-to-direction

(deftest is-not-on-same-row-according-to-direction--success
  (is (=
       true
       (is-not-on-same-row-according-to-direction {:x 0 :y 0} {:x 1 :y 1} :left))))

(deftest is-not-on-same-row-according-to-direction--error
  (is (=
       false
       (is-not-on-same-row-according-to-direction {:x 0 :y 1} {:x 1 :y 1} :left))))

(deftest is-not-on-same-row-according-to-direction--success-horizontal
  (is (=
       true
       (is-not-on-same-row-according-to-direction {:x 0 :y 0} {:x 1 :y 0} :down))))

(deftest is-not-on-same-row-according-to-direction--error-horizontal
  (is (=
       false
       (is-not-on-same-row-according-to-direction {:x 1 :y 0} {:x 1 :y 0} :up))))


; ---
; ---
; is-in-direction-from-position

(deftest is-in-direction-from-position--same-row-success
  (is (=
       true
       (is-in-direction-from-position {:x 0 :y 0} {:x 1 :y 0} :left))))

(deftest is-in-direction-from-position--different-row
  (is (=
       false
       (is-in-direction-from-position {:x 0 :y 0} {:x 1 :y 1} :left))))


; tests
; tests
; tests

; (draw/setup "game" tiles-at-left-board)

; (run-tests)
