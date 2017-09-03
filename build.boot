(set-env!
 :resource-paths #{"resources" "src"}
 :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                 [oss-world/core "0.0.0-SNAPSHOT" :scope "provided"]
                 [halgari/fn-fx "0.3.0-SNAPSHOT" :scope "provided"]])

(def proj-sym 'oss-world/oss-world-example-clojure)

(task-options!
 pom {:project proj-sym
      :version "1.0"
      :description "An example plugin to demonstrate how OSS World plugins work."})

(deftask build
  "Builds this into a jar that can be placed into the plugins folder."
  []
  (comp
   (pom)
   (jar)
   (install)
   (target)))
