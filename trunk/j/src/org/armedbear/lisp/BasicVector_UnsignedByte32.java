/*
 * BasicVector_UnsignedByte32.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: BasicVector_UnsignedByte32.java,v 1.1 2005-03-23 18:16:53 piso Exp $
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

// A basic vector is a specialized vector that is not displaced to another
// array, has no fill pointer, and is not expressly adjustable.
public final class BasicVector_UnsignedByte32 extends AbstractVector
{
    private int capacity;

    // FIXME We should really use an array of unboxed values!
    private LispObject[] elements;

    public BasicVector_UnsignedByte32(int capacity)
    {
        elements = new LispObject[capacity];
        for (int i = capacity; i-- > 0;)
            elements[i] = Fixnum.ZERO;
        this.capacity = capacity;
    }

    public BasicVector_UnsignedByte32(LispObject obj) throws ConditionThrowable
    {
        if (obj.listp()) {
            elements = obj.copyToArray();
            capacity = elements.length;
        } else if (obj instanceof AbstractVector) {
            capacity = obj.length();
            elements = new LispObject[capacity];
            for (int i = 0; i < capacity; i++)
                elements[i] = obj.elt(i);
        } else
            Debug.assertTrue(false);
    }

    public BasicVector_UnsignedByte32(LispObject[] array)
    {
        elements = array;
        capacity = array.length;
    }

    public LispObject typeOf()
    {
        return list3(Symbol.SIMPLE_ARRAY, UNSIGNED_BYTE_32,
                     new Cons(new Fixnum(capacity)));
    }

    public LispObject classOf()
    {
        return BuiltInClass.VECTOR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.SIMPLE_ARRAY)
            return T;
        if (type == BuiltInClass.SIMPLE_ARRAY)
            return T;
        return super.typep(type);
    }

    public LispObject getElementType()
    {
        return UNSIGNED_BYTE_32;
    }

    public boolean isSimpleVector()
    {
        return false;
    }

    public boolean hasFillPointer()
    {
        return false;
    }

    public boolean isAdjustable()
    {
        return false;
    }

    public int capacity()
    {
        return capacity;
    }

    public int length()
    {
        return capacity;
    }

    public LispObject elt(int index) throws ConditionThrowable
    {
        try {
            return elements[index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    // Ignores fill pointer.
    public LispObject AREF(int index) throws ConditionThrowable
    {
        try {
            return elements[index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, elements.length);
            return NIL; // Not reached.
        }
    }

    // Ignores fill pointer.
    public LispObject AREF(LispObject index) throws ConditionThrowable
    {
        try {
            return elements[((Fixnum)index).value];
        }
        catch (ClassCastException e) {
            return signal(new TypeError(index, Symbol.FIXNUM));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(Fixnum.getValue(index), elements.length);
            return NIL; // Not reached.
        }
    }

    public LispObject getRowMajor(int index) throws ConditionThrowable
    {
        try {
            return elements[index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    public void setRowMajor(int index, LispObject newValue) throws ConditionThrowable
    {
        try {
            elements[index] = newValue;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
        }
    }

    public LispObject subseq(int start, int end) throws ConditionThrowable
    {
        BasicVector_UnsignedByte32 v = new BasicVector_UnsignedByte32(end - start);
        int i = start, j = 0;
        try {
            while (i < end)
                v.elements[j++] = elements[i++];
            return v;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return signal(new TypeError("Array index out of bounds: " + i + "."));
        }
    }

    public void fill(LispObject obj) throws ConditionThrowable
    {
        for (int i = capacity; i-- > 0;)
            elements[i] = obj;
    }

    public void shrink(int n) throws ConditionThrowable
    {
        if (n < capacity) {
            LispObject[] newArray = new LispObject[n];
            System.arraycopy(elements, 0, newArray, 0, n);
            elements = newArray;
            capacity = n;
            return;
        }
        if (n == capacity)
            return;
        signal(new LispError());
    }

    public LispObject reverse() throws ConditionThrowable
    {
        BasicVector_UnsignedByte32 result = new BasicVector_UnsignedByte32(capacity);
        int i, j;
        for (i = 0, j = capacity - 1; i < capacity; i++, j--)
            result.elements[i] = elements[j];
        return result;
    }

    public LispObject nreverse() throws ConditionThrowable
    {
        int i = 0;
        int j = capacity - 1;
        while (i < j) {
            LispObject temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
            ++i;
            --j;
        }
        return this;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       LispObject initialElement,
                                       LispObject initialContents)
        throws ConditionThrowable
    {
        if (initialContents != NIL) {
            LispObject[] newElements = new LispObject[newCapacity];
            if (initialContents.listp()) {
                LispObject list = initialContents;
                for (int i = 0; i < newCapacity; i++) {
                    newElements[i] = list.car();
                    list = list.cdr();
                }
            } else if (initialContents.vectorp()) {
                for (int i = 0; i < newCapacity; i++)
                    newElements[i] = initialContents.elt(i);
            } else
                signal(new TypeError(initialContents, Symbol.SEQUENCE));
            return new BasicVector_UnsignedByte32(newElements);
        }
        if (capacity != newCapacity) {
            LispObject[] newElements = new LispObject[newCapacity];
            System.arraycopy(elements, 0, newElements, 0,
                             Math.min(capacity, newCapacity));
            for (int i = capacity; i < newCapacity; i++)
                newElements[i] = initialElement;
            return new BasicVector_UnsignedByte32(newElements);
        }
        // No change.
        return this;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       AbstractArray displacedTo,
                                       int displacement)
    {
        return new ComplexVector(newCapacity, displacedTo, displacement);
    }
}
