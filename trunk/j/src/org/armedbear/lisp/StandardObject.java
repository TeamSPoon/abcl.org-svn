/*
 * StandardObject.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: StandardObject.java,v 1.15 2003-12-19 19:19:52 piso Exp $
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.armedbear.lisp;

public class StandardObject extends LispObject
{
    private Layout layout;
    private Vector slots;

    protected StandardObject()
    {
        layout = new Layout(BuiltInClass.STANDARD_OBJECT, Fixnum.ZERO);
    }

    protected StandardObject(LispClass cls, Vector slots)
    {
        layout = cls.getLayout();
        Debug.assertTrue(layout != null);
        this.slots = slots;
    }

    public final LispClass getLispClass()
    {
        return layout.getLispClass();
    }

    public final LispObject getSlots()
    {
        return slots;
    }

    public LispObject typeOf()
    {
        return layout.getLispClass().getSymbol();
    }

    public LispClass classOf()
    {
        return layout.getLispClass();
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.STANDARD_OBJECT)
            return T;
        if (type == BuiltInClass.STANDARD_OBJECT)
            return T;
        LispClass cls = layout != null ? layout.getLispClass() : null;
        if (cls != null) {
            if (type == cls)
                return T;
            if (type == cls.getSymbol())
                return T;
            LispObject cpl = cls.getCPL();
            while (cpl != NIL) {
                if (type == cpl.car())
                    return T;
                if (type == ((LispClass)cpl.car()).getSymbol())
                    return T;
                cpl = cpl.cdr();
            }
        }
        return super.typep(type);
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("#<");
        LispClass cls = layout.getLispClass();
        if (cls != null)
            sb.append(cls.getSymbol().getName());
        else
            sb.append("STANDARD-OBJECT");
        sb.append(" @ #x");
        sb.append(Integer.toHexString(hashCode()));
        sb.append(">");
        return sb.toString();
    }

    // ### std-instance-layout
    private static final Primitive1 STD_INSTANCE_LAYOUT =
        new Primitive1("std-instance-layout", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).layout;
            return signal(new TypeError(arg, "standard object"));
        }
    };

    // ### %set-std-instance-layout
    private static final Primitive2 _SET_STD_INSTANCE_LAYOUT =
        new Primitive2("%set-std-instance-layout", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                ((StandardObject)first).layout = (Layout) second;
                return second;
            }
            catch (ClassCastException e) {
                if (!(first instanceof StandardObject))
                    return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
                if (!(second instanceof Layout))
                    return signal(new TypeError(second, "layout"));
                // Not reached.
                return NIL;
            }
        }
    };

    // ### std-instance-class
    private static final Primitive1 STD_INSTANCE_CLASS =
        new Primitive1("std-instance-class", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).layout.getLispClass();
            return signal(new TypeError(arg, Symbol.STANDARD_OBJECT));
        }
    };

    // ### std-instance-slots
    private static final Primitive1 STD_INSTANCE_SLOTS =
        new Primitive1("std-instance-slots", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).slots;
            return signal(new TypeError(arg, Symbol.STANDARD_OBJECT));
        }
    };

    // ### %set-std-instance-slots
    private static final Primitive2 _SET_STD_INSTANCE_SLOTS =
        new Primitive2("%set-std-instance-slots", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first instanceof StandardObject) {
                if (second instanceof Vector) {
                    ((StandardObject)first).slots = (Vector) second;
                    return second;
                }
                return signal(new TypeError(second, Symbol.VECTOR));
            }
            return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
        }
    };

    // ### instance-ref
    // instance-ref object index => value
    private static final Primitive2 INSTANCE_REF =
        new Primitive2("instance-ref", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return ((StandardObject)first).slots.AREF(second);
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
        }
    };

    // ### %set-instance-ref
    // %set-instance-ref object index new-value => new-value
    private static final Primitive3 _SET_INSTANCE_REF =
        new Primitive3("%set-instance-ref", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((StandardObject)first).slots.set(Fixnum.getValue(second),
                                                  third);
                return third;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
        }
    };

    // ### allocate-slot-storage
    // allocate-slot-storage size initial-value
    private static final Primitive2 ALLOCATE_SLOT_STORAGE =
        new Primitive2("allocate-slot-storage", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Vector v = new Vector(Fixnum.getValue(first));
            v.fill(second);
            return v;
        }
    };

    // ### allocate-std-instance
    // allocate-std-instance class slots => instance
    private static final Primitive2 ALLOCATE_STD_INSTANCE =
        new Primitive2("allocate-std-instance", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first == BuiltInClass.STANDARD_CLASS)
                return new StandardClass();
            if (first instanceof LispClass) {
                if (second instanceof Vector) {
                    Symbol symbol = ((LispClass)first).getSymbol();
                    Vector slots = (Vector) second;
                    if (symbol == Symbol.STANDARD_GENERIC_FUNCTION)
                        return new GenericFunction((LispClass)first, slots);
                    LispObject cpl = ((LispClass)first).getCPL();
                    while (cpl != NIL) {
                        LispObject obj = cpl.car();
                        if (obj == BuiltInClass.CONDITION)
                            return new Condition((LispClass)first, slots);
                        cpl = cpl.cdr();
                    }
                    return new StandardObject((LispClass)first, slots);
                }
                return signal(new TypeError(second, Symbol.VECTOR));
            }
            return signal(new TypeError(first, Symbol.CLASS));
        }
    };
}
