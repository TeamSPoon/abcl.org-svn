/*
 * StandardObject.java
 *
 * Copyright (C) 2003-2004 Peter Graves
 * $Id: StandardObject.java,v 1.29 2004-11-06 13:49:46 piso Exp $
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
    private SimpleVector slots;

    protected StandardObject()
    {
        layout = new Layout(BuiltInClass.STANDARD_OBJECT, Fixnum.ZERO, NIL);
    }

    protected StandardObject(LispClass cls, SimpleVector slots)
    {
        layout = cls.getLayout();
        Debug.assertTrue(layout != null);
        this.slots = slots;
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject result = NIL;
        result = result.push(new Cons("LAYOUT", layout));
        result = result.push(new Cons("SLOTS", slots));
        return result.nreverse();
    }

    public final LispClass getLispClass()
    {
        return layout.getLispClass();
    }

    public LispObject typeOf()
    {
        // "For objects of metaclass structure-class or standard-class, and for
        // conditions, type-of returns the proper name of the class returned by
        // class-of if it has a proper name, and otherwise returns the class
        // itself."
        Symbol symbol = layout.getLispClass().getSymbol();
        if (symbol != NIL)
            return symbol;
        return layout.getLispClass();
    }

    public LispObject classOf()
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

    public String writeToString() throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        int maxLevel = Integer.MAX_VALUE;
        LispObject printLevel = _PRINT_LEVEL_.symbolValue(thread);
        if (printLevel instanceof Fixnum)
            maxLevel = ((Fixnum)printLevel).value;
        LispObject currentPrintLevel =
            _CURRENT_PRINT_LEVEL_.symbolValue(thread);
        int currentLevel = Fixnum.getValue(currentPrintLevel);
        if (currentLevel >= maxLevel)
            return "#";
        LispClass cls = layout.getLispClass();
        return unreadableString(cls != null ? cls.getSymbol().getName() : "STANDARD-OBJECT");
    }

    // ### std-instance-layout
    private static final Primitive STD_INSTANCE_LAYOUT =
        new Primitive("std-instance-layout", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((StandardObject)arg).layout;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, Symbol.STANDARD_OBJECT));
            }
        }
    };

    // ### %set-std-instance-layout
    private static final Primitive _SET_STD_INSTANCE_LAYOUT =
        new Primitive("%set-std-instance-layout", PACKAGE_SYS, false)
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
    private static final Primitive STD_INSTANCE_CLASS =
        new Primitive("std-instance-class", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).layout.getLispClass();
            return signal(new TypeError(arg, Symbol.STANDARD_OBJECT));
        }
    };

    // ### std-instance-slots
    private static final Primitive STD_INSTANCE_SLOTS =
        new Primitive("std-instance-slots", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).slots;
            return signal(new TypeError(arg, Symbol.STANDARD_OBJECT));
        }
    };

    // ### %set-std-instance-slots
    private static final Primitive _SET_STD_INSTANCE_SLOTS =
        new Primitive("%set-std-instance-slots", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first instanceof StandardObject) {
                if (second instanceof SimpleVector) {
                    ((StandardObject)first).slots = (SimpleVector) second;
                    return second;
                }
                return signal(new TypeError(second, Symbol.SIMPLE_VECTOR));
            }
            return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
        }
    };

    // ### standard-instance-access instance location => value
    private static final Primitive STANDARD_INSTANCE_ACCESS =
        new Primitive("standard-instance-access", PACKAGE_SYS, true,
                      "instance location")
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

    // ### %set-standard-instance-access instance location new-value => new-value
    private static final Primitive _SET_STANDARD_INSTANCE_ACCESS =
        new Primitive("%set-standard-instance-access", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((StandardObject)first).slots.setRowMajor(Fixnum.getValue(second),
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
    private static final Primitive ALLOCATE_SLOT_STORAGE =
        new Primitive("allocate-slot-storage", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                SimpleVector v = new SimpleVector(((Fixnum)first).value);
                v.fill(second);
                return v;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.FIXNUM));
            }
        }
    };

    // ### allocate-std-instance
    // allocate-std-instance class slots => instance
    private static final Primitive ALLOCATE_STD_INSTANCE =
        new Primitive("allocate-std-instance", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first == BuiltInClass.STANDARD_CLASS)
                return new StandardClass();
            if (first instanceof LispClass) {
                if (second instanceof SimpleVector) {
                    Symbol symbol = ((LispClass)first).getSymbol();
                    SimpleVector slots = (SimpleVector) second;
                    if (symbol == Symbol.STANDARD_GENERIC_FUNCTION)
                        return new GenericFunction((LispClass)first, slots);
                    if (symbol == Symbol.STANDARD_METHOD)
                        return new Method((LispClass)first, slots);
                    LispObject cpl = ((LispClass)first).getCPL();
                    while (cpl != NIL) {
                        LispObject obj = cpl.car();
                        if (obj == BuiltInClass.CONDITION)
                            return new Condition((LispClass)first, slots);
                        cpl = cpl.cdr();
                    }
                    return new StandardObject((LispClass)first, slots);
                }
                return signal(new TypeError(second, Symbol.SIMPLE_VECTOR));
            }
            return signal(new TypeError(first, Symbol.CLASS));
        }
    };
}
