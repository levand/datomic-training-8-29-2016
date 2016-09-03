(ns exproj.core
  (:require [datomic.api :as d])
  (:import [java.util UUID]))

(comment

  (def uri "datomic:dev://localhost:4334/apex-training2")

  (def uri "datomic:mem://my-db")

  (d/create-database uri)

  (def conn (d/connect uri))

  (def schema-tx
    [

     {:db/id #db/id[:db.part/db]
      :db/ident :person/name
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/one
      :db/doc "A person's name"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :person/id
      :db/valueType :db.type/uuid
      :db/cardinality :db.cardinality/one
      :db/unique :db.unique/identity
      :db/doc "A person's true ID"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :person/nick
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/many
      :db/doc "A person's nicknames"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :person/friends
      :db/valueType :db.type/ref
      :db/cardinality :db.cardinality/many
      :db/doc "A person's friends"
      :db.install/_attribute :db.part/db}
     ])

  (def schema-tx
      [

       {:db/id #db/id[:db.part/db]
        :db/ident :apex/data-source
        :db/valueType :db.type/string
        :db/cardinality :db.cardinality/one
        :db/doc "The source of a transaction"
        :db.install/_attribute :db.part/db}])

  (def schema-tx
    [

     {:db/id #db/id[:db.part/db]
      :db/ident :apex/trust-level
      :db/valueType :db.type/keyword
      :db/cardinality :db.cardinality/one
      :db/doc "The trust level of a transaction"
      :db.install/_attribute :db.part/db}])

  (d/transact conn schema-tx)

  (d/transact conn
      [[:db/add #db/id[:db.part/user -1] :person/name "Luke"]
       [:db/add #db/id[:db.part/user -1] :person/nick "Clojureman"]

       [:db/add #db/id[:db.part/user -2] :person/name "Bob"]
       [:db/add #db/id[:db.part/user -2] :person/nick "Bobski"]
       [:db/add #db/id[:db.part/user -1] :person/friends #db/id[:db.part/user -2] ]
       ]
      )

  (def db (d/db conn))

  (d/q '[:find ?fn
         :in $
         :where
         [?e :person/name "Luke"]
         [?e :person/friends ?f]
         [?f :person/name ?fn]]
    db)

  (d/q '[:find ?eid
             :in $
             :where
             [?eid :person/name "Luke"]
           ]
        db)

  (def db (d/db conn))

  (d/q '[:find ?n ?id
         :in $
         :where
         [?e :person/name ?n]
         [?e :person/id ?id]]
        db2)



  (def luke-id (UUID/randomUUID))
  (d/transact conn
      [[:db/add 17592186045418 :person/id luke-id]])




  (def db (d/db conn))

  (d/q '[:find ?nick ?friend ?person
           :in $
           :where
           [?person :person/name "Luke"]
           [?person :person/friends ?friend]
           [?friend :person/nick ?nick]]
      db)

  (d/transact conn
      [{:db/id #db/id[:db.part/user]
        :person/name "Mike"
        :person/friends [{:db/id #db/id[:db.part/user]
                          :person/name "Kris"}
                         {:db/id #db/id[:db.part/user]
                          :person/nick "Pshaun"}
                         {:db/id #db/id[:db.part/user]
                          :person/id luke-id}]}])


  (d/q '[:find ?a-n ?b-n
         :in $
         :where
         [?a :person/friends ?b]
         [?a :person/name ?a-n]
         [?b :person/name ?b-n]]
    db)

    (d/q '[:find ?p ?n
           :in $
           :where
           [?p :person/name ?n]
           (not
             [$db1 ?p :person/id])]
      db)


  (defn is-short-name?
    [name]
    (<= (count name) 3))

  (partition-all 2
    (sort
        (d/q '[:find [?n ...]
                 :in $
                 :where
                 [_ :person/name ?n]
                 ]
            db)))









  (def myrule
    '[
      [(acq ?person-a ?person-b)
       [?person-a :person/friends ?person-b]]

      [(acq ?person-a ?person-b)
       [?person-b :person/friends ?person-a]]

      ])

  (d/q '[:find ?pn ?an
         :in $ %
         :where
         (acq ?p ?a)
         [?p :person/name ?pn]
         [?a :person/name ?an]]
    db myrule)

  (d/pull db '[*
               {:person/friends [*]
                :person/_friends [*]}]
    17592186045418)


  (d/pull db '[:person/name :person/nick]
    17592186045418)


  (def datoms
    (seq (d/datoms db2 :aevt :person/name)))



  (d/transact conn
    [

     {:db/id #db/id[:db.part/user]
      :person/name "Santa Claus"
      :person/nick "Fatman"}

     {:db/id #db/id[:db.part/tx]
      :apex/data-source "Roy"}
     ]

    )

  (d/transact conn
    [[:db.fn/retractEntity 17592186045423]])

  (def db (d/db conn))

  (d/q '[:find ?c ?name ?t
           :in $
           :where
           [?c :person/name ?name ?t]]
          db)

  (def minutes-ago
    (java.util.Date.
      (- (.getTime (java.util.Date.))
         1200)))

  (def old-db
    (d/as-of db 13194139534318))


  (d/q '[:find ?t
         :in $
         :where
         [?t :apex/data-source "Roy"]]
    old-db)

  (d/pull db '[*] 13194139534318)

  (def log (d/log conn))

  (d/transact conn [{:db/id 13194139534318
                     :apex/trust-level :low}])

  (sort-by #(nth % 3)
  (seq (d/datoms (d/history db)
         :aevt :person/name)))


  (d/q '[:find ?e ?v ?t
         :in $
         :where
         [?e :person/name ?v ?t false]
         ]
    (d/history db))

  )