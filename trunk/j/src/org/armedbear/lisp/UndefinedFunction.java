/*
 * UndefinedFunction.java
 *
 * Copyright (C) 2002-2004 Peter Graves
 * $Id: UndefinedFunction.java,v 1.9 2004-04-24 12:46:17 piso Exp $
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

public final class UndefinedFunction extends CellError
{
    // obj is either the undefined function itself or an initArgs list.
    public UndefinedFunction(LispObject obj) throws ConditionThrowable
    {
        super(obj instanceof Cons ? obj : list2(Keyword.NAME, obj));
    }

    public LispObject typeOf()
    {
        return Symbol.UNDEFINED_FUNCTION;
    }

    public LispClass classOf()
    {
        return BuiltInClass.UNDEFINED_FUNCTION;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.UNDEFINED_FUNCTION)
            return T;
        if (type == BuiltInClass.UNDEFINED_FUNCTION)
            return T;
        return super.typep(type);
    }

    public String getMessage()
    {
        LispObject cellName = getCellName();
        StringBuffer sb = new StringBuffer("The function ");
        // FIXME
        try {
            sb.append(cellName.writeToString());
        }
        catch (Throwable t) {}
        sb.append(" is undefined.");
        return sb.toString();
    }
}
