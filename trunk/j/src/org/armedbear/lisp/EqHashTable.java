/*
 * EqHashTable.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: EqHashTable.java,v 1.1 2004-06-04 16:25:15 piso Exp $
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

public class EqHashTable extends HashTable
{
    public EqHashTable(int size, LispObject rehashSize,
                        LispObject rehashThreshold)
        throws ConditionThrowable
    {
        super(TEST_EQ, size, rehashSize, rehashThreshold);
    }

    protected final boolean equals(LispObject o1, LispObject o2)
        throws ConditionThrowable
    {
        return o1 == o2;
    }
}
