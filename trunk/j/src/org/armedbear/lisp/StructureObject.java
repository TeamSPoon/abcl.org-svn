/*
 * StructureObject.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: StructureObject.java,v 1.53 2005-08-01 16:43:51 piso Exp $
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
    private final StructureClass structureClass;
    private final LispObject[] slots;

    public StructureObject(Symbol symbol, LispObject list)
        throws ConditionThrowable
    {
        structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
        slots = list.copyToArray();
    }

    public StructureObject(StructureObject obj)
    {
        this.structureClass = obj.structureClass;
        slots = new LispObject[obj.slots.length];
        for (int i = slots.length; i-- > 0;)
            slots[i] = obj.slots[i];
    }

    public LispObject typeOf()
    {
        return structureClass.getSymbol();
    }

    public LispObject classOf()
    {
        return structureClass;
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject result = NIL;
        result = result.push(new Cons("class", structureClass));
        LispObject effectiveSlots = structureClass.getSlots();
        LispObject[] effectiveSlotsArray = effectiveSlots.copyToArray();
        for (int i = 0; i < slots.length; i++) {
            SimpleVector slotDefinition = (SimpleVector) effectiveSlotsArray[i];
            LispObject slotName = slotDefinition.AREF(1);
            result = result.push(new Cons(slotName, slots[i]));
        }
        return result.nreverse();
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type instanceof StructureClass)
            return memq(type, structureClass.getCPL()) ? T : NIL;
        if (type == structureClass.getSymbol())
            return T;
        if (type == Symbol.STRUCTURE_OBJECT)
            return T;
        if (type == BuiltInClass.STRUCTURE_OBJECT)
            return T;
        if (type instanceof Symbol) {
            LispClass c = LispClass.findClass((Symbol)type);
            if (c != null)
                return memq(c, structureClass.getCPL()) ? T : NIL;
        }
        return super.typep(type);
    }

    public boolean equalp(LispObject obj) throws ConditionThrowable
    {
        if (this == obj)
            return true;
        if (obj instanceof StructureObject) {
            StructureObject o = (StructureObject) obj;
            if (structureClass != o.structureClass)
                return false;
            for (int i = 0; i < slots.length; i++) {
                if (!slots[i].equalp(o.slots[i]))
                    return false;
            }
            return true;
        }
        return false;
    }

    public LispObject getSlotValue(int index) throws ConditionThrowable
    {
        try {
            return slots[index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return signal(new LispError("Invalid slot index " + index +
                                        "  for " + writeToString()));
        }
    }

    public int getFixnumSlotValue(int index) throws ConditionThrowable
    {
        try {
            return ((Fixnum)slots[index]).value;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            signal(new LispError("Invalid slot index " + index + "  for "
                                 + writeToString()));
            // Not reached.
            return 0;
        }
        catch (ClassCastException e) {
            signalTypeError(slots[index], Symbol.FIXNUM);
            // Not reached.
            return 0;
        }
    }

    public LispObject setSlotValue(int index, LispObject value)
        throws ConditionThrowable
    {
        try {
            slots[index] = value;
            return value;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return signal(new LispError("Invalid slot index " + index +
                                        "  for " + writeToString()));
        }
    }

    public String writeToString() throws ConditionThrowable
    {
        try {
            final LispThread thread = LispThread.currentThread();
            // FIXME
            if (typep(Symbol.RESTART) != NIL) {
                Symbol PRINT_RESTART = PACKAGE_SYS.intern("PRINT-RESTART");
                LispObject fun = PRINT_RESTART.getSymbolFunction();
                StringOutputStream stream = new StringOutputStream();
                thread.execute(fun, this, stream);
                return stream.getString().getStringValue();
            }
            if (_PRINT_STRUCTURE_.symbolValue(thread) == NIL)
                return unreadableString(structureClass.getSymbol().writeToString());
            int maxLevel = Integer.MAX_VALUE;
            LispObject printLevel = _PRINT_LEVEL_.symbolValue(thread);
            if (printLevel instanceof Fixnum)
                maxLevel = ((Fixnum)printLevel).value;
            LispObject currentPrintLevel =
                _CURRENT_PRINT_LEVEL_.symbolValue(thread);
            int currentLevel = Fixnum.getValue(currentPrintLevel);
            if (currentLevel >= maxLevel && slots.length > 0)
                return "#";
            StringBuffer sb = new StringBuffer("#S(");
            sb.append(structureClass.getSymbol().writeToString());
            if (currentLevel < maxLevel) {
                LispObject effectiveSlots = structureClass.getSlotDefinitions();
                LispObject[] effectiveSlotsArray = effectiveSlots.copyToArray();
                Debug.assertTrue(effectiveSlotsArray.length == slots.length);
                final LispObject printLength = _PRINT_LENGTH_.symbolValue(thread);
                final int limit;
                if (printLength instanceof Fixnum)
                    limit = Math.min(slots.length, ((Fixnum)printLength).value);
                else
                    limit = slots.length;
                final boolean printCircle =
                    _PRINT_CIRCLE_.symbolValue(thread) != NIL;
                for (int i = 0; i < limit; i++) {
                    sb.append(' ');
                    SimpleVector slotDefinition = (SimpleVector) effectiveSlotsArray[i];
                    // FIXME AREF(1)
                    LispObject slotName = slotDefinition.AREF(1);
                    Debug.assertTrue(slotName instanceof Symbol);
                    sb.append(':');
                    sb.append(((Symbol)slotName).name.getStringValue());
                    sb.append(' ');
                    if (printCircle) {
                        StringOutputStream stream = new StringOutputStream();
                        thread.execute(Symbol.OUTPUT_OBJECT.getSymbolFunction(),
                                       slots[i], stream);
                        sb.append(stream.getString().getStringValue());
                    } else
                        sb.append(slots[i].writeToString());
                }
                if (limit < slots.length)
                    sb.append(" ...");
            }
            sb.append(')');
            return sb.toString();
        }
        catch (StackOverflowError e) {
            signal(new StorageCondition("Stack overflow."));
            return null; // Not reached.
        }
    }

    // ### structure-object-p object => generalized-boolean
    private static final Primitive STRUCTURE_OBJECT_P =
        new Primitive("structure-object-p", PACKAGE_SYS, true, "object")
    {
        public LispObject execute(LispObject arg)
        {
            return (arg instanceof StructureObject) ? T : NIL;
        }
    };

    // ### structure-length instance => length
    private static final Primitive STRUCTURE_LENGTH =
        new Primitive("structure-length", PACKAGE_SYS, true, "instance")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return new Fixnum(((StructureObject)arg).slots.length);
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, Symbol.STRUCTURE_OBJECT));
            }
        }
    };

    // ### structure-ref instance index => value
    private static final Primitive _STRUCTURE_REF =
        new Primitive("structure-ref", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return ((StructureObject)first).slots[((Fixnum)second).value];
            }
            catch (ClassCastException e) {
                if (first instanceof StructureObject)
                    return signalTypeError(second, Symbol.FIXNUM);
                else
                    return signalTypeError(first, Symbol.STRUCTURE_OBJECT);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                return signal(new LispError("Internal error."));
            }
        }
    };

    // ### structure-set instance index new-value => new-value
    private static final Primitive _STRUCTURE_SET =
        new Primitive("structure-set", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((StructureObject)first).slots[((Fixnum)second).value] = third;
                return third;
            }
            catch (ClassCastException e) {
                if (first instanceof StructureObject)
                    return signalTypeError(second, Symbol.FIXNUM);
                else
                    return signalTypeError(first, Symbol.STRUCTURE_OBJECT);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                return signal(new LispError("Internal error."));
            }
        }
    };

    // ### %make-structure name slot-values => object
    private static final Primitive _MAKE_STRUCTURE =
        new Primitive("%make-structure", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return new StructureObject(((Symbol)first), second);
            }
            catch (ClassCastException e) {
                return signalTypeError(first, Symbol.SYMBOL);
            }
        }
    };

    // ### copy-structure
    // copy-structure structure => copy
    private static final Primitive COPY_STRUCTURE =
        new Primitive("copy-structure", "structure")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return new StructureObject((StructureObject)arg);
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, "STRUCTURE-OBJECT"));
            }
        }
    };
}
