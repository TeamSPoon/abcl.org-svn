;;; compiler-macro.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: compiler-macro.lisp,v 1.8 2005-07-12 02:35:06 piso Exp $
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

(export 'compiler-macroexpand)

(defvar *compiler-macros* (make-hash-table :test #'equal))

(defun compiler-macro-function (name &optional environment)
  (declare (ignore environment))
  (gethash-2op-1ret name (the hash-table *compiler-macros*)))

(defun (setf compiler-macro-function) (new-function name &optional environment)
  (declare (ignore environment))
  (setf (gethash name (the hash-table *compiler-macros*)) new-function))

(defmacro define-compiler-macro (name lambda-list &rest body)
  (let* ((form (gensym))
         (env (gensym)))
    (multiple-value-bind (body decls)
        (parse-defmacro lambda-list form body name 'defmacro :environment env)
      (let ((expander `(lambda (,form ,env) (block ,(fdefinition-block-name name) ,body))))
        `(progn
           (setf (compiler-macro-function ',name) (function ,expander))
           ',name)))))

;;; Adapted from OpenMCL.
(defun compiler-macroexpand-1 (form &optional env)
  (let ((expander nil)
        (new-form nil))
    (if (and (consp form)
             (symbolp (%car form))
             (setq expander (compiler-macro-function (%car form) env)))
        (values (setq new-form (funcall expander form env))
                (neq new-form form))
        (values form
                nil))))

(defun compiler-macroexpand (form &optional env)
  (let ((expanded-p nil))
    (loop
      (multiple-value-bind (expansion exp-p)
          (compiler-macroexpand-1 form env)
        (if exp-p
            (setf form expansion
                  expanded-p t)
            (return))))
    (values form expanded-p)))

