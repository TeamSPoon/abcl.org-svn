;;; early-defuns.lisp
;;;
;;; Copyright (C) 2003-2004 Peter Graves
;;; $Id: early-defuns.lisp,v 1.7 2004-01-17 17:31:22 piso Exp $
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

(defun require-type (arg type)
  (unless (typep arg type)
    (error 'simple-type-error
           :format-control "The value ~S is not of type ~A."
           :format-arguments (list arg type))))

(defun normalize-type (type)
  (when (symbolp type)
    (case type
      (BIT
       (return-from normalize-type '(integer 0 1)))
      (CONS
       (return-from normalize-type '(cons t t)))
      (FIXNUM
       (return-from normalize-type
                    '(integer #.most-negative-fixnum #.most-positive-fixnum)))
      (SIGNED-BYTE
       (return-from normalize-type 'integer))
      (UNSIGNED-BYTE
       (return-from normalize-type '(integer 0 *)))
      (BASE-CHAR
       (return-from normalize-type 'character))
      ((SHORT-FLOAT SINGLE-FLOAT DOUBLE-FLOAT LONG-FLOAT)
       (return-from normalize-type 'float))
      (ARRAY
       (return-from normalize-type '(array * *)))
      (t
       (unless (get type 'deftype-definition)
         (return-from normalize-type type)))))
  ;; Fall through...
  (let (tp i)
    (loop
      (if (consp type)
          (setf tp (car type) i (cdr type))
          (setf tp type i nil))
      (if (and (symbolp tp) (get tp 'deftype-definition))
          (setf type (apply (get tp 'deftype-definition) i))
          (return)))
    (case tp
      (CONS
       (let* ((len (length i))
              (car-typespec (if (> len 0) (car i) t))
              (cdr-typespec (if (> len 1) (cadr i) t)))
         (unless (and car-typespec cdr-typespec)
           (return-from normalize-type nil))
         (when (eq car-typespec '*)
           (setf car-typespec t))
         (when (eq cdr-typespec '*)
           (setf cdr-typespec t))
         (setf i (list car-typespec cdr-typespec))))
      ((ARRAY SIMPLE-ARRAY)
       (unless i
         (return-from normalize-type (list tp '* '*)))
       (when (null (car i)) ; Element type is NIL.
         (if (eq tp 'simple-array)
             (setf tp 'simple-string)
             (setf tp 'string))
         (when (cadr i) ; rank/dimensions
           (cond ((and (consp (cadr i)) (= (length (cadr i)) 1))
                  (if (eq (caadr i) '*)
                      (setf i nil)
                      (setf i (cadr i))))
                 ((eql (cadr i) 1)
                  (setf i nil))
                 (t
                  (error "invalid type specifier ~S" type)))))
       (when (= (length i) 1)
         (setf i (append i '(*)))))
      ((SHORT-FLOAT SINGLE-FLOAT DOUBLE-FLOAT LONG-FLOAT)
       (setf tp 'float)))
    (if i (cons tp i) tp)))

(defun caaaar (list) (car (car (car (car list)))))
(defun caaadr (list) (car (car (car (cdr list)))))
(defun caaddr (list) (car (car (cdr (cdr list)))))
(defun cadddr (list) (car (cdr (cdr (cdr list)))))
(defun cddddr (list) (cdr (cdr (cdr (cdr list)))))
(defun cdaaar (list) (cdr (car (car (car list)))))
(defun cddaar (list) (cdr (cdr (car (car list)))))
(defun cdddar (list) (cdr (cdr (cdr (car list)))))
(defun caadar (list) (car (car (cdr (car list)))))
(defun cadaar (list) (car (cdr (car (car list)))))
(defun cadadr (list) (car (cdr (car (cdr list)))))
(defun caddar (list) (car (cdr (cdr (car list)))))
(defun cdaadr (list) (cdr (car (car (cdr list)))))
(defun cdadar (list) (cdr (car (cdr (car list)))))
(defun cdaddr (list) (cdr (car (cdr (cdr list)))))
(defun cddadr (list) (cdr (cdr (car (cdr list)))))


;;; SOME, EVERY, NOTANY, NOTEVERY (from ECL)

(defun some (predicate sequence &rest more-sequences)
  (setq more-sequences (cons sequence more-sequences))
  (do ((i 0 (1+ i))
       (l (apply #'min (mapcar #'length more-sequences))))
    ((>= i l) nil)
    (let ((that-value
           (apply predicate
                  (mapcar #'(lambda (z) (elt z i)) more-sequences))))
      (when that-value (return that-value)))))

(defun every (predicate sequence &rest more-sequences)
  (setq more-sequences (cons sequence more-sequences))
  (do ((i 0 (1+ i))
       (l (apply #'min (mapcar #'length more-sequences))))
    ((>= i l) t)
    (unless (apply predicate (mapcar #'(lambda (z) (elt z i)) more-sequences))
      (return nil))))

(defun notany (predicate sequence &rest more-sequences)
  (not (apply #'some predicate sequence more-sequences)))

(defun notevery (predicate sequence &rest more-sequences)
  (not (apply #'every predicate sequence more-sequences)))

