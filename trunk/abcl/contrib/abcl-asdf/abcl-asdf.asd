;;;; -*- Mode: LISP -*-
(in-package :asdf)

(defsystem :abcl-asdf
  :author "Mark Evenson"
  :version "0.3.1"
  :depends-on ("jss") 
  :components 
  ((:module base :pathname "" :components
	    ((:file "abcl-asdf")
             (:file "asdf-jar" 
                    :depends-on ("abcl-asdf"))
             (:file "maven-embedder" 
                    :depends-on ("abcl-asdf" "asdf-jar"))))))
