/*
 * cell_error_name.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: cell_error_name.java,v 1.1 2003-09-21 02:00:21 piso Exp $
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

import java.math.BigInteger;

public final class cell_error_name extends Primitive1
{
    private cell_error_name(String name)
    {
        super(name);
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        if (arg instanceof CellError)
            return ((CellError)arg).getCellName();
        throw new ConditionThrowable(new TypeError(arg, "CELL-ERROR"));
    }

    private static final cell_error_name CELL_ERROR_NAME =
        new cell_error_name("cell-error-name");
}
