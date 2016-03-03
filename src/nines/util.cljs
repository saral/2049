(ns nines.util
  (:require ))
(enable-console-print!)

(defonce unique-ids (atom {}))

(defn- next-int-id! [key]
  (key
   (if (contains? @unique-ids key)
    (swap! unique-ids update key inc)
    (swap! unique-ids assoc key 0))))

(defn next-id! [key]
  (keyword (str (next-int-id! key))))

;;(println (next-id! :den))
;;(println (next-id! :den))
