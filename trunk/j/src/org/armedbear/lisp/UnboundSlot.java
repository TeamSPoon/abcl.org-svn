/*
 * UnboundSlot.java
 *
 * Copyright (C) 2003-2004 Peter Graves
 * $Id: UnboundSlot.java,v 1.4 2004-10-13 00:22:20 piso Exp $
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

public final class UnboundSlot extends CellError
{
    private LispObject instance = NIL;

    public UnboundSlot(LispObject initArgs) throws ConditionThrowable
    {
        super(initArgs);
        LispObject first, second;
        while (initArgs != NIL) {
            first = initArgs.car();
            initArgs = initArgs.cdr();
            second = initArgs.car();
            if (first == Keyword.INSTANCE) {
                instance = second;
                break;
            }
            initArgs = initArgs.cdr();
        }
    }

    public LispObject getInstance()
    {
        return instance;
    }

    public String getMessage()
    {
        StringBuffer sb = new StringBuffer("The slot ");
        sb.append(safeWriteToString(getCellName()));
        sb.append(" is unbound in the object ");
        sb.append(safeWriteToString(instance));
        sb.append('.');
        return sb.toString();
    }

    public LispObject typeOf()
    {
        return Symbol.UNBOUND_SLOT;
    }

    public LispObject classOf()
    {
        return BuiltInClass.UNBOUND_SLOT;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.UNBOUND_SLOT)
            return T;
        if (type == BuiltInClass.UNBOUND_SLOT)
            return T;
        return super.typep(type);
    }
}
