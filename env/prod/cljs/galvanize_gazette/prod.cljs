(ns galvanize-gazette.app
  (:require [galvanize-gazette.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
