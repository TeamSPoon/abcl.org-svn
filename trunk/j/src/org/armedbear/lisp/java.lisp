;;; java.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: java.lisp,v 1.10 2003-12-20 22:40:02 asimon Exp $
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

(in-package "JAVA")

(defun jregister-handler (object event handler &key data count)
  (%jregister-handler object event handler data count))

(defun jclass-name (class)
  "Returns the name of CLASS as a Lisp string"
  (jcall (jmethod "java.lang.Class" "getName") class))

(defun jobject-class (obj)
  "Returns the Java class that OBJ belongs to"
  (jcall (jmethod "java.lang.Object" "getClass") obj))

(defun ensure-jclass (class-or-string)
  "If handed a string, return a Class object."
  (if (stringp class-or-string)
      (jclass class-or-string)
      class-or-string))

(defun jclass-superclass (class)
  "Returns the superclass of CLASS, or NIL if it hasn't got one"
  (jcall (jmethod "java.lang.Class" "getSuperclass") (ensure-jclass class)))

(defun jclass-interfaces (class)
  "Returns the vector of interfaces of CLASS"
  (jcall (jmethod "java.lang.Class" "getInterfaces") (ensure-jclass class)))

(defun jclass-interface-p (class)
  "Returns T if CLASS is an interface"
  (jcall (jmethod "java.lang.Class" "isInterface") (ensure-jclass class)))

(defun jclass-superclass-p (class-1 class-2)
  "Returns T if CLASS-1 is a superclass or interface of CLASS-2"
  (jcall (jmethod "java.lang.Class" "isAssignableFrom" "java.lang.Class") 
	 (ensure-jclass class-1)
	 (ensure-jclass class-2)))

(defun jclass-array-p (class)
  "Returns T if CLASS is an array class"
  (jcall (jmethod "java.lang.Class" "isArray") (ensure-jclass class)))

(defun jarray-component-type (atype)
  "Returns the component type of the array type ATYPE"
  (assert (jclass-array-p atype))
  (jcall (jmethod "java.lang.Class" "getComponentType") atype))

(defun (setf jarray-ref) (new-value java-array &rest indices)
  (apply #'jarray-set java-array new-value indices))

(defun jclass-constructors (class)
  "Returns a vector of constructors for CLASS"
  (jcall (jmethod "java.lang.Class" "getConstructors") (ensure-jclass class)))

(defun jconstructor-params (constructor)
  "Returns a vector of parameter types (Java classes) for CONSTRUCTOR"
  (jcall (jmethod "java.lang.reflect.Constructor" "getParameterTypes") constructor))

(defun jclass-fields (class &key declared public)
  "Returns a vector of all (or just the declared/public, if DECLARED/PUBLIC is true) fields of CLASS"
  (let* ((getter (if declared "getDeclaredFields" "getFields"))
         (fields (jcall (jmethod "java.lang.Class" getter) (ensure-jclass class))))
    (if public (delete-if-not #'jmember-public-p fields) fields)))

(defun jfield-type (field)
  "Returns the type (Java class) of FIELD"
  (jcall (jmethod "java.lang.reflect.Field" "getType") field))

(defun jfield-name (field)
  "Returns the name of FIELD as a Lisp string"
  (jcall (jmethod "java.lang.reflect.Field" "getName") field))

(defun jclass-methods (class &key declared public)
  "Return a vector of all (or just the declared/public, if DECLARED/PUBLIC is true) methods of CLASS"
  (let* ((getter (if declared "getDeclaredMethods" "getMethods"))
         (methods (jcall (jmethod "java.lang.Class" getter) (ensure-jclass class))))
    (if public (delete-if-not #'jmember-public-p methods) methods)))

(defun jmethod-params (method)
  "Returns a vector of parameter types (Java classes) for METHOD"
  (jcall (jmethod "java.lang.reflect.Method" "getParameterTypes") method))

(defun jmethod-return-type (method)
  "Returns the result type (Java class) of the METHOD"
  (jcall (jmethod "java.lang.reflect.Method" "getReturnType") method))

(defun jmethod-name (method)
  "Returns the name of METHOD as a Lisp string"
  (jcall (jmethod "java.lang.reflect.Method" "getName") method))

(defun jinstance-of-p (obj class)
  "OBJ is an instance of CLASS (or one of its subclasses)"
  (and (java-object-p obj)
       (jcall (jmethod "java.lang.Class" "isInstance" "java.lang.Object") (ensure-jclass class) obj)))

(defun jmember-static-p (member)
  "MEMBER is a static member of its declaring class"
  (jstatic (jmethod "java.lang.reflect.Modifier" "isStatic" "int")
    "java.lang.reflect.Modifier"
    (jcall (jmethod "java.lang.reflect.Member" "getModifiers") member)))

(defun jmember-public-p (member)
  "MEMBER is a public member of its declaring class"
  (jstatic (jmethod "java.lang.reflect.Modifier" "isPublic" "int")
    "java.lang.reflect.Modifier"
    (jcall (jmethod "java.lang.reflect.Member" "getModifiers") member)))
