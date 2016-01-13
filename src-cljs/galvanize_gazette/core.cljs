(ns galvanize-gazette.core
  (:require [reagent.core :as r :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(defn navbar []
  (fn []
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
           [:div.col-md-12 "All The News Thats Fit To Link"]]]]]]]]))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of galvanize-gazette... work in progress"]]])

(def app-db (r/atom {}))

(defn post-story []
  (POST "http://localhost:3000/api/story" :params {:title (get-in @app-db [:add-story (str "Title")]) 
                          :link (get-in @app-db [:add-story "Link"])
                          :imageurl (get-in @app-db [:add-story "Image URL"])
                          :summary (get-in @app-db [:add-story "summary"])}
        :handler #(println "POST SUCCESFUL!::: " % @app-db)
        :error-handler #(println "POST (un?)SUCCESFUL!::: " %)))

(defn get-stories []
  (GET "http://localhost:3000/api/story"
       {:headers {"Accept" "application/transit+json"}
        :handler #(swap! app-db assoc-in [:stories] %)}))

(defn add-story-form  [label type]
  (let [id (str "add-story-" label) type (or type "text") input-info (r/atom "")]
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


(defn home-page []
  (let [hide-form (r/atom false) input-info (r/atom "") todays-stories (doall (get-stories)
                                                                              (get-in app-db [:stories]))]
    (fn []
      (println (str todays-stories))
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
        [:div.col-md-4]]
       ])))

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
;; (secretary/set-config! :prefix "")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

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
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
