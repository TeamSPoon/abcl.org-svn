/*
 * AbstractVector.java
 *
 * Copyright (C) 2003-2004 Peter Graves
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

public abstract class AbstractVector extends AbstractArray
{
    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.VECTOR)
            return T;
        if (type == BuiltInClass.VECTOR)
            return T;
        if (type == Symbol.SEQUENCE)
            return T;
        if (type == BuiltInClass.SEQUENCE)
            return T;
        return super.typep(type);
    }

    public final LispObject VECTORP()
    {
        return T;
    }

    public final boolean vectorp()
    {
        return true;
    }

    public boolean equalp(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof AbstractVector) {
            if (length() != obj.length())
                return false;
            AbstractVector v = (AbstractVector) obj;
            for (int i = length(); i-- > 0;)
                if (!getRowMajor(i).equalp(v.getRowMajor(i)))
                    return false;
            return true;
        }
        return false;
    }

    public int getRank()
    {
        return 1;
    }

    public final LispObject getDimensions()
    {
        return new Cons(new Fixnum(capacity()));
    }

    public final int getDimension(int n) throws ConditionThrowable
    {
        if (n != 0) {
            signal(new TypeError("bad dimension for vector"));
            // Not reached.
            return 0;
        }
        return capacity();
    }

    public final int getTotalSize()
    {
        return capacity();
    }

    public abstract int capacity();

    public abstract LispObject subseq(int start, int end) throws ConditionThrowable;

    public abstract void shrink(int n) throws ConditionThrowable;

    public int checkIndex(int index) throws ConditionThrowable
    {
        if (index < 0 || index >= capacity())
            badIndex(index, capacity());
        return index;
    }

    public int checkIndex(LispObject index) throws ConditionThrowable
    {
        int i = Fixnum.getValue(index);
        if (i < 0 || i >= capacity())
            badIndex(i, capacity());
        return i;
    }

    protected void badIndex(int index, int limit) throws ConditionThrowable
    {
        StringBuffer sb = new StringBuffer("Invalid array index ");
        sb.append(index);
        sb.append(" for ");
        sb.append(toString());
        if (limit > 0) {
            sb.append(" (should be >= 0 and < ");
            sb.append(limit);
            sb.append(").");
        }
        signal(new TypeError(sb.toString()));
    }

    public void setFillPointer(int n) throws ConditionThrowable
    {
        noFillPointer();
    }

    public void setFillPointer(LispObject obj) throws ConditionThrowable
    {
        noFillPointer();
    }

    public boolean isSimpleVector()
    {
        return false;
    }

    public abstract LispObject reverse() throws ConditionThrowable;

    public LispObject nreverse() throws ConditionThrowable
    {
        int i = 0;
        int j = length() - 1;
        while (i < j) {
            LispObject temp = getRowMajor(i);
            setRowMajor(i, getRowMajor(j));
            setRowMajor(j, temp);
            ++i;
            --j;
        }
        return this;
    }

    public String writeToString() throws ConditionThrowable
    {
        StringBuffer sb = new StringBuffer("#(");
        final LispObject printLength = _PRINT_LENGTH_.symbolValue();
        final int limit;
        if (printLength instanceof Fixnum)
            limit = Math.min(length(), ((Fixnum)printLength).value);
        else
            limit = length();
        for (int i = 0; i < limit; i++) {
            if (i > 0)
                sb.append(' ');
            sb.append(getRowMajor(i).writeToString());
        }
        if (limit < length())
            sb.append(" ...");
        sb.append(')');
        return sb.toString();
    }

    public abstract AbstractVector adjustVector(int size,
                                                LispObject initialElement,
                                                LispObject initialContents)
        throws ConditionThrowable;

    public abstract AbstractVector adjustVector(int size,
                                                AbstractArray displacedTo,
                                                int displacement)
        throws ConditionThrowable;

    public LispObject vectorPushExtend(LispObject element)
        throws ConditionThrowable
    {
        return noFillPointer();
    }

    public LispObject vectorPushExtend(LispObject element, LispObject extension)
        throws ConditionThrowable
    {
        return noFillPointer();
    }
}
