;;; print-object.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: print-object.lisp,v 1.7 2005-02-20 14:11:53 piso Exp $
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

(require 'clos)

(when (autoloadp 'print-object)
  (fmakunbound 'print-object))

(defgeneric print-object (object stream))

(defmethod print-object ((object t) stream)
  (print-unreadable-object (object stream :type t :identity t)))

(defmethod print-object ((object structure-object) stream)
  (write-string (%write-to-string object) stream))

(defmethod print-object ((object standard-object) stream)
  (print-unreadable-object (object stream :identity t)
    (format stream "~S" (class-name (class-of object))))
  instance)

(defmethod print-object ((class standard-class) stream)
  (print-unreadable-object (class stream :identity t)
    (format stream "~S ~S"
            (class-name (class-of class))
            (class-name class)))
  class)

(defmethod print-object ((gf standard-generic-function) stream)
  (print-unreadable-object (gf stream :identity t)
    (format stream "~S ~S"
            (class-name (class-of gf))
            (generic-function-name gf)))
  gf)

(defmethod print-object ((method standard-method) stream)
  (print-unreadable-object (method stream :identity t)
    (format stream "~S ~S~{ ~S~} ~S"
            (class-name (class-of method))
            (generic-function-name
             (method-generic-function method))
            (method-qualifiers method)
            (mapcar #'class-name
                    (method-specializers method))))
  method)

(defmethod print-object ((restart restart) stream)
  (if *print-escape*
      (print-unreadable-object (restart stream :type t :identity t)
        (prin1 (restart-name restart) stream))
      (restart-report restart stream)))

(defmethod print-object ((c condition) stream)
  (if *print-escape*
      (call-next-method)
      (let ((report (condition-report c)))
        (cond ((stringp report)
               (write-string report stream))
              (t
               (%format stream "~A" c))))))

(defmethod print-object ((c simple-condition) stream)
  (if *print-escape*
      (call-next-method)
      (let ((format-control (simple-condition-format-control c)))
        (if format-control
            (apply #'format stream format-control
                   (simple-condition-format-arguments c))
            (call-next-method)))))

(defmethod print-object ((x undefined-function) stream)
  (if *print-escape*
      (call-next-method)
      (format stream "The function ~S is undefined." (cell-error-name x))))

(provide 'print-object)
