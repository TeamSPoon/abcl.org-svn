;;; debug.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: debug.lisp,v 1.5 2003-10-04 10:53:35 piso Exp $
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

(defun debug-loop ()
  (let ((tpl::*break-level* (1+ tpl::*break-level*)))
  (fresh-line *debug-io*)
  (format *debug-io* "Type :CONTINUE to return from break or :RESET to return to top level.~%")
    (loop
      (catch 'debug-loop-catcher
        (handler-case
            (tpl::repl)
          (error (c) (format t "Error: ~S.~%" c) (break) (throw 'debug-loop-catcher nil)))))))

(defun invoke-debugger (condition)
  (when *debugger-hook*
    (let ((hook-function *debugger-hook*)
          (*debugger-hook* nil))
      (funcall hook-function condition hook-function)))
  (catch 'tpl::continue-catcher
    (debug-loop)))

(defun break (&optional format-control &rest format-arguments)
  (fresh-line *debug-io*)
  (format *debug-io* "BREAK called.~%")
  (invoke-debugger nil))
