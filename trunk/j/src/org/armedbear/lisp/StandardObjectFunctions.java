/*
 * StandardObjectFunctions.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: StandardObjectFunctions.java,v 1.8 2005-05-21 15:52:55 piso Exp $
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

public class StandardObjectFunctions extends Lisp
{
    // ### allocate-std-instance class => instance
    private static final Primitive ALLOCATE_STD_INSTANCE =
        new Primitive("allocate-std-instance", PACKAGE_SYS, true, "class")
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            if (arg == BuiltInClass.STANDARD_CLASS)
                return new StandardClass();
            if (arg instanceof LispClass) {
                LispClass cls = (LispClass) arg;
                Layout layout = cls.getClassLayout();
                if (layout == null)
                    return signal(new LispError("No layout for " + arg.writeToString()));
                int length = layout.getLength();
                Symbol symbol = cls.getSymbol();
                if (symbol == Symbol.STANDARD_GENERIC_FUNCTION)
                    return new StandardGenericFunction();
                if (symbol == Symbol.STANDARD_METHOD)
                    return new StandardMethod();
                LispObject cpl = cls.getCPL();
                while (cpl != NIL) {
                    LispObject obj = cpl.car();
                    if (obj == BuiltInClass.CONDITION)
                        return new Condition(cls, length);
                    cpl = cpl.cdr();
                }
                return new StandardObject(cls, length);
            }
            return signal(new TypeError(arg, Symbol.CLASS));
        }
    };
}
