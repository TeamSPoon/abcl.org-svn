/*
 * Cons.java
 *
 * Copyright (C) 2002-2003 Peter Graves
 * $Id: Cons.java,v 1.31 2003-11-14 00:53:06 piso Exp $
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

public final class Cons extends LispObject
{
    private LispObject car;
    private LispObject cdr;

    public Cons(LispObject car, LispObject cdr)
    {
        this.car = car;
        this.cdr = cdr;
        ++count;
    }

    public Cons(LispObject car)
    {
        this.car = car;
        this.cdr = NIL;
        ++count;
    }

    public LispObject typeOf()
    {
        return Symbol.CONS;
    }

    public LispClass classOf()
    {
        return BuiltInClass.CONS;
    }

    public LispObject typep(LispObject typeSpecifier) throws ConditionThrowable
    {
        if (typeSpecifier == Symbol.LIST)
            return T;
        if (typeSpecifier == Symbol.CONS)
            return T;
        if (typeSpecifier == Symbol.SEQUENCE)
            return T;
        if (typeSpecifier == BuiltInClass.LIST)
            return T;
        if (typeSpecifier == BuiltInClass.CONS)
            return T;
        if (typeSpecifier == BuiltInClass.SEQUENCE)
            return T;
        if (typeSpecifier == Symbol.ATOM)
            return NIL;
        return super.typep(typeSpecifier);
    }

    public final LispObject CONSTANTP()
    {
        if (car == Symbol.QUOTE) {
            if (cdr instanceof Cons)
                if (((Cons)cdr).cdr == NIL)
                    return T;
        }
        return NIL;
    }

    public LispObject ATOM()
    {
        return NIL;
    }

    public boolean atom()
    {
        return false;
    }

    public final LispObject car()
    {
        return car;
    }

    public final LispObject cdr()
    {
        return cdr;
    }

    public final void setCar(LispObject car)
    {
        this.car = car;
    }

    public final void setCdr(LispObject cdr)
    {
        this.cdr = cdr;
    }

    public final LispObject cadr() throws ConditionThrowable
    {
        return cdr.car();
    }

    public final LispObject cddr() throws ConditionThrowable
    {
        return cdr.cdr();
    }

    public final int hashCode()
    {
        return car.hashCode() ^ cdr.hashCode();
    }

    public final boolean equal(LispObject obj) throws ConditionThrowable
    {
        if (this == obj)
            return true;
        if (obj instanceof Cons) {
            if (car.equal(((Cons)obj).car) && cdr.equal(((Cons)obj).cdr))
                return true;
        }
        return false;
    }

    public final boolean equalp(LispObject obj) throws ConditionThrowable
    {
        if (this == obj)
            return true;
        if (obj instanceof Cons) {
            if (car.equalp(((Cons)obj).car) && cdr.equalp(((Cons)obj).cdr))
                return true;
        }
        return false;
    }

    public final int length() throws ConditionThrowable
    {
        int length = 0;
        LispObject obj = this;
        try {
            while (obj != NIL) {
                ++length;
                obj = ((Cons)obj).cdr;
            }
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "list"));
        }
        return length;
    }

    public LispObject elt(int index) throws ConditionThrowable
    {
        if (index < 0) {
            throw new ConditionThrowable(new TypeError("ELT: invalid index " + index + " for " +
                                                       this));
        }
        int i = 0;
        Cons cons = this;
        try {
            while (true) {
                if (i == index)
                    return cons.car;
                cons = (Cons) cons.cdr;
                ++i;
            }
        }
        catch (ClassCastException e) {
            if (cons.cdr == NIL)
                throw new ConditionThrowable(new TypeError("ELT: invalid index " + index + " for " +
                                                           this));
            else
                throw new ConditionThrowable(new TypeError(this, "proper sequence"));
        }
    }

    public final LispObject nreverse() throws ConditionThrowable
    {
        // Following code is from CLISP.
        LispObject list3 = cdr;
        if (list3 instanceof Cons) {
            if (list3.cdr() instanceof Cons) {
                LispObject list1 = list3;
                LispObject list2 = NIL;
                do {
                    LispObject h = list3.cdr();
                    list3.setCdr(list2);
                    list2 = list3;
                    list3 = h;
                } while (list3.cdr() instanceof Cons);
                cdr = list2;
                list1.setCdr(list3);
            }
            LispObject h = this.car();
            car = list3.car();
            list3.setCar(h);
        }
        return this;
    }

    public final boolean listp()
    {
        return true;
    }

    public final LispObject LISTP()
    {
        return T;
    }

    public final LispObject ENDP()
    {
        return NIL;
    }

    public LispObject remove(LispObject item) throws ConditionThrowable
    {
        LispObject result = NIL;
        LispObject splice = null;
        for (LispObject list = this; list != NIL; list = list.cdr()) {
            LispObject obj = list.car();
            if (!obj.eql(item)) {
                if (splice == null) {
                    splice = new Cons(obj);
                    result = splice;
                } else {
                    Cons temp = new Cons(obj);
                    splice.setCdr(temp);
                    splice = temp;
                }
            }
        }
        return result;
    }

    public final LispObject[] copyToArray() throws ConditionThrowable
    {
        final int length = length();
        LispObject[] array = new LispObject[length];
        LispObject rest = this;
        for (int i = 0; i < length; i++) {
            array[i] = rest.car();
            rest = rest.cdr();
        }
        return array;
    }

    public String toString()
    {
        try {
            StringBuffer sb = new StringBuffer();
            if (car == Symbol.QUOTE) {
                if (cdr instanceof Cons) {
                    // Not a dotted list.
                    if (cdr.cdr() == NIL) {
                        sb.append('\'');
                        sb.append(cdr.car());
                        return sb.toString();
                    }
                }
            }
            if (car == Symbol.FUNCTION) {
                if (cdr instanceof Cons) {
                    // Not a dotted list.
                    if (cdr.cdr() == NIL) {
                        sb.append("#'");
                        sb.append(cdr.car());
                        return sb.toString();
                    }
                }
            }
            sb.append('(');
            LispObject p = this;
            sb.append(p.car());
            while ((p = p.cdr()) instanceof Cons) {
                sb.append(' ');
                sb.append(p.car());
            }
            if (p != NIL) {
                sb.append(" . ");
                sb.append(p);
            }
            sb.append(')');
            return sb.toString();
        }
        catch (Throwable t) {
            Debug.trace(t);
            return "";
        }
    }

    // Statistics for TIME.
    private static long count;

    /*package*/ static long getCount()
    {
        return count;
    }

    /*package*/ static void setCount(long n)
    {
        count = n;
    }
}
