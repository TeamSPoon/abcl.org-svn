;;;; Does not currently include all the MOP, but it should.

(in-package #:mop)

;;; StandardGenericFunction.java defines FUNCALLABLE-INSTANCE-FUNCTION and
;;; SET-FUNCALLABLE-INSTANCE-FUNCTION.
;;;
;;; TODO 
;;;
;;;   1. Verify that we can make FUNCALLABLE-STANDARD-CLASS instances
;;;      which work.
;;;
;;;   2. Tighten the type checks so that only instances of
;;;      FUNCALLABLE-STANDARD-CLASS are callable.

;;; AMOP pg. 240ff.
(defgeneric validate-superclass (class superclass)
 (:documentation 
  "This generic function is called to determine whether the class
  superclass is suitable for use as a superclass of class."))

(defmethod validate-superclass ((class class) (superclass class))
  (or (eql superclass +the-T-class+)
      (eql (class-of class) (class-of superclass))
      (or (and (eql (class-of class) +the-standard-class+)
               (eql (class-of superclass) +the-funcallable-standard-class+))
          (and (eql (class-of class) +the-funcallable-standard-class+)
               (eql (class-of superclass) +the-standard-class+)))))

;;; This is against the letter of the MOP, but very much in its spirit.
(defmethod validate-superclass ((class class)
                                (superclass forward-referenced-class))
  t)

(defmethod shared-initialize :before ((instance class)
                                      slot-names
                                      &key direct-superclasses
                                      &allow-other-keys)
  (declare (ignore slot-names))
  (dolist (superclass direct-superclasses)
    (assert (validate-superclass instance superclass) (instance superclass)
            "Class ~S is not compatible with superclass ~S"
            instance superclass)))

(export '(;; classes
          funcallable-standard-object
          funcallable-standard-class
          forward-referenced-class
          direct-slot-definition-class
          effective-slot-definition-class
          standard-method
          standard-accessor-method
          standard-reader-method
          standard-writer-method
          
          compute-effective-slot-definition
          compute-class-precedence-list
          compute-default-initargs
          compute-effective-slot-definition
          compute-effective-method
          compute-slots
          finalize-inheritance
          validate-superclass

          slot-value-using-class
          slot-boundp-using-class
          slot-makunbound-using-class

          ensure-class
          ensure-class-using-class
          ensure-generic-function-using-class

          class-default-initargs
          class-direct-default-initargs
          class-direct-slots
          class-direct-subclasses
          class-direct-superclasses
          class-finalized-p
          class-prototype

          add-direct-subclass
          remove-direct-subclass

          generic-function-lambda-list
          generic-function-argument-precedence-order
          generic-function-method-class

          method-function
          method-generic-function
          method-lambda-list
          method-specializers
          method-qualifiers

          standard-reader-method
          standard-writer-method
          reader-method-class
          writer-method-class

          slot-definition
          slot-definition-readers
          slot-definition-writers
          slot-definition-location
          standard-instance-access
          funcallable-standard-instance-access

          intern-eql-specializer
          eql-specializer-object
          specializer-direct-methods
          specializer-direct-generic-functions
          add-direct-method
          remove-direct-method

          extract-lambda-list
          extract-specializer-names

          add-dependent
          remove-dependent
          map-dependents
          update-dependent))

(provide 'mop)





