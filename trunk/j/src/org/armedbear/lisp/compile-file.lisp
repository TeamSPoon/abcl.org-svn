;;; compile-file.lisp
;;;
;;; Copyright (C) 2004 Peter Graves
;;; $Id: compile-file.lisp,v 1.45 2004-10-18 19:13:42 piso Exp $
;;;
;;; This program is free software; you can redistribute it and/or
;;; modify it under the terms of the GNU General Public License
;;; as published by the Free Software Foundation; either version 2
;;; of the License, or (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with this program; if not, write to the Free Software
;;; Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

(in-package #:system)

(require 'jvm)

(defvar *fbound-names*)

(defvar *class-number*)

(defvar *output-file-pathname*)

(defvar *source-position*)

(defun next-classfile-name ()
  (let ((name (format nil "~A-~D"
                      (pathname-name *compile-file-pathname*)
                      (incf *class-number*))))
    (namestring (merge-pathnames (make-pathname :name name :type "cls")
                                 *output-file-pathname*))))

(defmacro report-error (&rest forms)
  `(handler-case (progn ,@forms)
     (error (condition)
            (fresh-line)
            (%format t "~A Note: ~A~%" (jvm::load-verbose-prefix) condition)
            (values nil condition))))

;; Dummy function. Should never be called.
(defun dummy (&rest ignored)
  (assert nil))

(defun verify-load (classfile)
  (and classfile
       (let ((*default-pathname-defaults* *output-file-pathname*))
         (report-error
          (load-compiled-function classfile)))))

(defun process-toplevel-form (form stream compile-time-too)
  (cond ((atom form)
         (when compile-time-too
           (eval form)))
        (t
         (case (car form)
           (MACROLET
            (let ((new-form (precompiler::precompile-macrolet form)))
              (process-toplevel-form new-form stream compile-time-too)
              (return-from process-toplevel-form)))
           ((IN-PACKAGE DEFPACKAGE)
            (eval form))
           ((DEFVAR DEFPARAMETER)
            (if compile-time-too
                (eval form)
                ;; "If a DEFVAR or DEFPARAMETER form appears as a top level form,
                ;; the compiler must recognize that the name has been proclaimed
                ;; special. However, it must neither evaluate the initial-value
                ;; form nor assign the dynamic variable named NAME at compile
                ;; time."
                (let ((name (second form)))
                  (%defvar name))))
           (DEFCONSTANT
            ;; "If a DEFCONSTANT form appears as a top level form, the compiler
            ;; must recognize that [the] name names a constant variable. An
            ;; implementation may choose to evaluate the value-form at compile
            ;; time, load time, or both. Therefore, users must ensure that the
            ;; initial-value can be evaluated at compile time (regardless of
            ;; whether or not references to name appear in the file) and that
            ;; it always evaluates to the same value."
            (eval form))
           (DEFUN
            (let ((name (second form)))
              (%format t "; Processing function ~A~%" name)
              (let* ((lambda-list (third form))
                     (body (nthcdr 3 form))
                     (jvm::*speed* jvm::*speed*)
                     (jvm::*safety* jvm::*safety*)
                     (jvm::*debug* jvm::*debug*))
                (jvm::process-optimization-declarations body)
                (let* ((expr (list 'lambda lambda-list (list* 'block name body)) nil)
                       (classfile-name (next-classfile-name))
                       (classfile (report-error
                                   (jvm:compile-defun name expr nil classfile-name)))
                       (compiled-function (verify-load classfile)))
                  (cond (compiled-function
                         (%format t ";  ~A => ~A.cls~%" name
                                  (pathname-name (pathname classfile-name)))
                         (setf form
                               `(fset ',name
                                      (load-compiled-function ,(file-namestring classfile))
                                      ,*source-position*
                                      ',lambda-list))
                         (when compile-time-too
                           (fset name compiled-function)))
                        (t
                         (%format t ";  Unable to compile function ~A~%" name)
                         (let ((precompiled-function (precompile-form expr nil)))
                           (setf form
                                 `(fset ',name
                                        ,precompiled-function
                                        ,*source-position*
                                        ',lambda-list)))
                         (when compile-time-too
                           (eval form))))
                  (push name jvm::*toplevel-defuns*)
                  ;; If NAME is not fbound, provide a dummy definition so that
                  ;; getSymbolFunctionOrDie() will succeed when we try to verify that
                  ;; functions defined later in the same file can be loaded correctly.
                  (unless (fboundp name)
                    (setf (symbol-function name) #'dummy)
                    (push name *fbound-names*))))))
           (DEFMACRO
            (let ((name (second form)))
              (%format t "; Processing macro ~A~%" name)
              (eval form)
              (let* ((expr (function-lambda-expression (macro-function name)))
                     (classfile-name (next-classfile-name))
                     (classfile
                      (ignore-errors
                       (jvm:compile-defun nil expr nil classfile-name))))
                (if (verify-load classfile)
                    (progn
                      (%format t ";  Macro ~A => ~A.cls~%" name
                               (pathname-name (pathname classfile-name)))
                      (setf form
                            (if (special-operator-p name)
                                `(%put ',name 'macroexpand-macro
                                       (make-macro (load-compiled-function
                                                    ,(file-namestring classfile))))
                                `(fset ',name
                                       (make-macro (load-compiled-function
                                                    ,(file-namestring classfile)))
                                       ,*source-position*
                                       ',(third form)))))
                    (%format t ";  Unable to compile macro ~A~%" name)))))
           (DEFTYPE
            (eval form))
           (EVAL-WHEN
            (multiple-value-bind (ct lt e) (parse-eval-when-situations (cadr form))
              (let ((new-compile-time-too (or ct
                                              (and compile-time-too e)))
                    (body (cddr form)))
                (cond (lt
                       (process-toplevel-progn body stream new-compile-time-too))
                      (new-compile-time-too
                       (eval `(progn ,@body)))))
              (return-from process-toplevel-form)))
           (LOCALLY
            ;; FIXME Need to handle special declarations too!
            (let ((jvm::*speed* jvm::*speed*)
                  (jvm::*safety* jvm::*safety*)
                  (jvm::*debug* jvm::*debug*))
              (jvm::process-optimization-declarations (cdr form))
              (process-toplevel-progn (cdr form) stream compile-time-too)
              (return-from process-toplevel-form)))
           (PROGN
            (process-toplevel-progn (cdr form) stream compile-time-too)
            (return-from process-toplevel-form))
           (t
            (when (and (symbolp (car form))
                       (macro-function (car form)))
              (process-toplevel-form (macroexpand-1 form) stream compile-time-too)
              (return-from process-toplevel-form))
            (when compile-time-too
              (eval form))))))
  (when (and (consp form) (neq (car form) 'QUOTE))
    (let ((*print-fasl* t)
          (*print-level* nil)
          (*print-length* nil))
      (if (eq (car form) 'IMPORT)
          ;; Make sure package prefix is printed when symbols are imported.
          (let ((*package* (find-package "COMMON-LISP")))
            (write form :stream stream))
          (write form :stream stream)))
    (terpri stream)))

(defun process-toplevel-progn (forms stream compile-time-too)
  (dolist (form forms)
    (process-toplevel-form form stream compile-time-too)))

;;; Adapted from SBCL.
;;; Parse an EVAL-WHEN situations list, returning three flags,
;;; (VALUES COMPILE-TOPLEVEL LOAD-TOPLEVEL EXECUTE), indicating
;;; the types of situations present in the list.
(defun parse-eval-when-situations (situations)
  (when (or (not (listp situations))
	    (set-difference situations
			    '(:compile-toplevel
			      compile
			      :load-toplevel
			      load
			      :execute
			      eval)))
    (error "Bad EVAL-WHEN situation list: ~S." situations))
  (values (intersection '(:compile-toplevel compile) situations)
	  (intersection '(:load-toplevel load) situations)
	  (intersection '(:execute eval) situations)))

(defun compile-file (input-file &key output-file verbose print external-format)
  (unless (or (and (probe-file input-file) (not (file-directory-p input-file)))
              (pathname-type input-file))
    (let ((pathname (merge-pathnames (make-pathname :type "lisp") input-file)))
      (when (probe-file pathname)
        (setf input-file pathname))))
  (unless output-file
    (setf output-file (compile-file-pathname input-file)))
  (let* ((*output-file-pathname* output-file)
         (type (pathname-type output-file))
         (temp-file (merge-pathnames (make-pathname :type (concatenate 'string type "-tmp"))
                                     output-file)))
    (with-open-file (in input-file :direction :input)
      (let* ((*compile-file-pathname* (pathname in))
             (*compile-file-truename* (truename in))
             (*class-number* 0)
             (namestring (namestring *compile-file-truename*))
             (start (get-internal-real-time))
             elapsed)
        (%format t "; Compiling ~A ...~%" namestring)
        (with-open-file (out temp-file :direction :output :if-exists :supersede)
          (let ((*readtable* *readtable*)
                (*package* *package*)
                (jvm::*speed* jvm::*speed*)
                (jvm::*safety* jvm::*safety*)
                (jvm::*debug* jvm::*debug*)
                (jvm::*toplevel-defuns* ())
                (*fbound-names* ()))
            (write "; -*- Mode: Lisp -*-" :escape nil :stream out)
            (terpri out)
            (let ((*package* (find-package '#:cl)))
              (write (list 'init-fasl :version *fasl-version*) :stream out)
              (terpri out)
              (write (list 'setq '*fasl-source* *compile-file-truename*) :stream out)
              (terpri out))
            (loop
              (let* ((*source-position* (file-position in))
                     (form (read in nil in)))
                (when (eq form in)
                  (return))
                (process-toplevel-form form out nil)))
            (dolist (name *fbound-names*)
              (fmakunbound name))))
        (setf elapsed (/ (- (get-internal-real-time) start) 1000.0))
        (rename-file temp-file output-file)
        (%format t "; Compiled ~A (~A seconds)~%" namestring elapsed))))
  (values (truename output-file) nil nil))
