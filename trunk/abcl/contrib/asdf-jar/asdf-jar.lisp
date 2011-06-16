(defpackage :asdf-jar
  (:use :cl)
  (:export #:package))

(in-package :asdf-jar)


(defvar *systems*)
(defmethod asdf:perform :before ((op asdf:compile-op) (c asdf:system))
  (push c *systems*))

(defun package (system-name 
                &key (out #p"/var/tmp/") 
                     (recursive t) 
                     (verbose t))
  (let* ((system 
          (asdf:find-system system-name))
	 (name 
          (slot-value system 'asdf::name))
         (version 
          (slot-value system 'asdf:version))
         (package-jar-name 
          (format nil "~A~A-~A.jar" name (when recursive "-all") version))
         (package-jar
          (make-pathname :directory out :defaults package-jar-name))
         (mapping (make-hash-table :test 'equal)))
    (when verbose 
      (format verbose "~&Packaging ASDF definition of ~A~&as ~A." system package-jar))
    (setf *systems* nil)
    (asdf:compile-system system :force t)
    (let* ((dir (asdf:component-pathname system))
	   (wild-contents (merge-pathnames "**/*" dir))
	   (contents (directory wild-contents))
	   (topdir (truename (merge-pathnames "../" dir))))
      (when verbose
	(format verbose "~&Packaging contents in ~A." package-jar))
      (dolist (system (append (list system) *systems*))
        (let ((base (slot-value system 'asdf:absolute-pathname))
              (name (slot-value system 'asdf:name))
              (asdf (slot-value system source-file)))
          (setf (gethash asdf mapping) (relative-path base name asdf))))
          ;;; XXX iterate through the rest of the contents of the
          ;;; system, adding appropiate entries
      (system:zip package-jar mapping))))

(defun relative-path (base dir file) 
  (let* ((relative 
          (nthcdr (length (pathname-directory base)) (pathname-directory file)))
         (entry-dir `(:relative ,dir ,@(when relative relative))))
    (make-pathname :directory entry-dir
                   :defaults file)))

(defun tmpdir (name)
  "Return temporary directory."
  (let* ((temp-file (java:jcall "getAbsolutePath" 
                               (java:jstatic "createTempFile" "java.io.File" "foo" "tmp")))
         (temp-path (pathname temp-file)))
    (make-pathname 
     :directory (nconc (pathname-directory temp-path)
                       (list name)))))















    
	

  
  
