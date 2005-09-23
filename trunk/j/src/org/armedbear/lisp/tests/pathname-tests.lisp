;;; pathname-tests.lisp
;;;
;;; This software is in the public domain and is provided with absolutely no
;;; warranty.

;;; Define a reasonable value for this in ~/.abclrc.
(defvar *ansi-tests-translations*)

(unless (member "RT" *modules* :test #'string=)
  (unless (boundp '*ansi-tests-translations*)
    (setf *ansi-tests-translations*
          '(("*.*.*" #-windows "/home/peter/gcl/ansi-tests/*.*"
                     #+windows "c:/cygwin/home/peter/gcl/ansi-tests/*.*")))
    (warn "~S is not defined; using ~S"
          '*ansi-tests-translations*
          *ansi-tests-translations*))
  (setf (logical-pathname-translations "ansi-tests") *ansi-tests-translations*)
  (load "ansi-tests:rt-package.lsp")
  (load #+abcl (compile-file-if-needed "ansi-tests:rt.lsp")
        ;; Force compilation to avoid fasl name conflict between SBCL and
        ;; Allegro.
        #-abcl (compile-file "ansi-tests:rt.lsp"))
  (let ((*package* (find-package '#:rt)))
    (export (find-symbol (string '#:*expected-failures*))))
  (provide "RT"))

(rt:rem-all-tests)
(setf rt:*expected-failures* nil)

(defpackage #:pathname-tests (:use #:cl #:regression-test))

(in-package #:pathname-tests)

(defmacro expect (test-form)
  `(unless (ignore-errors ,test-form)
     (format t "Expected ~S~%" ',test-form)))

(defmacro signals-error (form error-name)
  `(locally (declare (optimize safety))
     (handler-case ,form
     (error (c) (typep c ,error-name))
     (:no-error (&rest ignored) (declare (ignore ignored)) nil))))

(defun check-physical-pathname (pathname expected-directory expected-name expected-type)
  (let* ((directory (pathname-directory pathname))
         (name (pathname-name pathname))
         (type (pathname-type pathname))
         (ok t))
    (unless (and (pathnamep pathname)
                 (not (typep pathname 'logical-pathname)))
      (format t "~&~S => ~S; expected ~S~%" pathname (type-of pathname) 'pathname)
      (setf ok nil))
    (unless (and (equal directory expected-directory)
                 (equal name      expected-name)
                 (equal type      expected-type))
      (format t "~&~S => ~S ~S ~S; expected ~S ~S ~S~%"
              pathname directory name type expected-directory expected-name expected-type)
      (setf ok nil))
    ok))

(defun check-logical-pathname (pathname expected-host expected-directory
                                        expected-name expected-type
                                        expected-version)
  (let* ((host (pathname-host pathname))
         (directory (pathname-directory pathname))
         (name (pathname-name pathname))
         (type (pathname-type pathname))
         (version (pathname-version pathname))
         ;; Allegro's logical pathnames don't canonicalize their string
         ;; components to upper case.
         (test #-allegro 'equal
               #+allegro 'equalp)
         (ok t))
    (unless (typep pathname 'logical-pathname)
      (format t "~&~S => ~S; expected ~S~%" pathname (type-of pathname) 'logical-pathname)
      (setf ok nil))
    ;; "The device component of a logical pathname is always :UNSPECIFIC..." 19.3.2.1
    #-allegro ;; Except on Allegro, where it's NIL.
    (unless (eq (pathname-device pathname) :unspecific)
      (format t "~S => device is ~S, not ~S~%"
              pathname (pathname-device pathname) :unspecific)
      (setf ok nil))
    (unless (and (or (not (stringp host))
                     (funcall test host expected-host))
                 (funcall test directory expected-directory)
                 (funcall test name expected-name)
                 (funcall test type expected-type)
                 (eql version expected-version))
      (format t "~&~S => ~S ~S ~S ~S ~S; expected ~S ~S ~S ~S ~S~%"
              pathname
              host directory name type version
              expected-host expected-directory expected-name expected-type
              expected-version)
      (setf ok nil))
    ok))

(defun check-translate-pathname (args expected)
  (declare (optimize safety))
  (declare (type list args))
  (declare (type string expected))
  (let ((result (namestring (apply 'translate-pathname args))))
    (unless (equal result
                   #-windows expected
                   #+windows (substitute #\\ #\/ expected))
      (format t "(translate-pathname ~S ~S ~S) => ~S; expected ~S~%"
              (first args) (second args) (third args) result expected))))

(defmacro check-readable (pathname)
  `(equal ,pathname (read-from-string (write-to-string ,pathname :readably t))))

(defmacro check-namestring (pathname namestring)
  `(string= (namestring ,pathname)
            #+windows (substitute #\\ #\/ ,namestring)
            #-windows ,namestring))

(deftest physical.1
  (check-physical-pathname #p"/" '(:absolute) nil nil)
  t)
(deftest physical.2
  (check-physical-pathname #p"/foo" '(:absolute) "foo" nil)
  t)
(deftest physical.3
  (check-physical-pathname #p"/foo." '(:absolute) "foo" "")
  t)
(deftest physical.4
  (check-physical-pathname #p"/foo.b" '(:absolute) "foo" "b")
  t)
(deftest physical.5
  (check-physical-pathname #p"/foo.bar." '(:absolute) "foo.bar" "")
  t)
(deftest physical.6
  (check-physical-pathname #p"/foo.bar.baz" '(:absolute) "foo.bar" "baz")
  t)
(deftest physical.7
  (check-physical-pathname #p"/foo/bar" '(:absolute "foo") "bar" nil)
  t)
(deftest physical.8
  (check-physical-pathname #p"/foo..bar" '(:absolute) "foo." "bar")
  t)
(deftest physical.9
  (check-physical-pathname #p"foo.bar" nil "foo" "bar")
  t)
(deftest physical.10
  (check-physical-pathname #p"foo.bar.baz" nil "foo.bar" "baz")
  t)
(deftest physical.11
  (check-physical-pathname #p"foo/" '(:relative "foo") nil nil)
  t)
(deftest physical.12
  (check-physical-pathname #p"foo/bar" '(:relative "foo") "bar" nil)
  t)
(deftest physical.13
  (check-physical-pathname #p"foo/bar/baz" '(:relative "foo" "bar") "baz" nil)
  t)
(deftest physical.14
  (check-physical-pathname #p"foo/bar/" '(:relative "foo" "bar") nil nil)
  t)
#+allegro
(deftest physical.15
  ;; This reduction is wrong.
  (check-physical-pathname #p"foo/bar/.." '(:relative "foo") nil nil)
  t)
#+allegro
(deftest physical.16
  (check-physical-pathname #p"/foo/../" '(:absolute) nil nil)
  t)
(deftest physical.17
  (check-physical-pathname #p".lisprc" nil ".lisprc" nil)
  t)
(deftest physical.18
  (check-physical-pathname #p"x.lisprc" nil "x" "lisprc")
  t)

(deftest physical.19
  #-allegro
  (check-physical-pathname (make-pathname :name ".") nil "." nil)
  #+allegro
  (check-physical-pathname (make-pathname :name ".") '(:relative) nil nil)
  t)

(deftest physical.20
  (check-readable (make-pathname :name "."))
  t)

;; #p"."
(deftest physical.21
  #+(or allegro abcl cmu)
  (check-physical-pathname #p"." '(:relative) nil nil)
  #+(or sbcl clisp)
  ;; No trailing separator character means it's a file.
  (check-physical-pathname #p"." nil "." nil)
  t)
#+cmu
(pushnew 'physical.21 rt:*expected-failures*)

;; #p"./"
;; Trailing separator character means it's a directory.
(deftest physical.22
  #+(or allegro abcl clisp cmu)
  (check-physical-pathname #p"./" '(:relative) nil nil)
  #+(or sbcl)
  ;; Is this more exact?
  (check-physical-pathname #p"./" '(:relative ".") nil nil)
  t)
#+cmu
(pushnew 'physical.22 rt:*expected-failures*)

(deftest physical.23
  #-allegro
  (check-physical-pathname (make-pathname :name "..") nil ".." nil)
  #+allegro
  (check-physical-pathname (make-pathname :name "..") '(:relative :back) nil nil)
  t)

;; #p".."
(deftest physical.24
  #+(or allegro)
  (check-physical-pathname #p".." '(:relative :back) nil nil)
  #+(or abcl cmu)
  (check-physical-pathname #p".." '(:relative :up) nil nil)
  ;; Other implementations think it's a file.
  #+(or)
  ;; If it's a file, to a human its name would be "..". No implementation gets
  ;; this right.
  (check-physical-pathname #p".." nil ".." nil)
  #+(or sbcl clisp)
  ;; These implementations parse ".." as the name "." followed by another dot and
  ;; the type string "", which no human would do.
  (check-physical-pathname #p".." nil "." "")
  t)
#+cmu
(pushnew 'physical.24 rt:*expected-failures*)

;; #p"../"
(deftest physical.25
  #+allegro
  (check-physical-pathname #p"../" '(:relative :back) nil nil)
  #+(or abcl sbcl cmu clisp)
  (check-physical-pathname #p"../" '(:relative :up) nil nil)
  t)

;; Lots of dots.
#+(or allegro abcl cmu)
(deftest lots-of-dots.1
  (check-physical-pathname #p"..." nil "..." nil)
  t)
#+cmu
(pushnew 'lots-of-dots.1 rt:*expected-failures*)
#+(or allegro abcl cmu)
(deftest lots-of-dots.2
  (check-physical-pathname #p"......" nil "......" nil)
  t)
#+cmu
(pushnew 'lots-of-dots.2 rt:*expected-failures*)

(deftest physical.26
  (check-physical-pathname #p"foo.*" nil "foo" :wild)
  t)

#-sbcl
(deftest physical.27
  #-allegro
  (string= (namestring (make-pathname :name "..")) "..")
  #+allegro
  (string= (namestring (make-pathname :name "..")) "../")
  t)

(deftest physical.28
  (string= (namestring (make-pathname :directory '(:relative :up)))
           #+windows "..\\"
           #-windows "../")
  t)

;; Silly names.
#-(or allegro sbcl)
(deftest silly.1
  #+(or abcl clisp)
  (signals-error (make-pathname :name "abc/def") 'error)
  #-(or abcl clisp)
  (check-readable (make-pathname :name "abc/def"))
  t)
#+cmu
(pushnew 'silly.1 rt:*expected-failures*)

;; If the prefix isn't a defined logical host, it's not a logical pathname.
#-cmu
;; CMUCL parses this as (:ABSOLUTE #<SEARCH-LIST foo>) "bar.baz" "42".
(deftest logical.1
  #+allegro
  ;; Except in Allegro.
  (check-logical-pathname #p"foo:bar.baz.42" "foo" nil "bar" "baz" nil)
  #-allegro
  (check-physical-pathname #p"foo:bar.baz.42" nil "foo:bar.baz" "42")
  t)

;; Define a logical host.
(setf (logical-pathname-translations "effluvia")
      '(("**;*.*.*" "/usr/local/**/*.*")))

;; LOGICAL-PATHNAME-TRANSLATIONS
#-allegro
(deftest logical-pathname-translations.1
  #+(or sbcl cmu)
  (equal (logical-pathname-translations "effluvia")
                 '(("**;*.*.*" "/usr/local/**/*.*")))
  #+clisp
  (equal (logical-pathname-translations "effluvia")
         '((#p"EFFLUVIA:**;*.*.*" "/usr/local/**/*.*")))
  #+abcl
  (equal (logical-pathname-translations "effluvia")
         '((#p"EFFLUVIA:**;*.*.*" #p"/usr/local/**/*.*")))
  t)

#+sbcl
(deftest physical.29
  ;; Even though "effluvia" is defined as a logical host, "bop" is not a valid
  ;; logical pathname version, so this can't be a logical pathname.
  (check-physical-pathname #p"effluvia:bar.baz.bop" nil "effluvia:bar.baz" "bop")
  t)

;; Parse error.
(deftest logical-pathname.1
  (signals-error (logical-pathname "effluvia::foo.bar")
                 #-(or allegro clisp) 'parse-error
                 #+(or allegro clisp) 'type-error)
  t)

#-allegro
(progn
  (check-logical-pathname #p"effluvia:bar.baz.42" "EFFLUVIA" '(:absolute) "BAR" "BAZ" 42)
  (expect (string= (write-to-string #p"effluvia:bar.baz.42" :escape t)
                   "#P\"EFFLUVIA:BAR.BAZ.42\"")))
#+allegro
;; Allegro returns NIL for the device and directory and drops the version
;; entirely (even from the namestring).
(progn
  (check-logical-pathname #p"effluvia:bar.baz.42" "effluvia" nil "bar" "baz" nil)
  (expect (string= (write-to-string #p"effluvia:bar.baz" :escape t)
                   "#p\"effluvia:bar.baz\"")))

;; (setf *pathname* (parse-namestring "**;*.*.*" "effluvia"))
(expect (typep (parse-namestring "**;*.*.*" "effluvia") 'logical-pathname))
#-allegro
(expect (string= (namestring (parse-namestring "**;*.*.*" "effluvia")) "EFFLUVIA:**;*.*.*"))
#+allegro
;; Allegro preserves case and drops the version component.
(expect (string= (namestring (parse-namestring "**;*.*.*" "effluvia")) "effluvia:**;*.*"))

;; The version can be a bignum.
;; (setf *pathname* (pathname "effluvia:bar.baz.2147483648"))
#-allegro
(check-logical-pathname #p"effluvia:bar.baz.2147483648" "EFFLUVIA" '(:absolute) "BAR" "BAZ" 2147483648)
;;(expect (= (pathname-version #p"effluvia:bar.baz.2147483648") 2147483648))
#-(or sbcl allegro)
;; SBCL has a bug when the version is a bignum.
(expect (string= (namestring #p"effluvia:bar.baz.2147483648")
                 "EFFLUVIA:BAR.BAZ.2147483648"))

#-allegro
(check-logical-pathname #p"effluvia:foo.*" "EFFLUVIA" '(:absolute) "FOO" :wild nil)
#+allegro
(check-logical-pathname #p"effluvia:foo.*" "effluvia" nil "foo" :wild nil)

#-allegro
(check-logical-pathname #p"effluvia:*.lisp" "EFFLUVIA" '(:absolute) :wild "LISP" nil)
#+allegro
(check-logical-pathname #p"effluvia:*.lisp" "effluvia" nil :wild "lisp" nil)

#-allegro
(check-logical-pathname #p"effluvia:bar.baz.newest" "EFFLUVIA" '(:absolute) "BAR" "BAZ" :newest)
#+allegro
(check-logical-pathname #p"effluvia:bar.baz.newest" "effluvia" nil "bar" "baz" nil)

#-allegro
(check-logical-pathname #p"EFFLUVIA:BAR.BAZ.NEWEST" "EFFLUVIA" '(:absolute) "BAR" "BAZ" :newest)
#+allegro
(check-logical-pathname #p"EFFLUVIA:BAR.BAZ.NEWEST" "EFFLUVIA" nil "BAR" "BAZ" nil)

;; The directory component.
(check-logical-pathname #p"effluvia:foo;bar.baz" "EFFLUVIA" '(:absolute "FOO") "BAR" "BAZ" nil)
#-allegro
(expect (string= (namestring #p"effluvia:foo;bar.baz") "EFFLUVIA:FOO;BAR.BAZ"))
#+allegro
(expect (string= (namestring #p"effluvia:foo;bar.baz") "effluvia:foo;bar.baz"))

#-allegro
(progn
  (check-logical-pathname #p"effluvia:;bar.baz" "EFFLUVIA" '(:relative) "BAR" "BAZ" nil)
  (expect (string= (namestring #p"effluvia:;bar.baz") "EFFLUVIA:;BAR.BAZ")))
#+allegro
;; Allegro drops the directory component and removes the semicolon from the
;; namestring.
(progn
  (check-logical-pathname #p"effluvia:;bar.baz" "EFFLUVIA" nil "BAR" "BAZ" nil)
  (expect (string= (namestring #p"effluvia:;bar.baz") "effluvia:bar.baz")))

;; "If a relative-directory-marker precedes the directories, the directory
;; component parsed is as relative; otherwise, the directory component is
;; parsed as absolute."
#-allegro
(expect (equal (pathname-directory #p"effluvia:foo.baz") '(:absolute)))
#+allegro
(expect (equal (pathname-directory #p"effluvia:foo.baz") nil))

(expect (typep  #p"effluvia:" 'logical-pathname))
#-allegro
(expect (equal (pathname-directory #p"effluvia:") '(:absolute)))
#+allegro
(expect (equal (pathname-directory #p"effluvia:") nil))

;; PARSE-NAMESTRING
(expect (typep (parse-namestring "foo.bar" "effluvia") 'logical-pathname))
#-allegro
(expect (string= (namestring (parse-namestring "foo.bar" "effluvia")) "EFFLUVIA:FOO.BAR"))
#+allegro
(expect (string= (namestring (parse-namestring "foo.bar" "effluvia")) "effluvia:foo.bar"))

;; WILD-PATHNAME-P
(expect (wild-pathname-p #p"effluvia:;*.baz"))

;; PATHNAME-MATCH-P
(expect (pathname-match-p "/foo/bar/baz" "/*/*/baz"))
(expect (pathname-match-p "/foo/bar/baz" "/**/baz"))
(expect (pathname-match-p "/foo/bar/quux/baz" "/**/baz"))
(expect (pathname-match-p "foo.bar" "/**/*.*"))
(expect (pathname-match-p "/usr/local/bin/foo.bar" "/**/foo.bar"))
(expect (not (pathname-match-p "/usr/local/bin/foo.bar" "**/foo.bar")))
(expect (pathname-match-p "/foo/bar.txt" "/**/*.*"))
(expect (not (pathname-match-p "/foo/bar.txt" "**/*.*")))
(expect (pathname-match-p #p"effluvia:foo.bar" #p"effluvia:**;*.*.*"))

;; TRANSLATE-PATHNAME
#-clisp
(deftest translate-pathname.1
  (equal (translate-pathname "foo" "*" "bar") #p"bar")
  t)
(deftest translate-pathname.2
  (equal (translate-pathname "foo" "*" "*")   #p"foo")
  t)

#-abcl
;; ABCL doesn't implement this translation.
(deftest translate-pathname.3
  (string= (pathname-name (translate-pathname "foobar" "*" "foo*")) "foofoobar")
  t)

(deftest translate-pathname.4
  (string= (namestring (translate-pathname "test.txt" "*.txt" "*.text"))
           "test.text")
  t)

(deftest translate-pathname.5
  (check-namestring (translate-pathname "foo/bar" "*/bar" "*/baz") "foo/baz")
  t)
(expect (equal (translate-pathname "foo/bar" "*/bar" "*/baz") #p"foo/baz"))
(expect (string= (namestring (translate-pathname "foo.bar" "*.*" "/usr/local/*.*"))
                 #-windows "/usr/local/foo.bar"
                 #+windows "\\usr\\local\\foo.bar"))
(expect (equal (translate-pathname "foo.bar" "*.*" "/usr/local/*.*")
               #p"/usr/local/foo.bar"))

(check-translate-pathname '("/foo/" "/*/" "/usr/local/*/") "/usr/local/foo/")
(check-translate-pathname '("/foo/baz/bar.txt" "/**/*.*" "/usr/local/**/*.*")
                          "/usr/local/foo/baz/bar.txt")

(expect (equal (translate-pathname "/foo/" "/*/" "/usr/local/*/bar/") #p"/usr/local/foo/bar/"))

(expect (equal (translate-pathname "/foo/bar.txt" "/*/*.*" "/usr/local/*/*.*")
               #P"/usr/local/foo/bar.txt"))

;; "TRANSLATE-PATHNAME translates SOURCE (that matches FROM-WILDCARD)..."
(expect (not (pathname-match-p "/foo/bar.txt" "**/*.*")))
;; Since (pathname-match-p "/foo/bar.txt" "**/*.*" ) => NIL...
#+(or clisp allegro abcl cmu)
;; This seems to be the correct behavior.
(expect (signals-error (translate-pathname "/foo/bar.txt" "**/*.*" "/usr/local/**/*.*") 'error))
#+(or sbcl)
;; This appears to be a bug, since SOURCE doesn't match FROM-WILDCARD.
(expect (equal (translate-pathname "/foo/bar.txt" "**/*.*" "/usr/local/**/*.*")
               #p"/usr/local/foo/bar.txt"))

(expect (pathname-match-p "/foo/bar.txt" "/**/*.*"))
(expect (equal (translate-pathname "/foo/bar.txt" "/**/*.*" "/usr/local/**/*.*")
               #p"/usr/local/foo/bar.txt"))

;; TRANSLATE-LOGICAL-PATHNAME
#-clisp
(expect (equal (translate-pathname "foo.bar" "/**/*.*" "/usr/local/") #p"/usr/local/foo.bar"))

#+clisp
(expect (equal (translate-logical-pathname "effluvia:foo.bar") #p"/usr/local/foo.bar"))
#+(or sbcl cmu)
;; Device mismatch.
(progn
  (expect (eq (pathname-device (translate-logical-pathname "effluvia:foo.bar")) :unspecific))
  (expect (eq (pathname-device #p"/usr/local/foo/bar") nil)))
(expect (string= (namestring (translate-logical-pathname "effluvia:foo.bar"))
                 #-windows "/usr/local/foo.bar"
                 #+windows "\\usr\\local\\foo.bar"))
(expect (string= (namestring (translate-logical-pathname "effluvia:foo;bar.txt"))
                 #-windows "/usr/local/foo/bar.txt"
                 #+windows "\\usr\\local\\foo\\bar.txt"))

#-allegro
(check-logical-pathname #p"effluvia:Foo.Bar" "EFFLUVIA" '(:absolute) "FOO" "BAR" nil)
#+allegro
(check-logical-pathname #p"effluvia:Foo.Bar" "effluvia" nil "Foo" "Bar" nil)

;; "TRANSLATE-PATHNAME [and thus also TRANSLATE-LOGICAL-PATHNAME] maps
;; customary case in SOURCE into customary case in the output pathname."
(deftest translate-logical-pathname.1
  #-allegro
  (check-physical-pathname (translate-logical-pathname #p"effluvia:Foo.Bar")
                           '(:absolute "usr" "local") "foo" "bar")
  #+allegro
  ;; Allegro preserves case.
  (check-physical-pathname (translate-logical-pathname #p"effluvia:Foo.Bar")
                           '(:absolute "usr" "local") "Foo" "Bar")
  t)

(deftest merge-pathnames.1
  #-allegro
  (check-logical-pathname (merge-pathnames "effluvia:foo.bar")
                          "EFFLUVIA" '(:absolute) "FOO" "BAR" :newest)
  #+allegro
  ;; Allegro's MERGE-PATHNAMES apparently calls TRANSLATE-LOGICAL-PATHNAME.
  (check-physical-pathname (merge-pathnames "effluvia:foo.bar")
                           '(:absolute "usr" "local") "foo" "bar")
  t)

;; The following tests are adapted from SBCL's pathnames.impure.lisp.
(setf (logical-pathname-translations "demo0")
      '(("**;*.*.*" "/tmp/")))
(deftest sbcl.1
  (pathname-match-p "demo0:file.lisp" (logical-pathname "demo0:tmp;**;*.*.*"))
  nil)

#-clisp
(deftest sbcl.2
  (check-namestring (translate-logical-pathname "demo0:file.lisp") "/tmp/file.lisp")
  t)

(setf (logical-pathname-translations "demo1")
      '(("**;*.*.*" "/tmp/**/*.*") (";**;*.*.*" "/tmp/rel/**/*.*")))
;; Remove "**" from the resulting pathname when the source directory is NIL.
(deftest sbcl.3
  (equal (namestring (translate-logical-pathname "demo1:foo.lisp"))
         #-windows "/tmp/**/foo.lisp"
         #+windows "\\tmp\\**\\foo.lisp")
  nil)
(deftest sbcl.4
  (check-namestring (translate-logical-pathname "demo1:foo.lisp") "/tmp/foo.lisp")
  t)
;;; Check for absolute/relative path confusion.
(deftest sbcl.5
  (pathname-match-p "demo1:;foo.lisp" "**;*.*.*")
  nil)
#-(or sbcl cmu allegro abcl)
;; BUG Pathnames should match if the following translation is to work.
(deftest sbcl.6
  (pathname-match-p "demo1:;foo.lisp" "demo1:;**;*.*.*")
  t)
#+clisp
(deftest sbcl.7
  (pathname-match-p "demo1:;foo.lisp" ";**;*.*.*")
  t)
(deftest sbcl.8
  (equal (namestring (translate-logical-pathname "demo1:;foo.lisp"))
         #+(and abcl windows) "\\tmp\\rel\\foo.lisp"
         #+(and abcl unix) "/tmp/rel/foo.lisp"
         #-(or allegro abcl) "/tmp/rel/foo.lisp"
         #+allegro "/tmp/foo.lisp")
  t)

(setf (logical-pathname-translations "demo2")
      '(("test;**;*.*" "/tmp/demo2/test")))
(deftest sbcl.9
  (equal (enough-namestring "demo2:test;foo.lisp")
         #+sbcl "DEMO2:;TEST;FOO.LISP"
         #+cmu #p"DEMO2:TEST;FOO.LISP" ;; BUG (must be string or NIL)
         #+clisp "TEST;FOO.LISP"
         #+allegro "/test/foo.lisp" ;; BUG
         #+abcl "DEMO2:TEST;FOO.LISP"
         )
  t)

#-(or allegro clisp cmu)
(deftest sbcl.10
  (signals-error (make-pathname :host "EFFLUVIA" :directory "!bla" :name "bar")
                 'error)
  t)
#-(or allegro cmu)
(deftest sbcl.11
  (signals-error (make-pathname :host "EFFLUVIA" :directory "bla" :name "!bar")
                 'error)
  t)
#-(or allegro cmu)
(deftest sbcl.12
  (signals-error (make-pathname :host "EFFLUVIA" :directory "bla" :name "bar" :type "&baz")
                 'error)
  t)

(deftest sbcl.13
  (equal (namestring (parse-namestring "" "EFFLUVIA")) "EFFLUVIA:")
  t)

(deftest sbcl.14
  #-cmu
  (equal (namestring (parse-namestring "" :unspecific)) "")
  #+cmu
  ;; It seems reasonable to signal an error here, since the HOST argument to
  ;; PARSE-NAMESTRING is specified to be "a valid pathname host, a logical host,
  ;; or NIL".
  (signals-error (parse-namestring "" :unspecific) 'type-error)
  t)

(deftest sbcl.15
  (equal (namestring (parse-namestring ""
                                       (pathname-host
                                        (translate-logical-pathname
                                         "EFFLUVIA:"))))
         "")
  t)

;; PARSE-NAMESTRING host mismatch: "If HOST is supplied and not NIL, and THING
;; contains a manifest host name, an error of type ERROR is signaled if the
;; hosts do not match."
(deftest sbcl.16
  (signals-error (parse-namestring "effluvia:foo.bar" "demo2") 'error)
  t)

(setf (logical-pathname-translations "bazooka")
      '(("todemo;*.*.*" "demo0:*.*.*")))
(deftest sbcl.17
  #+allegro ;; BUG
  (equal (namestring (translate-logical-pathname "bazooka:todemo;x.y")) "/tmp/todemo/x.y")
  #+clisp ;; BUG
  (signals-error (translate-logical-pathname "bazooka:todemo;x.y") 'error)
  #-(or allegro clisp)
  (equal (namestring (translate-logical-pathname "bazooka:todemo;x.y"))
         #-windows "/tmp/x.y"
         #+windows "\\tmp\\x.y")
  t)
(deftest sbcl.18
  #+clisp ;; BUG
  (signals-error (translate-logical-pathname "demo0:x.y") 'error)
  #-clisp
  (equal (namestring (translate-logical-pathname "demo0:x.y"))
         #-windows "/tmp/x.y"
         #+windows "\\tmp\\x.y")
  t)
#-(or allegro clisp)
(deftest sbcl.19
  (equal (namestring (translate-logical-pathname "bazooka:todemo;x.y"))
         (namestring (translate-logical-pathname "demo0:x.y")))
  t)

;; "If HOST is incorrectly supplied, an error of type TYPE-ERROR is signaled."
(deftest sbcl.20
  (signals-error (logical-pathname-translations "unregistered-host")
                 #+clisp 'error ;; BUG
                 #+cmu 'file-error ;; BUG
                 #-(or clisp cmu) 'type-error)
  t)

(deftest sbcl.21
  (string-equal (host-namestring (parse-namestring "OTHER-HOST:ILLEGAL/LPN")) "OTHER-HOST")
  nil)
(deftest sbcl.22
  (string= (pathname-name (parse-namestring "OTHER-HOST:ILLEGAL/LPN")) "LPN")
  t)

(setf (logical-pathname-translations "test0")
      '(("**;*.*.*"              "/library/foo/**/")))
(deftest sbcl.23
  (check-namestring (translate-logical-pathname "test0:foo;bar;baz;mum.quux")
                    "/library/foo/foo/bar/baz/mum.quux")
  t)
;; (setf (logical-pathname-translations "prog")
;;       '(("RELEASED;*.*.*"        "MY-UNIX:/sys/bin/my-prog/")
;;         ("RELEASED;*;*.*.*"      "MY-UNIX:/sys/bin/my-prog/*/")
;;         ("EXPERIMENTAL;*.*.*"    "MY-UNIX:/usr/Joe/development/prog/")
;;         ("EXPERIMENTAL;*;*.*.*"  "MY-UNIX:/usr/Joe/development/prog/*/")))
(setf (logical-pathname-translations "prog")
      '(("CODE;*.*.*"             "/lib/prog/")))
#-allegro
(deftest sbcl.24
  (check-namestring (translate-logical-pathname "prog:code;documentation.lisp")
                    "/lib/prog/documentation.lisp")
  t)
(setf (logical-pathname-translations "prog1")
      '(("CODE;DOCUMENTATION.*.*" "/lib/prog/docum.*")
        ("CODE;*.*.*"             "/lib/prog/")))
#-allegro
(deftest sbcl.25
  (check-namestring (translate-logical-pathname "prog1:code;documentation.lisp")
                    "/lib/prog/docum.lisp")
  t)

;; "ANSI section 19.3.1.1.5 specifies that translation to a filesystem which
;; doesn't have versions should ignore the version slot. CMU CL didn't ignore
;; this as it should, but we [i.e. SBCL] do."
#-cmu
;; CMUCL supports emacs-style versions.
(deftest sbcl.26
  (check-namestring (translate-logical-pathname "test0:foo;bar;baz;mum.quux.3")
                    "/library/foo/foo/bar/baz/mum.quux")
  t)

(eval-when (:compile-toplevel :load-toplevel :execute)
  (setf (logical-pathname-translations "scratch")
        '(("**;*.*.*" "/usr/local/doc/**/*"))))

#-(or allegro clisp)
;; FIXME Figure out why CLISP and Allegro don't like this test!
;; FIXME Figure out how to wrap this in DEFTEST!
(loop for (expected-result . params) in
  `(;; trivial merge
        (#P"/usr/local/doc/foo" #p"foo" #p"/usr/local/doc/")
        ;; If pathname does not specify a host, device, directory,
        ;; name, or type, each such component is copied from
        ;; default-pathname.
        ;; 1) no name, no type
        (#p"/supplied-dir/name.type" #p"/supplied-dir/" #p"/dir/name.type")
        ;; 2) no directory, no type
        (#p"/dir/supplied-name.type" #p"supplied-name" #p"/dir/name.type")
        ;; 3) no name, no dir (must use make-pathname as ".foo" is parsed
        ;; as a name)
        (#p"/dir/name.supplied-type"
         ,(make-pathname :type "supplied-type")
         #p"/dir/name.type")
        ;; If (pathname-directory pathname) is a list whose car is
        ;; :relative, and (pathname-directory default-pathname) is a
        ;; list, then the merged directory is [...]
        (#p"/aaa/bbb/ccc/ddd/qqq/www" #p"qqq/www" #p"/aaa/bbb/ccc/ddd/eee")
        ;; except that if the resulting list contains a string or
        ;; :wild immediately followed by :back, both of them are
        ;; removed.
        (#P"/aaa/bbb/ccc/blah/eee"
         ;; "../" in a namestring is parsed as :up not :back, so make-pathname
         ,(make-pathname :directory '(:relative :back "blah"))
         #p"/aaa/bbb/ccc/ddd/eee")
        ;; If (pathname-directory default-pathname) is not a list or
        ;; (pathname-directory pathname) is not a list whose car is
        ;; :relative, the merged directory is (or (pathname-directory
        ;; pathname) (pathname-directory default-pathname))
        (#P"/absolute/path/name.type"
         #p"/absolute/path/name"
         #p"/dir/default-name.type")
        ;; === logical pathnames ===
        ;; recognizes a logical pathname namestring when
        ;; default-pathname is a logical pathname
        ;; FIXME: 0.6.12.23 fails this one.
        ;;
        ;; And, as it happens, it's right to fail it. Because
        ;; #p"name1" is read in with the ambient *d-p-d* value, which
        ;; has a physical (Unix) host; therefore, the host of the
        ;; default-pathname argument to merge-pathnames is
        ;; irrelevant. The result is (correctly) different if
        ;; '#p"name1"' is replaced by "name1", below, though it's
        ;; still not what one might expect... -- CSR, 2002-05-09
        #+nil (#P"scratch:foo;name1" #p"name1" #p"scratch:foo;")
        ;; or when the namestring begins with the name of a defined
        ;; logical host followed by a colon [I assume that refers to pathname
        ;; rather than default-pathname]
        (#p"SCRATCH:FOO;NAME2" #p"scratch:;name2" #p"scratch:foo;")
        ;; conduct the previous set of tests again, with a lpn first argument
        (#P"SCRATCH:USR;LOCAL;DOC;FOO" #p"scratch:;foo" #p"/usr/local/doc/")
        (#p"SCRATCH:SUPPLIED-DIR;NAME.TYPE"
         #p"scratch:supplied-dir;"
         #p"/dir/name.type")
        (#p"SCRATCH:DIR;SUPPLIED-NAME.TYPE"
         #p"scratch:;supplied-name"
         #p"/dir/name.type")
        (#p"SCRATCH:DIR;NAME.SUPPLIED-TYPE"
         ,(make-pathname :host "scratch" :type "supplied-type")
         #p"/dir/name.type")
        (#p"SCRATCH:AAA;BBB;CCC;DDD;FOO;BAR"
         ,(make-pathname :host "scratch"
                         :directory '(:relative "foo")
                         :name "bar")
         #p"/aaa/bbb/ccc/ddd/eee")
        (#p"SCRATCH:AAA;BBB;CCC;FOO;BAR"
         ,(make-pathname :host "scratch"
                         :directory '(:relative :back "foo")
                         :name "bar")
         #p"/aaa/bbb/ccc/ddd/eee")
        (#p"SCRATCH:ABSOLUTE;PATH;NAME.TYPE"
         #p"scratch:absolute;path;name" #p"/dir/default-name.type")

        ;; FIXME: test version handling in LPNs
        )
  do (let ((result (apply #'merge-pathnames params)))
       (macrolet ((frob (op)
                    `(expect (equal (,op result) (,op expected-result)))))
         (frob pathname-host)
         (frob pathname-directory)
         (frob pathname-name)
         (frob pathname-type))))

(deftest sbcl.27
  (check-namestring (parse-namestring "/foo" (host-namestring #p"/bar")) "/foo")
  t)
(deftest sbcl.28
  (string= (namestring (parse-namestring "FOO" (host-namestring #p"SCRATCH:BAR")))
           "SCRATCH:FOO")
  t)
#-(or allegro clisp cmu)
(deftest sbcl.29
  (signals-error (setf (logical-pathname-translations "")
                       (list '("**;*.*.*" "/**/*.*")))
                 'error)
  t)

(rt:do-tests)
