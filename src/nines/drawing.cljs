(ns nines.drawing
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [monet.canvas :as cvs]
            [monet.core :as mnt-core]
            [cljs.core.async :refer [<! timeout chan]]))

(enable-console-print!)

(def settings {:board-bg-color  "#666666"
               :tile-bg-color   "#191d21"
               :tile-text-color "#ffffff"
               :tile-text-ratio 0.6
               :cell-size      80})

(defn- new-canvas [canvas-id]
  (let [canvas-dom (.getElementById js/document canvas-id)]
    (cvs/init canvas-dom "2d")))


;; draw functions
;; draw functions
;; draw functions

(defn- draw-tile-at-rect [ctx tile rect]
  (let [cell-size    (:cell-size settings)
        txt-px-pos-x (+ (:x rect) (/ cell-size 2))
        txt-px-pos-y (+ (:y rect) (/ cell-size 2))
        font         (str (* cell-size (:tile-text-ratio settings)) "px sans-serif")]
  (-> ctx
        (cvs/fill-style (settings :tile-bg-color))
        (cvs/fill-rect  rect)
        (cvs/fill-style (settings :tile-text-color))
        (cvs/font-style font)
        (cvs/text-align :center)
        (cvs/text-baseline :middle)
      (cvs/text {:text (:content tile) :x txt-px-pos-x :y txt-px-pos-y}))))

(defn- draw-tile [ctx val]
  (let [tile         (:tile val)
        cell-size    (:cell-size settings)
        px-pos-x     (* cell-size (-> tile :pos :x))
        px-pos-y     (* cell-size (-> tile :pos :y))
        rect      {:x px-pos-x :y px-pos-y :w cell-size :h cell-size}
        ]
    (draw-tile-at-rect ctx tile rect)))

(defn- draw-board [ctx board]
  (let [cell-size (:cell-size settings)
        width  (* cell-size (-> board :dimensions :width))
        height (* cell-size (-> board :dimensions :height))
        rect {:x 0 :y 0 :w width :h height}]
    (-> ctx
      (cvs/fill-style (settings :board-bg-color))
      (cvs/fill-rect  rect))))


;; canvas entities
;; canvas entities
;; canvas entities

(defn- new-board-entity [board]
  (cvs/entity board            ; val
              nil              ; update function
              draw-board))     ; draw function

(defn- new-tile-entity [tile]
    (cvs/entity {:tile tile}  ; val
                nil           ; update function
                draw-tile))   ; draw function


;; setup
;; setup
;; setup

(defn setup [canvas-id board]
  (let [canvas (new-canvas canvas-id)]
    (cvs/add-entity canvas :board (new-board-entity board))
    (doseq [tile (vals (:tiles board))]
      (cvs/add-entity canvas (:id tile) (new-tile-entity tile)))
    canvas))


;; event-handlers
;; event-handlers
;; event-handlers

(defn- handle-tile-appearance [event canvas]
  (let [tile (:tile event)]
    (cvs/add-entity canvas (:id tile) (new-tile-entity tile))))

(defn- handle-tile-slide [event canvas]
  (let [tile-id     (:tile-id event)]

    (cvs/update-entity canvas
                       tile-id
                       (fn [entity]
                         ;; (assoc entity :val )
                         ))))

(defn- handle-event [event canvas]
  (let [handlers {:tile-slide      handle-tile-slide
                  :tile-appearance handle-tile-appearance}
        handler  ((:key event) handlers)]

    (handler event canvas)))


;; engine
;; engine
;; engine

(defn- draw-engine [ch canvas]
  (go-loop []
           (let [event (<! ch)]
             (handle-event event canvas))
           (recur)))

(defn start-engine [canvas-id board]
  (let [ch     (chan 10)
        canvas (setup canvas-id board)]
    (draw-engine ch canvas)
    ch))
