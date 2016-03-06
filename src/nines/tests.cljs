(ns nines.tests
  (:require [cljs.test :refer-macros [deftest is testing run-tests use-fixtures]]
            [nines.logic :refer      [generate-move-events]]
            [nines.util :refer       [next-id!]]
            [nines.entities :refer   [add-tile new-tile new-board new-tile-appearance-event new-tile-slide-event]]
            [nines.drawing :as draw]
            ))

(enable-console-print!)

(def tiles-at-left-board
  (-> (new-board 4 4)
      (add-tile (new-tile :testId1 30 0 0))
      (add-tile (new-tile :testId2 31 0 1))))

(def empty-board (new-board 4 4))

(deftest no-events-for-empty-board
  (is (= [] (generate-move-events {:board empty-board} :left))))

(deftest no-events-if-tile-already-at-edge
  (is (= [] (generate-move-events {:board tiles-at-left-board} :left))))

(deftest events-for-sliding-tiles
  (is (=
       [(new-tile-slide-event {:id :testId1} {:x 3 :y 0})
        (new-tile-slide-event {:id :testId2} {:x 3 :y 1})]
       (generate-move-events {:board tiles-at-left-board} :right))))

(draw/setup "game" tiles-at-left-board)

(run-tests)
