/*
 * StructureObject.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: StructureObject.java,v 1.14 2003-09-20 18:18:28 piso Exp $
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

public final class StructureObject extends LispObject
{
    private final Symbol symbol;
    private final LispObject[] slots;

    public StructureObject(Symbol symbol, LispObject list) throws ConditionThrowable
    {
        this.symbol = symbol;
        slots = list.copyToArray();
    }

    public StructureObject(StructureObject obj)
    {
        this.symbol = obj.symbol;
        slots = new LispObject[obj.slots.length];
        for (int i = slots.length; i-- > 0;)
            slots[i] = obj.slots[i];
    }

    public LispObject typeOf()
    {
        return symbol;
    }

    public LispClass classOf()
    {
        return LispClass.findClass(symbol);
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == symbol)
            return T;
        if (type instanceof StructureClass)
            return type == LispClass.findClass(symbol) ? T : NIL;
        if (type == Symbol.STRUCTURE_OBJECT)
            return T;
        if (type == BuiltInClass.STRUCTURE_OBJECT)
            return T;
        return super.typep(type);
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("#S(");
        sb.append(symbol);
        // FIXME Use *PRINT-LENGTH*.
        final int limit = Math.min(slots.length, 10);
        for (int i = 0; i < limit; i++) {
            sb.append(' ');
            sb.append(slots[i]);
        }
        if (limit < slots.length)
            sb.append(" ...");
        sb.append(')');
        return sb.toString();
    }

    // ### %structure-ref
    // %structure-ref instance index => value
    private static final Primitive2 _STRUCTURE_REF =
        new Primitive2("%structure-ref", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return ((StructureObject)first).slots[((Fixnum)second).getValue()];
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    private static final Primitive1 _STRUCTURE_REF_0 =
        new Primitive1("%structure-ref-0", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((StructureObject)arg).slots[0];
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    private static final Primitive1 _STRUCTURE_REF_1 =
        new Primitive1("%structure-ref-1", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((StructureObject)arg).slots[1];
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    private static final Primitive1 _STRUCTURE_REF_2 =
        new Primitive1("%structure-ref-2", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((StructureObject)arg).slots[2];
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    // ### %structure-set
    // %structure-set instance index new-value => new-value
    private static final Primitive3 _STRUCTURE_SET =
        new Primitive3("%structure-set", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((StructureObject)first).slots[((Fixnum)second).getValue()] = third;
                return third;
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    private static final Primitive1 _STRUCTURE_SET_0 =
        new Primitive1("%structure-set-0", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                ((StructureObject)first).slots[0] = second;
                return second;
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    private static final Primitive1 _STRUCTURE_SET_1 =
        new Primitive1("%structure-set-1", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                ((StructureObject)first).slots[1] = second;
                return second;
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    private static final Primitive1 _STRUCTURE_SET_2 =
        new Primitive1("%structure-set-2", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                ((StructureObject)first).slots[2] = second;
                return second;
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                throw new ConditionThrowable(new LispError("internal error"));
            }
        }
    };

    // ### %make-structure
    // %make-structure name slot-values => object
    private static final Primitive2 _MAKE_STRUCTURE =
        new Primitive2("%make-structure", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new StructureObject(checkSymbol(first), checkList(second));
        }
    };

    // ### copy-structure
    // copy-structure structure => copy
    private static final Primitive1 COPY_STRUCTURE =
        new Primitive1("copy-structure") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return new StructureObject((StructureObject)arg);
            }
            catch (ClassCastException e) {
                throw new ConditionThrowable(new TypeError(arg, "STRUCTURE-OBJECT"));
            }
        }
    };
}
