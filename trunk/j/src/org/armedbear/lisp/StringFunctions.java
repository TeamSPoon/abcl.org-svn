/*
 * StringFunctions.java
 *
 * Copyright (C) 2003-2004 Peter Graves
 * $Id: StringFunctions.java,v 1.32 2004-11-28 15:43:50 piso Exp $
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

public final class StringFunctions extends Lisp
{
    // ### %string=
    // Case sensitive.
    private static final Primitive _STRING_EQUAL =
        new Primitive("%string=", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1, end1, start2, end2;
            try {
                start1 = ((Fixnum)args[2]).value;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(args[2], Symbol.FIXNUM));
            }
            if (args[3] == NIL) {
                end1 = array1.length;
            } else {
                try {
                    end1 = ((Fixnum)args[3]).value;
                }
                catch (ClassCastException e) {
                    return signal(new TypeError(args[3], Symbol.FIXNUM));
                }
            }
            try {
                start2 = ((Fixnum)args[4]).value;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(args[4], Symbol.FIXNUM));
            }
            if (args[5] == NIL) {
                end2 = array2.length;
            } else {
                try {
                    end2 = ((Fixnum)args[5]).value;
                }
                catch (ClassCastException e) {
                    return signal(new TypeError(args[5], Symbol.FIXNUM));
                }
            }
            if ((end1 - start1) != (end2 - start2))
                return NIL;
            try {
                for (int i = start1, j = start2; i < end1; i++, j++) {
                    if (array1[i] != array2[j])
                        return NIL;
                }
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // Shouldn't happen.
                Debug.trace(e);
                return NIL;
            }
            return T;
        }
    };

    // ### %%string=
    // Case sensitive.
    private static final Primitive __STRING_EQUAL =
        new Primitive("%%string=", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char[] array1 = first.STRING().getStringChars();
            char[] array2 = second.STRING().getStringChars();
            if (array1.length != array2.length)
                return NIL;
            for (int i = array1.length; i-- > 0;) {
                if (array1[i] != array2[i])
                    return NIL;
            }
            return T;
        }
    };

    // ### %string/=
    // Case sensitive.
    private static final Primitive _STRING_NOT_EQUAL =
        new Primitive("%string/=", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    if (j == end2)
                        return NIL; // Strings are identical.
                    return new Fixnum(i);
                }
                if (j == end2) {
                    // Reached end of string2 before end of string1.
                    return new Fixnum(i);
                }
                if (array1[i] != array2[j])
                    return new Fixnum(i);
                ++i;
                ++j;
            }
        }
    };

    // ### %string-equal
    // Case insensitive.
    private static final Primitive _STRING_EQUAL_IGNORE_CASE =
        new Primitive("%string-equal", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            if ((end1 - start1) != (end2 - start2))
                return NIL;
            int i, j;
            for (i = start1, j = start2; i < end1; i++, j++) {
                char c1 = array1[i];
                char c2 = array2[j];
                if (c1 == c2)
                    continue;
                if (Utilities.toUpperCase(c1) == Utilities.toUpperCase(c2))
                    continue;
                if (Utilities.toLowerCase(c1) == Utilities.toLowerCase(c2))
                    continue;
                return NIL;
            }
            return T;
        }
    };

    // ### %string-not-equal
    // Case sensitive.
    private static final Primitive _STRING_NOT_EQUAL_IGNORE_CASE =
        new Primitive("%string-not-equal", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    if (j == end2)
                        return NIL; // Strings are identical.
                    return new Fixnum(i);
                }
                if (j == end2) {
                    // Reached end of string2.
                    return new Fixnum(i);
                }
                char c1 = array1[i];
                char c2 = array2[j];
                if (c1 == c2 ||
                    Utilities.toUpperCase(c1) == Utilities.toUpperCase(c2) ||
                    Utilities.toLowerCase(c1) == Utilities.toLowerCase(c2))
                {
                    ++i;
                    ++j;
                    continue;
                }
                return new Fixnum(i);
            }
        }
    };

    // ### %string<
    // Case sensitive.
    private static final Primitive _STRING_LESS_THAN =
        new Primitive("%string<", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    if (j == end2)
                        return NIL; // Strings are identical.
                    return new Fixnum(i);
                }
                if (j == end2) {
                    // Reached end of string2.
                    return NIL;
                }
                char c1 = array1[i];
                char c2 = array2[j];
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 < c2)
                    return new Fixnum(i);
                // c1 > c2
                return NIL;
            }
        }
    };

    // ### %string<=
    // Case sensitive.
    private static final Primitive _STRING_GREATER_THAN =
        new Primitive("%string>", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    return NIL;
                }
                if (j == end2) {
                    // Reached end of string2.
                    return new Fixnum(i);
                }
                char c1 = array1[i];
                char c2 = array2[j];
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 < c2)
                    return NIL;
                // c1 > c2
                return new Fixnum(i);
            }
        }
    };

    // ### %string<=
    // Case sensitive.
    private static final Primitive _STRING_LE =
        new Primitive("%string<=", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    return new Fixnum(i);
                }
                if (j == end2) {
                    // Reached end of string2.
                    return NIL;
                }
                char c1 = array1[i];
                char c2 = array2[j];
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 > c2)
                    return NIL;
                // c1 < c2
                return new Fixnum(i);
            }
        }
    };

    // ### %string<=
    // Case sensitive.
    private static final Primitive _STRING_GE =
        new Primitive("%string>=", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    if (j == end2)
                        return new Fixnum(i); // Strings are identical.
                    return NIL;
                }
                if (j == end2) {
                    // Reached end of string2.
                    return new Fixnum(i);
                }
                char c1 = array1[i];
                char c2 = array2[j];
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 < c2)
                    return NIL;
                // c1 > c2
                return new Fixnum(i);
            }
        }
    };

    // ### %string-lessp
    // Case insensitive.
    private static final Primitive _STRING_LESSP =
        new Primitive("%string-lessp", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    if (j == end2)
                        return NIL; // Strings are identical.
                    return new Fixnum(i);
                }
                if (j == end2) {
                    // Reached end of string2.
                    return NIL;
                }
                char c1 = Utilities.toUpperCase(array1[i]);
                char c2 = Utilities.toUpperCase(array2[j]);
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 > c2)
                    return NIL;
                // c1 < c2
                return new Fixnum(i);
            }
        }
    };

    // ### %string-greaterp
    // Case insensitive.
    private static final Primitive _STRING_GREATERP =
        new Primitive("%string-greaterp", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    return NIL;
                }
                if (j == end2) {
                    // Reached end of string2.
                    return new Fixnum(i);
                }
                char c1 = Utilities.toUpperCase(array1[i]);
                char c2 = Utilities.toUpperCase(array2[j]);
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 < c2)
                    return NIL;
                // c1 > c2
                return new Fixnum(i);
            }
        }
    };

    // ### %string-not-lessp
    // Case insensitive.
    private static final Primitive _STRING_NOT_LESSP =
        new Primitive("%string-not-lessp", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    if (j == end2)
                        return new Fixnum(i); // Strings are identical.
                    return NIL;
                }
                if (j == end2) {
                    // Reached end of string2.
                    return new Fixnum(i);
                }
                char c1 = Utilities.toUpperCase(array1[i]);
                char c2 = Utilities.toUpperCase(array2[j]);
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 > c2)
                    return new Fixnum(i);
                // c1 < c2
                return NIL;
            }
        }
    };

    // ### %string-not-greaterp
    // Case insensitive.
    private static final Primitive _STRING_NOT_GREATERP =
        new Primitive("%string-not-greaterp", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 6)
                return signal(new WrongNumberOfArgumentsException(this));
            char[] array1 = args[0].STRING().getStringChars();
            char[] array2 = args[1].STRING().getStringChars();
            int start1 = Fixnum.getInt(args[2]);
            int end1 = Fixnum.getInt(args[3]);
            int start2 = Fixnum.getInt(args[4]);
            int end2 = Fixnum.getInt(args[5]);
            int i = start1;
            int j = start2;
            while (true) {
                if (i == end1) {
                    // Reached end of string1.
                    return new Fixnum(i);
                }
                if (j == end2) {
                    // Reached end of string2.
                    return NIL;
                }
                char c1 = Utilities.toUpperCase(array1[i]);
                char c2 = Utilities.toUpperCase(array2[j]);
                if (c1 == c2) {
                    ++i;
                    ++j;
                    continue;
                }
                if (c1 > c2)
                    return NIL;
                // c1 < c2
                return new Fixnum(i);
            }
        }
    };

    // ### %string-upcase
    private static final Primitive _STRING_UPCASE =
        new Primitive("%string-upcase", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            LispObject s = first.STRING();
            final int length = s.length();
            int start = (int) Fixnum.getValue(second);
            if (start < 0 || start > length)
                return signal(new TypeError("Invalid start position " + start + "."));
            int end;
            if (third == NIL)
                end = length;
            else
                end = (int) Fixnum.getValue(third);
            if (end < 0 || end > length)
                return signal(new TypeError("Invalid end position " + start + "."));
            if (start > end)
                return signal(new TypeError("Start (" + start + ") is greater than end (" + end + ")."));
            StringBuffer sb = new StringBuffer(length);
            char[] array = s.getStringChars();
            int i;
            for (i = 0; i < start; i++)
                sb.append(array[i]);
            for (i = start; i < end; i++)
                sb.append(Utilities.toUpperCase(array[i]));
            for (i = end; i < length; i++)
                sb.append(array[i]);
            return new SimpleString(sb);
        }
    };

    // ### %string-downcase
    private static final Primitive _STRING_DOWNCASE =
        new Primitive("%string-downcase", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            LispObject s = first.STRING();
            final int length = s.length();
            int start = (int) Fixnum.getValue(second);
            if (start < 0 || start > length)
                return signal(new TypeError("Invalid start position " + start + "."));
            int end;
            if (third == NIL)
                end = length;
            else
                end = (int) Fixnum.getValue(third);
            if (end < 0 || end > length)
                return signal(new TypeError("Invalid end position " + start + "."));
            if (start > end)
                return signal(new TypeError("Start (" + start + ") is greater than end (" + end + ")."));
            StringBuffer sb = new StringBuffer(length);
            char[] array = s.getStringChars();
            int i;
            for (i = 0; i < start; i++)
                sb.append(array[i]);
            for (i = start; i < end; i++)
                sb.append(Utilities.toLowerCase(array[i]));
            for (i = end; i < length; i++)
                sb.append(array[i]);
            return new SimpleString(sb);
        }
    };

    // ### %string-capitalize
    private static final Primitive _STRING_CAPITALIZE=
        new Primitive("%string-capitalize", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            LispObject s = first.STRING();
            final int length = s.length();
            int start = (int) Fixnum.getValue(second);
            if (start < 0 || start > length)
                return signal(new TypeError("Invalid start position " + start + "."));
            int end;
            if (third == NIL)
                end = length;
            else
                end = (int) Fixnum.getValue(third);
            if (end < 0 || end > length)
                return signal(new TypeError("Invalid end position " + start + "."));
            if (start > end)
                return signal(new TypeError("Start (" + start + ") is greater than end (" + end + ")."));
            StringBuffer sb = new StringBuffer(length);
            char[] array = s.getStringChars();
            boolean lastCharWasAlphanumeric = false;
            int i;
            for (i = 0; i < start; i++)
                sb.append(array[i]);
            for (i = start; i < end; i++) {
                char c = array[i];
                if (Character.isLowerCase(c)) {
                    sb.append(lastCharWasAlphanumeric ? c : Utilities.toUpperCase(c));
                    lastCharWasAlphanumeric = true;
                } else if (Character.isUpperCase(c)) {
                    sb.append(lastCharWasAlphanumeric ? Utilities.toLowerCase(c) : c);
                    lastCharWasAlphanumeric = true;
                } else {
                    sb.append(c);
                    lastCharWasAlphanumeric = Character.isDigit(c);
                }
            }
            for (i = end; i < length; i++)
                sb.append(array[i]);
            return new SimpleString(sb);
        }
    };

    // ### %nstring-upcase
    private static final Primitive _NSTRING_UPCASE =
        new Primitive("%nstring-upcase", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            AbstractString string;
            try {
                string = (AbstractString) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STRING));
            }
            final int length = string.length();
            int start = (int) Fixnum.getValue(second);
            if (start < 0 || start > length)
                return signal(new TypeError("Invalid start position " + start + "."));
            int end;
            if (third == NIL)
                end = length;
            else
                end = (int) Fixnum.getValue(third);
            if (end < 0 || end > length)
                return signal(new TypeError("Invalid end position " + start + "."));
            if (start > end)
                return signal(new TypeError("Start (" + start + ") is greater than end (" + end + ")."));
            for (int i = start; i < end; i++)
                string.setCharAt(i, Utilities.toUpperCase(string.charAt(i)));
            return string;
        }
    };

    // ### %nstring-downcase
    private static final Primitive _NSTRING_DOWNCASE =
        new Primitive("%nstring-downcase", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            AbstractString string;
            try {
                string = (AbstractString) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STRING));
            }
            final int length = string.length();
            int start = (int) Fixnum.getValue(second);
            if (start < 0 || start > length)
                return signal(new TypeError("Invalid start position " + start + "."));
            int end;
            if (third == NIL)
                end = length;
            else
                end = (int) Fixnum.getValue(third);
            if (end < 0 || end > length)
                return signal(new TypeError("Invalid end position " + start + "."));
            if (start > end)
                return signal(new TypeError("Start (" + start + ") is greater than end (" + end + ")."));
            for (int i = start; i < end; i++)
                string.setCharAt(i, Utilities.toLowerCase(string.charAt(i)));
            return string;
        }
    };

    // ### %nstring-capitalize
    private static final Primitive _NSTRING_CAPITALIZE =
        new Primitive("%nstring-capitalize", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            AbstractString string;
            try {
                string = (AbstractString) first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.STRING));
            }
            final int length = string.length();
            int start = (int) Fixnum.getValue(second);
            if (start < 0 || start > length)
                return signal(new TypeError("Invalid start position " + start + "."));
            int end;
            if (third == NIL)
                end = length;
            else
                end = (int) Fixnum.getValue(third);
            if (end < 0 || end > length)
                return signal(new TypeError("Invalid end position " + start + "."));
            if (start > end)
                return signal(new TypeError("Start (" + start + ") is greater than end (" + end + ")."));
            boolean lastCharWasAlphanumeric = false;
            for (int i = start; i < end; i++) {
                char c = string.charAt(i);
                if (Character.isLowerCase(c)) {
                    if (!lastCharWasAlphanumeric)
                        string.setCharAt(i, Utilities.toUpperCase(c));
                    lastCharWasAlphanumeric = true;
                } else if (Character.isUpperCase(c)) {
                    if (lastCharWasAlphanumeric)
                        string.setCharAt(i, Utilities.toLowerCase(c));
                    lastCharWasAlphanumeric = true;
                } else
                    lastCharWasAlphanumeric = Character.isDigit(c);
            }
            return string;
        }
    };

    // ### stringp
    public static final Primitive STRINGP = new Primitive("stringp", "object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.STRINGP();
        }
    };

    // ### simple-string-p
    public static final Primitive SIMPLE_STRING_P =
        new Primitive("simple-string-p", "object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.SIMPLE_STRING_P();
        }
    };

    // ### %make-string
    // %make-string size initial-element element-type => string
    // Returns a simple string.
    private static final Primitive _MAKE_STRING =
        new Primitive("%make-string", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject size, LispObject initialElement,
                                  LispObject elementType)
            throws ConditionThrowable
        {
            final int n;
            try {
                n = ((Fixnum)size).value;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(size, Symbol.FIXNUM));
            }
            if (n < 0 || n >= ARRAY_DIMENSION_MAX) {
                StringBuffer sb = new StringBuffer();
                sb.append("The size specified for this string (");
                sb.append(n);
                sb.append(')');
                if (n >= ARRAY_DIMENSION_MAX) {
                    sb.append(" is >= ARRAY-DIMENSION-LIMIT (");
                    sb.append(ARRAY_DIMENSION_MAX);
                    sb.append(").");
                } else
                    sb.append(" is negative.");
                return signal(new LispError(sb.toString()));
            }
            // Ignore elementType.
            SimpleString string = new SimpleString(n);
            if (initialElement != NIL) {
                // Initial element was specified.
                char c = checkCharacter(initialElement).getValue();
                string.fill(c);
            }
            return string;
        }
    };

    // ### char
    private static final Primitive CHAR = new Primitive("char", "string index")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return ((AbstractString)first).getRowMajor(((Fixnum)second).value);
            }
            catch (ClassCastException e) {
                if (first instanceof AbstractString)
                    return signal(new TypeError(second, Symbol.FIXNUM));
                else
                    return signal(new TypeError(first, Symbol.STRING));
            }
        }
    };

    // ### %set-char
    private static final Primitive _SET_CHAR =
        new Primitive("%set-char", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((AbstractString)first).setRowMajor(((Fixnum)second).value,
                                                    ((LispCharacter)third));
                return third;
            }
            catch (ClassCastException e) {
                if (!(first instanceof AbstractString))
                    return signal(new TypeError(first, Symbol.STRING));
                else if (!(second instanceof Fixnum))
                    return signal(new TypeError(second, Symbol.FIXNUM));
                else
                    return signal(new TypeError(third, Symbol.CHARACTER));
            }
        }
    };

    // ### string-position
    private static final Primitive STRING_POSITION =
        new Primitive("string-position", PACKAGE_EXT, true)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            char c = LispCharacter.getValue(first);
            AbstractString string;
            if (second instanceof AbstractString)
                string = (AbstractString) second;
            else
                return signal(new TypeError(second, Symbol.STRING));
            int start = Fixnum.getValue(third);
            for (int i = start, limit = string.length(); i < limit; i++) {
                if (string.charAt(i) == c)
                    return number(i);
            }
            return NIL;
        }
    };

    // ### simple-string-search pattern string => position
    // Searches string for a substring that matches pattern.
    private static final Primitive SIMPLE_STRING_SEARCH =
        new Primitive("simple-string-search", PACKAGE_EXT, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            // FIXME Don't call getStringValue() here! (Just look at the chars.)
            int index = second.getStringValue().indexOf(first.getStringValue());
            return index >= 0 ? new Fixnum(index) : NIL;
        }
    };

    // ### simple-string-fill string character => string
    private static final Primitive STRING_FILL =
        new Primitive("simple-string-fill", PACKAGE_EXT, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                AbstractString s = (AbstractString) first;
                s.fill(LispCharacter.getValue(second));
                return first;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.SIMPLE_STRING));
            }
        }
    };

    // ### schar
    private static final Primitive SCHAR = new Primitive("schar", "string index")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return LispCharacter.getInstance(((SimpleString)first).charAt(((Fixnum)second).value));
            }
            catch (ClassCastException e) {
                if (first instanceof SimpleString)
                    return signal(new TypeError(second, Symbol.FIXNUM));
                else
                    return signal(new TypeError(first, Symbol.SIMPLE_STRING));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                return signal(new TypeError("Array index out of bounds: " +
                                            ((Fixnum)second).value));
            }
        }
    };

    // ### %set-schar
    private static final Primitive _SET_SCHAR =
        new Primitive("%set-schar", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((SimpleString)first).setCharAt(((Fixnum)second).value,
                                              ((LispCharacter)third).value);
                return third;
            }
            catch (ClassCastException e) {
                if (!(first instanceof SimpleString))
                    return signal(new TypeError(first, Symbol.SIMPLE_STRING));
                if (!(second instanceof Fixnum))
                    return signal(new TypeError(second, Symbol.FIXNUM));
                return signal(new TypeError(third, Symbol.CHARACTER));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                return signal(new TypeError("Array index out of bounds: " +
                                            ((Fixnum)second).value));
            }
        }
    };
}
