;;; make-hash-table.lisp
;;;
;;; Copyright (C) 2003-2004 Peter Graves
;;; $Id: make-hash-table.lisp,v 1.5 2004-09-19 13:35:21 piso Exp $
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

(defun make-hash-table (&key (test 'eql) (size 11) (rehash-size 1.5)
			     (rehash-threshold 0.75))
  (setq test (coerce-to-function test))
  (unless (and (integerp size) (>= size 0))
    (error 'type-error "MAKE-HASH-TABLE: ~S is not a non-negative integer." size))
  (let ((size (max 11 (min size array-dimension-limit))))
    (%make-hash-table test size rehash-size rehash-threshold)))
