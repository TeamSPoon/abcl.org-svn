;;; jvm.lisp
;;;
;;; Copyright (C) 2003-2004 Peter Graves
;;; $Id: jvm.lisp,v 1.162 2004-05-09 14:13:13 piso Exp $
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

(require '#:opcodes)

(in-package "JVM")

(export '(compile-defun *catch-errors* jvm-compile jvm-compile-package))

(import 'sys::%format)

(shadow '(method variable))

(defvar *debug* nil)

(defvar *pool* nil)

(defvar *stream* nil)
(defvar *defun-name* nil)
(defvar *this-class* nil)
(defvar *pool-count* 1)

(defvar *code* ())
(defvar *static-code* ())
(defvar *fields* ())

(defvar *blocks* ())
(defvar *locals* ())
(defvar *max-locals* 0)

;; Returns index of allocated slot.
(defun allocate-local (symbol)
  (let ((index (length *locals*)))
    (push symbol *locals*)
    (when (< *max-locals* (1+ index))
      (setf *max-locals* (1+ index)))
    index))

;; Returns index of allocated slot.
(defun local-index (symbol)
  (let ((tail (memq symbol *locals*)))
    (and tail (1- (length tail)))))

(defvar *variables* ())

(defstruct variable
  name
  special-p)

(defun push-variable (var special-p)
  (push (make-variable :name var :special-p special-p) *variables*))

(defun find-variable (var)
  (find var *variables* :key 'variable-name))

(defvar *local-functions* ())

(defstruct local-function
  name
  args-to-add
  function)

(defvar *args* nil)
(defvar *using-arg-array* nil)
(defvar *hairy-arglist-p* nil)

(defvar *env* nil)
(defvar *closure-vars* nil)

(defvar *val* nil) ; index of value register

(defun clear ()
  (setf *pool* nil
        *pool-count* 1
        *code* nil)
  t)

(defun dump-pool ()
  (let ((pool (reverse *pool*))
        entry type)
    (dotimes (index (1- *pool-count*))
      (setq entry (car pool))
      (setq type (case (car entry)
                   (7 'class)
                   (9 'field)
                   (10 'method)
                   (11 'interface)
                   (8 'string)
                   (3 'integer)
                   (4 'float)
                   (5 'long)
                   (6 'double)
                   (12 'name-and-type)
                   (1 'utf8)))
      (format t "~D: ~A ~S~%" (1+ index) type entry)
      (setq pool (cdr pool))))
  t)

;; Returns index of entry (1-based).
(defun pool-add (entry)
  (setq *pool* (cons entry *pool*))
  (prog1
    *pool-count*
    (incf *pool-count*)))

;; Returns index of entry (1-based).
(defun pool-find-entry (entry)
  (do* ((remaining *pool* (cdr remaining))
        (i 0 (1+ i))
        (current (car remaining) (car remaining)))
       ((null remaining) nil)
    (when (equal current entry)
      (return-from pool-find-entry (- *pool-count* 1 i)))))

;; Adds entry if not already in pool. Returns index of entry (1-based).
(defun pool-get (entry)
  (or (pool-find-entry entry) (pool-add entry)))

(defun pool-name (name)
  (pool-get (list 1 (length name) name)))

(defun pool-name-and-type (name type)
  (let* ((name-index (pool-name name))
         (type-index (pool-name type)))
    (pool-get (list 12 name-index type-index))))

(defun pool-class (class-name)
  (let ((class-name class-name))
    (dotimes (i (length class-name))
      (when (eql (char class-name i) #\.)
        (setf (char class-name i) #\/)))
    (pool-get (list 7 (pool-name class-name)))))

;; (tag class-index name-and-type-index)
(defun pool-field (class-name field-name type-name)
  (let* ((class-index (pool-class class-name))
         (name-and-type-index (pool-name-and-type field-name type-name)))
    (pool-get (list 9 class-index name-and-type-index))))

;; (tag class-index name-and-type-index)
(defun pool-method (class-name method-name type-name)
  (let* ((class-index (pool-class class-name))
         (name-and-type-index (pool-name-and-type method-name type-name)))
    (pool-get (list 10 class-index name-and-type-index))))

(defun pool-string (string)
  (pool-get (list 8 (pool-name string))))

(defun pool-int (n)
  (pool-get (list 3 n)))

(defun u2 (n)
  (list (ash n -8) (logand n #xff)))

(defstruct instruction opcode args stack depth)

(defun inst (opcode &optional args)
  (unless (listp args)
    (setq args (list args)))
  (make-instruction :opcode opcode :args args :stack nil :depth nil))

(defun emit (instr &rest args)
  (unless (numberp instr)
    (setf instr (opcode-number instr)))
  (let ((instruction (inst instr args)))
    (setq *code* (cons instruction *code*))
    instruction))

(defmacro emit-store-value ()
  `(emit 'store-value))

(defmacro emit-push-value ()
  `(emit 'push-value))

(defun remove-store-value ()
  (let* ((instruction (car *code*))
         (opcode (instruction-opcode instruction)))
    (when (eql opcode 204) ; STORE-VALUE
      (setf *code* (cdr *code*))
      t)))

(defconstant +java-string+ "Ljava/lang/String;")
(defconstant +lisp-class+ "org/armedbear/lisp/Lisp")
(defconstant +lisp-object-class+ "org/armedbear/lisp/LispObject")
(defconstant +lisp-object+ "Lorg/armedbear/lisp/LispObject;")
(defconstant +lisp-string+ "Lorg/armedbear/lisp/SimpleString;")
(defconstant +lisp-symbol-class+ "org/armedbear/lisp/Symbol")
(defconstant +lisp-symbol+ "Lorg/armedbear/lisp/Symbol;")
(defconstant +lisp-thread-class+ "org/armedbear/lisp/LispThread")
(defconstant +lisp-thread+ "Lorg/armedbear/lisp/LispThread;")
(defconstant +lisp-cons-class+ "org/armedbear/lisp/Cons")
(defconstant +lisp-fixnum-class+ "org/armedbear/lisp/Fixnum")
(defconstant +lisp-fixnum+ "Lorg/armedbear/lisp/Fixnum;")
(defconstant +lisp-simple-string-class+ "org/armedbear/lisp/SimpleString")
(defconstant +lisp-simple-string+ "Lorg/armedbear/lisp/SimpleString;")
(defconstant +lisp-environment-class+ "org/armedbear/lisp/Environment")
(defconstant +lisp-environment+ "Lorg/armedbear/lisp/Environment;")

(defun emit-push-nil ()
  (emit 'getstatic +lisp-class+ "NIL" +lisp-object+))

(defun emit-push-t ()
  (emit 'getstatic +lisp-class+ "T" +lisp-symbol+))

(defun make-descriptor (arg-types return-type)
  (with-output-to-string (s)
    (princ #\( s)
    (dolist (type arg-types)
      (princ type s))
    (princ #\) s)
    (princ (if return-type return-type "V") s)))

(defun descriptor (designator)
  (cond ((stringp designator)
         designator)
        ((listp designator)
         (unless (= (length designator) 2)
           (error "Bad method type descriptor ~S." designator))
         (make-descriptor (car designator) (cadr designator)))
        (t
         (error "Bad method type descriptor ~S." designator))))

(defun emit-invokestatic (class-name method-name descriptor stack)
  (assert stack)
  (let ((instruction (emit 'invokestatic
                           class-name method-name (descriptor descriptor))))
    (setf (instruction-stack instruction) stack)
    (assert (eql (instruction-stack instruction) stack))))

(defun emit-invokespecial (class-name method-name descriptor stack)
  (let ((instruction (emit 'invokespecial
                           class-name method-name (descriptor descriptor))))
    (setf (instruction-stack instruction) stack)))

(defun emit-invokevirtual (class-name method-name descriptor stack)
  (let ((instruction (emit 'invokevirtual
                           class-name method-name (descriptor descriptor))))
    (setf (instruction-stack instruction) stack)))

;; Index of local variable used to hold the current thread.
(defvar *thread* nil)
(defvar *thread-var-initialized* nil)

(defun ensure-thread-var-initialized ()
  (unless *thread-var-initialized*
    ;; Put the code to initialize the local at the very beginning of the
    ;; function, to guarantee that the local gets initialized even if the code
    ;; at our current location is never executed, since the local may be
    ;; referenced elsewhere too.
    (let ((code *code*))
      (setf *code* ())
      (emit-invokestatic +lisp-thread-class+
                         "currentThread"
;;                          `(() ,+lisp-thread+)
                         (make-descriptor () +lisp-thread+)
                         1)
      (emit 'astore *thread*)
      (setf *code* (append code *code*)))
    (setf *thread-var-initialized* t)))

(defun emit-clear-values ()
  (ensure-thread-var-initialized)
  (emit 'aload *thread*)
  (emit 'aconst_null)
  (emit 'putfield +lisp-thread-class+ "_values" "[Lorg/armedbear/lisp/LispObject;"))

(defparameter single-valued-operators (make-hash-table :test 'eq))

(defun single-valued-p-init ()
  (dolist (op '(+ - * /
                1+ 1- + - < > <= >= = /=
                car cdr caar cadr cdar cddr cadar caddr cdddr cddddr
                first second third
                eq eql equal equalp
                length
                constantp symbolp
                list list*
                macro-function
                compiler-macro-function
                sys::%defun
                get
                atom
                compiled-function-p
                fdefinition
                special-operator-p keywordp functionp fboundp zerop consp listp
                numberp integerp floatp
                plusp minusp
                complexp arrayp readtablep packagep
                array-dimensions array-rank array-total-size
                array-element-type upgraded-array-element-type
                simple-vector-p simple-string-p bit-vector-p simple-bit-vector-p
                stringp
                row-major-aref
                quote function
                mapcar
                find position
                append nconc subseq adjoin
                revappend nreconc
                copy-seq
                assoc assoc-if assoc-if-not acons assq assql
                char-code code-char char-int digit-char-p
                member ext:memq
                remove remove-if remove-if-not delete delete-if delete-if-not
                special-variable-p
                gensym
                symbol-name symbol-function
                coerce
                reverse nreverse
                last
                cons rplaca rplacd
                copy-list
                make-sequence make-list make-array make-package make-hash-table
                make-string
                find-package
                pathname make-pathname pathname-name directory
                package-used-by-list package-shadowing-symbols
                nthcdr
                aref elt
                not null endp
                concatenate
                format prin1 princ print write
                compute-restarts find-restart restart-name
                string
                string=
                setq
                multiple-value-list push pop
                type-of class-of
                typep sys::%typep
                abs
                ash
                float-radix
                logand logandc1 logandc2 logeqv logior lognand
                lognot logorc1 logorc2 logxor
                logbitp
                slot-boundp slot-value slot-exists-p
                allocate-instance
                find-class
                class-name
                constantly
                exp expt log
                min max
                realpart imagpart
                integer-length
                sqrt isqrt gcd lcm signum
                char schar
                open
                svref
                fill-pointer
                symbol-value symbol-package package-name
                fourth
                vector-push vector-push-extend
                union nunion
                remove-duplicates delete-duplicates
                read-byte
                lambda
                ext:classp
                ext:fixnump
                ext:memql
                sys::generic-function-name
                precompiler::precompile1
                ))
    (setf (gethash op single-valued-operators) t)))

(eval-when (:load-toplevel :execute)
  (single-valued-p-init))

(defun single-valued-p (form)
  (cond ((atom form)
         t)
        ((memq (car form) '(and or))
         (every #'single-valued-p (cdr form)))
        (t
         (gethash (car form) single-valued-operators))))

(defun maybe-emit-clear-values (form)
  (unless (single-valued-p form)
;;     (format t "Not single-valued: ~S~%" form)
    (emit-clear-values)))

(defun emit-invoke-method (method-name)
  (unless (remove-store-value)
    (emit-push-value))
  (emit-invokevirtual +lisp-object-class+
                      method-name
                      "()Lorg/armedbear/lisp/LispObject;"
                      0)
  (emit-store-value))

;; CODE is a list.
(defun resolve-args (instruction)
  (let ((opcode (instruction-opcode instruction))
        (args (instruction-args instruction)))
    (case opcode
      (203 ; PUSH-VALUE
       (case *val*
         (0
          (inst 42)) ; ALOAD_0
         (1
          (inst 43)) ; ALOAD_1
         (2
          (inst 44)) ; ALOAD_2
         (3
          (inst 45)) ; ALOAD_3
         (t
          (inst 25 *val*))))
      (204 ; STORE-VALUE
       (case *val*
         (0
          (inst 75)) ; ASTORE_0
         (1
          (inst 76)) ; ASTORE_1
         (2
          (inst 77)) ; ASTORE_2
         (3
          (inst 78)) ; ASTORE_3
         (t
          (inst 58 *val*))))
      ((1 ; ACONST_NULL
        2 ; ICONST_M1
        3 ; ICONST_0
        4 ; ICONST_1
        5 ; ICONST_2
        6 ; ICONST_3
        7 ; ICONST_4
        8 ; ICONST_5
        42 ; ALOAD_0
        43 ; ALOAD_1
        44 ; ALOAD_2
        45 ; ALOAD_3
        50 ; AALOAD
        75 ; ASTORE_0
        76 ; ASTORE_1
        77 ; ASTORE_2
        78 ; ASTORE_3
        83 ; AASTORE
        87 ; POP
        89 ; DUP
        91 ; DUP_X2
        95 ; SWAP
        153 ; IFEQ
        154 ; IFNE
        166 ; IF_ACMPNE
        165 ; IF_ACMPEQ
        167 ; GOTO
        176 ; ARETURN
        177 ; RETURN
        202 ; LABEL
        )
       instruction)
      (25 ; ALOAD
       (let ((index (car args)))
         (cond ((= index 0)
                (inst 42)) ; ALOAD_O
               ((= index 1)
                (inst 43)) ; ALOAD_1
               ((= index 2)
                (inst 44)) ; ALOAD_2
               ((= index 3)
                (inst 45)) ; ALOAD_3
               ((<= 0 index 255)
                (inst 25 index))
               (t
                (error "ALOAD unsupported case")))))
      (58 ; ASTORE
       (let ((index (car args)))
         (cond ((= index 0)
                (inst 75)) ; ASTORE_O
               ((= index 1)
                (inst 76)) ; ASTORE_1
               ((= index 2)
                (inst 77)) ; ASTORE_2
               ((= index 3)
                (inst 78)) ; ASTORE_3
               ((<= 0 index 255)
                (inst 58 index))
               (t
                (error "ASTORE unsupported case")))))
      ((178 ; GETSTATIC class-name field-name type-name
        179 ; PUTSTATIC class-name field-name type-name
        )
       (let ((index (pool-field (first args) (second args) (third args))))
         (inst opcode (u2 index))))
      ((180 ; GETFIELD
        181 ; PUTFIELD class-name field-name type-name
        )
       (let ((index (pool-field (first args) (second args) (third args))))
         (inst opcode (u2 index))))
      ((182 ; INVOKEVIRTUAL class-name method-name descriptor
        183 ; INVOKESPECIAL class-name method-name descriptor
        184 ; INVOKESTATIC class-name method-name descriptor
        )
       (let ((index (pool-method (first args) (second args) (third args))))
;;          (inst opcode (u2 index))))
         (setf (instruction-args instruction) (u2 index))
         instruction))
      ((187 ; NEW class-name
        189 ; ANEWARRAY class-name
        192 ; CHECKCAST class-name
        193 ; INSTANCEOF class-name
        )
       (let ((index (pool-class (first args))))
         (inst opcode (u2 index))))
      ((16 ; BIPUSH
        17 ; SIPUSH
        )
       (let ((n (first args)))
         (cond ((= n 0)
                (inst 3)) ; ICONST_0
               ((= n 1)
                (inst 4)) ; ICONST_1
               ((= n 2)
                (inst 5)) ; ICONST_2
               ((= n 3)
                (inst 6)) ; ICONST_3
               ((= n 4)
                (inst 7)) ; ICONST_4
               ((= n 5)
                (inst 8)) ; ICONST_5
               ((<= -128 n 127)
                (inst 16 (logand n #xff))) ; BIPUSH
               (t ; SIPUSH
                (inst 17 (u2 n))))))
      (18 ; LDC
       (unless (= (length args) 1)
         (error "wrong number of args for LDC"))
       (if (> (car args) 255)
           (inst 19 (u2 (car args))) ; LDC_W
           (inst opcode args)))
      (t
       (error "RESOLVE-ARGS unsupported opcode ~D" opcode)))))

;; CODE is a list of INSTRUCTIONs.
(defun resolve-opcodes (code)
  (map 'vector #'resolve-args code))

(defun branch-opcode-p (opcode)
  (member opcode
    '(153 ; IFEQ
      154 ; IFNE
      165 ; IF_ACMPEQ
      166 ; IF_ACMPNE
      167 ; GOTO
      )))

(defun walk-code (code start-index depth)
  (do* ((i start-index (1+ i))
        (limit (length code)))
       ((>= i limit) depth)
    (let ((instruction (svref code i)))
      (when (instruction-depth instruction)
        (return-from walk-code))
      (setf (instruction-depth instruction) depth)
      (setf depth (+ depth (instruction-stack instruction)))
      (if (branch-opcode-p (instruction-opcode instruction))
          (let ((label (car (instruction-args instruction))))
;;             (format t "target = ~S~%" target)
            (walk-code code (symbol-value label) depth)
            )
          ()))))

(defun analyze-stack ()
  (sys::require-type *code* 'vector)
  (dotimes (i (length *code*))
    (let* ((instruction (svref *code* i))
           (opcode (instruction-opcode instruction)))
      (when (eql opcode 202)
        (let ((label (car (instruction-args instruction))))
          (set label i)))
      (unless (instruction-stack instruction)
        (setf (instruction-stack instruction) (opcode-stack-effect opcode)))))
  (walk-code *code* 0 0)
  (let ((max-stack 0))
    (dotimes (i (length *code*))
      (let ((instruction (svref *code* i)))
        (setf max-stack (max max-stack (instruction-depth instruction)))))
;;     (format t "max-stack = ~D~%" max-stack)
    max-stack))

(defun finalize-code ()
  (setf *code* (nreverse (coerce *code* 'vector))))

(defun print-code()
  (dotimes (i (length *code*))
    (let ((instruction (svref *code* i)))
      (format t "~A ~S~%"
              (opcode-name (instruction-opcode instruction))
              (instruction-args instruction)))))

(defun validate-labels ()
  (dotimes (i (length *code*))
    (let* ((instruction (svref *code* i))
           (opcode (instruction-opcode instruction)))
      (when (eql opcode 202)
        (let ((label (car (instruction-args instruction))))
          (set label i))))))

(defun optimize-code ()
  (when *debug*
    (format t "----- before optimization -----~%")
    (print-code))
  (loop
    (let ((changed-p nil))
      ;; Make a list of the labels that are actually branched to.
      (let ((branch-targets ()))
        (dotimes (i (length *code*))
          (let ((instruction (svref *code* i)))
            (when (branch-opcode-p (instruction-opcode instruction))
              (push (car (instruction-args instruction)) branch-targets))))
;;         (format t "branch-targets = ~S~%" branch-targets)
        ;; Remove labels that are not used as branch targets.
        (dotimes (i (length *code*))
          (let ((instruction (svref *code* i)))
            (when (= (instruction-opcode instruction) 202) ; LABEL
              (let ((label (car (instruction-args instruction))))
                (unless (member label branch-targets)
                  (setf (instruction-opcode instruction) 0)'
                  (setf changed-p t)))))))
      (setf *code* (delete 0 *code* :key #'instruction-opcode))
      (dotimes (i (length *code*))
        (let ((instruction (svref *code* i)))
          (when (and (< i (1- (length *code*)))
                     (= (instruction-opcode instruction) 167) ; GOTO
                     (let ((next-instruction (svref *code* (1+ i))))
                       (cond ((and (= (instruction-opcode next-instruction) 202) ; LABEL
                                   (eq (car (instruction-args instruction))
                                       (car (instruction-args next-instruction))))
                              ;; GOTO next instruction.
                              (setf (instruction-opcode instruction) 0)
                              (setf changed-p t))
                             ((= (instruction-opcode next-instruction) 167) ; GOTO
                              ;; One GOTO right after another.
                              (setf (instruction-opcode next-instruction) 0)
                              (setf changed-p t))
                              ))))))
      (setf *code* (delete 0 *code* :key #'instruction-opcode))
      ;; Reduce GOTOs.
      (validate-labels)
      (dotimes (i (length *code*))
        (let ((instruction (svref *code* i)))
          (when (eql (instruction-opcode instruction) 167) ; GOTO
            (let* ((label (car (instruction-args instruction)))
                   (target-index (1+ (symbol-value label)))
                   (instr1 (svref *code* target-index))
                   (instr2 (if (eql (instruction-opcode instr1) 203) ; PUSH-VALUE
                               (svref *code* (1+ target-index))
                               nil)))
              (when (and instr2 (eql (instruction-opcode instr2) 176)) ; ARETURN
                (let ((previous-instruction (svref *code* (1- i))))
                  (when (eql (instruction-opcode previous-instruction) 204) ; STORE-VALUE
                    (setf (instruction-opcode previous-instruction) 176) ; ARETURN
                    (setf (instruction-opcode instruction) 0)
                    (setf changed-p t))))))))
      (setf *code* (delete 0 *code* :key #'instruction-opcode))
      ;; Look for sequence STORE-VALUE LOAD-VALUE ARETURN.
      (dotimes (i (- (length *code*) 2))
        (let ((instr1 (svref *code* i))
              (instr2 (svref *code* (+ i 1)))
              (instr3 (svref *code* (+ i 2))))
          (when (and (eql (instruction-opcode instr1) 204)
                     (eql (instruction-opcode instr2) 203)
                     (eql (instruction-opcode instr3) 176))
            (setf (instruction-opcode instr1) 176)
            (setf (instruction-opcode instr2) 0)
            (setf (instruction-opcode instr3) 0)
            (setf changed-p t))))
      (setf *code* (delete 0 *code* :key #'instruction-opcode))
      (unless changed-p
          (return))))
  (when *debug*
    (format t "----- after optimization -----~%")
    (print-code)))

(defvar *max-stack*)

;; CODE is a list of INSTRUCTIONs.
(defun code-bytes (code)
  (let ((code (resolve-opcodes code))
        (length 0))
    ;; Pass 1: calculate label offsets and overall length.
    (dotimes (i (length code))
      (let* ((instruction (aref code i))
             (opcode (instruction-opcode instruction)))
        (if (= opcode 202) ; LABEL
            (let ((label (car (instruction-args instruction))))
              (set label length))
            (incf length (opcode-size opcode)))))
    ;; Pass 2: replace labels with calculated offsets.
    (let ((index 0))
      (dotimes (i (length code))
        (let ((instruction (aref code i)))
          (when (branch-opcode-p (instruction-opcode instruction))
            (let* ((label (car (instruction-args instruction)))
                   (offset (- (symbol-value `,label) index)))
              (setf (instruction-args instruction) (u2 offset))))
          (unless (= (instruction-opcode instruction) 202) ; LABEL
            (incf index (opcode-size (instruction-opcode instruction)))))))
    ;; Expand instructions into bytes, skipping LABEL pseudo-instructions.
    (let ((bytes (make-array length))
          (index 0))
      (dotimes (i (length code))
        (let ((instruction (aref code i)))
          (unless (= (instruction-opcode instruction) 202) ; LABEL
            (setf (svref bytes index) (instruction-opcode instruction))
            (incf index)
            (dolist (byte (instruction-args instruction))
              (setf (svref bytes index) byte)
              (incf index)))))
      bytes)))

(defun write-u1 (n)
  (sys::write-8-bits (logand n #xFF) *stream*))

(defun write-u2 (n)
  (sys::write-8-bits (ash n -8) *stream*)
  (sys::write-8-bits (logand n #xFF) *stream*))

(defun write-u4 (n)
  (write-u2 (ash n -16))
  (write-u2 (logand n #xFFFF)))

(defun write-s4 (n)
  (cond ((minusp n)
         (write-u4 (1+ (logxor (- n) #xFFFFFFFF))))
        (t
         (write-u4 n))))

(defun write-utf8 (string)
  (dotimes (i (length string))
    (let ((c (char string i)))
      (if (eql c #\null)
          (progn
            (write-u1 #xC0)
            (write-u1 #x80))
          (write-u1 (char-int c))))))

(defun utf8-length (string)
  (let ((len 0))
    (dotimes (i (length string))
      (incf len (if (eql (char string i) #\null) 2 1)))
    len))

(defun write-constant-pool-entry (entry)
  (write-u1 (first entry))
  (case (first entry)
    (1 ; UTF8
     (write-u2 (utf8-length (third entry)))
     (write-utf8 (third entry)))
    (3 ; int
     (write-s4 (second entry)))
    ((5 6)
     (write-u4 (second entry))
     (write-u4 (third entry)))
    ((9 10 11 12)
     (write-u2 (second entry))
     (write-u2 (third entry)))
    ((7 8)
     (write-u2 (second entry)))
    (t
     (error "WRITE-CP-ENTRY unhandled tag ~D~%" (car entry)))
  ))

(defun write-constant-pool ()
  (write-u2 *pool-count*)
  (dolist (entry (reverse *pool*))
    (write-constant-pool-entry entry)))

(defstruct field
  access-flags
  name
  descriptor
  name-index
  descriptor-index)

(defstruct method
  access-flags
  name
  descriptor
  name-index
  descriptor-index
  max-stack
  max-locals
  code)

(defun make-constructor (super name args body)
  (let* ((constructor (make-method :name "<init>"
                                   :descriptor "()V"))
         (*code* ()))
    (setf (method-name-index constructor) (pool-name (method-name constructor)))
    (setf (method-descriptor-index constructor) (pool-name (method-descriptor constructor)))
    (setf (method-max-locals constructor) 1)
    (cond (*hairy-arglist-p*
           (emit 'aload_0) ;; this
           (emit 'aconst_null) ;; name
           (let* ((*print-level* nil)
                  (*print-length* nil)
                  (s (format nil "~S" args)))
             (emit 'ldc
                   (pool-string s))
             (emit-invokestatic +lisp-class+
                                "readObjectFromString"
;;                                 "(Ljava/lang/String;)Lorg/armedbear/lisp/LispObject;"
                                `((,+java-string+) ,+lisp-object+)
                                0))
           (emit-push-nil) ;; body
           (emit 'aconst_null) ;; environment
           (emit-invokespecial super
                               "<init>"
;;                                "(Lorg/armedbear/lisp/Symbol;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/Environment;)V"
                               `((,+lisp-symbol+ ,+lisp-object+ ,+lisp-object+ ,+lisp-environment+) nil)
                               -4))
          (t
           (emit 'aload_0)
           (emit-invokespecial super
                               "<init>"
;;                                "()V"
                               '(nil nil)
                               0)))
    (setf *code* (append *static-code* *code*))
    (emit 'return)
    (finalize-code)
    (optimize-code)
    (setf (method-max-stack constructor) (analyze-stack))
    (setf (method-code constructor) (code-bytes *code*))
    constructor))

(defun write-code-attr (method)
  (let* ((name-index (pool-name "Code"))
         (code (method-code method))
         (code-length (length code))
         (length (+ code-length 12))
         (max-stack (or (method-max-stack method) 20))
         (max-locals (or (method-max-locals method) 1)))
    (write-u2 name-index)
    (write-u4 length)
    (write-u2 max-stack)
    (write-u2 max-locals)
    (write-u4 code-length)
    (dotimes (i code-length)
      (write-u1 (svref code i)))
    (write-u2 0) ; handler table length
    (write-u2 0) ; attributes count
    ))

(defun write-method (method)
  (write-u2 (or (method-access-flags method) #x1)) ; access flags
  (write-u2 (method-name-index method))
  (write-u2 (method-descriptor-index method))
  (write-u2 1) ; attributes count
  (write-code-attr method))

(defun write-field (field)
  (write-u2 (or (field-access-flags field) #x1)) ; access flags
  (write-u2 (field-name-index field))
  (write-u2 (field-descriptor-index field))
  (write-u2 0)) ; attributes count

(defun declare-field (name descriptor)
  (let ((field (make-field :name name :descriptor descriptor)))
    (setf (field-access-flags field) (logior #x8 #x2)) ; private static
    (setf (field-name-index field) (pool-name (field-name field)))
    (setf (field-descriptor-index field) (pool-name (field-descriptor field)))
    (setq *fields* (cons field *fields*))))

(defun sanitize (symbol)
  (let* ((input (symbol-name symbol))
         (output (make-array (length input) :fill-pointer 0 :element-type 'character)))
    (dotimes (i (length input))
      (let ((c (char-upcase (char input i))))
        (cond ((<= #.(char-code #\A) (char-code c) #.(char-code #\Z))
               (vector-push c output))
              ((eql c #\-)
               (vector-push #\_ output)))))
    (when (plusp (length output))
      output)))

(defvar *declared-symbols* nil)
(defvar *declared-functions* nil)
(defvar *declared-strings* nil)

(defun declare-symbol (symbol)
  (let ((g (gethash symbol *declared-symbols*)))
    (unless g
      (let ((*code* *static-code*)
            (s (sanitize symbol)))
        (setf g (symbol-name (gensym)))
        (when s
          (setf g (concatenate 'string g "_" s)))
        (declare-field g +lisp-symbol+)
        (emit 'ldc (pool-string (symbol-name symbol)))
        (emit 'ldc (pool-string (package-name (symbol-package symbol))))
        (emit-invokestatic +lisp-class+
                           "internInPackage"
                           "(Ljava/lang/String;Ljava/lang/String;)Lorg/armedbear/lisp/Symbol;"
                           -1)
        (emit 'putstatic
              *this-class*
              g
              +lisp-symbol+)
        (setq *static-code* *code*)
        (setf (gethash symbol *declared-symbols*) g)))
    g))

(defun declare-function (symbol)
  (let ((f (gethash symbol *declared-functions*)))
    (unless f
      (setf f (symbol-name (gensym)))
      (let ((s (sanitize symbol)))
        (when s
          (setf f (concatenate 'string f "_" s))))
      (let ((*code* *static-code*)
            (g (gethash symbol *declared-symbols*)))
        (cond (g
               (emit 'getstatic
                     *this-class*
                     g
                     +lisp-symbol+))
              (t
               (emit 'ldc (pool-string (symbol-name symbol)))
               (emit 'ldc (pool-string (package-name (symbol-package symbol))))
               (emit-invokestatic +lisp-class+
                                  "internInPackage"
                                  "(Ljava/lang/String;Ljava/lang/String;)Lorg/armedbear/lisp/Symbol;"
                                  -1)))
        (declare-field f +lisp-object+)
        (emit-invokevirtual +lisp-symbol-class+
                            "getSymbolFunctionOrDie"
                            "()Lorg/armedbear/lisp/LispObject;"
                            0)
        (emit 'putstatic
              *this-class*
              f
              +lisp-object+)
        (setq *static-code* *code*)
        (setf (gethash symbol *declared-functions*) f)))
    f))

(defun declare-keyword (symbol)
  (let ((g (symbol-name (gensym)))
        (*code* *static-code*))
    (declare-field g +lisp-symbol+)
    (emit 'ldc (pool-string (symbol-name symbol)))
    (emit-invokestatic "org/armedbear/lisp/Keyword"
                       "internKeyword"
                       "(Ljava/lang/String;)Lorg/armedbear/lisp/Symbol;"
                       0)
    (emit 'putstatic
          *this-class*
          g
          +lisp-symbol+)
    (setq *static-code* *code*)
    g))

(defun declare-fixnum (n)
  (let ((g (symbol-name (gensym)))
        (*code* *static-code*))
    (declare-field g +lisp-fixnum+)
    (emit 'new +lisp-fixnum-class+)
    (emit 'dup)
    (case n
      (-1
       (emit 'iconst_m1))
      (0
       (emit 'iconst_0))
      (1
       (emit 'iconst_1))
      (2
       (emit 'iconst_2))
      (3
       (emit 'iconst_3))
      (4
       (emit 'iconst_4))
      (5
       (emit 'iconst_5))
      (t
       (emit 'ldc (pool-int n))))
    (emit-invokespecial +lisp-fixnum-class+
                        "<init>"
                        "(I)V"
                        -2)
    (emit 'putstatic
          *this-class*
          g
          +lisp-fixnum+)
    (setf *static-code* *code*)
    g))

(defun declare-object-as-string (obj)
  (let* ((g (symbol-name (gensym)))
         (*print-level* nil)
         (*print-length* nil)
         (s (format nil "~S" obj))
         (*code* *static-code*))
    (declare-field g +lisp-object+)
    (emit 'ldc
          (pool-string s))
    (emit-invokestatic +lisp-class+
                       "readObjectFromString"
                       "(Ljava/lang/String;)Lorg/armedbear/lisp/LispObject;"
                       0)
    (emit 'putstatic
          *this-class*
          g
          +lisp-object+)
    (setf *static-code* *code*)
    g))

(defun declare-object (obj)
  (let ((key (symbol-name (gensym))))
    (sys::remember key obj)
    (let* ((g1 (declare-string key))
           (g2 (symbol-name (gensym)))
           (*code* *static-code*))
      (declare-field g2 +lisp-object+)
      (emit 'getstatic
            *this-class*
            g1
            +lisp-string+)
      (emit 'dup)
      (emit-invokestatic +lisp-class+
                         "recall"
                         "(Lorg/armedbear/lisp/SimpleString;)Lorg/armedbear/lisp/LispObject;"
                         0)
      (emit 'putstatic
            *this-class*
            g2
            +lisp-object+)
      (emit-invokestatic +lisp-class+
                         "forget"
                         "(Lorg/armedbear/lisp/SimpleString;)V"
                         -1)
      (setf *static-code* *code*)
      g2)))

(defun declare-lambda (obj)
  (let* ((g (symbol-name (gensym)))
         (*print-level* nil)
         (*print-length* nil)
         (s (format nil "~S" obj))
         (*code* *static-code*))
    (declare-field g +lisp-object+)
    (emit 'ldc
          (pool-string s))
    (emit-invokestatic +lisp-class+
                       "readObjectFromString"
                       "(Ljava/lang/String;)Lorg/armedbear/lisp/LispObject;"
                       0)
    (emit-invokestatic +lisp-class+
                       "coerceToFunction"
                       "(Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/Function;"
                       0)
    (emit 'putstatic
          *this-class*
          g
          +lisp-object+)
    (setf *static-code* *code*)
    g))

(defun declare-string (string)
  (let ((g (gethash string *declared-strings*)))
    (unless g
      (let ((*code* *static-code*))
        (setf g (symbol-name (gensym)))
        (declare-field g +lisp-simple-string+)
        (emit 'new +lisp-simple-string-class+)
        (emit 'dup)
        (emit 'ldc (pool-string string))
        (emit-invokespecial +lisp-simple-string-class+
                            "<init>"
                            "(Ljava/lang/String;)V"
                            -2)
        (emit 'putstatic
              *this-class*
              g
              +lisp-simple-string+)
        (setf *static-code* *code*)
        (setf (gethash string *declared-strings*) g)))
    g))

(defun compile-constant (form)
  (cond
   ((fixnump form)
    (let* ((n form)
           (translations '((0 . "ZERO") (1 . "ONE") (2 . "TWO") (-1 . "MINUS_ONE")))
           (translation (cdr (assoc n translations))))
      (if translation
          (emit 'getstatic
                +lisp-fixnum-class+
                translation
                +lisp-fixnum+)
          (emit 'getstatic
                *this-class*
                (declare-fixnum n)
                +lisp-fixnum+))
          (emit-store-value)))
   ((numberp form)
    (let ((g (declare-object-as-string form)))
      (emit 'getstatic
            *this-class*
            g
            +lisp-object+)
      (emit-store-value)))
   ((stringp form)
    (if *compile-file-truename*
        (let ((g (declare-string form)))
          (emit 'getstatic
                *this-class*
                g
                +lisp-simple-string+)
          (emit-store-value))
        (let ((g (declare-object form)))
          (emit 'getstatic
                *this-class*
                g
                +lisp-object+)
          (emit-store-value))))
   ((vectorp form)
    (let ((g (declare-object-as-string form)))
      (emit 'getstatic
            *this-class*
            g
            +lisp-object+)
      (emit-store-value)))
   ((characterp form)
    (let ((g (declare-object-as-string form)))
      (emit 'getstatic
            *this-class*
            g
            +lisp-object+)
      (emit-store-value)))
   ((symbolp form)
    (when (null (symbol-package form))
      ;; An uninterned symbol.
      (let ((g (if *compile-file-truename*
                   (declare-object-as-string form)
                   (declare-object form))))
        (emit 'getstatic
              *this-class*
              g
              +lisp-object+)
        (emit-store-value))))
   ((or (classp form) (hash-table-p form) (typep form 'generic-function))
    (let ((g (declare-object form)))
      (emit 'getstatic
            *this-class*
            g
            +lisp-object+)
      (emit-store-value)))
   (t
    (error "COMPILE-CONSTANT unhandled case ~S" form))))

(defun compile-binary-operation (op args)
  (compile-form (first args))
  (unless (remove-store-value)
    (emit-push-value))
  (maybe-emit-clear-values (first args))
  (compile-form (second args))
  (unless (remove-store-value)
    (emit-push-value))
  (maybe-emit-clear-values (second args))
  (emit-invokevirtual +lisp-object-class+
                      op
                      "(Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                      -1)
  (emit-store-value))

(defparameter unary-operators (make-hash-table :test 'eq))

(defun define-unary-operator (operator translation)
  (setf (gethash operator unary-operators) translation))

(define-unary-operator '1+              "incr")
(define-unary-operator '1-              "decr")
(define-unary-operator 'ABS             "ABS")
(define-unary-operator 'ATOM            "ATOM")
(define-unary-operator 'BIT-VECTOR-P    "BIT_VECTOR_P")
(define-unary-operator 'CADR            "cadr")
(define-unary-operator 'CAR             "car")
(define-unary-operator 'CDDR            "cddr")
(define-unary-operator 'CDR             "cdr")
(define-unary-operator 'CHARACTERP      "CHARACTERP")
(define-unary-operator 'COMPLEXP        "COMPLEXP")
(define-unary-operator 'CONSTANTP       "CONSTANTP")
(define-unary-operator 'DENOMINATOR     "DENOMINATOR")
(define-unary-operator 'ENDP            "ENDP")
(define-unary-operator 'EVENP           "EVENP")
(define-unary-operator 'FIRST           "car")
(define-unary-operator 'FLOATP          "FLOATP")
(define-unary-operator 'INTEGERP        "INTEGERP")
(define-unary-operator 'LENGTH          "LENGTH")
(define-unary-operator 'LISTP           "LISTP")
(define-unary-operator 'MINUSP          "MINUSP")
(define-unary-operator 'NREVERSE        "nreverse")
(define-unary-operator 'NUMBERP         "NUMBERP")
(define-unary-operator 'NUMERATOR       "NUMERATOR")
(define-unary-operator 'ODDP            "ODDP")
(define-unary-operator 'PLUSP           "PLUSP")
(define-unary-operator 'RATIONALP       "RATIONALP")
(define-unary-operator 'REALP           "REALP")
(define-unary-operator 'REST            "cdr")
(define-unary-operator 'SECOND          "cadr")
(define-unary-operator 'SIMPLE-STRING-P "SIMPLE_STRING_P")
(define-unary-operator 'STRINGP         "STRINGP")
(define-unary-operator 'SYMBOLP         "SYMBOLP")
(define-unary-operator 'VECTORP         "VECTORP")
(define-unary-operator 'ZEROP           "ZEROP")

(defun compile-function-call-1 (fun args)
  (let ((s (gethash fun unary-operators)))
    (if s
        (progn
          (compile-form (first args))
          (maybe-emit-clear-values (first args))
          (emit-invoke-method s)
          t)
        nil)))

(defparameter binary-operators (make-hash-table :test 'eq))

(defun define-binary-operator (operator translation)
  (setf (gethash operator binary-operators) translation))

(define-binary-operator 'eql               "EQL")
(define-binary-operator '+                 "add")
(define-binary-operator '-                 "subtract")
(define-binary-operator '/                 "divideBy")
(define-binary-operator '*                 "multiplyBy")
(define-binary-operator '<                 "IS_LT")
(define-binary-operator '<=                "IS_LE")
(define-binary-operator '>                 "IS_GT")
(define-binary-operator '>=                "IS_GE")
(define-binary-operator '=                 "IS_E")
(define-binary-operator '/=                "IS_NE")
(define-binary-operator 'mod               "MOD")
(define-binary-operator 'ash               "ash")
(define-binary-operator 'aref              "AREF")
(define-binary-operator 'sys::simple-typep "typep")

(defun compile-function-call-2 (fun args)
  (let ((translation (gethash fun binary-operators)))
    (if translation
        (compile-binary-operation translation args)
        (case fun
          (EQ
           (compile-form (first args))
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values (first args))
           (compile-form (second args))
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values (second args))
           (let ((label1 (gensym))
                 (label2 (gensym)))
             (emit 'if_acmpeq `,label1)
             (emit-push-nil)
             (emit 'goto `,label2)
             (emit 'label `,label1)
             (emit-push-t)
             (emit 'label `,label2))
           (emit-store-value)
           t)
          (LIST
           (compile-form (first args))
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values (first args))
           (compile-form (second args))
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values (second args))
           (emit-invokestatic +lisp-class+
                              "list2"
                              "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/Cons;"
                              -1)
           (emit-store-value)
           t)
          (t
           nil)))))

(defun compile-function-call-3 (fun args)
  (case fun
    (LIST
     (compile-form (first args))
     (unless (remove-store-value)
       (emit-push-value))
     (maybe-emit-clear-values (first args))
     (compile-form (second args))
     (unless (remove-store-value)
       (emit-push-value))
     (maybe-emit-clear-values (second args))
     (compile-form (third args))
     (unless (remove-store-value)
       (emit-push-value))
     (maybe-emit-clear-values (third args))
     (emit-invokestatic +lisp-class+
                        "list3"
                        "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/Cons;"
                        -2)
     (emit-store-value)
     t)
    (t
     nil)))

(defvar *toplevel-defuns* nil)

(defun unsafe-p (args)
  (if (atom args)
      nil
      (case (car args)
        (QUOTE
         nil)
        ((RETURN-FROM GO)
         t)
        (t
         (dolist (arg args)
           (when (unsafe-p arg)
             (return t)))))))

(defun rewrite-function-call (form)
  (let ((args (cdr form)))
    (if (unsafe-p args)
        (let ((syms ())
              (lets ()))
          ;; Preserve the order of evaluation of the arguments!
          (dolist (arg args)
            (if (constantp arg)
                (push arg syms)
                (let ((sym (gensym)))
                  (push sym syms)
                  (push (list sym arg) lets))))
          (list 'LET* (nreverse lets) (list* (car form) (nreverse syms))))
        form)))

(defun process-args (args)
  (dolist (form args)
    (compile-form form)
    (unless (remove-store-value)
      (emit-push-value))
    (maybe-emit-clear-values form)))

(defun compile-function-call (form &optional for-effect)
  (let ((new-form (rewrite-function-call form)))
    (when (neq new-form form)
      (return-from compile-function-call (compile-form new-form))))
  (let ((fun (car form))
        (args (cdr form)))
    (unless (symbolp fun)
      (error "COMPILE-FUNCTION-CALL ~S is not a symbol" fun))
    (let ((numargs (length args))
          local-function)
      (when (sys::built-in-function-p fun)
        (case numargs
          (1
           (when (compile-function-call-1 fun args)
             (return-from compile-function-call)))
          (2
           (when (compile-function-call-2 fun args)
             (return-from compile-function-call)))
          (3
           (when (compile-function-call-3 fun args)
             (return-from compile-function-call)))))
      (cond
       ((setf local-function
              (find fun *local-functions* :key #'local-function-name))
        (format t "compiling call to local function ~S~%" local-function)
        (when (local-function-args-to-add local-function)
          (format t "args-to-add = ~S~%" (local-function-args-to-add local-function))
          (setf args (append (local-function-args-to-add local-function) args))
          (setf numargs (length args))
          (format t "args = ~S~%" args))
        (let* ((g (declare-object (local-function-function local-function))))
          (emit 'getstatic
                *this-class*
                g
                +lisp-object+)))
       ((eq fun *defun-name*)
        (emit 'aload 0)) ; this
       ((or (sys::built-in-function-p fun) (memq fun *toplevel-defuns*))
        (let ((f (declare-function fun)))
          (emit 'getstatic
                *this-class*
                f
                +lisp-object+)))
       (t
        (let ((g (declare-symbol fun)))
          (emit 'getstatic
                *this-class*
                g
                +lisp-symbol+))
        (emit-invokevirtual +lisp-symbol-class+
                            "getSymbolFunctionOrDie"
                            "()Lorg/armedbear/lisp/LispObject;"
                            0)))
      (case numargs
        (0
         (emit-invokevirtual +lisp-object-class+
                             "execute"
                             "()Lorg/armedbear/lisp/LispObject;"
                             0))
        (1
         (process-args args)
         (emit-invokevirtual +lisp-object-class+
                             "execute"
                             "(Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                             -1))
        (2
         (process-args args)
         (emit-invokevirtual +lisp-object-class+
                             "execute"
                             "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                             -2))
        (3
         (process-args args)
         (emit-invokevirtual +lisp-object-class+
                             "execute"
                             "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                             -3))
        (4
         (process-args args)
         (emit-invokevirtual +lisp-object-class+
                             "execute"
                             "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                             -4))
        (t
         (emit 'sipush (length args))
         (emit 'anewarray "org/armedbear/lisp/LispObject")
         (let ((i 0))
           (dolist (arg args)
             (emit 'dup)
             (emit 'sipush i)
             (compile-form arg)
             (unless (remove-store-value)
               (emit-push-value)) ; leaves value on stack
             (emit 'aastore) ; store value in array
             (maybe-emit-clear-values arg)
             (incf i))) ; array left on stack here
         ;; Stack: function array-ref
         (emit-invokevirtual +lisp-object-class+
                             "execute"
                             "([Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                             -1)))
      (if for-effect
          (progn
            (emit 'pop)
            (maybe-emit-clear-values form))
          (emit-store-value)))))

(defun compile-test-not/null (form)
  (let ((arg (second form)))
    (cond ((and (consp arg)
                (memq (car arg) '(NOT NULL)))
           (compile-form (second arg))
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values (second arg))
           (emit-push-nil)
           'if_acmpeq)
          (t
           (compile-form arg)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg)
           (emit-push-nil)
           'if_acmpne))))

(defun compile-test (form)
  ;; Use a Java boolean if possible.
  (when (and (consp form)
             (not (special-operator-p (car form))))
    (let ((new-form (rewrite-function-call form)))
      (when (neq new-form form)
        (return-from compile-test (compile-test new-form))))
    (case (length form)
      (2
       (let ((op (car form))
             (arg (cadr form)))
         (when (memq op '(NOT NULL))
           (return-from compile-test (compile-test-not/null form)))
         (when (eq op 'SYMBOLP)
           (compile-form arg)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg)
           (emit 'instanceof +lisp-symbol-class+)
           (return-from compile-test 'ifeq))
         (when (eq op 'CONSP)
           (compile-form arg)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg)
           (emit 'instanceof +lisp-cons-class+)
           (return-from compile-test 'ifeq))
         (when (eq op 'ATOM)
           (compile-form arg)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg)
           (emit 'instanceof +lisp-cons-class+)
           (return-from compile-test 'ifne))
         (let ((s (cdr (assq op
                             '((CHARACTERP . "characterp")
                               (ENDP       . "endp")
                               (EVENP      . "evenp")
                               (FLOATP     . "floatp")
                               (INTEGERP   . "integerp")
                               (LISTP      . "listp")
                               (MINUSP     . "minusp")
                               (NUMBERP    . "numberp")
                               (ODDP       . "oddp")
                               (PLUSP      . "plusp")
                               (RATIONALP  . "rationalp")
                               (REALP      . "realp")
                               (STRINGP    . "stringp")
                               (VECTORP    . "vectorp")
                               (ZEROP      . "zerop"))))))
           (when s
             (compile-form arg)
             (unless (remove-store-value)
               (emit-push-value))
             (maybe-emit-clear-values arg)
             (emit-invokevirtual +lisp-object-class+
                                 s
                                 "()Z"
                                 0)
             (return-from compile-test 'ifeq)))))
      (3
       (let ((op (car form))
             (arg1 (second form))
             (arg2 (third form)))
         (when (eq op 'EQ)
           (compile-form arg1)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg1)
           (compile-form arg2)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg2)
           (return-from compile-test 'if_acmpne))
         (let ((s (cdr (assq op
                             '((=      . "isEqualTo")
                               (/=     . "isNotEqualTo")
                               (<      . "isLessThan")
                               (<=     . "isLessThanOrEqualTo")
                               (>      . "isGreaterThan")
                               (>=     . "isGreaterThanOrEqualTo")
                               (EQL    . "eql")
                               (EQUAL  . "equal")
                               (EQUALP . "equalp"))))))
           (when s
             (compile-form arg1)
             (unless (remove-store-value)
               (emit-push-value))
             (maybe-emit-clear-values arg1)
             (compile-form arg2)
             (unless (remove-store-value)
               (emit-push-value))
             (maybe-emit-clear-values arg2)
             (emit-invokevirtual +lisp-object-class+
                                 s
                                 "(Lorg/armedbear/lisp/LispObject;)Z"
                                 -1)
             (return-from compile-test 'ifeq)))))))
  ;; Otherwise...
  (compile-form form)
  (unless (remove-store-value)
    (emit-push-value))
  (maybe-emit-clear-values form)
  (emit-push-nil)
  'if_acmpeq)

(defun compile-if (form for-effect)
  (let* ((test (second form))
         (consequent (third form))
         (alternate (fourth form))
         (label1 (gensym))
         (label2 (gensym)))
    (cond ((eq test t)
           (compile-form consequent for-effect))
          (t
           (emit (compile-test test) `,label1)
           (compile-form consequent for-effect)
           (emit 'goto `,label2)
           (emit 'label `,label1)
           (compile-form alternate for-effect)
           (emit 'label `,label2)))))

(defun compile-multiple-value-list (form for-effect)
  (compile-form (second form))
  (unless (remove-store-value)
    (emit-push-value))
  (emit-invokestatic +lisp-class+
                     "multipleValueList"
                     "(Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                     0)
  (emit-store-value))

(defun compile-multiple-value-bind (form for-effect)
  (let* ((*locals* *locals*)
         (*variables* *variables*)
         (specials ())
         (vars (second form))
         (specialp nil)
         env-var)
    ;; Process declarations.
    (dolist (f (cdddr form))
      (unless (and (consp f) (eq (car f) 'declare))
        (return))
      (let ((decls (cdr f)))
        (dolist (decl decls)
          (when (eq (car decl) 'special)
            (setf specials (append (cdr decl) specials))))))
    ;; Are we going to bind any special variables?
    (dolist (var vars)
      (when (or (memq var specials) (special-variable-p var))
        (setf specialp t)
        (return)))
    ;; If so...
    (when specialp
      ;; Save current dynamic environment.
      (setf env-var (allocate-local nil))
      (ensure-thread-var-initialized)
      (emit 'aload *thread*)
      (emit 'getfield +lisp-thread-class+ "dynEnv" +lisp-environment+)
      (emit 'astore env-var))
    (compile-form (third form)) ;; Values form.
    (unless (remove-store-value)
      (emit-push-value))
    (ensure-thread-var-initialized)
    (emit 'aload *thread*)
    (emit 'swap)
    (emit 'bipush (length vars))
    (emit-invokevirtual +lisp-thread-class+
                        "getValues"
                        "(Lorg/armedbear/lisp/LispObject;I)[Lorg/armedbear/lisp/LispObject;"
                        0)
    ;; Values array is now on the stack (at runtime).
    (let ((index 0))
      (dolist (var vars)
        (setf specialp (if (or (memq var specials) (special-variable-p var)) t nil))
        (push-variable var specialp)
        (when (< index (1- (length vars)))
          (emit 'dup))
        (emit 'bipush index)
        (incf index)
        (emit 'aaload)
        ;; Value is on the runtime stack at this point.
        (cond (specialp
               (let ((g (declare-symbol var)))
                 (emit 'aload *thread*)
                 (emit 'swap)
                 (emit 'getstatic
                       *this-class*
                       g
                       +lisp-symbol+)
                 (emit 'swap)
                 (emit-invokevirtual +lisp-thread-class+
                                     "bindSpecial"
                                     "(Lorg/armedbear/lisp/Symbol;Lorg/armedbear/lisp/LispObject;)V"
                                     -2)))
              (t
               (emit 'astore (allocate-local var))))))
    (emit-clear-values)
    ;; Body.
    (do ((body (cdddr form) (cdr body)))
        ((null (cdr body))
         (compile-form (car body) for-effect))
      (compile-form (car body) t)
      (maybe-emit-clear-values (car body)))
    (when specialp
      ;; Restore dynamic environment.
      (emit 'aload *thread*)
      (emit 'aload env-var)
      (emit 'putfield +lisp-thread-class+ "dynEnv" +lisp-environment+))))

(defun compile-let/let* (form for-effect)
  (let* ((*locals* *locals*)
         (*variables* *variables*)
         (specials ())
         (varlist (cadr form))
         (specialp nil)
         env-var)
    ;; Process declarations.
    (dolist (f (cddr form))
      (unless (and (consp f) (eq (car f) 'declare))
        (return))
      (let ((decls (cdr f)))
        (dolist (decl decls)
          (when (eq (car decl) 'special)
            (setf specials (append (cdr decl) specials))))))
    ;; Are we going to bind any special variables?
    (dolist (varspec varlist)
      (let ((var (if (consp varspec) (car varspec) varspec)))
        (when (or (memq var specials) (special-variable-p var))
          (setf specialp t)
          (return))))
    ;; If so...
    (when specialp
      ;; Save current dynamic environment.
      (setf env-var (allocate-local nil))
      (ensure-thread-var-initialized)
      (emit 'aload *thread*)
      (emit 'getfield +lisp-thread-class+ "dynEnv" +lisp-environment+)
      (emit 'astore env-var))
    (ecase (car form)
      (LET
       (compile-let-vars varlist specials))
      (LET*
       (compile-let*-vars varlist specials)))
    ;; Body of LET/LET*.
    (do ((body (cddr form) (cdr body)))
        ((null (cdr body))
         (compile-form (car body) for-effect))
      (compile-form (car body) t)
      (maybe-emit-clear-values (car body)))
    (when specialp
      ;; Restore dynamic environment.
      (emit 'aload *thread*)
      (emit 'aload env-var)
      (emit 'putfield +lisp-thread-class+ "dynEnv" +lisp-environment+))))

(defun compile-let-vars (varlist specials)
  ;; Generate code to evaluate the initforms and leave the resulting values
  ;; on the stack.
  (let ((last-push-was-nil nil))
    (dolist (varspec varlist)
      (let (var initform)
        (if (consp varspec)
            (setq var (car varspec)
                  initform (cadr varspec))
            (setq var varspec
                  initform nil))
        (cond (initform
               (compile-form initform)
               (unless (remove-store-value)
                 (emit-push-value))
               (maybe-emit-clear-values initform)
               (setf last-push-was-nil nil))
              (t
               (if last-push-was-nil
                   (emit 'dup)
                   (emit-push-nil))
               (setf last-push-was-nil t))))))
  ;; Add local variables to local variables vector.
  (dolist (varspec varlist)
    (let* ((var (if (consp varspec) (car varspec) varspec))
           (specialp (if (or (memq var specials) (special-variable-p var)) t nil)))
      (unless specialp
        (allocate-local var))
      (push-variable var specialp)))
  ;; At this point the initial values are on the stack. Now generate code to
  ;; pop them off one by one and store each one in the corresponding local or
  ;; special variable. In order to do this, we must process the variable list
  ;; in reverse order.
  (do* ((varlist (reverse varlist) (cdr varlist))
        (varspec (car varlist) (car varlist)))
       ((null varlist))
    (let* ((var (if (consp varspec) (car varspec) varspec)))
      (cond ((or (memq var specials) (special-variable-p var))
             (ensure-thread-var-initialized)
             (emit 'aload *thread*)
             (emit 'swap)
             (let ((g (declare-symbol var)))
               (emit 'getstatic
                     *this-class*
                     g
                     +lisp-symbol+)
               (emit 'swap)
               (emit-invokevirtual +lisp-thread-class+
                                   "bindSpecial"
                                   "(Lorg/armedbear/lisp/Symbol;Lorg/armedbear/lisp/LispObject;)V"
                                   -2)))
            (t
             (let ((index (local-index var)))
               (unless index
                 (error "COMPILE-LET-VARS: can't find local variable ~S." var))
               (emit 'astore index)))))))

(defun compile-let*-vars (varlist specials)
  ;; Generate code to evaluate initforms and bind variables.
  (dolist (varspec varlist)
    (let (var initform specialp)
      (if (consp varspec)
          (setf var (car varspec)
                initform (cadr varspec))
          (setf var varspec
                initform nil))
      (setf specialp (if (or (memq var specials) (special-variable-p var)) t nil))
      (push-variable var specialp)
      (when specialp
        (ensure-thread-var-initialized))
      (cond (initform
             (compile-form initform)
             (unless (remove-store-value)
               (emit-push-value))
             (maybe-emit-clear-values initform))
            (t
             (emit-push-nil)))
      (cond (specialp
             (let ((g (declare-symbol var)))
               ;; Initial value is on the runtime stack at this point.
               (emit 'aload *thread*)
               (emit 'swap)
               (emit 'getstatic
                     *this-class*
                     g
                     +lisp-symbol+)
               (emit 'swap)
               (emit-invokevirtual +lisp-thread-class+
                                   "bindSpecial"
                                   "(Lorg/armedbear/lisp/Symbol;Lorg/armedbear/lisp/LispObject;)V"
                                   -2)))
            (t
             (emit 'astore (allocate-local var)))))))

;; Returns list of declared specials.
(defun process-special-declarations (forms)
  (let ((specials ()))
    (dolist (form forms)
      (unless (and (consp form) (eq (car form) 'declare))
        (return))
      (let ((decls (cdr form)))
        (dolist (decl decls)
          (when (eq (car decl) 'special)
            (setf specials (append (cdr decl) specials))))))
    specials))

(defun compile-locally (form for-effect)
  (let ((*variables* *variables*)
        (specials (process-special-declarations (cdr form))))
    (dolist (var specials)
      (push-variable var t))
    (cond ((null (cdr form))
           (emit-push-nil)
           (emit-store-value))
          (t
           (do ((forms (cdr form) (cdr forms)))
               ((null forms))
             (compile-form (car forms) (cdr forms)))))))

(defvar *tags* ())

(defstruct tag name label)

(defun label-for-tag (name)
  (let ((index (position name *tags* :from-end t :key #'tag-name)))
    (when index
      (tag-label (aref *tags* index)))))

(defun compile-tagbody (form for-effect)
  (let ((saved-fp (fill-pointer *tags*))
        (body (cdr form)))
    ;; Scan for tags.
    (dolist (f body)
      (when (atom f)
        (let ((name f)
              (label (gensym)))
          (vector-push (make-tag :name name :label label) *tags*))))
    (dolist (f body)
      (cond ((atom f)
             (let ((label (label-for-tag f)))
               (unless label
                 (error "COMPILE-TAGBODY: tag not found: ~S" f))
               (emit 'label label)))
            (t
             (compile-form f t))))
    (setf (fill-pointer *tags*) saved-fp))
  ;; TAGBODY returns NIL.
  (emit-clear-values)
  (unless for-effect
    (emit-push-nil)
    (emit-store-value)))

(defun compile-go (form for-effect)
  (let* ((name (cadr form))
         (label (label-for-tag name)))
    (unless label
      (error "COMPILE-GO: tag not found: ~S" name))
  (emit 'goto label)))

(defun compile-atom (form for-effect)
  (unless (= (length form) 2)
    (error "Wrong number of arguments for ATOM."))
  (compile-form (cadr form) nil)
  (unless (remove-store-value)
    (emit-push-value))
  (emit 'instanceof +lisp-cons-class+)
  (let ((label1 (gensym))
        (label2 (gensym)))
    (emit 'ifeq `,label1)
    (emit-push-nil)
    (emit 'goto `,label2)
    (emit 'label `,label1)
    (emit-push-t)
    (emit 'label `,label2)
    (emit-store-value)))

(defun contains-return (form)
  (if (atom form)
      nil
      (case (car form)
        (QUOTE
         nil)
        (RETURN-FROM
         t)
        (t
         (dolist (subform form)
           (when (contains-return subform)
             (return t)))))))

(defun compile-block (form for-effect)
  (let* ((block-label (cadr form))
         (body (cddr form))
         (block-exit (gensym))
         (*blocks* (acons block-label block-exit *blocks*))
         (*locals* *locals*)
         env-var)
    (when (contains-return body)
      ;; Save current dynamic environment.
      (setf env-var (allocate-local nil))
      (ensure-thread-var-initialized)
      (emit 'aload *thread*)
      (emit 'getfield +lisp-thread-class+ "dynEnv" +lisp-environment+)
      (emit 'astore env-var))
    ;; Compile subforms.
    (do ((subforms body (cdr subforms)))
        ((null subforms))
      (let ((subform (car subforms))
            (really-for-effect (or (cdr subforms) for-effect)))
        (compile-form subform really-for-effect)
        (when really-for-effect
          (maybe-emit-clear-values subform))))
    (emit 'label `,block-exit)
    (when env-var
      ;; Restore dynamic environment.
      (emit 'aload *thread*)
      (emit 'aload env-var)
      (emit 'putfield +lisp-thread-class+ "dynEnv" +lisp-environment+))))

(defun compile-return-from (form for-effect)
  (let* ((rest (cdr form))
         (block-label (car rest))
         (block-exit (cdr (assoc block-label *blocks*)))
         (result-form (cadr rest)))
    (unless block-exit
      (error "No block named ~S is currently visible." block-label))
    (compile-form result-form)
    (emit 'goto `,block-exit)))

(defun compile-cons (form for-effect)
  (unless (= (length form) 3)
    (error "Wrong number of arguments for CONS."))
  (let ((new-form (rewrite-function-call form)))
    (when (neq new-form form)
      (return-from compile-cons (compile-form new-form))))
  (emit 'new +lisp-cons-class+)
  (emit 'dup)
  (compile-form (second form))
  (unless (remove-store-value)
    (emit-push-value))
  (maybe-emit-clear-values (second form))
  (compile-form (third form))
  (unless (remove-store-value)
    (emit-push-value))
  (maybe-emit-clear-values (third form))
  (emit-invokespecial "org/armedbear/lisp/Cons"
                      "<init>"
                      "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)V"
                      -3)
  (emit-store-value))

(defun compile-progn (form for-effect)
  (cond ((null (cdr form))
         (emit-push-nil)
         (emit-store-value))
        (t
         (do ((forms (cdr form) (cdr forms)))
             ((null forms))
           (compile-form (car forms) (cdr forms))))))

(defun rewrite-setq (form)
  (let ((expr (third form)))
    (if (unsafe-p expr)
        (let ((sym (gensym)))
          (list 'LET (list (list sym expr)) (list 'SETQ (second form) sym)))
        form)))

(defun compile-setq (form for-effect)
  (unless (= (length form) 3)
    (return-from compile-setq (compile-form (precompiler::precompile-setq form)
                                            for-effect)))
  (let* ((rest (cdr form))
         (sym (car rest))
         (index (local-index sym)))
    (when index
      (compile-form (cadr rest))
      (unless (remove-store-value)
        (emit-push-value))
      (maybe-emit-clear-values (cadr rest))
      (cond (for-effect
             (emit 'astore index))
            (t
             (emit 'dup)
             (emit 'astore index)
             (emit-store-value)))
      (return-from compile-setq))
    ;; index is NIL, look in *args* ...
    (setq index (position sym *args*))
    (when index
      (cond (*using-arg-array*
             (emit 'aload 1)
             (emit 'bipush index)
             (compile-form (cadr rest))
             (unless (remove-store-value)
               (emit-push-value))
             (cond (for-effect
                    (emit 'aastore))
                   (t
                    (emit 'dup)
                    (emit-store-value)
                    (emit 'aastore))))
            (t
             (compile-form (cadr rest))
             (unless (remove-store-value)
               (emit-push-value))
             (cond (for-effect
                    (emit 'astore (1+ index)))
                   (t
                    (emit 'dup)
                    (emit 'astore (1+ index))
                    (emit-store-value)))))
      (maybe-emit-clear-values (cadr rest))
      (return-from compile-setq))
    (when (memq sym *closure-vars*)
      (let ((g (declare-object *env*)))
        (emit 'getstatic
              *this-class*
              g
              +lisp-object+))
      ;; FIXME Move this to constructor!
      ;; Cast it to an Environment object.
      (emit 'checkcast +lisp-environment-class+)
      (let ((g (declare-symbol sym)))
        (emit 'getstatic
              *this-class*
              g
              +lisp-symbol+))
      (compile-form (cadr rest))
      (unless (remove-store-value)
        (emit-push-value))
      (maybe-emit-clear-values (cadr rest))
      ;; stack: env sym val
      (emit 'dup_x2)
      (emit-invokevirtual +lisp-environment-class+
                          "rebind"
                          "(Lorg/armedbear/lisp/Symbol;Lorg/armedbear/lisp/LispObject;)V"
                          -3)
      (emit-store-value)
      (return-from compile-setq))
    ;; still not found
    ;; must be a global variable
    (let ((new-form (rewrite-setq form)))
      (when (neq new-form form)
        (return-from compile-setq (compile-form new-form))))
    (let ((g (declare-symbol sym)))
      (emit 'getstatic
            *this-class*
            g
            +lisp-symbol+)
      (compile-form (cadr rest))
      (unless (remove-store-value)
        (emit-push-value))
      (maybe-emit-clear-values (cadr rest))
      (ensure-thread-var-initialized)
      (emit 'aload *thread*)
      (emit-invokestatic +lisp-class+
                         "setSpecialVariable"
                         "(Lorg/armedbear/lisp/Symbol;Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispThread;)Lorg/armedbear/lisp/LispObject;"
                         -1)
      (emit-store-value))))

(defun compile-quote (form for-effect)
   (let ((obj (second form)))
     (cond ((null obj)
            (emit-push-nil)
            (emit-store-value))
           ((symbolp obj)
            (if (symbol-package obj)
                (let ((g (declare-symbol obj)))
                  (emit 'getstatic
                        *this-class*
                        g
                        +lisp-symbol+)
                  (emit-store-value))
                (compile-constant obj)))
           ((listp obj)
            (let ((g (declare-object-as-string obj)))
              (emit 'getstatic
                    *this-class*
                    g
                    +lisp-object+)
              (emit-store-value)))
           ((constantp obj)
            (compile-constant obj))
           (t
            (error "COMPILE-QUOTE: unsupported case: ~S" form)))))

(defun compile-rplacd (form for-effect)
  (let ((args (cdr form)))
    (unless (= (length args) 2)
      (error "wrong number of arguments for RPLACD"))
    (compile-form (first args))
    (unless (remove-store-value)
      (emit-push-value))
    (unless for-effect
      (emit 'dup))
    (compile-form (second args))
    (unless (remove-store-value)
      (emit-push-value))
    (emit-invokevirtual +lisp-object-class+
                        "setCdr"
                        "(Lorg/armedbear/lisp/LispObject;)V"
                        -2)
    (unless for-effect
      (emit-store-value))))

(defun compile-declare (form for-effect)
  ;; Nothing to do.
  )

(defun compile-local-function-def (def)
  (format t "*locals* = ~S~%" *locals*)
  (format t "*args* = ~S~%" *args*)
  (let* ((name (car def))
         (arglist (cadr def))
         (body (cddr def))
         (form (list* 'lambda arglist body))
         (args-to-add (remove-duplicates (union (remove nil (coerce *locals* 'list))
                                                (remove nil (coerce *args* 'list)))))
         function)
    (format t "form = ~S~%" form)
    (format t "args-to-add = ~S~%" args-to-add)
    (format t "new arglist = ~S~%" (append args-to-add arglist))
    (when args-to-add
      (setf arglist (append args-to-add arglist))
      (setf form (list* 'lambda arglist body)))
    (format t "form = ~S~%" form)
    (setf function
          (sys::load-compiled-function (compile-defun name form nil "flet.out")))
    (format t "function = ~S~%" function)
    (push (make-local-function :name name
                               :args-to-add args-to-add
                               :function function)
          *local-functions*)))

(defun compile-local-functions (defs)
  (dolist (def defs)
    (compile-local-function-def def)))

(defun compile-flet (form for-effect)
  (if nil
      (let ((*local-functions* *local-functions*)
            (locals (cadr form))
            (body (cddr form)))
        (compile-local-functions locals)
        (do ((forms body (cdr forms)))
            ((null forms))
          (compile-form (car forms) (cdr forms))))
      (error "COMPILE-FLET: unsupported case.")))

(defun compile-funcall (form for-effect)
;;   (format t "COMPILE-FUNCALL form = ~S~%" form)
  (compile-function-call form for-effect))

(defun compile-function (form for-effect)
   (let ((obj (second form)))
     (cond ((symbolp obj)
            (if (or (sys::built-in-function-p obj) (memq obj *toplevel-defuns*))
                (let ((g (declare-function obj)))
                  (emit 'getstatic
                        *this-class*
                        g
                        +lisp-object+)
                  (emit-store-value))
                (let ((g (declare-symbol obj)))
                  (emit 'getstatic
                        *this-class*
                        g
                        +lisp-symbol+)
                  (emit-invokevirtual +lisp-object-class+
                                      "getSymbolFunctionOrDie"
                                      "()Lorg/armedbear/lisp/LispObject;"
                                      0)
                  (emit-store-value))))
           ((and (consp obj) (eq (car obj) 'LAMBDA))
            (let ((closure-vars (remove-duplicates (union (remove nil (coerce *locals* 'list))
                                                          (remove nil (coerce *args* 'list)))))
                  (lambda-body (cddr obj)))
              (cond (closure-vars
                     (error "COMPILE-FUNCTION: unable to compile LAMBDA form defined in non-null lexical environment."))
                    ((contains-return lambda-body)
                     (error "COMPILE-FUNCTION: unable to compile LAMBDA form containing RETURN or RETURN-FROM."))
                    (t
                     (fresh-line)
                     (format t "~A Processing LAMBDA form~%" (load-verbose-prefix))
                     (let ((g (if *compile-file-truename*
                                  (declare-lambda obj)
                                  (declare-object (sys::coerce-to-function obj)))))
                       (emit 'getstatic
                             *this-class*
                             g
                             +lisp-object+)
                       (emit-store-value))))))
;;             ;; FIXME We need to construct a proper lexical environment here
;;             ;; and pass it to coerceToFunction().
;;             (let ((g (declare-object-as-string obj)))
;;               (emit 'getstatic
;;                     *this-class*
;;                     g
;;                     +lisp-object+)
;;               (emit-invokestatic +lisp-class+
;;                                  "coerceToFunction"
;;                                  "(Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/Function;"
;;                                  0)
;;               (emit-store-value)))
           (t
            (error "COMPILE-FUNCTION: unknown case: ~S" form)))))

(defun compile-plus (form for-effect)
  (let ((new-form (rewrite-function-call form)))
    (when (neq new-form form)
      (return-from compile-plus (compile-form new-form))))
  (let* ((args (cdr form))
         (len (length args)))
    (case len
      (2
       (let ((first (first args))
             (second (second args)))
         (cond
          ((eql first 1)
           (compile-form second)
           (emit-invoke-method "incr"))
          ((eql second 1)
           (compile-form first)
           (emit-invoke-method "incr"))
          (t
           (compile-binary-operation "add" args)))))
      (t
       (compile-function-call form for-effect)))))

(defun compile-minus (form for-effect)
  (let ((new-form (rewrite-function-call form)))
    (when (neq new-form form)
      (return-from compile-minus (compile-form new-form))))
  (let* ((args (cdr form))
         (len (length args)))
    (case len
      (2
       (let ((first (first args))
             (second (second args)))
         (cond
          ((eql second 1)
           (compile-form first)
           (emit-invoke-method "decr"))
          (t
           (compile-binary-operation "subtract" args)))))
      (t
       (compile-function-call form for-effect)))))

(defun compile-not/null (form for-effect)
  (unless (= (length form) 2)
    (error 'simple-program-error
           :format-control "Wrong number of arguments for ~S."
           :format-arguments (list (car form))))
  (let ((arg (second form)))
    (cond ((null arg)
           (emit-push-t))
          ((constantp arg)
           (emit-push-nil))
          ((and (consp arg)
                (memq (car arg) '(NOT NULL)))
           (compile-form (second arg))
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values (second arg))
           (emit-push-nil)
           (let ((label1 (gensym))
                 (label2 (gensym)))
             (emit 'if_acmpeq `,label1)
             (emit-push-t)
             (emit 'goto `,label2)
             (emit 'label `,label1)
             (emit-push-nil)
             (emit 'label `,label2)))
          (t
           (compile-form arg)
           (unless (remove-store-value)
             (emit-push-value))
           (maybe-emit-clear-values arg)
           (emit-push-nil)
           (let ((label1 (gensym))
                 (label2 (gensym)))
             (emit 'if_acmpeq `,label1)
             (emit-push-nil)
             (emit 'goto `,label2)
             (emit 'label `,label1)
             (emit-push-t)
             (emit 'label `,label2)))))
  (emit-store-value))

(defun compile-values (form for-effect)
  (let ((args (cdr form)))
    (cond ((= (length args) 2)
           (ensure-thread-var-initialized)
           (emit 'aload *thread*)
           (cond ((and (eq (car args) t)
                       (eq (cadr args) t))
                  (emit-push-t)
                  (emit 'dup))
                 ((and (eq (car args) nil)
                       (eq (cadr args) nil))
                  (emit-push-nil)
                  (emit 'dup))
                 (t
                  (compile-form (car args))
                  (unless (remove-store-value)
                    (emit-push-value))
                  (compile-form (cadr args))
                  (unless (remove-store-value)
                    (emit-push-value))))
           (emit-invokevirtual +lisp-thread-class+
                               "setValues"
                               "(Lorg/armedbear/lisp/LispObject;Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
                               -2)
           (emit-store-value))
          (t
           (compile-function-call form for-effect)))))

(defun compile-variable-ref (var)
  (let ((v (find-variable var)))
    (unless (and v (variable-special-p v))
      (let ((index (local-index var)))
        (when index
          (emit 'aload index)
          (emit-store-value)
          (return-from compile-variable-ref)))
      ;; Not found in locals; look in args.
      (let ((index (position var *args*)))
        (when index
          (cond (*using-arg-array*
                 (emit 'aload 1)
                 (emit 'bipush index)
                 (emit 'aaload)
                 (emit-store-value)
                 (return-from compile-variable-ref))
                (t
                 (emit 'aload (1+ index))
                 (emit-store-value)
                 (return-from compile-variable-ref)))))))
  (when (memq var *closure-vars*)
;;     (let ((g (declare-object *env*)))
;;       (emit 'getstatic
;;             *this-class*
;;             g
;;             +lisp-object+))
;;     ;; FIXME Move this to constructor!
;;     ;; Cast it to an Environment object.
;;     (emit 'checkcast +lisp-environment-class+)
;;     (let ((g (declare-symbol var)))
;;       (emit 'getstatic
;;             *this-class*
;;             g
;;             +lisp-symbol+))
;;     (emit-invokevirtual +lisp-environment-class+
;;                         "lookup"
;;                         "(Lorg/armedbear/lisp/LispObject;)Lorg/armedbear/lisp/LispObject;"
;;                         -1)
;;     (emit-store-value)
;;     (return-from compile-variable-ref))
    (error "COMPILE-VARIABLE-REF: unsupported case."))
  ;; Otherwise it must be a global variable.
  (unless (special-variable-p var)
    ;; FIXME This should be a warning!
    (%format t "~A Note: undefined variable ~S~%" (load-verbose-prefix) var))
  (let ((g (declare-symbol var)))
    (emit 'getstatic
          *this-class*
          g
          +lisp-symbol+)
    (emit-invokevirtual +lisp-symbol-class+
                        "symbolValue"
                        "()Lorg/armedbear/lisp/LispObject;"
                        0)
    (emit-store-value)
    (return-from compile-variable-ref)))

(defun compile-form (form &optional for-effect)
  (cond ((consp form)
         (let ((op (car form))
               handler)
           (cond ((symbolp op)
                  (cond ((setf handler (get op 'jvm-compile-handler))
                         (funcall handler form for-effect))
                        ((macro-function op)
                         (compile-form (macroexpand form)))
                        ((special-operator-p op)
                         (error "COMPILE-FORM: unsupported special operator ~S." op))
                        (t
                         (compile-function-call form for-effect))))
                 ((and (consp op) (eq (car op) 'LAMBDA))
                  (let ((new-form (list* 'FUNCALL form)))
                    (compile-form new-form)))
                 (t
                  (error "COMPILE-FORM unhandled case ~S" form)))))
        ((null form)
         (unless for-effect
           (emit-push-nil)
           (emit-store-value)))
        ((eq form t)
         (unless for-effect
           (emit-push-t)
           (emit-store-value)))
        ((keywordp form)
         (let ((g (declare-keyword form)))
           (emit 'getstatic
                 *this-class*
                 g
                 +lisp-symbol+)
           (emit-store-value)))
        ((symbolp form)
         (compile-variable-ref form))
        ((constantp form)
         (unless for-effect
           (compile-constant form)))
        (t
         (error "COMPILE-FORM unhandled case ~S" form))))

;; Returns descriptor.
(defun analyze-args (args)
  (assert (not (memq '&AUX args)))
  (when (or (memq '&KEY args)
            (memq '&OPTIONAL args)
            (memq '&REST args))
    (setq *using-arg-array* t)
    (setq *hairy-arglist-p* t)
    (return-from analyze-args #.(format nil "([~A)~A" +lisp-object+ +lisp-object+)))
  (case (length args)
    (0 #.(format nil "()~A" +lisp-object+))
    (1 #.(format nil "(~A)~A" +lisp-object+ +lisp-object+))
    (2 #.(format nil "(~A~A)~A" +lisp-object+ +lisp-object+ +lisp-object+))
    (3 #.(format nil "(~A~A~A)~A" +lisp-object+ +lisp-object+ +lisp-object+ +lisp-object+))
    (4 #.(format nil "(~A~A~A~A)~A" +lisp-object+ +lisp-object+ +lisp-object+ +lisp-object+ +lisp-object+))
    (t (setq *using-arg-array* t)
       #.(format nil "([~A)~A" +lisp-object+ +lisp-object+))))

(defun compile-defun (name form environment &optional (classfile "out.class"))
  (unless (eq (car form) 'LAMBDA)
    (return-from compile-defun nil))
  (setf form (precompile-form form t))
  (let* ((*defun-name* name)
         (*declared-symbols* (make-hash-table))
         (*declared-functions* (make-hash-table))
         (*declared-strings* (make-hash-table :test 'eq))
         (class-name
          (let* ((pathname (pathname classfile))
                 (name (pathname-name classfile)))
            (dotimes (i (length name))
              (when (eql (char name i) #\-)
                (setf (char name i) #\_)))
            name))
         (*this-class*
          (concatenate 'string "org/armedbear/lisp/" class-name))
         (args (cadr form))
         (body (cddr form))
         (*using-arg-array* nil)
         (*hairy-arglist-p* nil)
         (descriptor (analyze-args args))
         (execute-method (make-method :name "execute"
                                      :descriptor descriptor))
         (*code* ())
         (*static-code* ())
         (*fields* ())
         (*blocks* ())
         (*tags* (make-array 256 :fill-pointer 0)) ; FIXME Remove hard limit!
         (*args* (make-array 256 :fill-pointer 0)) ; FIXME Remove hard limit!
         (*locals* ())
         (*max-locals* 0)
         (*env* environment)
         (*closure-vars* (if environment (sys::environment-vars environment) nil))
         (*variables* ())
         (*pool* ())
         (*pool-count* 1)
         (*val* nil)
         (*thread* nil)
         (*thread-var-initialized* nil))
    (setf (method-name-index execute-method)
          (pool-name (method-name execute-method)))
    (setf (method-descriptor-index execute-method)
          (pool-name (method-descriptor execute-method)))
    (if *hairy-arglist-p*
        (let* ((fun (sys::make-compiled-function nil args body))
               (vars (sys::varlist fun)))
          (dolist (var vars)
            (vector-push var *args*)))
        (dolist (arg args)
          (vector-push arg *args*)))
    (allocate-local nil) ;; "this" pointer
    (if *using-arg-array*
        (allocate-local nil) ;; One slot for arg array.
        (dolist (arg args) ;; One slot for each argument.
          (allocate-local nil)))
    ;; Reserve the next available slot for the value register.
    (setf *val* (allocate-local nil))
    ;; Reserve the next available slot for the thread register.
    (setf *thread* (allocate-local nil))
    (when *hairy-arglist-p*
      (emit 'aload_0)
      (emit 'aload_1)
      (emit-invokevirtual *this-class*
                          "processArgs"
                          "([Lorg/armedbear/lisp/LispObject;)[Lorg/armedbear/lisp/LispObject;"
                          -1)
      (emit 'astore_1))
    (dolist (f body)
      (compile-form f))
    (unless (remove-store-value)
      (emit-push-value)) ; leave result on stack
    (emit 'areturn)
    (finalize-code)
    (optimize-code)
    (setf (method-max-stack execute-method) (analyze-stack))
    (setf (method-code execute-method) (code-bytes *code*))
    (setf (method-max-locals execute-method) *max-locals*)

    (let* ((super
            (if *hairy-arglist-p*
                "org.armedbear.lisp.CompiledFunction"
                (case (length args)
                  (0 "org.armedbear.lisp.Primitive0")
                  (1 "org.armedbear.lisp.Primitive1")
                  (2 "org.armedbear.lisp.Primitive2")
                  (3 "org.armedbear.lisp.Primitive3")
                  (4 "org.armedbear.lisp.Primitive4")
                  (t "org.armedbear.lisp.Primitive"))))
           (this-index (pool-class *this-class*))
           (super-index (pool-class super))
           (constructor (make-constructor super *defun-name* args body)))
      (pool-name "Code") ; Must be in pool!

      ;; Write class file.
      (with-open-file (*stream* classfile
                                :direction :output
                                :element-type '(unsigned-byte 8)
                                :if-exists :supersede)
        (write-u4 #xCAFEBABE)
        (write-u2 3)
        (write-u2 45)
        (write-constant-pool)
        ;; access flags
        (write-u2 #x21)
        (write-u2 this-index)
        (write-u2 super-index)
        ;; interfaces count
        (write-u2 0)
        ;; fields count
        (write-u2 (length *fields*))
        ;; fields
        (dolist (field *fields*)
          (write-field field))
        ;; methods count
        (write-u2 2)
        ;; methods
        (write-method execute-method)
        (write-method constructor)
        ;; attributes count
        (write-u2 0))))
  classfile)

(defun get-lambda-to-compile (definition-designator)
  (if (and (consp definition-designator)
           (eq (car definition-designator) 'LAMBDA))
      definition-designator
      (multiple-value-bind (lambda-expression environment)
        (function-lambda-expression definition-designator)
	(unless lambda-expression
	  (error :format-control "Can't find a definition for ~S."
                 :format-arguments (list definition-designator)))
        (values lambda-expression environment))))

(defun load-verbose-prefix ()
  (let ((s (make-array (max sys::*load-depth* 1)
                       :element-type 'character
                       :initial-element #\space)))
    (setf (char s 0) #\;)
    s))

(setq *compile-print* t)

(defvar *catch-errors* t)

(defun %jvm-compile (name definition)
  (let ((prefix (load-verbose-prefix)))
    (when *compile-print*
      (if name
          (progn
            (%format t "~A Compiling ~S ...~%" prefix name)
            (when (and (fboundp name)
                       (sys::%typep (fdefinition name) 'generic-function))
              (%format t "~A Unable to compile generic function ~S~%" prefix name)
              (return-from %jvm-compile (values name nil t)))
            (unless (symbolp name)
              (%format t "~A Unable to compile ~S~%" prefix name)
              (return-from %jvm-compile (values name nil t))))
          (%format t "~A Compiling top-level form ...~%" prefix)))
    (unless definition
      (resolve name)
      (setf definition (fdefinition name))
      (when (compiled-function-p definition)
        (when (and *compile-print* name)
          (%format t "~A Already compiled ~S~%" prefix name))
        (return-from %jvm-compile (values name nil nil))))
    (multiple-value-bind (expr env) (get-lambda-to-compile definition)
      (let* ((*package* (if (and name (symbol-package name))
                            (symbol-package name)
                            *package*))
             (classfile (compile-defun name expr env))
             (compiled-definition (sys::load-compiled-function classfile)))
        (when (and name (functionp compiled-definition))
          (sys::%set-lambda-name compiled-definition name)
          (sys::%set-call-count compiled-definition (sys::%call-count definition))
          (sys::%set-arglist compiled-definition (sys::arglist definition))
          (if (macro-function name)
              (setf (fdefinition name) (sys::make-macro compiled-definition))
              (setf (fdefinition name) compiled-definition)))
        (when *compile-print*
          (if name
              (%format t "~A Compiled ~S~%" prefix name)
              (%format t "~A Compiled top-level form~%" prefix)))
        (values (or name compiled-definition) nil nil)))))

(defun jvm-compile (name &optional definition)
  (if *catch-errors*
      (let ((prefix (load-verbose-prefix)))
        (handler-case
            (%jvm-compile name definition)
          (error (c)
                 (fresh-line)
                 (%format t "~A Note: ~A~%" prefix c)
                 (if name
                     (%format t "~A Unable to compile ~S.~%" prefix name)
                     (%format t "~A Unable to compile top-level form.~%" prefix))
                 (precompiler::precompile name definition))))
      (%jvm-compile name definition)))

(defun jvm-compile-package (package-designator)
  (let ((pkg (if (packagep package-designator)
                 package-designator
                 (find-package package-designator))))
      (dolist (sym (sys::package-symbols pkg))
        (when (fboundp sym)
          (unless (or (special-operator-p sym) (macro-function sym))
            ;; Force autoload to be resolved.
            (resolve sym)
            (let ((f (fdefinition sym)))
              (unless (compiled-function-p f)
                (jvm-compile sym)))))))
  t)

(defun install-handler (fun &optional handler)
  (let ((handler (or handler
                     (find-symbol (concatenate 'string "COMPILE-" (symbol-name fun)) 'jvm))))
    (unless (and handler (fboundp handler))
      (error "Handler not found: ~S" handler))
    (setf (get fun 'jvm-compile-handler) handler)))

(mapc #'install-handler '(atom
                          block
                          cons
                          declare
                          flet
                          funcall
                          function
                          go
                          if
                          locally
                          multiple-value-bind
                          multiple-value-list
                          progn
                          quote
                          return-from
                          rplacd
                          setq
                          tagbody
                          values))

(install-handler 'let  'compile-let/let*)
(install-handler 'let* 'compile-let/let*)
(install-handler '+    'compile-plus)
(install-handler '-    'compile-minus)
(install-handler 'not  'compile-not/null)
(install-handler 'null 'compile-not/null)

(defun process-optimization-declarations (forms)
  (let (alist ())
    (dolist (form forms)
      (unless (and (consp form) (eq (car form) 'declare))
        (return))
      (let ((decl (cadr form)))
        (when (eq (car decl) 'optimize)
          (dolist (spec (cdr decl))
            (let ((val 3)
                  (quantity spec))
              (if (consp spec)
                  (setq quantity (car spec) val (cadr spec)))
              (if (and (fixnump val) (<= 0 val 3) (memq quantity '(debug speed space safety compilation-speed)))
                  (push (cons quantity val) alist)))))))
    alist))

(defun compile (name &optional definition)
  (jvm-compile name definition))

(eval-when (:execute)
  (let ((*auto-compile* nil))
    (mapc #'jvm-compile '(pool-add
                          pool-find-entry
                          pool-name
                          pool-get
                          compile-form))))

(provide 'jvm)
