(ns galvanize-gazette.core
(:require [reagent.core :as r :refer [atom]]
          [reagent.session :as session]
          [secretary.core :as secretary :include-macros true]
          [goog.events :as events]
          [clojure.string :as string]
          [goog.history.EventType :as HistoryEventType]
          [markdown.core :refer [md->html]]
          [ajax.core :refer [GET POST]])
(:import goog.History))

;; (def AJAX-ADDRESS "http://galvanize-gazette-demo.herokuapp.com/api")
(def AJAX-ADDRESS "http://localhost:3000/api")

;; meta-redirect for causing a redirect to a different page 

(defn navbar []
  [:div.row
   [:div.col-md-12 {:style {:background-image "url(img/devils-tower.jpg)"
                            :background-size "100%"
                            :height "200px"
                            :width "100%"}}
    [:p]
    [:div.container
     [:div.row
      [:a {:href "/" :style {:color "black"}}
       [:div.col-md-5 {:style {:margin "25px 0 25px 0" :opacity "0.7" :border-style "solid" :border-color "black" :border-width "2px" :background-color "white"}}
        [:div.row.h1
         [:div.col-md-12 "Galvanize Gazette"]]
        [:div.row.h3
         [:div.col-md-12 "All The News Thats Fit To Link"]]]]]]]])

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of galvanize-gazette... work in progress"]]])

(def app-db (r/atom {:opinions ["I loved it.  Very happy." "What a piece of junk, not very happy" "Sweet site, excellent fish."]}))

(defn post-story []
  (POST (str AJAX-ADDRESS "/story") :params {:title (get-in @app-db [:add-story (str "Title")]) 
                                             :link (get-in @app-db [:add-story "Link"])
                                             :imageurl (get-in @app-db [:add-story "Image URL"])
                                             :summary (get-in @app-db [:add-story "summary"])}
        :handler #(println "POST SUCCESFUL!::: " % @app-db)
        :error-handler #(println "POST (un?)SUCCESFUL!::: " %)))

(defn post-opinion [id content]
  (println (str AJAX-ADDRESS "/opinions" " id " id " content " content))
  (POST (str AJAX-ADDRESS "/opinions") :params {:story_id id
                                                :content content}
        :handler #(println "opinion post succesful: " %)
        :error-handler #(println "POST (un?)SUCCESFUL!::: " %)))


(defn get-stories []
  (GET (str AJAX-ADDRESS "/stories")
       {:headers {"Accept" "application/transit+json"}
        :error-handler (fn [err] (println "GET ERROR" err))
        :handler (fn [e] 
                   (swap! app-db assoc-in [:stories] e))}))

(defn get-opinions [id]
  (GET (str AJAX-ADDRESS "/opinions/" id)
       {:headers {"Accept" "application/transit+json"}
        :error-handler (fn [err] (println "GET ERROR" err))
        :handler (fn [e] 
                   (swap! app-db assoc-in [:opinions] e))}))


(defn add-story-form  [label type]
  (let [id (str "add-story-" label) type (or type "text") input-info (r/atom "")]
    (get-stories)
    (fn []
      ;; (println app-db)
      ;; (println (get-in @app-db [:add-story "Image URL"]))
      [:div.form-group.form-group-md
       [:div.col-md-2
        [:label {:for id :style {:font-size "18px"}} (str label ": ")]]
       [:div.col-md-4
        [:input.form-control {:type type
                              :id id
                              :value @input-info
                              :on-change (fn [e] (reset! input-info (.-target.value e))
                                           (swap! app-db assoc-in [:add-story label] @input-info))}]]])))

(defn story-summary [story]
  [:div.col-md-4
   [:a {:href (str "#/stories/" (:id story)) :on-click #(swap! app-db assoc-in [:stories-info-page] story)}
    [:div {:style {:width "100%" :height "200px" :background-size "100%"
                   :background-image (str "url(" (:imageurl story) ")")}}]]
   [:div.h1 [:a {:href (str "#/stories/" (:id story))
                 :on-click #(swap! app-db assoc-in [:stories-info-page] story)} (:title story)]]])


(defn home-page []
  (get-stories)
  (let [hide-form (r/atom false) input-info (r/atom "") todays-stories (get-in app-db [:stories])]
    (fn []
      [:div.container
       [:div.row
        [:a.pull-right {:on-click #(swap! hide-form not)} (str (if @hide-form "Show" "Hide"))]
        [:div.col-md-12 {:hidden @hide-form}
         [:div.h1 "Add A Story"]
         [:br]
         [:br]
         [:form.form-horizontal 
          [add-story-form "Title" "text"]
          [add-story-form "Link" "url"]
          [add-story-form "Image URL" "url"]
          [:div.form-group.form-group-md
           [:div.col-md-2
            [:label {:for "add-story-summary" :style {:font-size "18px"}} (str "Summary:" " ")]]
           [:div.col-md-6
            [:textarea.form-control {:rows "4" :id "add-story-summary"
                                     :on-change (fn [e] (reset! input-info (.-target.value e))
                                                  (swap! app-db assoc-in [:add-story "summary"] @input-info))}]]]
          [:div.col-md-offset-2
           [:button.btn.btn-lg.btn-primary {:on-click (fn [e]
                                                        (post-story)
                                                        (get-stories))} "Link it up!"]]
          ]]]
       [:div.row
        [:div.col-md-12.h1 "Today's News"]]
       [:div.row
        (for [story (get-in @app-db [:stories])]
          ^{:key (:id story)}[story-summary story])]])))

(def opinions ["I loved it.  Very happy." "What a piece of junk, not very happy"])

(defn custom-words-filter?
  [word]
  (let [wordlist ["the" "a" "i" "it" "of" "not" ""]]
    (some (partial = word) wordlist)))

(defn word-frequencies
  [wordslist]
  (filter #(not (custom-words-filter? (first %)))
          (frequencies
           (string/split
            (.toLowerCase 
             (string/join " " wordslist)) #"\W"))))

(defn stories-page []
  (get-stories)
  (println "stories" (get-in @app-db [:stories]))
  (println "lah" (into {} (map #(hash-map (:id %) %) (:stories @app-db))))
  ;; (println (get-in @app-db [:stories-info-page]))
  ;; (println "stories" (get-in @app-db [:stories-info-page :link]))
  ;; (println (word-frequencies (:opinions app-db)))
  ;; (swap! app-db assoc-in [:story-by-id (get-in [:stories :id] @app-db)])
  
  ;; (println (str "PATHNAME? " js/window.location))
  (fn []
    (get-stories)
    (let [id (get-in @app-db [:story-page-id]) story (get-in @app-db [:stories-info-page]) input-info (r/atom "")]
      [:div.container
       ;;[:div.h1 (get-in @app-db [:story-page-id])]
       [:div.row
        [:div.col-md-12.h1 (:title story)]]
       [:div.row
        [:div.col-md-4 
         [:div {:style {:width "100%" :height "200px" :background-size "100%"
                        :background-image (str "url(" (:imageurl story) ")")}}]]
        [:div.col-md-8.h2 (:summary story)]
        [:p]
        [:div.col-md-8.h2
         [:a {:href (:link story)} "View Site"]]]
       [:div.row
        [:div.col-md-7.h1 "Opinions"]
        [:div.col-md-5.h1 "Expert Analysis"]
        [:div.col-md-6
         [:div.form-group.form-group-md
          [:label {:for "opinion" :style {:font-size "18px"}} "Whats your opinion?"]
          [:textarea.form-control {:rows "2" :id "opinion"
                                   :on-change (fn [e] (reset! input-info (.-target.value e)))}]
          [:p]
          [:button.btn.btn-md.btn-primary {:on-click (fn [e]
                                                       (println (get-in @app-db [:opinions]))
                                                       (println id input-info)
                                                       (post-opinion id @input-info)
                                                       (swap! app-db assoc-in [:opinions] (cons @input-info (get-in @app-db [:opinions]))))}
           "Opine"]]
         (for [opinion (get-in @app-db [:opinions])]
           ^{:key (first opinion)} [:div
                                    [:h3 opinion]
                                    [:hr {:size "10" :noshade "true"}]])]
        [:div.col-md-offset-4
         [:div.col-md-7
          [:div.col-md-offset-4
           ;; (frequencies (clojure.string/split (clojure.string/join " " test-list) #" ")) --> this will return {word n word2 n2 . . . wordnk nk}
           [:div.col-md-8.h2 [:ul
                              (for [word (word-frequencies (:opinions @app-db))]
                                ^{:key (first word)} [:li (str (first word) "(" (second word) ")")])]]]]]
        ]
       ]
      )))

(def pages
  {:home #'home-page
   :about #'about-page
   :stories #'stories-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

(secretary/defroute stories-with-id "/stories/:id" {:as params}
  (println "ROUTING HAPPEND " params)
  (println (:id params))
  (swap! app-db assoc-in [:story-page-id] (:id params))
  (println (get-in @app-db [:story-page-id]))
  (session/put! :page :stories))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-components))
