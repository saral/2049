(ns nines.logic
  (:require [nines.util :as util]
            [nines.entities :refer [new-tile new-tile-disappearance-event new-tile-appearance-event new-tile-slide-event new-countdown-event]]
            ))
(enable-console-print!)

(def logic-settings
  {:min-content 8
   :max-content 18})

; generate-new-tile-event
; generate-new-tile-event
; generate-new-tile-event

(defn- is-full [board]
  (let [dimensions (:dimensions board)
        width      (:width dimensions)
        height     (:height dimensions)
        cell-count (* width height)
        pos-count  (count (keys (:tiles board)))]
    (>= pos-count cell-count)))

(defn- tile-at-position [{:keys [tiles]} pos]
  (first
   (filterv #(= pos (:pos %))
            (vals tiles))))

(defn- random-free-position [board]
  (let [dimensions (:dimensions board)
        width      (:width dimensions)
        height     (:height dimensions)
        rand-x     (js/parseInt (* width (js/Math.random)))
        rand-y     (js/parseInt (* height (js/Math.random)))
        rand-pos   {:x rand-x :y rand-y}]
    (if (nil? (tile-at-position board rand-pos))
      rand-pos
      (random-free-position board))))

(defn random-content []
  (let [max-content (:max-content logic-settings)
        min-content (:min-content logic-settings)
        range       (- max-content min-content)]
    (+ min-content (js/parseInt (* range (js/Math.random))))))

(defn generate-new-tile-events [{:keys [board]} event]
  (if (not (is-full board))
    (let [pos           (random-free-position board)
          content       (random-content)
          tile          (new-tile (util/next-id! :tile) content (:x pos) (:y pos))]
      [(new-tile-appearance-event tile)])))


; ---
; ---
; generate-countdown-events

(defn new-content [tile]
  (dec (:content tile)))

(defn new-countdown-events [tile new-content]
  (if (< new-content 0)
    (new-tile-disappearance-event tile)
    (new-countdown-event (:id tile) new-content)))

(defn generate-countdown-events [model event]
  (let [tiles           (vals (:tiles (:board model)))
        tiles-to-count  (filterv #(:counting? %) tiles)]
    (map #(new-countdown-events % (new-content %)) tiles-to-count)))


; generate-move-events
; generate-move-events
; generate-move-events

(def orientation {:left  {:row :y :col :x :dir-fn <}
                  :right {:row :y :col :x :dir-fn >}
                  :up    {:row :x :col :y :dir-fn <}
                  :down  {:row :x :col :y :dir-fn >}})

(defn is-not-on-same-row-according-to-direction [pos reference-pos direction]
  (let [orientation  (direction orientation)
        pos-row      ((:row orientation) pos)
        ref-pos-row  ((:row orientation) reference-pos)]
    (not (= pos-row ref-pos-row))))

(defn is-before-col-according-to-direction [pos reference-pos direction]
  (let [orientation  (direction orientation)
        pos-col      ((:col orientation) pos)
        ref-pos-col  ((:col orientation) reference-pos)
        dir-fn       (:dir-fn orientation)]
    (dir-fn pos-col ref-pos-col)))

(defn is-in-direction-from-position [pos reference-pos direction]
  (if (is-not-on-same-row-according-to-direction pos reference-pos direction)
    false
    (is-before-col-according-to-direction pos reference-pos direction)))

(defn tiles-in-direction-from-position [tiles direction pos]
  (filterv #(is-in-direction-from-position (:pos %) pos direction) tiles))

(defn positions-in-direction-from-position [positions direction pos]
  (filterv #(is-in-direction-from-position (:new-pos %) pos direction) positions))

(defn first-col-in-orientation [orientation dimensions]
  (let [dir-fn  (:dir-fn orientation)
        col-key (:col orientation)]
    (if (= dir-fn <)
      0
      (- ((if (= :x col-key) :width :height) dimensions) 1))))

(defn first-pos-in-dir [reference-pos direction dimensions]
  (let [orientation (direction orientation)
        dir-fn      (:dir-fn orientation)
        row-key     (:row orientation)
        col-key     (:col orientation)
        row         (row-key reference-pos)]
    {row-key row
     col-key (first-col-in-orientation orientation dimensions)}))

(defn next-pos-in-dir [reference-pos direction]
  (let [orientation (direction orientation)
        dir-fn      (:dir-fn orientation)
        row-key     (:row orientation)
        col-key     (:col orientation)
        row         (row-key reference-pos)
        col         (col-key reference-pos)]
  {row-key row
   col-key ((if (= dir-fn <) + -) col 1)}))

(defn non-merging-tile-at-pos [tile-positions pos]
  (first
   (filterv #(and
              (= pos (:new-pos %))
              (nil?  (:merging-with %)))
            tile-positions)))

(defn last-tile-position-in-direction [tile-positions direction reference-pos]
  (let [orientation (direction orientation)
        row-key     (:row orientation)
        col-key     (:col orientation)
        dir-fn      (:dir-fn orientation)
        comp-fn     #(dir-fn (col-key %1) (col-key %2))
        positions-in-dir (positions-in-direction-from-position (vals tile-positions) direction reference-pos)
        last-tile-pos    (last (sort-by :new-pos comp-fn positions-in-dir))]
    (if (nil? last-tile-pos)
      nil
      (non-merging-tile-at-pos positions-in-dir (:new-pos last-tile-pos)))))

(declare calculate-tile-positions)

(defn calculate-tile-position [tiles direction dimensions current-tile-positions tile]
  (if (contains? current-tile-positions (:id tile))
    current-tile-positions
    (let [tiles-in-dir       (tiles-in-direction-from-position tiles direction (:pos tile))
          new-tile-positions (calculate-tile-positions tiles-in-dir direction current-tile-positions dimensions)
          last-tile-pos-in-dir (last-tile-position-in-direction new-tile-positions direction (:pos tile))]
      (if (nil? last-tile-pos-in-dir)
        (assoc new-tile-positions
          (:id tile)
          {:tile tile
           :new-pos (first-pos-in-dir (:pos tile) direction dimensions)})
        (if (= (:content (:tile last-tile-pos-in-dir)) (:content tile))
          (assoc new-tile-positions
            (:id tile)
            {:tile tile
             :new-pos      (:new-pos last-tile-pos-in-dir)
             :merging-with (:tile    last-tile-pos-in-dir)})
          (assoc new-tile-positions
            (:id tile)
            {:tile tile
             :new-pos (next-pos-in-dir (:new-pos last-tile-pos-in-dir) direction)}))))))

(defn calculate-tile-positions [tiles direction current-tile-positions dimensions]
  (reduce (partial calculate-tile-position tiles direction dimensions) current-tile-positions tiles))

(defn generate-move-events [{:keys [board] :as model} direction]
  (let [tiles          (vals (:tiles board))
        tile-positions (calculate-tile-positions tiles direction {} (:dimensions board))]
    (map #(new-tile-slide-event
           (first %)
           (:new-pos      (second %))
           (:merging-with (second %)))
         tile-positions)))

