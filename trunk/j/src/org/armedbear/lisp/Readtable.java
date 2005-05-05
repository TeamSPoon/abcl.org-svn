/*
 * Readtable.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: Readtable.java,v 1.42 2005-05-05 15:14:09 piso Exp $
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

import java.util.ArrayList;

public final class Readtable extends LispObject
{
    public static final byte SYNTAX_TYPE_CONSTITUENT           = 0;
    public static final byte SYNTAX_TYPE_WHITESPACE            = 1;
    public static final byte SYNTAX_TYPE_TERMINATING_MACRO     = 2;
    public static final byte SYNTAX_TYPE_NON_TERMINATING_MACRO = 3;
    public static final byte SYNTAX_TYPE_SINGLE_ESCAPE         = 4;
    public static final byte SYNTAX_TYPE_MULTIPLE_ESCAPE       = 5;

    private final byte[]          syntax               = new byte[CHAR_MAX];
    private final LispObject[]    readerMacroFunctions = new LispObject[CHAR_MAX];
    private final DispatchTable[] dispatchTables       = new DispatchTable[CHAR_MAX];

    private LispObject readtableCase;

    public Readtable()
    {
        syntax[9]    = SYNTAX_TYPE_WHITESPACE; // tab
        syntax[10]   = SYNTAX_TYPE_WHITESPACE; // linefeed
        syntax[12]   = SYNTAX_TYPE_WHITESPACE; // form feed
        syntax[13]   = SYNTAX_TYPE_WHITESPACE; // return
        syntax[' ']  = SYNTAX_TYPE_WHITESPACE;

        syntax['"']  = SYNTAX_TYPE_TERMINATING_MACRO;
        syntax['\''] = SYNTAX_TYPE_TERMINATING_MACRO;
        syntax['(']  = SYNTAX_TYPE_TERMINATING_MACRO;
        syntax[')']  = SYNTAX_TYPE_TERMINATING_MACRO;
        syntax[',']  = SYNTAX_TYPE_TERMINATING_MACRO;
        syntax[';']  = SYNTAX_TYPE_TERMINATING_MACRO;
        syntax['`']  = SYNTAX_TYPE_TERMINATING_MACRO;

        syntax['#']  = SYNTAX_TYPE_NON_TERMINATING_MACRO;

        syntax['\\'] = SYNTAX_TYPE_SINGLE_ESCAPE;
        syntax['|']  = SYNTAX_TYPE_MULTIPLE_ESCAPE;

        readerMacroFunctions[';']  = LispReader.READ_COMMENT;
        readerMacroFunctions['"']  = LispReader.READ_STRING;
        readerMacroFunctions['(']  = LispReader.READ_LIST;
        readerMacroFunctions[')']  = LispReader.READ_RIGHT_PAREN;
        readerMacroFunctions['\''] = LispReader.READ_QUOTE;
        readerMacroFunctions['#']  = LispReader.READ_DISPATCH_CHAR;

        // BACKQUOTE-MACRO and COMMA-MACRO are defined in backquote.lisp.
        readerMacroFunctions['`']  = Symbol.BACKQUOTE_MACRO;
        readerMacroFunctions[',']  = Symbol.COMMA_MACRO;

        DispatchTable dt = new DispatchTable();
        dt.functions['(']  = LispReader.SHARP_LEFT_PAREN;
        dt.functions['*']  = LispReader.SHARP_STAR;
        dt.functions['.']  = LispReader.SHARP_DOT;
        dt.functions[':']  = LispReader.SHARP_COLON;
        dt.functions['A']  = LispReader.SHARP_A;
        dt.functions['B']  = LispReader.SHARP_B;
        dt.functions['C']  = LispReader.SHARP_C;
        dt.functions['O']  = LispReader.SHARP_O;
        dt.functions['P']  = LispReader.SHARP_P;
        dt.functions['R']  = LispReader.SHARP_R;
        dt.functions['S']  = LispReader.SHARP_S;
        dt.functions['X']  = LispReader.SHARP_X;
        dt.functions['\''] = LispReader.SHARP_QUOTE;
        dt.functions['\\'] = LispReader.SHARP_BACKSLASH;
        dt.functions['|']  = LispReader.SHARP_VERTICAL_BAR;
        dt.functions[')']  = LispReader.SHARP_ILLEGAL;
        dt.functions['<']  = LispReader.SHARP_ILLEGAL;
        dt.functions[' ']  = LispReader.SHARP_ILLEGAL;
        dt.functions[8]    = LispReader.SHARP_ILLEGAL; // backspace
        dt.functions[9]    = LispReader.SHARP_ILLEGAL; // tab
        dt.functions[10]   = LispReader.SHARP_ILLEGAL; // newline, linefeed
        dt.functions[12]   = LispReader.SHARP_ILLEGAL; // page
        dt.functions[13]   = LispReader.SHARP_ILLEGAL; // return
        dispatchTables['#'] = dt;

        readtableCase = Keyword.UPCASE;
    }

    public Readtable(LispObject obj) throws ConditionThrowable
    {
        Readtable rt;
        if (obj == NIL)
            rt = checkReadtable(_STANDARD_READTABLE_.symbolValue());
        else
            rt = checkReadtable(obj);
        synchronized (rt) {
            System.arraycopy(rt.syntax, 0, syntax, 0,
                             CHAR_MAX);
            System.arraycopy(rt.readerMacroFunctions, 0, readerMacroFunctions, 0,
                             CHAR_MAX);
            // Deep copy.
            for (int i = dispatchTables.length; i-- > 0;) {
                DispatchTable dt = rt.dispatchTables[i];
                if (dt != null)
                    dispatchTables[i] = new DispatchTable(dt);
            }
            readtableCase = rt.readtableCase;
        }
    }

    // FIXME synchronization
    private static void copyReadtable(Readtable from, Readtable to)
    {
        System.arraycopy(from.syntax, 0, to.syntax, 0,
                         CHAR_MAX);
        System.arraycopy(from.readerMacroFunctions, 0, to.readerMacroFunctions, 0,
                         CHAR_MAX);
        for (int i = from.dispatchTables.length; i-- > 0;) {
            DispatchTable dt = from.dispatchTables[i];
            if (dt != null)
                to.dispatchTables[i] = new DispatchTable(dt);
            else
                to.dispatchTables[i] = null;
        }
        to.readtableCase = from.readtableCase;
    }

    public LispObject typeOf()
    {
        return Symbol.READTABLE;
    }

    public LispObject classOf()
    {
        return BuiltInClass.READTABLE;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.READTABLE)
            return T;
        if (type == BuiltInClass.READTABLE)
            return T;
        return super.typep(type);
    }

    public String toString()
    {
        return unreadableString("READTABLE");
    }

    public LispObject getReadtableCase()
    {
        return readtableCase;
    }

    public boolean isWhitespace(char c)
    {
        if (c < CHAR_MAX)
            return syntax[c] == SYNTAX_TYPE_WHITESPACE;
        return false;
    }

    public byte getSyntaxType(char c)
    {
        if (c < CHAR_MAX)
            return syntax[c];
        return SYNTAX_TYPE_CONSTITUENT;
    }

    public boolean isInvalid(char c)
    {
        switch (c) {
            case 8:
            case 9:
            case 10:
            case 12:
            case 13:
            case 32:
            case 127:
                return true;
            default:
                return false;
        }
    }

    public void checkInvalid(char c, Stream stream) throws ConditionThrowable
    {
        // "... no mechanism is provided for changing the constituent trait of
        // a character." (2.1.4.2)
        if (isInvalid(c)) {
            String name = LispCharacter.charToName(c);
            StringBuffer sb = new StringBuffer("Invalid character");
            if (name != null) {
                sb.append(" #\\");
                sb.append(name);
            }
            signal(new ReaderError(sb.toString(), stream));
        }
    }

    public LispObject getReaderMacroFunction(char c)
    {
        if (c < CHAR_MAX)
            return readerMacroFunctions[c];
        else
            return null;
    }

    private LispObject getMacroCharacter(char c) throws ConditionThrowable
    {
        LispObject function = getReaderMacroFunction(c);
        LispObject non_terminating_p;
        if (function != null) {
            if (syntax[c] == SYNTAX_TYPE_NON_TERMINATING_MACRO)
                non_terminating_p = T;
            else
                non_terminating_p = NIL;
        } else {
            function = NIL;
            non_terminating_p = NIL;
        }
        return LispThread.currentThread().setValues(function, non_terminating_p);
    }

    private void makeDispatchMacroCharacter(char dispChar, LispObject non_terminating_p)
    {
        byte syntaxType;
        if (non_terminating_p != NIL)
            syntaxType = SYNTAX_TYPE_NON_TERMINATING_MACRO;
        else
            syntaxType = SYNTAX_TYPE_TERMINATING_MACRO;
        // FIXME synchronization
        syntax[dispChar] = syntaxType;
        readerMacroFunctions[dispChar] = LispReader.READ_DISPATCH_CHAR;
        dispatchTables[dispChar] = new DispatchTable();
    }

    public LispObject getDispatchMacroCharacter(char dispChar, char subChar)
        throws ConditionThrowable
    {
        DispatchTable dispatchTable = dispatchTables[dispChar];
        if (dispatchTable == null) {
            LispCharacter c = LispCharacter.getInstance(dispChar);
            return signal(new LispError(c.writeToString() +
                                        " is not a dispatch character."));
        }
        LispObject function =
            dispatchTable.functions[Utilities.toUpperCase(subChar)];
        return (function != null) ? function : NIL;
    }

    public void setDispatchMacroCharacter(char dispChar, char subChar,
                                          LispObject function)
        throws ConditionThrowable
    {
        DispatchTable dispatchTable = dispatchTables[dispChar];
        if (dispatchTable == null) {
            LispCharacter c = LispCharacter.getInstance(dispChar);
            signal(new LispError(c.writeToString() +
                                 " is not a dispatch character."));
        }
        dispatchTable.functions[Utilities.toUpperCase(subChar)] = function;
    }

    private static class DispatchTable
    {
        public LispObject[] functions = new LispObject[CHAR_MAX];

        public DispatchTable()
        {
        }

        public DispatchTable(DispatchTable dt)
        {
            for (int i = 0; i < functions.length; i++)
                functions[i] = dt.functions[i];
        }
    }

    // ### readtablep
    private static final Primitive READTABLEP =
        new Primitive("readtablep", "object")
    {
        public LispObject execute(LispObject arg)
        {
            return arg instanceof Readtable ? T : NIL;
        }
    };

    // ### *standard-readtable*
    // internal symbol
    public static final Symbol _STANDARD_READTABLE_ =
        internSpecial("*STANDARD-READTABLE*", PACKAGE_SYS, new Readtable());

    // ### copy-readtable
    private static final Primitive COPY_READTABLE =
        new Primitive("copy-readtable", "&optional from-readtable to-readtable")
    {
        public LispObject execute() throws ConditionThrowable
        {
            return new Readtable(currentReadtable());
        }

        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Readtable(arg);
        }

        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Readtable from;
            if (first == NIL)
                from = checkReadtable(_STANDARD_READTABLE_.symbolValue());
            else
                from = checkReadtable(first);
            if (second == NIL)
                return new Readtable(from);
            Readtable to = checkReadtable(second);
            copyReadtable(from, to);
            return to;
        }
    };

    // ### get-macro-character char &optional readtable
    // => function, non-terminating-p
    private static final Primitive GET_MACRO_CHARACTER =
        new Primitive("get-macro-character", "char &optional readtable")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            char c = LispCharacter.getValue(arg);
            Readtable rt = currentReadtable();
            return rt.getMacroCharacter(c);
        }

        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c = LispCharacter.getValue(first);
            Readtable rt;
            if (second == NIL)
                rt = new Readtable(NIL);
            else
                rt = checkReadtable(second);
            return rt.getMacroCharacter(c);
        }
    };

    // ### set-macro-character char new-function &optional non-terminating-p readtable
    // => t
    private static final Primitive SET_MACRO_CHARACTER =
        new Primitive("set-macro-character",
                      "char new-function &optional non-terminating-p readtable")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            char c = LispCharacter.getValue(first);
            Readtable rt = currentReadtable();
            // FIXME synchronization
            rt.syntax[c] = SYNTAX_TYPE_TERMINATING_MACRO;
            rt.readerMacroFunctions[c] = coerceToFunction(second);
            return T;
        }

        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            char c = LispCharacter.getValue(first);
            Readtable rt = currentReadtable();
            byte syntaxType;
            if (third != NIL)
                syntaxType = SYNTAX_TYPE_NON_TERMINATING_MACRO;
            else
                syntaxType = SYNTAX_TYPE_TERMINATING_MACRO;
            // FIXME synchronization
            rt.syntax[c] = syntaxType;
            rt.readerMacroFunctions[c] = coerceToFunction(second);
            return T;
        }

        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 4)
                return signal(new WrongNumberOfArgumentsException(this));
            char c = LispCharacter.getValue(args[0]);
            byte syntaxType;
            if (args[2] != NIL)
                syntaxType = SYNTAX_TYPE_NON_TERMINATING_MACRO;
            else
                syntaxType = SYNTAX_TYPE_TERMINATING_MACRO;
            Readtable rt = checkReadtable(args[3]);
            // FIXME synchronization
            rt.syntax[c] = syntaxType;
            rt.readerMacroFunctions[c] = coerceToFunction(args[1]);
            return T;
        }
    };

    // ### make-dispatch-macro-character char &optional non-terminating-p readtable
    // => t
    private static final Primitive MAKE_DISPATCH_MACRO_CHARACTER =
        new Primitive("make-dispatch-macro-character",
                      "char &optional non-terminating-p readtable")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 3)
                return signal(new WrongNumberOfArgumentsException(this));
            char dispChar = LispCharacter.getValue(args[0]);
            LispObject non_terminating_p;
            if (args.length > 1)
                non_terminating_p = args[1];
            else
                non_terminating_p = NIL;
            Readtable readtable;
            if (args.length > 2)
                readtable = checkReadtable(args[2]);
            else
                readtable = currentReadtable();
            readtable.makeDispatchMacroCharacter(dispChar, non_terminating_p);
            return T;
        }
    };

    // ### get-dispatch-macro-character
    // get-dispatch-macro-character disp-char sub-char &optional readtable
    // => function
    private static final Primitive GET_DISPATCH_MACRO_CHARACTER =
        new Primitive("get-dispatch-macro-character",
                      "disp-char sub-char &optional readtable")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 2 || args.length > 3)
                return signal(new WrongNumberOfArgumentsException(this));
            char dispChar = LispCharacter.getValue(args[0]);
            char subChar = LispCharacter.getValue(args[1]);
            Readtable readtable;
            if (args.length == 3)
                readtable = checkReadtable(args[2]);
            else
                readtable = currentReadtable();
            return readtable.getDispatchMacroCharacter(dispChar, subChar);
        }
    };

    // ### set-dispatch-macro-character
    // set-dispatch-macro-character disp-char sub-char new-function &optional readtable
    // => t
    private static final Primitive SET_DISPATCH_MACRO_CHARACTER =
        new Primitive("set-dispatch-macro-character",
                      "disp-char sub-char new-function &optional readtable")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 3 || args.length > 4)
                return signal(new WrongNumberOfArgumentsException(this));
            char dispChar = LispCharacter.getValue(args[0]);
            char subChar = LispCharacter.getValue(args[1]);
            LispObject function = coerceToFunction(args[2]);
            Readtable readtable;
            if (args.length == 4)
                readtable = checkReadtable(args[3]);
            else
                readtable = currentReadtable();
            readtable.setDispatchMacroCharacter(dispChar, subChar, function);
            return T;
        }
    };

    // ### set-syntax-from-char
    // to-char from-char &optional to-readtable from-readtable => t
    private static final Primitive SET_SYNTAX_FROM_CHAR =
        new Primitive("set-syntax-from-char",
                      "to-char from-char &optional to-readtable from-readtable")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 2 || args.length > 4)
                return signal(new WrongNumberOfArgumentsException(this));
            char toChar = LispCharacter.getValue(args[0]);
            char fromChar = LispCharacter.getValue(args[1]);
            Readtable toReadtable;
            if (args.length > 2)
                toReadtable = checkReadtable(args[2]);
            else
                toReadtable = currentReadtable();
            Readtable fromReadtable;
            if (args.length > 3)
                fromReadtable = checkReadtable(args[3]);
            else
                fromReadtable = new Readtable(NIL);
            // FIXME synchronization
            toReadtable.syntax[toChar] = fromReadtable.syntax[fromChar];
            toReadtable.readerMacroFunctions[toChar] =
                fromReadtable.readerMacroFunctions[fromChar];
            // "If the character is a dispatching macro character, its entire
            // dispatch table of reader macro functions is copied."
            if (fromReadtable.dispatchTables[fromChar] != null) {
                toReadtable.dispatchTables[toChar] =
                    new DispatchTable(fromReadtable.dispatchTables[fromChar]);
            }
            return T;
        }
    };

    // ### readtable-case readtable => mode
    private static final Primitive READTABLE_CASE =
        new Primitive("readtable-case", "readtable")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((Readtable)arg).readtableCase;
            }
            catch (ClassCastException e) {
                return signal(new TypeError(arg, Symbol.READTABLE));
            }
        }
    };

    // ### %set-readtable-case readtable new-mode => new-mode
    private static final Primitive _SET_READTABLE_CASE =
        new Primitive("%set-readtable-case", PACKAGE_SYS, false,
                       "readtable new-mode")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                Readtable readtable = (Readtable) first;
                if (second == Keyword.UPCASE || second == Keyword.DOWNCASE ||
                    second == Keyword.INVERT || second == Keyword.PRESERVE)
                {
                    readtable.readtableCase = second;
                    return second;
                }
                return signal(new TypeError(second, list5(Symbol.MEMBER,
                                                          Keyword.INVERT,
                                                          Keyword.PRESERVE,
                                                          Keyword.DOWNCASE,
                                                          Keyword.UPCASE)));
            }
            catch (ClassCastException e) {
                return signal(new TypeError(first, Symbol.READTABLE));
            }
        }
    };
}
