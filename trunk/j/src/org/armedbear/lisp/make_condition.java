/*
 * make_condition.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: make_condition.java,v 1.10 2003-11-02 13:58:45 piso Exp $
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

public final class make_condition extends Primitive2
{
    private make_condition()
    {
        super("%make-condition", PACKAGE_SYS, false);
    }

    // ### %make-condition
    // %make-condition type slot-initializations => condition
    public LispObject execute(LispObject type, LispObject initArgs)
        throws ConditionThrowable
    {
        if (type == Symbol.CONDITION)
            return new Condition(initArgs);
        if (type == Symbol.SIMPLE_CONDITION)
            return new SimpleCondition(initArgs);
        if (type == Symbol.ERROR)
            return new LispError(initArgs);
        if (type == Symbol.ARITHMETIC_ERROR)
            return new ArithmeticError(initArgs);
        if (type == Symbol.CELL_ERROR)
            return new CellError(initArgs);
        if (type == Symbol.CONTROL_ERROR)
            return new ControlError(initArgs);
        if (type == Symbol.DIVISION_BY_ZERO)
            return new DivisionByZero(initArgs);
        if (type == Symbol.PACKAGE_ERROR)
            return new PackageError(initArgs);
        if (type == Symbol.SIMPLE_ERROR)
            return new SimpleError(initArgs);
        if (type == Symbol.TYPE_ERROR)
            return new TypeError(initArgs);
        if (type == Symbol.SIMPLE_TYPE_ERROR)
            return new SimpleTypeError(initArgs);
        if (type == Symbol.UNBOUND_SLOT)
            return new UnboundSlot(initArgs);
        if (type == Symbol.UNBOUND_VARIABLE)
            return new UnboundVariable(initArgs);
        if (type == Symbol.UNDEFINED_FUNCTION)
            return new UndefinedFunction(initArgs);

        return NIL;
    }

    private static final make_condition MAKE_CONDITION = new make_condition();
}
