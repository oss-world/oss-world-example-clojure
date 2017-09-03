(ns oss-world-example.core
  (:require [oss-world.api :as api]
            [fn-fx.fx-dom :as dom]
            [fn-fx.diff :refer [defui render should-update? component]]
            [fn-fx.controls :as ui]
            [clojure.java.io :as io])
  (:import (javafx.scene.image Image)))

(def id
  "A unique id for this plugin. This should be the same as the id defined in
  the plugin.edn."
  "oss-world-example")

(defui Stage
  (render [this args]
          (ui/stage
           :title "Example Plugin"
           :shown (:shown? args)
           :on-hiding {:event :hiding}
           :scene (ui/scene
                   :stylesheets (api/get-default-stylesheets)
                   :root (ui/grid-pane
                          :hgap 10
                          :vgap 10
                          :alignment :center
                          :children [(ui/label
                                      :text "Config value: "
                                     :grid-pane/column-index 0
                                     :grid-pane/column-span 1
                                     :grid-pane/row-index 0
                                     :grid-pane/row-span 1)
                                     (ui/text-field
                                      :id :config-value
                                      :text (get-in args [:config :val])
                                      :grid-pane/column-index 1
                                      :grid-pane/column-span 1
                                      :grid-pane/row-index 0
                                      :grid-pane/row-span 1)
                                     (ui/button
                                      :text "Set config value"
                                      :on-action {:event :set-config
                                                  :fn-fx/include {:config-value
                                                                  #{:text}}}
                                      :grid-pane/column-index 0
                                      :grid-pane/column-span 1
                                      :grid-pane/row-index 1
                                      :grid-pane/row-span 1)
                                     (ui/button
                                      :text "Close"
                                      :on-action {:event :close}
                                      :grid-pane/column-index 1
                                      :grid-pane/column-span 1
                                      :grid-pane/row-index 1
                                      :grid-pane/row-span 1)])))))

(def config (atom {}))

(defn set-config
  "This function stores the config value in a state atom and creates a recent activity
  to document the change."
  [val]
  (println "running?")
  (swap! config assoc :val val)
  (api/add-recent-activity [(.toString (io/resource "oss_world_example/recent-activity.png"))
                       (str "Example config value set to: " val)]))

(defn menu-clicked
  "This is passed as a callback and runs when the menu item for this plugin is
  clicked."
  []
  (let [config-value (api/get-config id)]
    (if (empty? config-value)
      (reset! config {:val "Example Value"})
      (reset! config config-value)))
  (let [data-state (atom {:config @config
                          :shown? true})
        ;; Note that data-state and the config are decoupled. If you update
        ;; config without updating data-state the ui will not change. In this
        ;; case that is fine because the ui is the only thing updating config.
        handler-fn (fn [{:keys [event] :as event-data}]
                     (println "UI Event: " event-data)
                     (case event
                       :set-config (set-config (get-in event-data [:fn-fx/includes :config-value :text]))
                       :hiding (do (println "Saving config.")
                                   (api/save-config id @config))
                       :close (do (swap! data-state assoc :shown? false)
                                  (api/set-stage-visibility true))))
        ui-state (agent (dom/app (stage @data-state) handler-fn))]
    (add-watch data-state :ui (fn [_ _ old-state new-state]
                                (send ui-state
                                      (fn [old-ui]
                                        (dom/update-app old-ui (stage new-state)))))))
  (api/set-stage-visibility
   false))

(def menu-activity
  [:example-activity
   (Image. (.toString (io/resource "oss_world_example/icon.png")))
   "Example Plugin"
   menu-clicked])

(defn entry
  "This function is defined in the plugin.edn file and will be run when the
  plugin is loaded."
  []
  (api/add-activity menu-activity))
