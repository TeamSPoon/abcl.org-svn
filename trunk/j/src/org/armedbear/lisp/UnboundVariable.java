/*
 * UnboundVariable.java
 *
 * Copyright (C) 2002-2003 Peter Graves
 * $Id: UnboundVariable.java,v 1.1 2003-09-19 16:04:29 piso Exp $
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

public final class UnboundVariable extends LispError
{
    private final String name;

    public UnboundVariable(String name)
    {
        this.name = name;
    }

    public String getMessage()
    {
        if (name == null)
            return "unbound variable";
        return "the variable " + name + " has no value";
    }

    public LispObject typeOf()
    {
        return Symbol.UNBOUND_VARIABLE;
    }

    public LispClass classOf()
    {
        return LispClass.UNBOUND_VARIABLE;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.UNBOUND_VARIABLE)
            return T;
        return super.typep(type);
    }
}
