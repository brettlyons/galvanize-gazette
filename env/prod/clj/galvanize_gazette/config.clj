(ns galvanize-gazette.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[galvanize-gazette started successfully]=-"))
   :middleware identity})
