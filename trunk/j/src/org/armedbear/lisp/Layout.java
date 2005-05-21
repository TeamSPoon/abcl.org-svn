/*
 * Layout.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: Layout.java,v 1.16 2005-05-21 15:46:31 piso Exp $
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

public final class Layout extends LispObject
{
    private boolean invalid;

    private final LispClass cls;
    private final LispObject[] slotNames;
    private final LispObject classSlots;

    public Layout(LispClass cls, LispObject instanceSlots, LispObject classSlots)
    {
        this.cls = cls;
        Debug.assertTrue(instanceSlots.listp());
        int length = 0;
        try {
            length = instanceSlots.length();
        }
        catch (Throwable t) {
            // Shouldn't happen.
            Debug.trace(t);
        }
        slotNames = new LispObject[length];
        int i = 0;
        try {
            while (instanceSlots != NIL) {
                slotNames[i++] = instanceSlots.car();
                instanceSlots = instanceSlots.cdr();
            }
        }
        catch (Throwable t) {
            // Shouldn't happen.
            Debug.trace(t);
        }
        Debug.assertTrue(i == length);
        this.classSlots = classSlots;
    }

    public Layout(LispClass cls, LispObject[] instanceSlotNames,
                  LispObject classSlots)
    {
        this.cls = cls;
        this.slotNames = instanceSlotNames;
        this.classSlots = classSlots;
    }

    // Copy constructor.
    private Layout(Layout oldLayout)
    {
        cls = oldLayout.cls;
        slotNames = oldLayout.slotNames;
        classSlots = oldLayout.classSlots;
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject result = NIL;
        result = result.push(new Cons("class", cls));
        for (int i = 0; i < slotNames.length; i++) {
            result = result.push(new Cons("slot " + i, slotNames[i]));
        }
        result = result.push(new Cons("class slots", classSlots));
        return result.nreverse();
    }

    public boolean isInvalid()
    {
        return invalid;
    }

    public void invalidate()
    {
        invalid = true;
    }

    public LispClass getLispClass()
    {
        return cls;
    }

    public LispObject[] getSlotNames()
    {
        return slotNames;
    }

    public int getLength()
    {
        return slotNames.length;
    }

    public LispObject getClassSlots()
    {
        return classSlots;
    }

    public String writeToString()
    {
        return unreadableString("LAYOUT");
    }

    // ### make-layout
    private static final Primitive MAKE_LAYOUT =
        new Primitive("make-layout", PACKAGE_SYS, true,
                      "class instance-slots class-slots")
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                return new Layout((LispClass)first, checkList(second),
                                  checkList(third));
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.CLASS));
            }
        }

    };

    // ### layout-class
    private static final Primitive LAYOUT_CLASS =
        new Primitive("layout-class", PACKAGE_SYS, true, "layout")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((Layout)arg).cls;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, "layout"));
            }
        }
    };

    // ### layout-length
    private static final Primitive LAYOUT_LENGTH =
        new Primitive("layout-length", PACKAGE_SYS, true, "layout")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return new Fixnum(((Layout)arg).slotNames.length);
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, "layout"));
            }
        }
    };

    public int getSlotIndex(LispObject slotName)
    {
        for (int i = slotNames.length; i-- > 0;) {
            if (slotNames[i] == slotName)
                return i;
        }
        return -1;
    }

    public LispObject getClassSlotLocation(LispObject slotName)
        throws ConditionThrowable
    {
        LispObject rest = classSlots;
        while (rest != NIL) {
            LispObject location = rest.car();
            if (location.car() == slotName)
                return location;
            rest = rest.cdr();
        }
        return null;
    }

    // ### layout-slot-index layout slot-name => index
    private static final Primitive LAYOUT_SLOT_INDEX =
        new Primitive("layout-slot-index", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                final LispObject slotNames[] = ((Layout)first).slotNames;
                for (int i = slotNames.length; i-- > 0;) {
                    if (slotNames[i] == second)
                        return new Fixnum(i);
                }
                return NIL;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, "layout"));
            }
        }
    };

    // ### layout-slot-location layout slot-name => location
    private static final Primitive LAYOUT_SLOT_LOCATION =
        new Primitive("layout-slot-location", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                final LispObject slotNames[] = ((Layout)first).slotNames;
                final int limit = slotNames.length;
                for (int i = 0; i < limit; i++) {
                    if (slotNames[i] == second)
                        return new Fixnum(i);
                }
                // Reaching here, it's not an instance slot.
                LispObject rest = ((Layout)first).classSlots;
                while (rest != NIL) {
                    LispObject location = rest.car();
                    if (location.car() == second)
                        return location;
                    rest = rest.cdr();
                }
                return NIL;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, "layout"));
            }
        }
    };

    // ### %make-instances-obsolete class => class
    private static final Primitive _MAKE_INSTANCES_OBSOLETE =
        new Primitive("%make-instances-obsolete", PACKAGE_SYS, true, "class")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            final LispClass cls;
            try {
                cls = (LispClass) arg;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, Symbol.CLASS));
            }
            Layout oldLayout = cls.getClassLayout();
            Layout newLayout = new Layout(oldLayout);
            cls.setClassLayout(newLayout);
            oldLayout.invalidate();
            return arg;
        }
    };
}
