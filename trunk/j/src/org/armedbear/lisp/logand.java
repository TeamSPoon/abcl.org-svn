/*
 * logand.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: logand.java,v 1.2 2003-09-10 16:57:28 piso Exp $
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

// ### logand
// logand &rest integers => result-integer
public final class logand extends Primitive
{
    private logand(String name)
    {
        super(name);
    }

    public LispObject execute()
    {
        return new Fixnum(-1);
    }

    public LispObject execute(LispObject first, LispObject second)
        throws LispError
    {
        if (first instanceof Fixnum && second instanceof Fixnum) {
            return new Fixnum(((Fixnum)first).getValue() &
                              ((Fixnum)second).getValue());
        } else {
            BigInteger n1, n2;
            if (first instanceof Fixnum)
                n1 = ((Fixnum)first).getBigInteger();
            else if (first instanceof Bignum)
                n1 = ((Bignum)first).getValue();
            else
                throw new TypeError(first, "integer");
            if (second instanceof Fixnum)
                n2 = ((Fixnum)second).getBigInteger();
            else if (second instanceof Bignum)
                n2 = ((Bignum)second).getValue();
            else
                throw new TypeError(second, "integer");
            return number(n1.and(n2));
        }
    }
    public LispObject execute(LispObject[] args) throws LispError
    {
        BigInteger result = BigInteger.valueOf(-1);
        for (int i = 0; i < args.length; i++) {
            BigInteger n;
            if (args[i] instanceof Fixnum)
                n = ((Fixnum)args[i]).getBigInteger();
            else if (args[i] instanceof Bignum)
                n = ((Bignum)args[i]).getValue();
            else
                throw new TypeError(args[i], "integer");
            result = result.and(n);
        }
        return number(result);
    }

    private static final logand LOGAND = new logand("logand");
}
