/*
 * file_string_length.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: file_string_length.java,v 1.1 2004-01-28 18:15:31 piso Exp $
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

// ### file-string-length
public final class file_string_length extends Primitive2
{
    private file_string_length()
    {
        super("file-string-length", "stream object");
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        // Ignore stream arg.
        // FIXME Unicode!
        if (second instanceof LispCharacter)
            return Fixnum.ONE;
        else if (second instanceof LispString)
            return number(second.length());
        else
            return signal(new TypeError(String.valueOf(second) +
                                        " is neither a string nor a character."));
    }

    private static final Primitive2 FILE_STRING_LENGTH =
        new file_string_length();
}
