(ns aerospike-clj.batch-policy
  "This namespace contains functions to create batch policies.
   These policies are only usable with `com.aerospike/aerospike-client` version 6.0.0 or higher."
  (:require [aerospike-clj.policy :as policy])
  (:import #_{:clj-kondo/ignore [:unused-import]}
    (com.aerospike.client.policy BatchPolicy
                                 BatchWritePolicy
                                 ClientPolicy
                                 CommitLevel
                                 GenerationPolicy
                                 RecordExistsAction)))

(defn map->batch-policy
  "Create a `BatchPolicy` from a map.
  This function is slow due to possible reflection."
  ^BatchPolicy [conf]
  (let [bp   (BatchPolicy. (policy/map->policy conf))
        conf (merge {"timeoutDelay" 3000} conf)]
    (policy/set-java bp conf "allowInline")
    (policy/set-java bp conf "respondAllKeys")
    (policy/set-java bp conf "maxConcurrentThreads")
    (policy/set-java bp conf "sendSetName")
    bp))

(defn map->batch-write-policy
  "Create a `BatchWritePolicy` from a map. Enumeration names should start with capitalized letter.
  This function is slow due to possible reflection."
  ^BatchWritePolicy [conf]
  (let [p (BatchWritePolicy.)]
    (policy/set-java-enum p conf "RecordExistsAction")
    (policy/set-java-enum p conf "CommitLevel")
    (policy/set-java-enum p conf "GenerationPolicy")
    (policy/set-java p conf "filterExp")
    (policy/set-java p conf "generation")
    (policy/set-java p conf "expiration")
    (policy/set-java p conf "durableDelete")
    (policy/set-java p conf "sendKey")
    p))

(defn add-batch-write-policy
  "Set the [[batchWritePolicyDefault]] or the [[batchParentPolicyWriteDefault]] in a [[ClientPolicy]]."
  [^ClientPolicy client-policy conf]
  (set! (.batchParentPolicyWriteDefault client-policy) (get conf "batchParentPolicyWriteDefault" (map->batch-policy conf)))
  (set! (.batchWritePolicyDefault client-policy) (get conf "batchWritePolicyDefault" (map->batch-write-policy conf))))

(defn create-client-policy
  "This is a wrapper around [[policy/create-client-policy]] that adds the batch policies to the client policy."
  ^ClientPolicy [event-loops conf]
  (let [client-policy (policy/create-client-policy event-loops conf)]
    (add-batch-write-policy client-policy conf)
    client-policy))