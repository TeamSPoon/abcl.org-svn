;;; write-to-string.lisp
;;;
;;; Copyright (C) 2004 Peter Graves
;;; $Id: write-to-string.lisp,v 1.4 2004-06-11 16:53:39 piso Exp $
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

(in-package "SYSTEM")

(defun write-to-string (object &key
                               ((:escape *print-escape*) *print-escape*)
                               ((:radix *print-radix*) *print-radix*)
                               ((:base *print-base*) *print-base*)
                               ((:circle *print-circle*) *print-circle*)
                               ((:pretty *print-pretty*) *print-pretty*)
                               ((:level *print-level*) *print-level*)
                               ((:length *print-length*) *print-length*)
                               ((:case *print-case*) *print-case*)
                               ((:array *print-array*) *print-array*)
                               ((:gensym *print-gensym*) *print-gensym*)
                               ((:readably *print-readably*) *print-readably*)
                               ((:right-margin *print-right-margin*) *print-right-margin*)
                               ((:miser-width *print-miser-width*) *print-miser-width*)
                               ((:lines *print-lines*) *print-lines*)
                               ((:pprint-dispatch *print-pprint-dispatch*) *print-pprint-dispatch*))
  (if (or *print-pretty* *print-circle*)
      (cond ((or (symbolp object)
                 (stringp object)
                 (fixnump object))
             (%write-to-string object))
            (t
             (let ((stream (make-string-output-stream)))
               (output-object object stream)
               (get-output-stream-string stream))))
      (%write-to-string object)))
