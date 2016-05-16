;;;; -*- Mode: LISP -*-

;;;; Need to have jna.jar present for CFFI to work.
(asdf:defsystem :jna 
    :version "4.2.2"
    :description  "<> asdf:defsystem <urn:abcl.org/release/1.3.0/contrib/jna#4.2.2>"
    :defsystem-depends-on (jss abcl-asdf)
    :components ((:mvn "net.java.dev.jna/jna/4.2.2"
                  :alternate-uri "http://repo1.maven.org/maven2/net/java/dev/jna/jna/4.2.2/jna-4.2.2.jar"
                  :classname "com.sun.jna.Native")))

(in-package :asdf) 
(defmethod perform :after ((o load-op) (c (eql (find-system :jna))))
  (when (jss:find-java-class "com.sun.jna.Native")
    (provide :jna)))

                         
