/*
 * FloatingPointOverflow.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: FloatingPointOverflow.java,v 1.2 2004-10-13 00:22:18 piso Exp $
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

public class FloatingPointOverflow extends ArithmeticError
{
    public FloatingPointOverflow()
    {
    }

    public FloatingPointOverflow(LispObject initArgs)
        throws ConditionThrowable
    {
        super(initArgs);
    }

    public FloatingPointOverflow(String message)
    {
        super(message);
    }

    public LispObject typeOf()
    {
        return Symbol.FLOATING_POINT_OVERFLOW;
    }

    public LispObject classOf()
    {
        return BuiltInClass.FLOATING_POINT_OVERFLOW;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.FLOATING_POINT_OVERFLOW)
            return T;
        if (type == BuiltInClass.FLOATING_POINT_OVERFLOW)
            return T;
        return super.typep(type);
    }
}
