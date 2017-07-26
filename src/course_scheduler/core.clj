(ns course-scheduler.core
 (require [clojure.set :refer :all]))


(def answer-annotations
  (fn [courses registrants]
    (let [checking-set (set (:taking-now registrants))]
      (map (fn [course]
             (assoc course
                    :spaces-left (- (:limit course)
                                    (:registered course))
                    :already-in? (contains? checking-set
                                            (:course-name course))))
           courses))))

(def domain-annotations
  (fn [courses]
    (map (fn [course]
           (assoc course
                  :empty? (zero? (:registered course))
                  :full? (zero? (:spaces-left course))))
         courses)))

(def note-unavailability
  (fn [courses instructor-count manager?]
    (let [out-of-instructors?
          (= instructor-count
             (count (filter (fn [course] (not (:empty? course)))
                            courses)))]
      (map (fn [course]
             (assoc course
                    :unavailable? (or (:full? course)
                                      (and out-of-instructors?
                                           (:empty? course))
                                      (and manager?
                                           (not (:morning? course)))))) 
           courses))))

(def annotate
  (fn [courses registrants instructor-count]
    (-> courses
        (answer-annotations registrants)
        domain-annotations
        (note-unavailability instructor-count (:manager? registrants)))))


(def separate
  (fn [pred sequence]
    [(filter pred sequence) (remove pred sequence)]))

(def visible-courses
  (fn [courses]
    (let [[guaranteed possibles] (separate :already-in? courses)]
      (concat guaranteed (remove :unavailable? possibles)))))

(def final-shape
  (fn [courses]
    (let [desired-keys [:course-name :morning? :registered :spaces-left :already-in?]]
      (map (fn [course]
             (select-keys course desired-keys))
           courses))))



(def half-day-solution
  (fn [courses registrants instructor-count]
    (-> courses
        (annotate registrants instructor-count)
        visible-courses
        ((fn [courses] (sort-by :course-name courses)))
        final-shape)))

(def solution
  (fn [courses registrants instructor-count]
    (map (fn [courses]
           (half-day-solution courses registrants instructor-count))
         (separate :morning? courses))))
