(defproject net.unit8.jobstreamer/job-streamer-control-bus (clojure.string/trim-newline (slurp "VERSION"))
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [javax/javaee-api "7.0"]
                 [environ "1.0.0"]
                 [bouncer "0.3.3"]
                 [net.unit8.wscl/websocket-classloader "0.2.1"]
                 [net.unit8.logback/logback-websocket-appender "0.1.0"]
                 [io.undertow/undertow-websockets-jsr "1.1.1.Final"]
                 [com.datomic/datomic-free "0.9.5206" :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-nop
                                                                   com.amazonaws/aws-java-sdk]]
                 [org.jsoup/jsoup "1.8.3"]
                 [datomic-schema "1.3.0"]
                 [liberator "0.13"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5" :exclusions [javax.servlet/servlet-api]]
                 [ring "1.4.0" :exclusions [ring/ring-jetty-adapter]]
                 [http-kit "2.1.19"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [org.jboss.weld.se/weld-se "2.2.7.Final"]
                 [net.unit8.weld/weld-prescan "0.1.0"]

                 ;; for Scheduler
                 [org.quartz-scheduler/quartz "2.2.1"]

                 ;; for monitoring agents
                 [org.rrd4j/rrd4j "2.2.1"]]

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :plugins [[lein-ring "0.8.13"]
            [lein-libdir "0.1.1"]]
  :libdir-path "lib"
  :main job-streamer.control-bus.core
  :ring {:handler job-streamer.control-bus.core/app
         :init    job-streamer.control-bus.core/init}
  :aot :all
  :pom-plugins [[org.apache.maven.plugins/maven-assembly-plugin "2.5.5"
                 {:configuration [:descriptors [:descriptor "src/assembly/dist.xml"]]}]
                [org.apache.maven.plugins/maven-compiler-plugin "3.3"
                 {:configuration ([:source "1.7"] [:target "1.7"]) }]]

  :profiles {:test {:dependencies [[junit "4.12"]]}})
