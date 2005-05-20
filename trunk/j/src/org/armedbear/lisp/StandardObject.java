/*
 * StandardObject.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: StandardObject.java,v 1.43 2005-05-20 18:31:13 piso Exp $
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
    protected Layout layout;
    protected LispObject[] slots;

    protected StandardObject()
    {
        layout = new Layout(BuiltInClass.STANDARD_OBJECT, NIL, NIL);
    }

    protected StandardObject(LispClass cls, int length)
    {
        layout = cls.getClassLayout();
        slots = new LispObject[length];
        for (int i = slots.length; i-- > 0;)
            slots[i] = UNBOUND_VALUE;
    }

    private StandardObject(LispClass cls)
    {
        layout = cls.getClassLayout();
        slots = new LispObject[layout.getLength()];
        for (int i = slots.length; i-- > 0;)
            slots[i] = UNBOUND_VALUE;
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject parts = NIL;
        if (layout != null) {
            if (layout.isInvalid()) {
                // Update instance.
                layout = updateLayout();
            }
        }
        parts = parts.push(new Cons("LAYOUT", layout));
        if (layout != null) {
            LispObject[] slotNames = layout.getSlotNames();
            if (slotNames != null) {
                for (int i = 0; i < slotNames.length; i++) {
                    parts = parts.push(new Cons(slotNames[i], slots[i]));
                }
            }
        }
        return parts.nreverse();
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
        LispClass c1 = layout.getLispClass();
        // The proper name of a class is "a symbol that names the class whose
        // name is that symbol".
        Symbol symbol = c1.getSymbol();
        if (symbol != NIL) {
            // TYPE-OF.9
            LispObject c2 = LispClass.findClass(symbol);
            if (c2 == c1)
                return symbol;
        }
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
        return unreadableString(typeOf().writeToString());
    }

    private Layout updateLayout() throws ConditionThrowable
    {
        Debug.assertTrue(layout.isInvalid());
        Layout oldLayout = layout;
        LispClass cls = oldLayout.getLispClass();
        Layout newLayout = cls.getClassLayout();
        Debug.assertTrue(!newLayout.isInvalid());
        StandardObject newInstance = new StandardObject(cls);
        Debug.assertTrue(newInstance.layout == newLayout);
        LispObject added = NIL;
        LispObject discarded = NIL;
        LispObject plist = NIL;
        // Old local slots.
        LispObject[] oldSlotNames = oldLayout.getSlotNames();
        for (int i = 0; i < oldSlotNames.length; i++) {
            LispObject slotName = oldSlotNames[i];
            int j = newLayout.getSlotIndex(slotName);
            if (j >= 0)
                newInstance.slots[j] = slots[i];
            else {
                discarded = discarded.push(slotName);
                if (slots[i] != UNBOUND_VALUE) {
                    plist = plist.push(slotName);
                    plist = plist.push(slots[i]);
                }
            }
        }
        // Old shared slots.
        LispObject rest = oldLayout.getClassSlots(); // A list.
        if (rest != null) {
            while (rest != NIL) {
                LispObject location = rest.car();
                LispObject slotName = location.car();
                int i = newLayout.getSlotIndex(slotName);
                if (i >= 0)
                    newInstance.slots[i] = location.cdr();
                rest = rest.cdr();
            }
        }
        // Go through all the new local slots to compute the added slots.
        LispObject[] newSlotNames = newLayout.getSlotNames();
        for (int i = 0; i < newSlotNames.length; i++) {
            LispObject slotName = newSlotNames[i];
            int j = oldLayout.getSlotIndex(slotName);
            if (j >= 0)
                continue;
            LispObject location = oldLayout.getClassSlotLocation(slotName);
            if (location != null)
                continue;
            // Not found.
            added = added.push(slotName);
        }
        // Swap slots.
        LispObject[] tempSlots = slots;
        slots = newInstance.slots;
        newInstance.slots = tempSlots;
        // Swap layouts.
        Layout tempLayout = layout;
        layout = newInstance.layout;
        newInstance.layout = tempLayout;
        Debug.assertTrue(!layout.isInvalid());
        // Call UPDATE-INSTANCE-FOR-REDEFINED-CLASS.
        Symbol.UPDATE_INSTANCE_FOR_REDEFINED_CLASS.execute(this, added,
                                                           discarded, plist);
        return newLayout;
    }

    // ### swap-slots instance-1 instance-2 => nil
    private static final Primitive SWAP_SLOTS =
        new Primitive("swap-slots", PACKAGE_SYS, true, "instance-1 instance-2")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            StandardObject obj1, obj2;
            try {
                obj1 = (StandardObject) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
            try {
                obj2 = (StandardObject) second;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(second, Symbol.STANDARD_OBJECT));
            }
            LispObject[] temp = obj1.slots;
            obj1.slots = obj2.slots;
            obj2.slots = temp;
            return NIL;
        }
    };

    // ### std-instance-layout
    private static final Primitive STD_INSTANCE_LAYOUT =
        new Primitive("std-instance-layout", PACKAGE_SYS, true)
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
        new Primitive("%set-std-instance-layout", PACKAGE_SYS, true)
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
        new Primitive("std-instance-class", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((StandardObject)arg).layout.getLispClass();
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, Symbol.STANDARD_OBJECT));
            }
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
                return ((StandardObject)first).slots[Fixnum.getValue(second)]; // FIXME
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
        }
    };

    // ### %set-standard-instance-access instance location new-value => new-value
    private static final Primitive _SET_STANDARD_INSTANCE_ACCESS =
        new Primitive("%set-standard-instance-access", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((StandardObject)first).slots[Fixnum.getValue(second)] = third; // FIXME
                return third;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
        }
    };

    // ### std-slot-boundp
    private static final Primitive STD_SLOT_BOUNDP =
        new Primitive("std-slot-boundp", PACKAGE_SYS, true,
                      "instance slot-name")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            StandardObject instance;
            try {
                instance = (StandardObject) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
            Layout layout = instance.layout;
            if (layout.isInvalid()) {
                // Update instance.
                layout = instance.updateLayout();
            }
            int index = layout.getSlotIndex(second);
            if (index >= 0) {
                // Found instance slot.
                LispObject value = instance.slots[index];
                return value != UNBOUND_VALUE ? T : NIL;
            }
            // Check for class slot.
            LispObject location = layout.getClassSlotLocation(second);
            if (location != null) {
                LispObject value = location.cdr();
                return value != UNBOUND_VALUE ? T : NIL;
            }
            final LispThread thread = LispThread.currentThread();
            LispObject value =
                thread.execute(Symbol.SLOT_MISSING, instance.getLispClass(),
                               instance, second, Symbol.SLOT_BOUNDP);
            // "If slot-missing is invoked and returns a value, a boolean
            // equivalent to its primary value is returned by slot-boundp."
            thread._values = null;
            return value != NIL ? T : NIL;
        }
    };

    // ### std-slot-value
    private static final Primitive STD_SLOT_VALUE =
        new Primitive("std-slot-value", PACKAGE_SYS, true,
                      "instance slot-name")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            LispObject value = null;
            StandardObject instance;
            try {
                instance = (StandardObject) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
            Layout layout = instance.layout;
            if (layout.isInvalid()) {
                // Update instance.
                layout = instance.updateLayout();
            }
            int index = layout.getSlotIndex(second);
            if (index >= 0) {
                // Found instance slot.
                value = instance.slots[index];
            } else {
                // Check for class slot.
                LispObject location = layout.getClassSlotLocation(second);
                if (location == null)
                    return Symbol.SLOT_MISSING.execute(instance.getLispClass(),
                                                       instance, second,
                                                       Symbol.SLOT_VALUE);
                value = location.cdr();
            }
            if (value == UNBOUND_VALUE) {
                value = Symbol.SLOT_UNBOUND.execute(instance.getLispClass(),
                                                    instance, second);
                LispThread.currentThread()._values = null;
            }
            return value;
        }
    };

    // ### %set-std-slot-value
    private static final Primitive _SET_STD_SLOT_VALUE =
        new Primitive("%set-std-slot-value", PACKAGE_SYS, true,
                      "instance slot-name new-value")
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            StandardObject instance;
            try {
                instance = (StandardObject) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STANDARD_OBJECT));
            }
            Layout layout = instance.layout;
            if (layout.isInvalid()) {
                // Update instance.
                layout = instance.updateLayout();
            }
            int index = layout.getSlotIndex(second);
            if (index >= 0) {
                // Found instance slot.
                instance.slots[index] = third;
                return third;
            }
            // Check for class slot.
            LispObject location = layout.getClassSlotLocation(second);
            if (location != null) {
                location.setCdr(third);
                return third;
            }
            LispObject[] args = new LispObject[5];
            args[0] = instance.getLispClass();
            args[1] = instance;
            args[2] = second;
            args[3] = Symbol.SETF;
            args[4] = third;
            Symbol.SLOT_MISSING.execute(args);
            return third;
        }
    };
}
