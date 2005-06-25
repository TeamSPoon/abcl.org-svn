/*
 * FaslReader.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: FaslReader.java,v 1.1 2005-06-25 19:36:29 piso Exp $
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

public final class FaslReader extends Lisp
{
    // ### fasl-read-comment
    public static final ReaderMacroFunction FASL_READ_COMMENT =
        new ReaderMacroFunction("fasl-read-comment", PACKAGE_SYS, false,
                                "stream character")
    {
        public LispObject execute(Stream stream, char ignored)
            throws ConditionThrowable
        {
            while (true) {
                int n = stream._readChar();
                if (n < 0)
                    return null;
                if (n == '\n')
                    return null;
            }
        }
    };

    // ### fasl-read-string
    public static final ReaderMacroFunction FASL_READ_STRING =
        new ReaderMacroFunction("fasl-read-string", PACKAGE_SYS, false,
                                "stream character")
    {
        public LispObject execute(Stream stream, char terminator)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            final Readtable rt = (Readtable) _READTABLE_.symbolValue(thread);
            StringBuffer sb = new StringBuffer();
            while (true) {
                int n = stream._readChar();
                if (n < 0) {
                    signal(new EndOfFile(stream));
                    // Not reached.
                    return null;
                }
                char c = (char) n;
                if (rt.getSyntaxType(c) == Readtable.SYNTAX_TYPE_SINGLE_ESCAPE) {
                    // Single escape.
                    n = stream._readChar();
                    if (n < 0) {
                        signal(new EndOfFile(stream));
                        // Not reached.
                        return null;
                    }
                    sb.append((char)n);
                    continue;
                }
                if (Utilities.isPlatformWindows) {
                    if (c == '\r') {
                        n = stream._readChar();
                        if (n < 0) {
                            signal(new EndOfFile(stream));
                            // Not reached.
                            return null;
                        }
                        if (n == '\n') {
                            sb.append('\n');
                        } else {
                            // '\r' was not followed by '\n'.
                            stream._unreadChar(n);
                            sb.append('\r');
                        }
                        continue;
                    }
                }
                if (c == terminator)
                    break;
                // Default.
                sb.append(c);
            }
            return new SimpleString(sb);
        }
    };

    // ### fasl-read-list
    public static final ReaderMacroFunction FASL_READ_LIST =
        new ReaderMacroFunction("fasl-read-list", PACKAGE_SYS, false,
                                "stream character")
    {
        public LispObject execute(Stream stream, char ignored)
            throws ConditionThrowable
        {
            return stream.readList(false);
        }
    };

    // ### fasl-read-right-paren
    public static final ReaderMacroFunction FASL_READ_RIGHT_PAREN =
        new ReaderMacroFunction("fasl-read-right-paren", PACKAGE_SYS, false,
                                "stream character")
    {
        public LispObject execute(Stream stream, char ignored)
            throws ConditionThrowable
        {
            return signal(new ReaderError("Unmatched right parenthesis.", stream));
        }
    };

    // ### fasl-read-quote
    public static final ReaderMacroFunction FASL_READ_QUOTE =
        new ReaderMacroFunction("fasl-read-quote", PACKAGE_SYS, false,
                                "stream character")
    {
        public LispObject execute(Stream stream, char ignored)
            throws ConditionThrowable
        {
            return new Cons(Symbol.QUOTE,
                            new Cons(stream.read(true, NIL, true)));
        }
    };

    // ### fasl-read-dispatch-char
    public static final ReaderMacroFunction FASL_READ_DISPATCH_CHAR =
        new ReaderMacroFunction("fasl-read-dispatch-char", PACKAGE_SYS, false,
                                "stream character")
    {
        public LispObject execute(Stream stream, char c)
            throws ConditionThrowable
        {
            return stream.readDispatchChar(c);
        }
    };

    // ### fasl-sharp-left-paren
    public static final DispatchMacroFunction FASL_SHARP_LEFT_PAREN =
        new DispatchMacroFunction("fasl-sharp-left-paren", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject list = stream.readList(true);
            if (_BACKQUOTE_COUNT_.symbolValue(thread).zerop()) {
                if (n >= 0) {
                    LispObject[] array = new LispObject[n];
                    for (int i = 0; i < n; i++) {
                        array[i] = list.car();
                        if (list.cdr() != NIL)
                            list = list.cdr();
                    }
                    return new SimpleVector(array);
                } else
                    return new SimpleVector(list);
            }
            return new Cons(_BQ_VECTOR_FLAG_.symbolValue(thread), list);
        }
    };

    // ### fasl-sharp-star
    public static final DispatchMacroFunction FASL_SHARP_STAR =
        new DispatchMacroFunction("fasl-sharp-star", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char ignored, int n)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            final Readtable rt = (Readtable) _READTABLE_.symbolValue(thread);
            final boolean suppress = _READ_SUPPRESS_.symbolValue(thread) != NIL;
            StringBuffer sb = new StringBuffer();
            while (true) {
                int ch = stream._readChar();
                if (ch < 0)
                    break;
                char c = (char) ch;
                if (c == '0' || c == '1')
                    sb.append(c);
                else {
                    int syntaxType = rt.getSyntaxType(c);
                    if (syntaxType == Readtable.SYNTAX_TYPE_WHITESPACE ||
                        syntaxType == Readtable.SYNTAX_TYPE_TERMINATING_MACRO) {
                        stream._unreadChar(c);
                        break;
                    } else if (!suppress) {
                        String name = LispCharacter.charToName(c);
                        if (name == null)
                            name = "#\\" + c;
                        signal(new ReaderError("Illegal element for bit-vector: " + name,
                                               stream));
                    }
                }
            }
            if (suppress)
                return NIL;
            if (n >= 0) {
                // n was supplied.
                final int length = sb.length();
                if (length == 0) {
                    if (n > 0)
                        return signal(new ReaderError("No element specified for bit vector of length " +
                                                      n + '.',
                                                      stream));
                }
                if (n > length) {
                    final char c = sb.charAt(length - 1);
                    for (int i = length; i < n; i++)
                        sb.append(c);
                } else if (n < length) {
                    return signal(new ReaderError("Bit vector is longer than specified length: #" +
                                                  n + '*' + sb.toString(),
                                                  stream));
                }
            }
            return new SimpleBitVector(sb.toString());
        }
    };

    // ### fasl-sharp-dot
    public static final DispatchMacroFunction FASL_SHARP_DOT =
        new DispatchMacroFunction("fasl-sharp-dot", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            if (_READ_EVAL_.symbolValue(thread) == NIL)
                return signal(new ReaderError("Can't read #. when *READ-EVAL* is NIL.",
                                              stream));
            else
                return eval(stream.read(true, NIL, true),
                            new Environment(), thread);
        }
    };

    // ### fasl-sharp-colon
    public static final DispatchMacroFunction FASL_SHARP_COLON =
        new DispatchMacroFunction("fasl-sharp-colon", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            LispThread thread = LispThread.currentThread();
            Symbol symbol = (Symbol) stream.readSymbol();
            LispObject pkg = Load._FASL_ANONYMOUS_PACKAGE_.symbolValue(thread);
            if (pkg == NIL) {
                thread.bindSpecial(Load._FASL_ANONYMOUS_PACKAGE_,
                                   pkg = new Package());
            }
            symbol = ((Package)pkg).intern(symbol.getName());
            symbol.setPackage(NIL);
            return symbol;
        }
    };

    // ### fasl-sharp-a
    public static final DispatchMacroFunction FASL_SHARP_A =
        new DispatchMacroFunction("fasl-sharp-a", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readArray(n);
        }
    };

    // ### fasl-sharp-b
    public static final DispatchMacroFunction FASL_SHARP_B =
        new DispatchMacroFunction("fasl-sharp-b", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readRadix(2);
        }
    };

    // ### fasl-sharp-c
    public static final DispatchMacroFunction FASL_SHARP_C =
        new DispatchMacroFunction("fasl-sharp-c", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readComplex();
        }
    };

    // ### fasl-sharp-o
    public static final DispatchMacroFunction FASL_SHARP_O =
        new DispatchMacroFunction("fasl-sharp-o", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readRadix(8);
        }
    };

    // ### fasl-sharp-p
    public static final DispatchMacroFunction FASL_SHARP_P =
        new DispatchMacroFunction("fasl-sharp-p", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readPathname();
        }
    };

    // ### fasl-sharp-r
    public static final DispatchMacroFunction FASL_SHARP_R =
        new DispatchMacroFunction("fasl-sharp-r", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readRadix(n);
        }
    };

    // ### fasl-sharp-s
    public static final DispatchMacroFunction FASL_SHARP_S =
        new DispatchMacroFunction("fasl-sharp-s", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readStructure();
        }
    };

    // ### fasl-sharp-x
    public static final DispatchMacroFunction FASL_SHARP_X =
        new DispatchMacroFunction("fasl-sharp-x", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readRadix(16);
        }
    };

    // ### fasl-sharp-quote
    public static final DispatchMacroFunction FASL_SHARP_QUOTE =
        new DispatchMacroFunction("fasl-sharp-quote", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return new Cons(Symbol.FUNCTION,
                            new Cons(stream.read(true, NIL, true)));
        }
    };

    // ### fasl-sharp-backslash
    public static final DispatchMacroFunction FASL_SHARP_BACKSLASH =
        new DispatchMacroFunction("fasl-sharp-backslash", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            return stream.readCharacterLiteral();
        }
    };

    // ### fasl-sharp-vertical-bar
    public static final DispatchMacroFunction FASL_SHARP_VERTICAL_BAR =
        new DispatchMacroFunction("sharp-vertical-bar", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            stream.skipBalancedComment();
            return null;
        }
    };

    // ### fasl-sharp-illegal
    public static final DispatchMacroFunction FASL_SHARP_ILLEGAL =
        new DispatchMacroFunction("fasl-sharp-illegal", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            StringBuffer sb = new StringBuffer("Illegal # macro character: #\\");
            String s = LispCharacter.charToName(c);
            if (s != null)
                sb.append(s);
            else
                sb.append(c);
            return signal(new ReaderError(sb.toString(), stream));
        }
    };
}
