(ns course-scheduler.core-test
  (:require [midje.sweet :refer :all]
            [course-scheduler.core :refer :all]))

(facts "about `answer-annotations`"
  (fact "it adds keys to a sequence of maps stating the number of spaces left 
        for a course and whether a user is already signed up for it" 
    (answer-annotations [{:course-name "oop" :limit 4 :registered 3} 
                         {:course-name "fp" :limit 1 :registered 1}] 
                        ["fp"]) 
    =>

    '({:course-name "oop" :limit 4 :registered 3 
       :spaces-left 1 :already-in? false} 
      {:course-name "fp" :limit 1 :registered 1 
       :spaces-left 0 :already-in? true})))


(facts "about `domain-annotations`"
  (fact "it adds keys stating whether a course is empty or full"
    (domain-annotations [{:registered 1, :spaces-left 1}
                         {:registered 0, :spaces-left 1}
                         {:registered 1, :spaces-left 0}])
    =>
    
   '({:registered 1, :spaces-left 1, :full? false, :empty? false}
     {:registered 0, :spaces-left 1, :full? false, :empty? true}
     {:registered 1, :spaces-left 0, :full? true, :empty? false})))
  

(facts "about `note-unavailability`"
  (fact "it marks course as unavailable if it is full or there are 
        no more instructors"
    (note-unavailability [{:full? false, :empty? false}
                          {:full? false, :empty? true}
                          {:full? true, :empty? false}] 3)
    =>

   '({:full? false, :empty? false, :unavailable? false}
     {:full? false, :empty? true, :unavailable? false}
     {:full? true, :empty? false, :unavailable? true})

    (note-unavailability [{:full? false, :empty? false}
                          {:full? false, :empty? true}
                          {:full? true, :empty? false}] 2)
    =>

    '({:full? false, :empty? false, :unavailable? false}
      {:full? false, :empty? true, :unavailable? true}
      {:full? true, :empty? false, :unavailable? true})))


(facts "about `annotate`"
  (fact "it adds keys from `answer-annotations`, `domain-annotations`, 
        and `note-unavailablility`"
    (annotate [{:course-name "oop" :limit 4 :registered 3} 
               {:course-name "fp" :limit 1 :registered 1} 
               {:course-name "tdd" :limit 2 :registered 0}] 
              ["fp"] 2) 
    =>

    '({:course-name "oop", :limit 4, :registered 3, :full? false, 
       :empty? false, :unavailable? false, :already-in? false, :spaces-left 1}
      {:course-name "fp", :limit 1, :registered 1, :full? true, 
       :empty? false, :unavailable? true, :already-in? true, :spaces-left 0}
      {:course-name "tdd", :limit 2, :registered 0, :full? false, 
       :empty? true, :unavailable? true, :already-in? false, :spaces-left 2})))


(facts "about `separate`"
  (fact "it separates a vector of maps into two vectors; those for 
        whom a key is truthy, and those for whom it is falsey"
    (separate :a [{:a true} {:a false} {:a 1} {:a nil} {:b true}])
    =>

    '([{:a true} {:a 1}] [{:a false} {:a nil} {:b true}])))


(facts "about `visible-courses`"
  (fact "it returns courses which user is in or which are available to user"
    (visible-courses [{:unavailable? false, :already-in? false}
                      {:unavailable? true, :already-in? true}
                      {:unavailable? true, :already-in? false}
                      {:unavailable? false, :already-in? true}])
    =>

     '({:unavailable? true, :already-in? true}
       {:unavailable? false, :already-in? true}
       {:unavailable? false, :already-in? false})))


(facts "about `final-shape`"
  (fact "it strips out undesired keys"
    (final-shape [{:course-name "oop", :limit 4, :registered 3, :full? false, 
                   :empty? false, :unavailable? false, :already-in? false, 
                   :spaces-left 1, :morning? true}
                  {:course-name "fp", :limit 1, :registered 1, :full? true, 
                   :empty? false, :unavailable? true, :already-in? true, 
                   :spaces-left 0, :morning? false}
                  {:course-name "tdd", :limit 2, :registered 0, :full? false, 
                   :empty? true, :unavailable? true, :already-in? false, 
                   :spaces-left 2, :morning? true}])
    =>
    
    '({:course-name "oop", :registered 3, :already-in? false, 
       :spaces-left 1, :morning? true}
      {:course-name "fp", :registered 1, :already-in? true, 
       :spaces-left 0, :morning? false}
      {:course-name "tdd", :registered 0, :already-in? false, 
       :spaces-left 2, :morning? true})))


(facts "about `half-day-solution`"
  (fact "it combines `annotate`, `visible`, `final-shape`, 
        and sorting the courses by :course-name"
    (half-day-solution [{:course-name "oop" :limit 4 :registered 3, 
                         :morning? true} 
                        {:course-name "fp" :limit 1 :registered 1, 
                         :morning? false} 
                        {:course-name "tdd" :limit 2 :registered 0, 
                         :morning? true}]
              ["fp"] 2) 
    =>

    '({:course-name "fp", :registered 1, :already-in? true, 
       :spaces-left 0, :morning? false}
      {:course-name "oop", :registered 3, :already-in? false, 
       :spaces-left 1, :morning? true})))


(facts "about `solution`"
  (fact "it processes morning and afternoon courses separately"
    (solution [{:course-name "oop" :limit 4 :registered 3, :morning? true} 
               {:course-name "fp" :limit 1 :registered 1, :morning? false} 
               {:course-name "tdd" :limit 2 :registered 0, :morning? true}]
              ["fp"] 2) 
    =>

      '([{:course-name "oop", :registered 3, :already-in? false, 
          :spaces-left 1, :morning? true}
         {:course-name "tdd", :registered 0, :already-in? false, 
          :spaces-left 2, :morning? true}]
        [{:course-name "fp", :registered 1, :already-in? true, 
          :spaces-left 0, :morning? false}])))
