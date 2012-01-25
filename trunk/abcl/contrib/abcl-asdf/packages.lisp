(defpackage #:abcl-asdf
  (:use :cl)
  (:export 
;;; Public API
   #:resolve-dependencies

   #:find-mvn

   #:*mvn-directory*

   #:init

;;; "Internal" API

;;;; Maven 
   #:*mvn-libs-directory*

   #:satisfy
   #:as-classpath

   #:resolve-artifact

   #:add-directory-jars-to-class-path
   #:need-to-add-directory-jar?
   
   #:*added-to-classpath*
   #:*inhibit-add-to-classpath*))

(defpackage #:abcl-asdf-test
  (:use :cl #+nil :abcl-test-lisp) ;;; FIXME
  (:export #:run))
