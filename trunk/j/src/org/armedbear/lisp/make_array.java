/*
 * make_array.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: make_array.java,v 1.1 2003-09-14 11:35:44 piso Exp $
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

// ### %make-array dimensions element-type initial-element initial-contents
// adjustable fill-pointer displaced-to displaced-index-offset => new-array
public final class make_array extends Primitive {
    public make_array(String name, Package pkg, boolean exported)
    {
        super(name, pkg, exported);
    }

    public LispObject execute(LispObject[] args) throws LispError
    {
        if (args.length != 9)
            throw new WrongNumberOfArgumentsException(this);
        LispObject dimensions = args[0];
        LispObject elementType = args[1];
        LispObject initialElement = args[2];
        LispObject initialElementProvided = args[3];
        LispObject initialContents = args[4];
        LispObject adjustable = args[5];
        LispObject fillPointer = args[6];
        LispObject displacedTo = args[7];
        LispObject displacedIndexOffset = args[8];
        if (initialElementProvided != NIL && initialContents != NIL) {
            throw new LispError("MAKE-ARRAY: cannot specify both " +
                                ":INITIAL-ELEMENT AND :INITIAL-CONTENTS");
        }
        final int rank = dimensions.listp() ? dimensions.length() : 1;
        if (displacedTo != NIL) {
            final AbstractArray array = checkArray(displacedTo);
            final int offset;
            if (displacedIndexOffset != NIL)
                offset = Fixnum.getValue(displacedIndexOffset);
            else
                offset = 0;
            if (initialElementProvided != NIL)
                throw new LispError(":INITIAL-ELEMENT must not be specified with :DISPLACED-TO");
            if (initialContents != NIL)
                throw new LispError(":INITIAL-CONTENTS must not be specified with :DISPLACED-TO");
            int[] dimv = new int[rank];
            for (int i = 0; i < rank; i++) {
                LispObject dim = dimensions.car();
                dimv[i] = Fixnum.getValue(dim);
                dimensions = dimensions.cdr();
            }
            return new DisplacedArray(dimv, array, offset);
        }
        if (rank == 1) {
            final int size;
            if (dimensions instanceof Cons)
                size = Fixnum.getValue(dimensions.car());
            else
                size = Fixnum.getValue(dimensions);
            int limit =
                Fixnum.getValue(Symbol.ARRAY_DIMENSION_LIMIT.getSymbolValue());
            if (size < 0 || size >= limit) {
                StringBuffer sb = new StringBuffer();
                sb.append("the size specified for this array (");
                sb.append(size);
                sb.append(')');
                if (size >= limit) {
                    sb.append(" is >= ARRAY-DIMENSION-LIMIT (");
                    sb.append(limit);
                    sb.append(')');
                } else
                    sb.append(" is negative");
                throw new LispError(sb.toString());
            }
            AbstractVector v;
            LispObject upgradedType =
                getUpgradedArrayElementType(elementType);
            if (upgradedType == Symbol.CHARACTER)
                v = new LispString(size);
            else if (elementType == Symbol.BIT)
                v = new BitVector(size);
            else
                v = new Vector(size);
            if (initialElementProvided != NIL) {
                // Initial element was specified.
                v.fill(initialElement);
            } else if (initialContents != NIL) {
                final int type = initialContents.getType();
                if ((type & TYPE_LIST) != 0) {
                    LispObject list = initialContents;
                    for (int i = 0; i < size; i++) {
                        v.set(i, list.car());
                        list = list.cdr();
                    }
                } else if ((type & TYPE_VECTOR) != 0) {
                    for (int i = 0; i < size; i++)
                        v.set(i, initialContents.elt(i));
                } else
                    throw new TypeError(initialContents, "sequence");
            }
            if (fillPointer != NIL)
                v.setFillPointer(fillPointer);
            return v;
        }
        // rank != 1
        int[] dimv = new int[rank];
        for (int i = 0; i < rank; i++) {
            LispObject dim = dimensions.car();
            dimv[i] = Fixnum.getValue(dim);
            dimensions = dimensions.cdr();
        }
        Array array;
        if (initialContents != NIL) {
            array = new Array(dimv, initialContents);
        } else {
            array = new Array(dimv);
            if (initialElementProvided != NIL)
                array.fill(initialElement);
        }
        return array;
    }

    private static final make_array _MAKE_ARRAY =
        new make_array("%MAKE-ARRAY", PACKAGE_SYS, false);
}
