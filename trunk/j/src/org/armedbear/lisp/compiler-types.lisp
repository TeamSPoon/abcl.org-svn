;;; compiler-types.lisp
;;;
;;; Copyright (C) 2005 Peter Graves
;;; $Id: compiler-types.lisp,v 1.6 2005-08-12 16:45:36 piso Exp $
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

;;; Type information that matters to the compiler.

(in-package #:system)

(export '(integer-type-low
          integer-type-high
          integer-type-p
          make-integer-type
          fixnum-type-p
          fixnum-constant-value
          make-compiler-type
          compiler-subtypep
          function-result-type
          defknown))

(defstruct (integer-type (:constructor %make-integer-type (low high)))
  low
  high)

(declaim (ftype (function (t) t) make-integer-type))
(defun make-integer-type (type)
  (when (eq type 'INTEGER)
    (return-from make-integer-type (%make-integer-type nil nil)))
  (setf type (normalize-type type))
  (when (and (consp type) (eq (%car type) 'INTEGER))
    (let ((low (second type))
          (high (third type)))
      (if (eq low '*)
          (setf low nil)
          (when (and (consp low) (integerp (%car low)))
            (setf low (1+ (%car low)))))
      (if (eq high '*)
          (setf high nil)
          (when (and (consp high) (integerp (%car high)))
            (setf high (1- (%car high)))))
      (%make-integer-type low high))))

(declaim (ftype (function (t) t) fixnum-type-p))
(defun fixnum-type-p (compiler-type)
  (and (integer-type-p compiler-type)
       (fixnump (integer-type-low compiler-type))
       (fixnump (integer-type-high compiler-type))))

(declaim (ftype (function (t) t) fixnum-constant-value))
(defun fixnum-constant-value (compiler-type)
  (when (and compiler-type (integer-type-p compiler-type))
    (let ((low (integer-type-low compiler-type))
          high)
      (when (fixnump low)
        (setf high (integer-type-high compiler-type))
        (when (and (fixnump high) (= high low))
          high)))))

(defun make-compiler-type (typespec)
  (if (integer-type-p typespec)
      typespec
      (let ((type (normalize-type typespec)))
        (cond ((consp type)
               (case (%car type)
                 (INTEGER
                  (make-integer-type type))
                 (t
                  t)))
              ((memq type '(SYMBOL CHARACTER))
               type)
              (t
               t)))))

(defun integer-type-subtypep (type1 typespec)
  (if (eq typespec 'INTEGER)
      t
      (let ((type2 (make-integer-type typespec)))
        (when type2
          (let ((low1 (integer-type-low type1))
                (high1 (integer-type-high type1))
                (low2 (integer-type-low type2))
                (high2 (integer-type-high type2)))
            (cond ((and low1 low2 high1 high2)
                   (and (>= low1 low2) (<= high1 high2)))
                  ((and low1 low2 (< low1 low2))
                   nil)
                  ((and high1 high2) (> high1 high2)
                   nil)
                  ((and (null low1) low2)
                   nil)
                  ((and (null high1) high2)
                   nil)
                  (t
                   t)))))))

(defun compiler-subtypep (compiler-type typespec)
  (cond ((eq typespec t)
         t)
        ((integer-type-p compiler-type)
         (integer-type-subtypep compiler-type typespec))))

(defvar *function-result-types* (make-hash-table :test 'equal))

(defun function-result-type (name)
  (if (symbolp name)
      (get name 'function-result-type)
      (gethash-2op-1ret name (the hash-table *function-result-types*))))

(defun set-function-result-type (name result-type)
  (setf (gethash name (the hash-table *function-result-types*)) result-type))

(defsetf function-result-type set-function-result-type)

(defun %defknown (name argument-types result-type)
  (declare (ignore argument-types))
  (setf (function-result-type name) (make-compiler-type result-type)))

(defmacro defknown (name argument-types result-type)
  `(eval-when (:compile-toplevel :load-toplevel :execute)
     (%defknown ',name ',argument-types ',result-type)))

(provide '#:compiler-types)
