/*
 * BinaryInputStream.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: BinaryInputStream.java,v 1.6 2003-11-03 16:07:53 piso Exp $
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class BinaryInputStream extends LispInputStream
{
    private final BufferedInputStream in;

    public BinaryInputStream(InputStream inputStream)
    {
        in = new BufferedInputStream(inputStream);
    }

    // read-byte stream &optional eof-error-p eof-value => byte
    public LispObject readByte(boolean eofError, LispObject eofValue)
        throws ConditionThrowable
    {
        int n;
        try {
            n = in.read();
        }
        catch (IOException e) {
            throw new ConditionThrowable(new StreamError(e));
        }
        if (n < 0) {
            if (eofError)
                throw new ConditionThrowable(new EndOfFile());
            else
                return eofValue;
        }
        return new Fixnum(n);
    }

    // Returns true if stream was open, otherwise implementation-dependent.
    public LispObject close(LispObject abort) throws ConditionThrowable
    {
        try {
            in.close();
            return T;
        }
        catch (IOException e) {
            throw new ConditionThrowable(new StreamError(e));
        }
    }

    public String toString()
    {
        return unreadableString("STREAM [binary input]");
    }
}
