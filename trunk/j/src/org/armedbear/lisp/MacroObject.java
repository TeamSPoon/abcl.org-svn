/*
 * MacroObject.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: MacroObject.java,v 1.5 2003-10-25 20:53:16 piso Exp $
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

public final class MacroObject extends Functional
{
    private LispObject expander;

    public MacroObject(LispObject expander)
    {
        this.expander = expander;
    }

    public int getFunctionalType()
    {
        return FTYPE_MACRO;
    }

    public LispObject getExpander()
    {
        return expander;
    }

    public String toString()
    {
        return unreadableString("MACRO-OBJECT");
    }
}
