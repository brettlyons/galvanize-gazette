(ns galvanize-gazette.config
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [galvanize-gazette.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[galvanize-gazette started successfully using the development profile]=-"))
   :middleware wrap-dev})
