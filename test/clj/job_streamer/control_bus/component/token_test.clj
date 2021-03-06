(ns job-streamer.control-bus.component.token-test
  (:require [job-streamer.control-bus.component.token :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.pprint :refer :all]
            [clojure.test :refer :all]))

(deftest token-provider-test
  (testing "New token"
    (let [token-provider (component/start (token-provider-component {:session-timeout (* 30 60)}))
          token1 (new-token token-provider "user1")
          token2 (new-token token-provider "user1")]
      (is (instance? java.util.UUID token1))
      (is (instance? java.util.UUID token2))
      (is (not= token1 token2))))

  (testing "Authenticate"
    (let [token-provider (component/start (token-provider-component {:session-timeout (* 30 60)}))
          token (new-token token-provider "user1")]
      (is (= "user1" (auth-by token-provider token)))))

  (testing "Violates the precondition of `auth-by`"
    (let [token-provider (component/start (token-provider-component {:session-timeout (* 30 60)}))
          token (new-token token-provider "user1")]
      (is (thrown? IllegalArgumentException (auth-by token-provider 123))))))
