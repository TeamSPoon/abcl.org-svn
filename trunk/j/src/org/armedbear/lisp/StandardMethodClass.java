/*
 * StandardMethodClass.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: StandardMethodClass.java,v 1.1 2005-05-20 18:25:24 piso Exp $
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

public final class StandardMethodClass extends StandardClass
{
    public static final int SLOT_INDEX_GENERIC_FUNCTION = 0;
    public static final int SLOT_INDEX_LAMBDA_LIST      = 1;
    public static final int SLOT_INDEX_SPECIALIZERS     = 2;
    public static final int SLOT_INDEX_QUALIFIERS       = 3;
    public static final int SLOT_INDEX_FUNCTION         = 4;
    public static final int SLOT_INDEX_FAST_FUNCTION    = 5;
    public static final int SLOT_INDEX_DOCUMENTATION    = 6;

    public StandardMethodClass()
    {
        super(Symbol.STANDARD_METHOD, list1(BuiltInClass.METHOD));
        LispObject slotNames = list7(Symbol.GENERIC_FUNCTION,
                                     PACKAGE_SYS.addInternalSymbol("LAMBDA-LIST"),
                                     PACKAGE_SYS.addInternalSymbol("SPECIALIZERS"),
                                     PACKAGE_SYS.addInternalSymbol("QUALIFIERS"),
                                     Symbol.FUNCTION,
                                     PACKAGE_SYS.addInternalSymbol("FAST-FUNCTION"),
                                     Symbol.DOCUMENTATION);
        setClassLayout(new Layout(this, slotNames, NIL));
    }
}
