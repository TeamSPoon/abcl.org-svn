;;; nsubstitute.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: nsubstitute.lisp,v 1.2 2003-06-10 15:23:03 piso Exp $
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
;;; NSUBSTITUTE (from CMUCL)

(in-package "COMMON-LISP")

(export '(nsubstitute nsubstitute-if nsubstitute-if-not))

;;; From CMUCL.

(defmacro real-count (count)
  `(cond ((null ,count) most-positive-fixnum)
         ((fixnump ,count) (if (minusp ,count) 0 ,count))
         ((integerp ,count) (if (minusp ,count) 0 most-positive-fixnum))
         (t ,count)))

(defun nlist-substitute* (new old sequence test test-not start end count key)
  (do ((list (nthcdr start sequence) (cdr list))
       (index start (1+ index)))
      ((or (= index end) (null list) (= count 0)) sequence)
    (when (if test-not
	      (not (funcall test-not old (apply-key key (car list))))
	      (funcall test old (apply-key key (car list))))
      (rplaca list new)
      (setq count (1- count)))))

(defun nvector-substitute* (new old sequence incrementer
                                test test-not start end count key)
  (do ((index start (+ index incrementer)))
      ((or (= index end) (= count 0)) sequence)
    (when (if test-not
	      (not (funcall test-not old (apply-key key (aref sequence index))))
	      (funcall test old (apply-key key (aref sequence index))))
      (setf (aref sequence index) new)
      (setq count (1- count)))))

(defun nsubstitute (new old sequence &key from-end (test #'eql) test-not
                        end count key (start 0))
  (let ((end (or end (length sequence)))
	(count (real-count count)))
    (if (listp sequence)
	(if from-end
	    (let ((length (length sequence)))
	      (nreverse (nlist-substitute*
			 new old (nreverse sequence)
			 test test-not (- length end) (- length start) count key)))
	    (nlist-substitute* new old sequence
			       test test-not start end count key))
	(if from-end
	    (nvector-substitute* new old sequence -1
				 test test-not (1- end) (1- start) count key)
	    (nvector-substitute* new old sequence 1
				 test test-not start end count key)))))


(defun nlist-substitute-if* (new test sequence start end count key)
  (do ((list (nthcdr start sequence) (cdr list))
       (index start (1+ index)))
      ((or (= index end) (null list) (= count 0)) sequence)
    (when (funcall test (apply-key key (car list)))
      (rplaca list new)
      (setq count (1- count)))))

(defun nvector-substitute-if* (new test sequence incrementer
                                   start end count key)
  (do ((index start (+ index incrementer)))
      ((or (= index end) (= count 0)) sequence)
    (when (funcall test (apply-key key (aref sequence index)))
      (setf (aref sequence index) new)
      (setq count (1- count)))))

(defun nsubstitute-if (new test sequence &key from-end (start 0) end count key)
  (let ((end (or end (length sequence)))
	(count (real-count count)))
    (if (listp sequence)
	(if from-end
	    (let ((length (length sequence)))
	      (nreverse (nlist-substitute-if*
			 new test (nreverse sequence)
			 (- length end) (- length start) count key)))
	    (nlist-substitute-if* new test sequence
				  start end count key))
	(if from-end
	    (nvector-substitute-if* new test sequence -1
				    (1- end) (1- start) count key)
	    (nvector-substitute-if* new test sequence 1
				    start end count key)))))


(defun nlist-substitute-if-not* (new test sequence start end count key)
  (do ((list (nthcdr start sequence) (cdr list))
       (index start (1+ index)))
      ((or (= index end) (null list) (= count 0)) sequence)
    (when (not (funcall test (apply-key key (car list))))
      (rplaca list new)
      (setq count (1- count)))))

(defun nvector-substitute-if-not* (new test sequence incrementer
                                       start end count key)
  (do ((index start (+ index incrementer)))
      ((or (= index end) (= count 0)) sequence)
    (when (not (funcall test (apply-key key (aref sequence index))))
      (setf (aref sequence index) new)
      (setq count (1- count)))))

(defun nsubstitute-if-not (new test sequence &key from-end (start 0)
			       end count key)
  (let ((end (or end (length sequence)))
	(count (real-count count)))
    (if (listp sequence)
	(if from-end
	    (let ((length (length sequence)))
	      (nreverse (nlist-substitute-if-not*
			 new test (nreverse sequence)
			 (- length end) (- length start) count key)))
	    (nlist-substitute-if-not* new test sequence
				      start end count key))
	(if from-end
	    (nvector-substitute-if-not* new test sequence -1
					(1- end) (1- start) count key)
	    (nvector-substitute-if-not* new test sequence 1
					start end count key)))))
