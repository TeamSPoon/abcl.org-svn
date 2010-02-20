;;; directory.lisp
;;;
;;; Copyright (C) 2004-2007 Peter Graves
;;; Copyright (C) 2008 Ville Voutilainen
;;; $Id$
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
;;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
;;;
;;; As a special exception, the copyright holders of this library give you
;;; permission to link this library with independent modules to produce an
;;; executable, regardless of the license terms of these independent
;;; modules, and to copy and distribute the resulting executable under
;;; terms of your choice, provided that you also meet, for each linked
;;; independent module, the terms and conditions of the license of that
;;; module.  An independent module is a module which is not derived from
;;; or based on this library.  If you modify this library, you may extend
;;; this exception to your version of the library, but you are not
;;; obligated to do so.  If you do not wish to do so, delete this
;;; exception statement from your version.

(in-package "SYSTEM")

(defun pathname-as-file (pathname)
  (let ((directory (pathname-directory pathname)))
    (make-pathname :host nil
                   :device (pathname-device pathname)
                   :directory (butlast directory)
                   :name (car (last directory))
                   :type nil
                   :version nil)))

(defun list-directories-with-wildcards (pathname)
  (let* ((directory (pathname-directory pathname))
	 (first-wild (position-if #'wild-p directory))
	 (wild (when first-wild (nthcdr first-wild directory)))
	 (non-wild (if first-wild
		       (nbutlast directory
				 (- (length directory) first-wild))
		       directory))
	 (newpath (make-pathname :directory non-wild
				 :name nil :type nil :defaults pathname))
	 (entries (list-directory newpath)))
    (if (not wild)
	entries (mapcan (lambda (entry)
                          (let* ((pathname (pathname entry))
                                 (directory (pathname-directory pathname))
                                 (rest-wild (cdr wild)))
                            (unless (pathname-name pathname)
			      (when (pathname-match-p (first (last directory)) (if (eql (car wild) :wild) "*" (car wild)))
				(when rest-wild
				  (setf directory (nconc directory rest-wild)))
  				(list-directories-with-wildcards
				 (make-pathname :directory directory
						:defaults newpath))))))
                        entries))))


(defun directory (pathspec &key)
  (let ((pathname (merge-pathnames pathspec)))
    (when (logical-pathname-p pathname)
      (setq pathname (translate-logical-pathname pathname)))
    (if (pathname-jar-p pathname)
        (directory-jar pathspec)
        (if (or (position #\* (namestring pathname))
                (wild-pathname-p pathname))
            (let ((namestring (directory-namestring pathname)))
              (when (and namestring (> (length namestring) 0))
                #+windows
                (let ((device (pathname-device pathname)))
                  (when device
                    (setq namestring (concatenate 'string device ":" namestring))))
                (let ((entries (list-directories-with-wildcards namestring))
                      (matching-entries ()))
                  (dolist (entry entries)
                    (cond ((file-directory-p entry)
                           (when (pathname-match-p (file-namestring (pathname-as-file entry)) (file-namestring pathname))
                             (push entry matching-entries)))
                          ((pathname-match-p (file-namestring entry) (file-namestring pathname))
                           (push entry matching-entries))))
                  matching-entries)))
            ;; Not wild.
            (let ((truename (probe-file pathname)))
              (if truename
                  (list (pathname truename))
                  nil))))))

;;; Thanks to Alan "Never touch Java unless you have to" Ruttenberg
;;; XXX need to handle JAR in JAR cases
;;; XXX doesn't handle non file: JAR entries
(defun directory-jar (pathname)
  (let* ((device (pathname-device pathname))
	 (jarfile (namestring (car device)))
	 (rest-pathname (namestring (make-pathname :directory `(:absolute ,@(cdr (pathname-directory pathname)))
						   :name (pathname-name pathname)
						   :type (pathname-type pathname)))))
    (if (or (position #\* (namestring rest-pathname))
	    (wild-pathname-p rest-pathname))
	(let ((jar (java:jnew "java.util.zip.ZipFile" jarfile)))
	  (let ((els (java:jcall "entries" jar)))
	    (loop :while (java:jcall "hasMoreElements" els)
	       :for name = (java:jcall "getName"
                                       (java:jcall "nextElement" els))
	       :when (pathname-match-p (concatenate 'string "/" name) rest-pathname)
	       :collect (make-pathname :device (pathname-device pathname)
                                       :name (pathname-name name)
                                       :type (pathname-type name)
                                       :directory `(:relative ,@(cdr (pathname-directory name)))))))
	(let ((truename (probe-file pathname)))
	  (if truename
              (list truename)
              nil)))))
