/*
 * CharacterFunctions.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: CharacterFunctions.java,v 1.3 2003-09-19 01:46:40 piso Exp $
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

public final class CharacterFunctions extends Lisp
{
    // ### char=
    private static final Primitive CHAR_EQUALS = new Primitive("char=") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return LispCharacter.getValue(first) == LispCharacter.getValue(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (length > 1) {
                final char c0 = LispCharacter.getValue(array[0]);
                for (int i = 0; i < length; i++) {
                    if (c0 != LispCharacter.getValue(array[i]))
                        return NIL;
                }
            }
            return T;
        }
    };

    // ### char-equal
    private static final Primitive CHAR_EQUAL = new Primitive("char-equal") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c1 = LispCharacter.getValue(first);
            char c2 = LispCharacter.getValue(second);
            if (c1 == c2)
                return T;
            if (Character.toUpperCase(c1) == Character.toUpperCase(c2))
                return T;
            if (Character.toLowerCase(c1) == Character.toLowerCase(c2))
                return T;
            return NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (length > 1) {
                final char c0 = LispCharacter.getValue(array[0]);
                for (int i = 1; i < length; i++) {
                    char c = LispCharacter.getValue(array[i]);
                    if (c0 == c)
                        continue;
                    if (Character.toUpperCase(c0) == Character.toUpperCase(c))
                        continue;
                    if (Character.toLowerCase(c0) == Character.toLowerCase(c))
                        continue;
                    return NIL;
                }
            }
            return T;
        }
    };

    // ### char-greaterp
    private static final Primitive CHAR_GREATERP =
        new Primitive("char-greaterp") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c1 = Character.toUpperCase(LispCharacter.getValue(first));
            char c2 = Character.toUpperCase(LispCharacter.getValue(second));
            return c1 > c2 ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (length > 1) {
                char[] chars = new char[length];
                for (int i = 0; i < length; i++)
                    chars[i] = Character.toUpperCase(LispCharacter.getValue(array[i]));
                for (int i = 1; i < length; i++) {
                    if (chars[i-1] <= chars[i])
                        return NIL;
                }
            }
            return T;
        }
    };

    // ### char-not-greaterp
    private static final Primitive CHAR_NOT_GREATERP =
        new Primitive("char-not-greaterp") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c1 = Character.toUpperCase(LispCharacter.getValue(first));
            char c2 = Character.toUpperCase(LispCharacter.getValue(second));
            return c1 <= c2 ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (length > 1) {
                char[] chars = new char[length];
                for (int i = 0; i < length; i++)
                    chars[i] = Character.toUpperCase(LispCharacter.getValue(array[i]));
                for (int i = 1; i < length; i++) {
                    if (chars[i] < chars[i-1])
                        return NIL;
                }
            }
            return T;
        }
    };

    // ### char-lessp
    private static final Primitive CHAR_LESSP =
        new Primitive("char-lessp") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c1 = Character.toUpperCase(LispCharacter.getValue(first));
            char c2 = Character.toUpperCase(LispCharacter.getValue(second));
            return c1 < c2 ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (length > 1) {
                char[] chars = new char[length];
                for (int i = 0; i < length; i++)
                    chars[i] = Character.toUpperCase(LispCharacter.getValue(array[i]));
                for (int i = 1; i < length; i++) {
                    if (chars[i-1] >= chars[i])
                        return NIL;
                }
            }
            return T;
        }
    };

    // ### char-not-lessp
    private static final Primitive CHAR_NOT_LESSP =
        new Primitive("char-not-lessp") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c1 = Character.toUpperCase(LispCharacter.getValue(first));
            char c2 = Character.toUpperCase(LispCharacter.getValue(second));
            return c1 >= c2 ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (length > 1) {
                char[] chars = new char[length];
                for (int i = 0; i < length; i++)
                    chars[i] = Character.toUpperCase(LispCharacter.getValue(array[i]));
                for (int i = 1; i < length; i++) {
                    if (chars[i] > chars[i-1])
                        return NIL;
                }
            }
            return T;
        }
    };
}
