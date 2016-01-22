(ns galvanize-gazette.routes.services
  (:require [ring.util.http-response :refer :all]
            [galvanize-gazette.db.core :as db]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

(s/defschema Thingie {:id Long
                      :hot Boolean
                      :tag (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

(defapi service-routes
  (ring.swagger.ui/swagger-ui
   "/swagger-ui")
  ;JSON docs available at the /swagger.json route
  (swagger-docs
    {:info {:title "Sample api"}})
  (context* "/api" []
            :tags ["thingie"]

            (POST* "/story" [] (fn [req]
                                 (println (str "THE PARAMS OF STORY POST REQUEST: " (:params req)))
                                 :summary "Puts a story into the db"
                                 (db/create-story! {:title (get-in req [:params :title])
                                                    :link (get-in req [:params :link])
                                                    :imageurl (get-in req [:params :imageurl])
                                                    :summary (get-in req [:params :summary])})
                                 (ok)))
            (POST* "/opinions" [] (fn [req]
                                    (println (str "THE PARAMS OF OPINION POST REQUEST: " (:params req)))
                                    :summary "Puts an opinion into the db"
                                    (db/create-opinion! {:story_id (get-in req [:params :story_id])
                                                       :content (get-in req [:params :content])})
                                    (ok)))
            
            (GET* "/stories" []
                  :query-params []
                  :summary "Returns a list of stories from the db"
                  (ok (db/get-stories)))
            

            (GET* "/opinions/:id" [id]
                  :query-params []
                  :summary "Returns the opinions associated with a story from the db"
                  (ok (db/get-opinions {:story_id id})))

            
            (GET* "/plus" []
                  :return       Long
                  :query-params [x :- Long, {y :- Long 1}]
                  :summary      "x+y with query-parameters. y defaults to 1."
                  (ok (+ x y)))

            (POST* "/minus" []
                   :return      Long
                   :body-params [x :- Long, y :- Long]
                   :summary     "x-y with body-parameters."
                   (ok (- x y)))

            (GET* "/times/:x/:y" []
                  :return      Long
                  :path-params [x :- Long, y :- Long]
                  :summary     "x*y with path-parameters"
                  (ok (* x y)))

            (POST* "/divide" []
                   :return      Double
                   :form-params [x :- Long, y :- Long]
                   :summary     "x/y with form-parameters"
                   (ok (/ x y)))

            (GET* "/power" []
                  :return      Long
                  :header-params [x :- Long, y :- Long]
                  :summary     "x^y with header-parameters"
                  (ok (long (Math/pow x y))))

            (PUT* "/echo" []
                  :return   [{:hot Boolean}]
                  :body     [body [{:hot Boolean}]]
                  :summary  "echoes a vector of anonymous hotties"
                  (ok body))

            (POST* "/echo" []
                   :return   (s/maybe Thingie)
                   :body     [thingie (s/maybe Thingie)]
                   :summary  "echoes a Thingie from json-body"
                   (ok thingie)))

  (context* "/context" []
            :tags ["context*"]
            :summary "summary inherited from context"
            (context* "/:kikka" []
                      :path-params [kikka :- s/Str]
                      :query-params [kukka :- s/Str]
                      (GET* "/:kakka" []
                            :path-params [kakka :- s/Str]
                            (ok {:kikka kikka
                                 :kukka kukka
                                 :kakka kakka})))))
