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
                              :height "300px"
                              :width "100%"}}
      [:p]
      [:div.row
       [:div.col-md-offset-1
        [:a {:href "/" :style {:color "black"}}
         [:div.col-md-5 {:style {:margin-top "40px" :opacity "0.7" :border-style "solid" :border-color "black" :border-width "2px" :background-color "white"}}
          [:div.row.h1
           [:div.col-md-12 "Galvanize Gazette"]]
          [:div.row.h3
           [:div.col-md-12"All The News Thats Fit To Link"]]]]]]]]))



(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of galvanize-gazette... work in progress"]]])

(defn add-story-form  [label type]
  (let [id (str "add-story-" label) type (or type "text")]
    (fn []
      [:div.form-group.form-group-md
       [:div.col-md-2
        [:label {:for id :style {:font-size "18px"}} (str label " ")]]
       [:div.col-md-4
        [:input.form-control {:type type :id id}]]])))

(defn home-page []
  (let [hide-form (r/atom false)]
    (fn [] 
      [:div.container
       [:div.row
        [:div.col-md-12
         [:div.h1 "Add A Story"]
         [:a.pull-right {:on-click #(swap! hide-form not)} (str (if @hide-form "Show" "Hide"))]
         [:br]
         [:br]
         [:form.form-horizontal {:hidden @hide-form}
          [add-story-form "Title:"]
          [add-story-form "Link:" "url"]
          [add-story-form "Image URL:" "url"]
          [:div.form-group.form-group-md
           [:div.col-md-2
            [:label {:for "add-story-summary" :style {:font-size "18px"}} (str "Summary:" " ")]]
           [:div.col-md-6
            [:textarea.form-control {:rows "4" :id "add-story-summary"}]]]
          [:div.col-md-offset-2
           [:button.btn.btn-lg.btn-primary "Link it up!"]]
          ]]]
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
