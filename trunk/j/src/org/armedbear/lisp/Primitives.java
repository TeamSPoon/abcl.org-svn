/*
 * Primitives.java
 *
 * Copyright (C) 2002-2003 Peter Graves
 * $Id: Primitives.java,v 1.457 2003-10-01 22:56:43 piso Exp $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class Primitives extends Module
{
    // Primitive
    private static final int DIVIDE                     = 1;
    private static final int EXIT                       = 2;
    private static final int MAX                        = 3;
    private static final int MIN                        = 4;
    private static final int MULTIPLY                   = 5;
    private static final int VALUES                     = 6;

    // Primitive1
    private static final int ABS                        = 7;
    private static final int ARRAYP                     = 8;
    private static final int ARRAY_HAS_FILL_POINTER_P   = 9;
    private static final int BIT_VECTOR_P               = 10;
    private static final int BOTH_CASE_P                = 11;
    private static final int CHARACTERP                 = 12;
    private static final int CHAR_CODE                  = 13;
    private static final int CHAR_DOWNCASE              = 14;
    private static final int CHAR_INT                   = 15;
    private static final int CHAR_UPCASE                = 16;
    private static final int CODE_CHAR                  = 17;
    private static final int COMPILED_FUNCTION_P        = 18;
    private static final int CONSP                      = 19;
    private static final int EVAL                       = 20;
    private static final int EVENP                      = 21;
    private static final int FBOUNDP                    = 22;
    private static final int FMAKUNBOUND                = 23;
    private static final int FOURTH                     = 24;
    private static final int FUNCTIONP                  = 25;
    private static final int IDENTITY                   = 26;
    private static final int KEYWORDP                   = 27;
    private static final int LENGTH                     = 28;
    private static final int LISTP                      = 29;
    private static final int LOWER_CASE_P               = 30;
    private static final int MAKE_SYMBOL                = 31;
    private static final int MAKUNBOUND                 = 32;
    private static final int NUMBERP                    = 33;
    private static final int ODDP                       = 34;
    private static final int PREDECESSOR                = 35;
    private static final int SECOND                     = 36;
    private static final int SIMPLE_BIT_VECTOR_P        = 37;
    private static final int SIMPLE_STRING_P            = 38;
    private static final int SIMPLE_VECTOR_P            = 39;
    private static final int SPECIAL_OPERATOR_P         = 40;
    private static final int STRINGP                    = 41;
    private static final int SUCCESSOR                  = 42;
    private static final int SYMBOL_FUNCTION            = 43;
    private static final int SYMBOL_NAME                = 44;
    private static final int SYMBOL_PACKAGE             = 45;
    private static final int SYMBOL_PLIST               = 46;
    private static final int SYMBOL_VALUE               = 47;
    private static final int THIRD                      = 48;
    private static final int UPPER_CASE_P               = 49;
    private static final int VALUES_LIST                = 50;
    private static final int VECTORP                    = 51;

    // Primitive2
    private static final int MEMBER                     = 52;
    private static final int RPLACA                     = 53;
    private static final int RPLACD                     = 54;
    private static final int SET                        = 55;

    private Primitives()
    {
        definePrimitive("*", MULTIPLY);
        definePrimitive("/", DIVIDE);
        definePrimitive("exit", EXIT);
        definePrimitive("max", MAX);
        definePrimitive("min", MIN);
        definePrimitive("values", VALUES);

        definePrimitive1("1+", SUCCESSOR);
        definePrimitive1("1-", PREDECESSOR);
        definePrimitive1("abs", ABS);
        definePrimitive1("array-has-fill-pointer-p", ARRAY_HAS_FILL_POINTER_P);
        definePrimitive1("arrayp", ARRAYP);
        definePrimitive1("bit-vector-p", BIT_VECTOR_P);
        definePrimitive1("both-case-p", BOTH_CASE_P);
        definePrimitive1("char-code", CHAR_CODE);
        definePrimitive1("char-downcase", CHAR_DOWNCASE);
        definePrimitive1("char-int", CHAR_INT);
        definePrimitive1("char-upcase", CHAR_UPCASE);
        definePrimitive1("characterp", CHARACTERP);
        definePrimitive1("code-char", CODE_CHAR);
        definePrimitive1("compiled-function-p", COMPILED_FUNCTION_P);
        definePrimitive1("consp", CONSP);
        definePrimitive1("eval", EVAL);
        definePrimitive1("evenp", EVENP);
        definePrimitive1("fboundp", FBOUNDP);
        definePrimitive1("fmakunbound", FMAKUNBOUND);
        definePrimitive1("fourth", FOURTH);
        definePrimitive1("functionp", FUNCTIONP);
        definePrimitive1("identity", IDENTITY);
        definePrimitive1("keywordp", KEYWORDP);
        definePrimitive1("length", LENGTH);
        definePrimitive1("listp", LISTP);
        definePrimitive1("lower-case-p", LOWER_CASE_P);
        definePrimitive1("make-symbol", MAKE_SYMBOL);
        definePrimitive1("makunbound", MAKUNBOUND);
        definePrimitive1("numberp", NUMBERP);
        definePrimitive1("oddp", ODDP);
        definePrimitive1("second", SECOND);
        definePrimitive1("simple-bit-vector-p", SIMPLE_BIT_VECTOR_P);
        definePrimitive1("simple-string-p", SIMPLE_STRING_P);
        definePrimitive1("simple-vector-p", SIMPLE_VECTOR_P);
        definePrimitive1("special-operator-p", SPECIAL_OPERATOR_P);
        definePrimitive1("stringp", STRINGP);
        definePrimitive1("symbol-function", SYMBOL_FUNCTION);
        definePrimitive1("symbol-name", SYMBOL_NAME);
        definePrimitive1("symbol-package", SYMBOL_PACKAGE);
        definePrimitive1("symbol-plist", SYMBOL_PLIST);
        definePrimitive1("symbol-value", SYMBOL_VALUE);
        definePrimitive1("third", THIRD);
        definePrimitive1("upper-case-p", UPPER_CASE_P);
        definePrimitive1("values-list", VALUES_LIST);
        definePrimitive1("vectorp", VECTORP);

        definePrimitive2("member", MEMBER);
        definePrimitive2("rplaca", RPLACA);
        definePrimitive2("rplacd", RPLACD);
        definePrimitive2("set", SET);
    }

    // Primitive
    public LispObject dispatch(LispObject[] args, int index)
        throws ConditionThrowable
    {
        switch (index) {
            case MULTIPLY: {                    // ### *
                LispObject result = Fixnum.ONE;
                for (int i = 0; i < args.length; i++)
                    result = result.multiplyBy(args[i]);
                return result;
            }
            case DIVIDE: {                      // ### /
                if (args.length < 1)
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException("/"));
                if (args.length == 1)
                    return Fixnum.ONE.divideBy(args[0]);
                LispObject result = args[0];
                for (int i = 1; i < args.length; i++)
                    result = result.divideBy(args[i]);
                return result;
            }
            case MIN: {                         // ### min
                if (args.length < 1)
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException("MIN"));
                LispObject result = args[0];
                if (!result.realp())
                    throw new ConditionThrowable(new TypeError(result, "real"));
                for (int i = 1; i < args.length; i++) {
                    if (args[i].isLessThan(result))
                        result = args[i];
                }
                return result;
            }
            case MAX: {                         // ### max
                if (args.length < 1)
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException("MAX"));
                LispObject result = args[0];
                if (!result.realp())
                    throw new ConditionThrowable(new TypeError(result, "real"));
                for (int i = 1; i < args.length; i++) {
                    if (args[i].isGreaterThan(result))
                        result = args[i];
                }
                return result;
            }
            case VALUES:                        // ### values
                return values(args);
            case EXIT:                          // ### exit
                exit();
                return T;
            default:
                Debug.trace("bad index " + index);
                throw new ConditionThrowable(new WrongNumberOfArgumentsException((String)null));
        }
    }

    // Primitive1
    public LispObject dispatch(LispObject arg, int index)
        throws ConditionThrowable
    {
        switch (index) {
            case IDENTITY:                      // ### identity
                return arg;
            case SECOND:                        // ### second
                return arg.cadr();
            case THIRD:                         // ### third
                return arg.cdr().cdr().car();
            case FOURTH:                        // ### fourth
                return arg.cdr().cdr().cdr().car();
            case FUNCTIONP:                     // ### functionp
                return arg instanceof Function ? T : NIL;
            case COMPILED_FUNCTION_P:           // ### compiled-function-p
                return arg.typep(Symbol.COMPILED_FUNCTION);
            case KEYWORDP:                      // ### keywordp
                if (arg instanceof Symbol) {
                    if (((Symbol)arg).getPackage() == PACKAGE_KEYWORD)
                        return T;
                }
                return NIL;
            case SPECIAL_OPERATOR_P:            // ### special-operator-p
                return arg.getSymbolFunction() instanceof SpecialOperator ? T : NIL;
            case EVENP:                         // ### evenp
                return arg.EVENP();
            case ODDP:                          // ### oddp
                return arg.ODDP();
            case NUMBERP:                       // ### numberp
                return arg.NUMBERP();
            case LENGTH:                        // ### length
                return arg.LENGTH();
            case CONSP:                         // ### consp
                return arg instanceof Cons ? T : NIL;
            case LISTP:                         // ### listp
                return arg.LISTP();
            case MAKE_SYMBOL:                   // ### make-symbol
                return new Symbol(LispString.getValue(arg));
            case FBOUNDP:                       // ### fboundp
                return arg.getSymbolFunction() != null ? T : NIL;
            case MAKUNBOUND:                    // ### makunbound
                checkSymbol(arg).setSymbolValue(null);
                return arg;
            case FMAKUNBOUND:                   // ### fmakunbound
                checkSymbol(arg).setSymbolFunction(null);
                return arg;
            case SYMBOL_NAME:                   // ### symbol-name
                if (arg.typep(Symbol.SYMBOL) != NIL)
                    return new LispString(arg.getName());
                throw new ConditionThrowable(new TypeError(arg, "symbol"));
            case SYMBOL_PACKAGE:                // ### symbol-package
                return checkSymbol(arg).getPackage();
            case SYMBOL_VALUE:                  // ### symbol-value
                if (arg == T)
                    return T;
                if (arg == NIL)
                    return NIL;
                return checkSymbol(arg).symbolValue();
            case SYMBOL_FUNCTION: {             // ### symbol-function
                LispObject function = arg.getSymbolFunction();
                if (function != null)
                    return function;
                throw new ConditionThrowable(new UndefinedFunction(arg));
            }
            case SYMBOL_PLIST:                  // ### symbol-plist
                try {
                    return ((Symbol)arg).getPropertyList();
                }
                catch (ClassCastException e) {
                    throw new ConditionThrowable(new TypeError(arg, "symbol"));
                }
            case ABS:                           // ### abs
                return arg.ABS();
            case ARRAYP:                        // ### arrayp
                return arg instanceof AbstractArray ? T : NIL;
            case ARRAY_HAS_FILL_POINTER_P:      // ### array-has-fill-pointer-p
                if (arg instanceof AbstractVector)
                    return ((AbstractVector)arg).getFillPointer() >= 0 ? T : NIL;
                if (arg instanceof AbstractArray)
                    return NIL;
                throw new ConditionThrowable(new TypeError(arg, "array"));
            case VECTORP:                       // ### vectorp
                return arg.VECTORP();
            case SIMPLE_VECTOR_P:               // ### simple-vector-p
                return arg.typep(Symbol.SIMPLE_VECTOR);
            case BIT_VECTOR_P:                  // ### bit-vector-p
                return arg.BIT_VECTOR_P();
            case SIMPLE_BIT_VECTOR_P:           // ### simple-bit-vector-p
                return arg.typep(Symbol.SIMPLE_BIT_VECTOR);
            case CHAR_CODE:                     // ### char-code
            case CHAR_INT:                      // ### char-int
                return new Fixnum(LispCharacter.getValue(arg));
            case CODE_CHAR:                     // ### code-char
                if (arg instanceof Fixnum) {
                    int n = Fixnum.getValue(arg);
                    if (n < 128)
                        return LispCharacter.getInstance((char)n);
                }
                return NIL;
            case CHARACTERP:                    // ### characterp
                return arg instanceof LispCharacter ? T : NIL;
            case BOTH_CASE_P: {                 // ### both-case-p
                char c = LispCharacter.getValue(arg);
                if (Character.isLowerCase(c) || Character.isUpperCase(c))
                    return T;
                return NIL;
            }
            case LOWER_CASE_P:                  // ### lower-case-p
                return Character.isLowerCase(LispCharacter.getValue(arg)) ? T : NIL;
            case UPPER_CASE_P:                  // ### upper-case-p
                return Character.isUpperCase(LispCharacter.getValue(arg)) ? T : NIL;
            case CHAR_DOWNCASE:                 // ### char-downcase
                return LispCharacter.getInstance(Utilities.toLowerCase(LispCharacter.getValue(arg)));
            case CHAR_UPCASE:                   // ### char-upcase
                return LispCharacter.getInstance(Utilities.toUpperCase(LispCharacter.getValue(arg)));
            case STRINGP:                       // ### stringp
                return arg.STRINGP();
            case SIMPLE_STRING_P:               // ### simple-string-p
                return arg.SIMPLE_STRING_P();
            case SUCCESSOR:                     // ### 1+
                return arg.incr();
            case PREDECESSOR:                   // ### 1-
                return arg.decr();
            case VALUES_LIST:                   // ### values-list
                return values(arg.copyToArray());
            case EVAL:                          // ### eval
                return eval(arg, new Environment(), LispThread.currentThread());
            default:
                Debug.trace("bad index " + index);
                throw new ConditionThrowable(new WrongNumberOfArgumentsException((String)null));
        }
    }

    // Primitive2
    public LispObject dispatch(LispObject first, LispObject second, int index)
        throws ConditionThrowable
    {
        switch (index) {
            case MEMBER: {                      // ### member
                // member item list &key key test test-not => tail
                // FIXME Support keyword arguments!
                LispObject rest = checkList(second);
                while (rest != NIL) {
                    if (first.eql(rest.car()))
                        return rest;
                    rest = rest.cdr();
                }
                return NIL;
            }
            case RPLACA:                        // ### rplaca
                first.setCar(second);
                return first;
            case RPLACD:                        // ### rplacd
                first.setCdr(second);
                return first;
            case SET:                           // ### set
                checkSymbol(first).setSymbolValue(second);
                return second;
            default:
                Debug.trace("bad index " + index);
                throw new ConditionThrowable(new WrongNumberOfArgumentsException((String)null));
        }
    }

    // ### eq
    private static final Primitive2 EQ = new Primitive2("eq") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first == second ? T : NIL;
        }
    };

    // ### eql
    private static final Primitive2 EQL = new Primitive2("eql") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.eql(second) ? T : NIL;
        }
    };

    // ### equal
    private static final Primitive2 EQUAL = new Primitive2("equal") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.equal(second) ? T : NIL;
        }
    };

    // ### equalp
    private static final Primitive2 EQUALP = new Primitive2("equalp") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.equalp(second) ? T : NIL;
        }
    };

    // ### cons
    private static final Primitive2 CONS = new Primitive2("cons") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new Cons(first, second);
        }
    };

    // ### elt
    private static final Primitive2 ELT = new Primitive2("elt") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.elt(Fixnum.getValue(second));
        }
    };

    // ### atom
    private static final Primitive1 ATOM = new Primitive1("atom") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg instanceof Cons ? NIL : T;
        }
    };

    // ### constantp
    private static final Primitive CONSTANTP = new Primitive("constantp") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.CONSTANTP();
        }
        public LispObject execute(LispObject first, LispObject second) throws ConditionThrowable
        {
            return first.CONSTANTP();
        }
    };

    // ### symbolp
    private static final Primitive1 SYMBOLP = new Primitive1("symbolp") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.SYMBOLP();
        }
    };

    // ### endp
    private static final Primitive1 ENDP = new Primitive1("endp") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.ENDP();
        }
    };

    // ### null
    private static final Primitive1 NULL = new Primitive1("null") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg == NIL ? T : NIL;
        }
    };

    // ### not
    private static final Primitive1 NOT = new Primitive1("not") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg == NIL ? T : NIL;
        }
    };

    // ### plusp
    private static final Primitive1 PLUSP = new Primitive1("plusp") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.PLUSP();
        }
    };

    // ### minusp
    private static final Primitive1 MINUSP = new Primitive1("minusp") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.MINUSP();
        }
    };

    // ### zerop
    private static final Primitive1 ZEROP = new Primitive1("zerop") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.ZEROP();
        }
    };

    // ### fixnump
    private static final Primitive1 FIXNUMP =
        new Primitive1("fixnump", PACKAGE_EXT, true) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg instanceof Fixnum ? T : NIL;
        }
    };

    // ### +
    private static final Primitive ADD = new Primitive("+") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.add(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            LispObject result = Fixnum.ZERO;
            final int length = args.length;
            for (int i = 0; i < length; i++)
                result = result.add(args[i]);
            return result;
        }
    };

    // ### -
    private static final Primitive SUBTRACT = new Primitive("-") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.subtract(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            switch (args.length) {
                case 0:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException("-"));
                case 1:
                    return Fixnum.ZERO.subtract(args[0]);
                case 2:
                    Debug.assertTrue(false);
                    return args[0].subtract(args[1]);
                default: {
                    LispObject result = args[0];
                    for (int i = 1; i < args.length; i++)
                        result = result.subtract(args[i]);
                    return result;
                }
            }
        }
    };

    // ### when
    private static final SpecialOperator WHEN = new SpecialOperator("when") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args == NIL)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject result = NIL;
            if (eval(args.car(), env, thread) != NIL) {
                args = args.cdr();
                while (args != NIL) {
                    result = eval(args.car(), env, thread);
                    args = args.cdr();
                }
            }
            return result;
        }
    };

    // ### unless
    private static final SpecialOperator UNLESS =
        new SpecialOperator("unless") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args == NIL)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject result = NIL;
            if (eval(args.car(), env, thread) == NIL) {
                args = args.cdr();
                while (args != NIL) {
                    result = eval(args.car(), env, thread);
                    args = args.cdr();
                }
            }
            return result;
        }
    };

    // ### %write
    // %write object stream => object
    private static final Primitive2 _WRITE =
        new Primitive2("%write", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            outSynonymOf(second).writeString(String.valueOf(first));
            return first;
        }
    };

    // ### princ
    // princ object &optional output-stream => object
    private static final Primitive PRINC = new Primitive("princ") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final CharacterOutputStream out;
            if (args.length == 1)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[1]);
            out.princ(args[0]);
            return args[0];
        }
    };

    // ### princ-to-string
    private static final Primitive1 PRINC_TO_STRING =
        new Primitive1("princ-to-string") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            LispThread thread = LispThread.currentThread();
            Environment oldDynEnv = thread.getDynamicEnvironment();
            thread.bindSpecial(_PRINT_ESCAPE_, NIL);
            LispString string = new LispString(String.valueOf(arg));
            thread.setDynamicEnvironment(oldDynEnv);
            return string;
        }
    };

    // ### prin1
    // prin1 object &optional output-stream => object
    private static final Primitive PRIN1 = new Primitive("prin1") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            CharacterOutputStream out =
                checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            out.prin1(arg);
            return arg;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            outSynonymOf(second).prin1(first);
            return first;
        }
    };

    // ### prin1-to-string
    private static final Primitive1 PRIN1_TO_STRING =
        new Primitive1("prin1-to-string") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new LispString(String.valueOf(arg));
        }
    };

    // ### print
    // print object &optional output-stream => object
    // PRINT is just like PRIN1 except that the printed representation of
    // object is preceded by a newline and followed by a space.
    private static final Primitive1 PRINT = new Primitive1("print") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            CharacterOutputStream out =
                checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            out.terpri();
            out.prin1(arg);
            out.writeString(" ");
            return arg;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            CharacterOutputStream out = outSynonymOf(second);
            out.terpri();
            out.prin1(first);
            out.writeString(" ");
            return first;
        }
    };

    // ### terpri
    // terpri &optional output-stream => nil
    private static final Primitive TERPRI = new Primitive("terpri") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final CharacterOutputStream out;
            if (args.length == 0)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[0]);
            return out.terpri();
        }
    };

    // ### fresh-line
    // fresh-line &optional output-stream => generalized-boolean
    private static final Primitive FRESH_LINE = new Primitive("fresh-line") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            CharacterOutputStream out;
            if (args.length == 0)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[0]);
            return out.freshLine();
        }
    };

    // ### boundp
    private static final Primitive1 BOUNDP = new Primitive1("boundp") {
        public LispObject execute(LispObject obj) throws ConditionThrowable
        {
            if (obj == NIL)
                return T;
            Symbol symbol = checkSymbol(obj);
            if (LispThread.currentThread().lookupSpecial(symbol) != null)
                return T;
            return symbol.getSymbolValue() != null ? T : NIL;
        }
    };

    // ### append
    public static final Primitive APPEND = new Primitive("append") {
        public LispObject execute()
        {
            return NIL;
        }
        public LispObject execute(LispObject arg)
        {
            return arg;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first == NIL)
                return second;
            // APPEND is required to copy its first argument.
            Cons result = new Cons(first.car());
            Cons splice = result;
            first = first.cdr();
            while (first != NIL) {
                Cons temp = new Cons(first.car());
                splice.setCdr(temp);
                splice = temp;
                first = first.cdr();
            }
            splice.setCdr(second);
            return result;
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            Cons result = null;
            Cons splice = null;
            final int limit = args.length - 1;
            int i;
            for (i = 0; i < limit; i++) {
                LispObject top = args[i];
                if (top == NIL)
                    continue;
                result = new Cons(top.car());
                splice = result;
                top = top.cdr();
                while (top != NIL) {
                    Cons temp = new Cons(top.car());
                    splice.setCdr(temp);
                    splice = temp;
                    top = top.cdr();
                }
                break;
            }
            if (result == null)
                return args[i];
            for (++i; i < limit; i++) {
                LispObject top = args[i];
                while (top != NIL) {
                    Cons temp = new Cons(top.car());
                    splice.setCdr(temp);
                    splice = temp;
                    top = top.cdr();
                }
            }
            splice.setCdr(args[i]);
            return result;
        }
    };

    // ### nconc
    private static final Primitive NCONC = new Primitive("nconc") {
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            switch (array.length) {
                case 0:
                    return NIL;
                case 1:
                    return array[0];
                default: {
                    LispObject result = null;
                    LispObject splice = null;
                    final int limit = array.length - 1;
                    int i;
                    for (i = 0; i < limit; i++) {
                        LispObject list = array[i];
                        if (list == NIL)
                            continue;
                        if (list instanceof Cons) {
                            if (splice != null) {
                                splice.setCdr(list);
                                splice = list;
                            }
                            while (list instanceof Cons) {
                                if (result == null) {
                                    result = list;
                                    splice = result;
                                } else {
                                    splice = list;
                                }
                                list = list.cdr();
                            }
                        } else
                            throw new ConditionThrowable(new TypeError(list, "list"));
                    }
                    if (result == null)
                        return array[i];
                    splice.setCdr(array[i]);
                    return result;
                }
            }
        }
    };

    // ### =
    // Numeric equality.
    private static final Primitive EQUALS = new Primitive("=") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isEqualTo(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispObject obj = array[0];
            for (int i = 1; i < length; i++) {
                if (array[i].isNotEqualTo(obj))
                    return NIL;
            }
            return T;
        }
    };

    // Returns true if no two numbers are the same; otherwise returns false.
    private static final Primitive NOT_EQUALS = new Primitive("/=") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isNotEqualTo(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length == 2)
                return array[0].isNotEqualTo(array[1]) ? T : NIL;
            if (length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            for (int i = 0; i < length; i++) {
                final LispObject obj = array[i];
                for (int j = i+1; j < length; j++) {
                    if (array[j].isEqualTo(obj))
                        return NIL;
                }
            }
            return T;
        }
    };

    // ### <
    // Numeric comparison.
    private static final Primitive LESS_THAN = new Primitive("<") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isLessThan(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            for (int i = 1; i < length; i++) {
                if (array[i].isLessThanOrEqualTo(array[i-1]))
                    return NIL;
            }
            return T;
        }
    };

    // ### <=
    private static final Primitive LE = new Primitive("<=") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isLessThanOrEqualTo(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            switch (array.length) {
                case 0:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
                case 1:
                    return T;
                case 2:
                    Debug.assertTrue(false);
                    return array[0].isLessThanOrEqualTo(array[1]) ? T : NIL;
                default: {
                    final int length = array.length;
                    for (int i = 1; i < length; i++) {
                        if (array[i].isLessThan(array[i-1]))
                            return NIL;
                    }
                    return T;
                }
            }
        }
    };

    // ### >
    private static final Primitive GREATER_THAN = new Primitive(">") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isGreaterThan(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            for (int i = 1; i < length; i++) {
                if (array[i].isGreaterThanOrEqualTo(array[i-1]))
                    return NIL;
            }
            return T;
        }
    };

    // ### >=
    private static final Primitive GE = new Primitive(">=") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isGreaterThanOrEqualTo(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            switch (length) {
                case 0:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
                case 1:
                    return T;
                case 2:
                    Debug.assertTrue(false);
                    return array[0].isGreaterThanOrEqualTo(array[1]) ? T : NIL;
                default:
                    for (int i = 1; i < length; i++) {
                        if (array[i].isGreaterThan(array[i-1]))
                            return NIL;
                    }
                    return T;
            }
        }
    };

    // ### assoc
    // assoc item alist &key key test test-not => entry
    // This is the bootstrap version (needed for %set-documentation).
    // Redefined properly in list.lisp.
    private static final Primitive ASSOC = new Primitive("assoc") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject item = args[0];
            LispObject alist = args[1];
            while (alist != NIL) {
                LispObject cons = alist.car();
                if (cons instanceof Cons) {
                    if (cons.car().eql(item))
                        return cons;
                } else if (cons != NIL)
                    throw new ConditionThrowable(new TypeError(cons, "list"));
                alist = alist.cdr();
            }
            return NIL;
        }
    };

    // ### nth
    // nth n list => object
    private static final Primitive2 NTH = new Primitive2("nth") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            int index = Fixnum.getValue(first);
            if (index < 0)
                throw new ConditionThrowable(new LispError("bad index to NTH: " + index));
            int i = 0;
            while (true) {
                if (i == index)
                    return second.car();
                second = second.cdr();
                if (second == NIL)
                    return NIL;
                ++i;
            }
        }
    };

    // ### %setnth
    // %setnth n list new-object => new-object
    private static final Primitive3 _SETNTH =
        new Primitive3("%setnth", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            int index = Fixnum.getValue(first);
            if (index < 0)
                throw new ConditionThrowable(new LispError("bad index to NTH: " + index));
            int i = 0;
            while (true) {
                if (i == index) {
                    second.setCar(third);
                    return third;
                }
                second = second.cdr();
                if (second == NIL)
                    throw new ConditionThrowable(new LispError(String.valueOf(index)) +
                        "is too large an index for SETF of NTH");
                ++i;
            }
        }
    };

    // ### nthcdr
    private static final Primitive2 NTHCDR = new Primitive2("nthcdr") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            final int index = Fixnum.getValue(first);
            if (index < 0)
                throw new ConditionThrowable(new TypeError("bad index to NTHCDR: " + index));
            for (int i = 0; i < index; i++) {
                second = second.cdr();
                if (second == NIL)
                    return NIL;
            }
            return second;
        }
    };

    // ### error
    private static final Primitive ERROR = new Primitive("error") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject datum = args[0];
            if (datum instanceof LispError)
                throw new ConditionThrowable((LispError)datum);
            if (datum instanceof Symbol) {
                if (datum == Symbol.PACKAGE_ERROR)
                    throw new ConditionThrowable(new PackageError(_format(args, 1)));
                if (datum == Symbol.PARSE_ERROR)
                    throw new ConditionThrowable(new ParseError(_format(args, 1)));
                if (datum == Symbol.PROGRAM_ERROR)
                    throw new ConditionThrowable(new ProgramError(_format(args, 1)));
                if (datum == Symbol.TYPE_ERROR)
                    throw new ConditionThrowable(new TypeError(_format(args, 1)));
                // Default.
                throw new ConditionThrowable(new SimpleError(((Symbol)datum).getName()));
            }
            throw new ConditionThrowable(new SimpleError(_format(args)));
        }
    };

    // ### signal
    private static final Primitive SIGNAL = new Primitive("signal") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (args[0] instanceof Condition)
                throw new ConditionThrowable((Condition)args[0]);
            throw new ConditionThrowable(new SimpleCondition());
        }
    };

    // ### format
    private static final Primitive FORMAT = new Primitive("format") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject destination = args[0];
            // Copy remaining arguments.
            LispObject[] _args = new LispObject[args.length-1];
            for (int i = 0; i < _args.length; i++)
                _args[i] = args[i+1];
            String s = _format(_args);
            if (destination == T) {
                checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue()).writeString(s);
                return NIL;
            }
            if (destination == NIL)
                return new LispString(s);
            if (destination instanceof CharacterOutputStream) {
                ((CharacterOutputStream)destination).writeString(s);
                return NIL;
            }
            if (destination instanceof TwoWayStream) {
                LispOutputStream out = ((TwoWayStream)destination).getOutputStream();
                if (out instanceof CharacterOutputStream) {
                    ((CharacterOutputStream)out).writeString(s);
                    return NIL;
                }
                throw new ConditionThrowable(new TypeError(destination, "character output stream"));
            }
            // Destination can also be a string with a fill pointer.
//             throw new ConditionThrowable(new LispError("FORMAT: not implemented"));
            return NIL;
        }
    };

    private static final String _format(LispObject[] args, int skip)
        throws ConditionThrowable
    {
        final int remaining = args.length - skip;
        if (remaining > 0) {
            LispObject[] array = new LispObject[remaining];
            for (int i = skip, j = 0; i < args.length; i++, j++)
                array[j] = args[i];
            return _format(array);
        } else
            return null;
    }

    private static final String _format(LispObject[] args) throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        String control = checkString(args[0]).getValue();
        StringBuffer sb = new StringBuffer();
        final int limit = control.length();
        int j = 1;
        final int NEUTRAL = 0;
        final int TILDE = 1;
        int state = NEUTRAL;
        for (int i = 0; i < limit; i++) {
            char c = control.charAt(i);
            if (state == NEUTRAL) {
                if (c == '~')
                    state = TILDE;
                else
                    sb.append(c);
            } else if (state == TILDE) {
                if (c == 'A' || c == 'a') {
                    if (j < args.length) {
                        LispObject obj = args[j++];
                        Environment oldDynEnv = thread.getDynamicEnvironment();
                        thread.bindSpecial(_PRINT_ESCAPE_, NIL);
                        sb.append(String.valueOf(obj));
                        thread.setDynamicEnvironment(oldDynEnv);
                    }
                } else if (c == 'S' || c == 's') {
                    if (j < args.length) {
                        LispObject obj = args[j++];
                        Environment oldDynEnv = thread.getDynamicEnvironment();
                        thread.bindSpecial(_PRINT_ESCAPE_, T);
                        sb.append(String.valueOf(obj));
                        thread.setDynamicEnvironment(oldDynEnv);
                    }
                } else if (c == 'D' || c == 'd') {
                    if (j < args.length) {
                        LispObject obj = args[j++];
                        Environment oldDynEnv = thread.getDynamicEnvironment();
                        thread.bindSpecial(_PRINT_ESCAPE_, NIL);
                        thread.bindSpecial(_PRINT_RADIX_, NIL);
                        thread.bindSpecial(_PRINT_BASE_, new Fixnum(10));
                        sb.append(String.valueOf(obj));
                        thread.setDynamicEnvironment(oldDynEnv);
                    }
                } else if (c == 'X' || c == 'x') {
                    if (j < args.length) {
                        LispObject obj = args[j++];
                        Environment oldDynEnv = thread.getDynamicEnvironment();
                        thread.bindSpecial(_PRINT_ESCAPE_, NIL);
                        thread.bindSpecial(_PRINT_RADIX_, NIL);
                        thread.bindSpecial(_PRINT_BASE_, new Fixnum(16));
                        sb.append(String.valueOf(obj));
                        thread.setDynamicEnvironment(oldDynEnv);
                    }
                } else if (c == '%') {
                    sb.append(System.getProperty("line.separator"));
                }
//                 else
//                     throw new ConditionThrowable(new LispError("FORMAT: not implemented"));
                state = NEUTRAL;
            } else {
                // There are no other valid states.
                Debug.assertTrue(false);
            }
        }
        return sb.toString();
    }

    // ### %defun
    // %defun name arglist body => name
    private static final Primitive3 _DEFUN =
        new Primitive3("%defun", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            if (first instanceof Symbol) {
                Symbol symbol = checkSymbol(first);
                if (symbol.getSymbolFunction() instanceof SpecialOperator) {
                    String message =
                        symbol.getName() + " is a special operator and may not be redefined";
                    throw new ConditionThrowable(new ProgramError(message));
                }
                LispObject arglist = checkList(second);
                LispObject body = checkList(third);
                if (body.car() instanceof LispString && body.cdr() != NIL) {
                    // Documentation.
                    symbol.setFunctionDocumentation(body.car());
                    body = body.cdr();
                }
                body = new Cons(symbol, body);
                body = new Cons(Symbol.BLOCK, body);
                body = new Cons(body, NIL);
                Closure closure = new Closure(symbol.getName(), arglist, body,
                                              new Environment());
                closure.setArglist(arglist);
                symbol.setSymbolFunction(closure);
                return symbol;
            }
            if (first instanceof Cons && first.car() == Symbol.SETF) {
                Symbol symbol = checkSymbol(first.cadr());
//                 if (symbol.getSymbolFunction() instanceof SpecialOperator) {
//                     String message =
//                         symbol.getName() + " is a special operator and may not be redefined";
//                     throw new ConditionThrowable(new ProgramError(message));
//                 }
                LispObject arglist = checkList(second);
                LispObject body = checkList(third);
                if (body.car() instanceof LispString && body.cdr() != NIL) {
                    // Documentation.
//                     symbol.setFunctionDocumentation(body.car());
                    body = body.cdr();
                }
                body = new Cons(symbol, body);
                body = new Cons(Symbol.BLOCK, body);
                body = new Cons(body, NIL);
                Closure closure = new Closure(arglist, body, new Environment());
                closure.setArglist(arglist);
//                 symbol.setSymbolFunction(closure);
                put(symbol, PACKAGE_SYS.intern("SETF-FUNCTION"), closure);
                return symbol;
            }
            throw new ConditionThrowable(new TypeError(first, "valid function name"));
        }
    };

    // ### lambda
    private static final SpecialOperator LAMBDA =
        new SpecialOperator("lambda") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            return new Closure(args.car(), args.cdr(), env);
        }
    };

    // ### macro-function
    // Need to support optional second argument specifying environment.
    private static final Primitive MACRO_FUNCTION =
        new Primitive("macro-function") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            Symbol symbol = checkSymbol(args[0]);
            LispObject obj = symbol.getSymbolFunction();
            if (obj instanceof MacroObject)
                return ((MacroObject)obj).getExpander();
            if (obj instanceof SpecialOperator) {
                LispObject macroObject =
                    get(symbol, Symbol.MACROEXPAND_MACRO, NIL);
                if (macroObject instanceof MacroObject)
                    return ((MacroObject)macroObject).getExpander();
                return NIL;
            }
            if (obj instanceof AutoloadMacro)
                return obj;

            return NIL;
        }
    };

    // ### defmacro
    private static final SpecialOperator DEFMACRO =
        new SpecialOperator("defmacro") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(args.car());
            LispObject lambdaList = checkList(args.cadr());
            LispObject body = args.cddr();
            LispObject block = new Cons(Symbol.BLOCK, new Cons(symbol, body));
            LispObject toBeApplied = list3(Symbol.LAMBDA, lambdaList, block);
            LispObject formArg = gensym("FORM-");
            LispObject envArg = gensym("ENV-"); // Ignored.
            LispObject expander =
                list3(Symbol.LAMBDA, list2(formArg, envArg),
                      list3(Symbol.APPLY, toBeApplied,
                            list2(Symbol.CDR, formArg)));
            Closure expansionFunction =
                new Closure(expander.cadr(), expander.cddr(), env);
            MacroObject macroObject = new MacroObject(expansionFunction);
            if (symbol.getSymbolFunction() instanceof SpecialOperator)
                put(symbol, Symbol.MACROEXPAND_MACRO, macroObject);
            else
                symbol.setSymbolFunction(macroObject);
            LispThread.currentThread().clearValues();
            return symbol;
        }
    };

    // ### make-macro
    private static final Primitive1 MAKE_MACRO =
        new Primitive1("make-macro", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new MacroObject(arg);
        }
    };

    // ### %defparameter
    private static final Primitive3 _DEFPARAMETER =
        new Primitive3("%defparameter", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            if (third instanceof LispString)
                symbol.setVariableDocumentation(third);
            else if (third != NIL)
                throw new ConditionThrowable(new TypeError(third, "string"));
            symbol.setSymbolValue(second);
            symbol.setSpecial(true);
            return symbol;
        }
    };

    // ### %defvar
    private static final Primitive1 _DEFVAR =
        new Primitive1("%defvar", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(arg);
            symbol.setSpecial(true);
            return symbol;
        }
    };

    // ### %defconstant
    private static final Primitive3 _DEFCONSTANT =
        new Primitive3("%defconstant", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            if (third instanceof LispString)
                symbol.setVariableDocumentation(third);
            else if (third != NIL)
                throw new ConditionThrowable(new TypeError(third, "string"));
            symbol.setSymbolValue(second);
            symbol.setConstant(true);
            return symbol;
        }
    };

    // ### cond
    private static final SpecialOperator COND = new SpecialOperator("cond") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject result = NIL;
            while (args != NIL) {
                LispObject clause = args.car();
                result = eval(clause.car(), env, thread);
                thread.clearValues();
                if (result != NIL) {
                    LispObject body = clause.cdr();
                    while (body != NIL) {
                        result = eval(body.car(), env, thread);
                        body = body.cdr();
                    }
                    return result;
                }
                args = args.cdr();
            }
            return result;
        }
    };

    // ### case
    private static final SpecialOperator CASE = new SpecialOperator("case") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject key = eval(args.car(), env, thread);
            args = args.cdr();
            while (args != NIL) {
                LispObject clause = args.car();
                LispObject keys = clause.car();
                boolean match = false;
                if (keys.listp()) {
                    while (keys != NIL) {
                        LispObject candidate = keys.car();
                        if (key.eql(candidate)) {
                            match = true;
                            break;
                        }
                        keys = keys.cdr();
                    }
                } else {
                    LispObject candidate = keys;
                    if (candidate == T || candidate == Symbol.OTHERWISE)
                        match = true;
                    else if (key.eql(candidate))
                        match = true;
                }
                if (match) {
                    return progn(clause.cdr(), env, thread);
                }
                args = args.cdr();
            }
            return NIL;
        }
    };

    // ### ecase
    private static final SpecialOperator ECASE = new SpecialOperator("ecase") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject key = eval(args.car(), env, thread);
            args = args.cdr();
            while (args != NIL) {
                LispObject clause = args.car();
                LispObject keys = clause.car();
                boolean match = false;
                if (keys instanceof Cons) {
                    while (keys != NIL) {
                        LispObject candidate = keys.car();
                        if (key.eql(candidate)) {
                            match = true;
                            break;
                        }
                        keys = keys.cdr();
                    }
                } else {
                    LispObject candidate = keys;
                    if (key.eql(candidate))
                        match = true;
                }
                if (match) {
                    return progn(clause.cdr(), env, thread);
                }
                args = args.cdr();
            }
            throw new ConditionThrowable(new TypeError("ECASE: no match for " + key));
        }
    };

    // ### handler-bind
    private static final SpecialOperator HANDLER_BIND =
        new SpecialOperator("handler-bind")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            LispObject bindings = checkList(args.car());
            final LispThread thread = LispThread.currentThread();
            LispObject forms = args.cdr();
            try {
                return progn(args.cdr(), env, thread);
            }
            catch (Return ret) {
                throw ret;
            }
            catch (ConditionThrowable c) {
                Condition condition = c.getCondition();
                while (bindings != NIL) {
                    Cons binding = checkCons(bindings.car());
                    LispObject type = binding.car();
                    if (condition.typep(type) != NIL) {
                        LispObject obj = eval(binding.cadr(), env, thread);
                        LispObject handler;
                        if (obj instanceof Symbol) {
                            handler = obj.getSymbolFunction();
                            if (handler == null)
                                throw new ConditionThrowable(new UndefinedFunction(obj));
                        } else
                            handler = obj;
                        LispObject[] handlerArgs = new LispObject[1];
                        handlerArgs[0] = condition;
                         // Might not return.
                        funcall(handler, handlerArgs, thread);
                    }
                    bindings = bindings.cdr();
                }
                // Re-throw.
                throw c;
            }
        }
    };

    // ### handler-case
    private static final SpecialOperator HANDLER_CASE =
        new SpecialOperator("handler-case")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            LispObject form = args.car();
            LispObject clauses = args.cdr();
            final LispThread thread = LispThread.currentThread();
            final int depth = thread.getStackDepth();
            LispObject result;
            try {
                result = eval(form, env, thread);
            }
            catch (ConditionThrowable c) {
                Condition condition = c.getCondition();
                thread.setStackDepth(depth);
                while (clauses != NIL) {
                    Cons clause = checkCons(clauses.car());
                    LispObject type = clause.car();
                    if (condition.typep(type) != NIL) {
                        LispObject parameterList = clause.cadr();
                        LispObject body = clause.cdr().cdr();
                        Closure handler = new Closure(parameterList, body, env);
                        int numArgs = parameterList.length();
                        if (numArgs == 1) {
                            LispObject[] handlerArgs = new LispObject[1];
                            handlerArgs[0] = condition;
                            return funcall(handler, handlerArgs, thread);
                        }
                        if (numArgs == 0) {
                            LispObject[] handlerArgs = new LispObject[0];
                            return funcall(handler, handlerArgs, thread);
                        }
                        throw new ConditionThrowable(new LispError("HANDLER-CASE: invalid handler clause"));
                    }
                    clauses = clauses.cdr();
                }
                // Re-throw.
                throw c;
            }
            // No error.
            while (clauses != NIL) {
                Cons clause = checkCons(clauses.car());
                if (clause.car() == Keyword.NO_ERROR) {
                    Closure closure = new Closure(clause.cadr(), clause.cddr(),
                                                  env);
                    if (thread.getValues() != null)
                        result = closure.execute(thread.getValues());
                    else
                        result = closure.execute(result);
                    break;
                }
                clauses = clauses.cdr();
            }
            return result;
        }
    };

    // ### upgraded-array-element-type
    // upgraded-array-element-type typespec &optional environment
    // => upgraded-typespec
    private static final Primitive UPGRADED_ARRAY_ELEMENT_TYPE =
        new Primitive("upgraded-array-element-type") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return getUpgradedArrayElementType(arg);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            // Ignore environment.
            return getUpgradedArrayElementType(first);
        }
    };

    // ### array-rank
    // array-rank array => rank
    private static final Primitive1 ARRAY_RANK =
        new Primitive1("array-rank") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Fixnum(checkArray(arg).getRank());
        }
    };

    // ### array-dimensions
    // array-dimensions array => dimensions
    // Returns a list of integers. Fill pointer (if any) is ignored.
    private static final Primitive1 ARRAY_DIMENSIONS =
        new Primitive1("array-dimensions") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return checkArray(arg).getDimensions();
        }
    };

    // ### array-dimension
    // array-dimension array axis-number => dimension
    private static final Primitive2 ARRAY_DIMENSION =
        new Primitive2("array-dimension") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new Fixnum(checkArray(first).getDimension(Fixnum.getValue(second)));
        }
    };

    // ### array-total-size
    // array-total-size array => size
    private static final Primitive1 ARRAY_TOTAL_SIZE =
        new Primitive1("array-total-size") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Fixnum(checkArray(arg).getTotalSize());
        }
    };


    // ### array-element-type
    // array-element-type array => typespec
    private static final Primitive1 ARRAY_ELEMENT_TYPE =
        new Primitive1("array-element-type") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return checkArray(arg).getElementType();
        }
    };

    // ### array-in-bounds-p
    // array-in-bounds-p array &rest subscripts => generalized-boolean
    private static final Primitive ARRAY_IN_BOUNDS_P =
        new Primitive("array-in-bounds-p") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            AbstractArray array = checkArray(args[0]);
            int rank = array.getRank();
            if (rank != args.length - 1) {
                StringBuffer sb = new StringBuffer("ARRAY-IN-BOUNDS-P: ");
                sb.append("wrong number of subscripts (");
                sb.append(args.length - 1);
                sb.append(") for array of rank ");
                sb.append(rank);
                throw new ConditionThrowable(new ProgramError(sb.toString()));
            }
            for (int i = 0; i < rank; i++) {
                LispObject arg = args[i+1];
                if (arg instanceof Fixnum) {
                    int subscript = ((Fixnum)arg).getValue();
                    if (subscript < 0 || subscript >= array.getDimension(i))
                        return NIL;
                } else if (arg instanceof Bignum) {
                    return NIL;
                } else
                    throw new ConditionThrowable(new TypeError(arg, "integer"));
            }
            return T;
        }
    };

    // ### %array-row-major-index
    // %array-row-major-index array subscripts => index
    private static final Primitive2 _ARRAY_ROW_MAJOR_INDEX =
        new Primitive2("%array-row-major-index", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractArray array = checkArray(first);
            LispObject[] subscripts = second.copyToArray();
            return number(arrayRowMajorIndex(array, subscripts));
        }
    };

    // ### aref
    // aref array &rest subscripts => element
    private static final Primitive AREF = new Primitive("aref") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            AbstractArray array = checkArray(arg);
            if (array.getRank() == 0)
                return array.getRowMajor(0);
            StringBuffer sb = new StringBuffer("AREF: ");
            sb.append("wrong number of subscripts (0) for array of rank ");
            sb.append(array.getRank());
            throw new ConditionThrowable(new ProgramError(sb.toString()));
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.AREF(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            AbstractArray array = checkArray(args[0]);
            LispObject[] subscripts = new LispObject[args.length - 1];
            for (int i = subscripts.length; i-- > 0;)
                subscripts[i] = args[i+1];
            int rowMajorIndex = arrayRowMajorIndex(array, subscripts);
            return array.getRowMajor(rowMajorIndex);
        }
    };

    private static final int arrayRowMajorIndex(AbstractArray array,
                                                LispObject[] subscripts) throws ConditionThrowable
    {
        final int rank = array.getRank();
        if (rank != subscripts.length) {
            StringBuffer sb = new StringBuffer("%ARRAY-ROW-MAJOR-INDEX: ");
            sb.append("wrong number of subscripts (");
            sb.append(subscripts.length);
            sb.append(") for array of rank ");
            sb.append(rank);
            throw new ConditionThrowable(new ProgramError(sb.toString()));
        }
        if (rank == 0)
            return 0;
        int sum = 0;
        int size = 1;
        for (int i = rank; i-- > 0;) {
            int dim = array.getDimension(i);
            int lastSize = size;
            size *= dim;
            LispObject subscript = subscripts[i];
            if (subscript instanceof Fixnum) {
                int n = ((Fixnum)subscript).getValue();
                if (n < 0 || n >= array.getDimension(i))
                    throw new ConditionThrowable(new ProgramError());
                sum += n * lastSize;
            } else if (subscript instanceof Bignum) {
                throw new ConditionThrowable(new ProgramError());
            } else
                throw new ConditionThrowable(new TypeError(subscript, "integer"));
        }
        return sum;
    }

    // ### row-major-aref
    // row-major-aref array index => element
    private static final Primitive2 ROW_MAJOR_AREF =
        new Primitive2("row-major-aref") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return checkArray(first).getRowMajor(Fixnum.getValue(second));
        }
    };

    // ### %set-row-major-aref
    // %set-row-major-aref array index new-value => new-value
    private static final Primitive3 _SET_ROW_MAJOR_AREF =
        new Primitive3("%set-row-major-aref", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            checkArray(first).setRowMajor(Fixnum.getValue(second), third);
            return third;
        }
    };

    // ### vector
    private static final Primitive VECTOR = new Primitive("vector") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            return new Vector(args);
        }
    };

    // ### svref
    // svref simple-vector index => element
    private static final Primitive2 SVREF = new Primitive2("svref") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(first);
            if (!v.isSimpleVector())
                throw new ConditionThrowable(new TypeError(first, "simple vector"));
            int index = v.checkIndex(second);
            return v.get(index);
        }
    };

    // ### %svset
    // %svset simple-vector index new-value => new-value
    private static final Primitive3 _SVSET =
        new Primitive3("%svset", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(first);
            if (!v.isSimpleVector())
                throw new ConditionThrowable(new TypeError(first, "simple vector"));
            int i = v.checkIndex(second);
            v.set(i, third);
            return third;
        }
    };

    // ### fill-pointer
    private static final Primitive1 FILL_POINTER =
        new Primitive1("fill-pointer") {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            int fillPointer = checkVector(arg).getFillPointer();
            if (fillPointer < 0)
                throw new ConditionThrowable(new TypeError("array does not have a fill pointer"));
            return new Fixnum(fillPointer);
        }
    };

    // ### %set-fill-pointer
    private static final Primitive2 _SET_FILL_POINTER =
        new Primitive2("%set-fill-pointer", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(first);
            int fillPointer = v.getFillPointer();
            if (fillPointer < 0)
                throw new ConditionThrowable(new TypeError("array does not have a fill pointer"));
            v.setFillPointer(second);
            return second;
        }
    };

    // ### vector-push
    // vector-push new-element vector => index-of-new-element
    private static final Primitive2 VECTOR_PUSH =
        new Primitive2("vector-push") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(second);
            int fillPointer = v.getFillPointer();
            if (fillPointer < 0)
                throw new ConditionThrowable(new TypeError("array does not have a fill pointer"));
            if (fillPointer >= v.capacity())
                return NIL;
            v.set(fillPointer, first);
            v.setFillPointer(fillPointer + 1);
            return new Fixnum(fillPointer);
        }
    };

    // ### vector-push-extend
    // vector-push new-element vector &optional extension => index-of-new-element
    private static final Primitive VECTOR_PUSH_EXTEND =
        new Primitive("vector-push-extend") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 2 || args.length > 3)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            AbstractVector v = checkVector(args[1]);
            int extension = 0;
            if (args.length == 3) {
                // Extension was supplied.
                extension = Fixnum.getValue(args[2]);
            }
            int fillPointer = v.getFillPointer();
            if (fillPointer < 0)
                throw new ConditionThrowable(new TypeError("array does not have a fill pointer"));
            if (fillPointer >= v.capacity()) {
                // Need to extend vector.
                extension = Math.max(extension, v.capacity() + 1);
                v.ensureCapacity(v.capacity() + extension);
            }
            v.set(fillPointer, args[0]);
            v.setFillPointer(fillPointer + 1);
            return new Fixnum(fillPointer);
        }
    };

    // ### vector-pop
    // vector-pop vector => element
    private static final Primitive1 VECTOR_POP = new Primitive1("vector-pop") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            AbstractVector v = checkVector(arg);
            int fillPointer = v.getFillPointer();
            if (fillPointer < 0)
                throw new ConditionThrowable(new TypeError("array does not have a fill pointer"));
            if (fillPointer == 0)
                throw new ConditionThrowable(new LispError("nothing left to pop"));
            int newFillPointer = v.checkIndex(fillPointer - 1);
            LispObject element = v.get(newFillPointer);
            v.setFillPointer(newFillPointer);
            return element;
        }
    };

    // ### type-of
    private static final Primitive1 TYPE_OF = new Primitive1("type-of") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.typeOf();
        }
    };

    // ### class-of
    private static final Primitive1 CLASS_OF = new Primitive1("class-of") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.classOf();
        }
    };

    // ### simple-typep
    private static final Primitive2 SIMPLE_TYPEP =
        new Primitive2("simple-typep", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.typep(second);
        }
    };

    // ### function-lambda-expression
    // function-lambda-expression function => lambda-expression, closure-p, name
    private static final Primitive1 FUNCTION_LAMBDA_EXPRESSION =
        new Primitive1("function-lambda-expression") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            LispObject[] values = new LispObject[3];
            Function function = checkFunction(arg);
            String name = function.getName();
            values[2] = name != null ? new LispString(name) : NIL;
            if (function instanceof Closure) {
                Closure closure = (Closure) function;
                LispObject expr = closure.getBody();
                expr = new Cons(closure.getParameterList(), expr);
                expr = new Cons(Symbol.LAMBDA, expr);
                values[0] = expr;
                Environment env = closure.getEnvironment();
                if (env == null || env.isEmpty())
                    values[1] = NIL;
                else
                    values[1] = T;
            } else
                values[0] = values[1] = NIL;
            LispThread.currentThread().setValues(values);
            return values[0];
        }
    };

    // ### funcall
    // This needs to be public for LispAPI.java.
    public static final Primitive FUNCALL = new Primitive("funcall") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            LispObject fun;
            if (arg instanceof Symbol)
                fun = arg.getSymbolFunction();
            else
                fun = arg;
            if (fun instanceof Function)
                return funcall0(fun, LispThread.currentThread());
            throw new ConditionThrowable(new UndefinedFunction(arg));
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            LispObject fun;
            if (first instanceof Symbol)
                fun = first.getSymbolFunction();
            else
                fun = first;
            if (fun instanceof Function)
                return funcall1(fun, second, LispThread.currentThread());
            throw new ConditionThrowable(new UndefinedFunction(first));
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            LispObject fun;
            if (first instanceof Symbol)
                fun = first.getSymbolFunction();
            else
                fun = first;
            if (fun instanceof Function)
                return funcall2(fun, second, third, LispThread.currentThread());
            throw new ConditionThrowable(new UndefinedFunction(first));
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject fun;
            if (args[0] instanceof Symbol)
                fun = args[0].getSymbolFunction();
            else
                fun = args[0];
            if (fun instanceof Function) {
                final int length = args.length - 1; // Number of arguments.
                if (length == 3) {
                    return funcall3(fun, args[1], args[2], args[3],
                                    LispThread.currentThread());
                } else {
                    LispObject[] funArgs = new LispObject[length];
                    System.arraycopy(args, 1, funArgs, 0, length);
                    return funcall(fun, funArgs, LispThread.currentThread());
                }
            }
            throw new ConditionThrowable(new UndefinedFunction(args[0]));
        }
    };

    // ### apply
    public static final Primitive APPLY = new Primitive("apply") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            LispObject spread = checkList(second);
            LispObject fun = first;
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (fun instanceof Function) {
                final int numFunArgs = spread.length();
                final LispThread thread = LispThread.currentThread();
                switch (numFunArgs) {
                    case 1:
                        return funcall1(fun, spread.car(), thread);
                    case 2:
                        return funcall2(fun, spread.car(), spread.cadr(), thread);
                    case 3:
                        return funcall3(fun, spread.car(), spread.cadr(),
                                        spread.cdr().cdr().car(), thread);
                    default: {
                        final LispObject[] funArgs = new LispObject[numFunArgs];
                        int j = 0;
                        while (spread != NIL) {
                            funArgs[j++] = spread.car();
                            spread = spread.cdr();
                        }
                        return funcall(fun, funArgs, thread);
                    }
                }
            }
            throw new ConditionThrowable(new TypeError(fun, "function"));
        }
        public LispObject execute(final LispObject[] args) throws ConditionThrowable
        {
            final int numArgs = args.length;
            if (numArgs < 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject spread = checkList(args[numArgs - 1]);
            LispObject fun = args[0];
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (fun instanceof Function) {
                final int numFunArgs = numArgs - 2 + spread.length();
                final LispObject[] funArgs = new LispObject[numFunArgs];
                int j = 0;
                for (int i = 1; i < numArgs - 1; i++)
                    funArgs[j++] = args[i];
                while (spread != NIL) {
                    funArgs[j++] = spread.car();
                    spread = spread.cdr();
                }
                return funcall(fun, funArgs, LispThread.currentThread());
            }
            throw new ConditionThrowable(new TypeError(fun, "function"));
        }
    };

    // ### mapcar
    private static final Primitive MAPCAR = new Primitive("mapcar") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            // First argument must be a function.
            LispObject fun = first;
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (!(fun instanceof Function))
                throw new ConditionThrowable(new UndefinedFunction(first));
            // Second argument must be a list.
            LispObject list = checkList(second);
            final LispThread thread = LispThread.currentThread();
            LispObject result = NIL;
            LispObject splice = null;
            while (list != NIL) {
                LispObject obj = funcall1(fun, list.car(), thread);
                if (splice == null) {
                    result = new Cons(obj, result);
                    splice = result;
                } else {
                    Cons cons = new Cons(obj);
                    splice.setCdr(cons);
                    splice = cons;
                }
                list = list.cdr();
            }
            return result;
        }
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            // First argument must be a function.
            LispObject fun = first;
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (!(fun instanceof Function))
                throw new ConditionThrowable(new UndefinedFunction(first));
            // Remaining arguments must be lists.
            LispObject list1 = checkList(second);
            LispObject list2 = checkList(third);
            final LispThread thread = LispThread.currentThread();
            LispObject result = NIL;
            LispObject splice = null;
            while (list1 != NIL && list2 != NIL) {
                LispObject obj =
                    funcall2(fun, list1.car(), list2.car(), thread);
                if (splice == null) {
                    result = new Cons(obj, result);
                    splice = result;
                } else {
                    Cons cons = new Cons(obj);
                    splice.setCdr(cons);
                    splice = cons;
                }
                list1 = list1.cdr();
                list2 = list2.cdr();
            }
            return result;
        }
        public LispObject execute(final LispObject[] args) throws ConditionThrowable
        {
            final int numArgs = args.length;
            if (numArgs < 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            // First argument must be a function.
            LispObject fun = args[0];
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (!(fun instanceof Function))
                throw new ConditionThrowable(new UndefinedFunction(args[0]));
            // Remaining arguments must be lists.
            int commonLength = -1;
            for (int i = 1; i < numArgs; i++) {
                if (!args[i].listp())
                    throw new ConditionThrowable(new TypeError(args[i], "list"));
                int len = args[i].length();
                if (commonLength < 0)
                    commonLength = len;
                else if (commonLength > len)
                    commonLength = len;
            }
            final LispThread thread = LispThread.currentThread();
            LispObject[] results = new LispObject[commonLength];
            final int numFunArgs = numArgs - 1;
            final LispObject[] funArgs = new LispObject[numFunArgs];
            for (int i = 0; i < commonLength; i++) {
                for (int j = 0; j < numFunArgs; j++)
                    funArgs[j] = args[j+1].car();
                results[i] = funcall(fun, funArgs, thread);
                for (int j = 1; j < numArgs; j++)
                    args[j] = args[j].cdr();
            }
            LispObject result = NIL;
            for (int i = commonLength; i-- > 0;)
                result = new Cons(results[i], result);
            return result;
        }
    };

    // ### macroexpand
    private static final Primitive MACROEXPAND = new Primitive("macroexpand") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length < 1 || length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject form = args[0];
            final Environment env;
            if (length == 2 && args[1] != NIL)
                env = checkEnvironment(args[1]);
            else
                env = new Environment();
            return macroexpand(form, env, LispThread.currentThread());
        }
    };

    // ### macroexpand-1
    private static final Primitive MACROEXPAND_1 =
        new Primitive("macroexpand-1") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length < 1 || length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject form = args[0];
            final Environment env;
            if (length == 2 && args[1] != NIL)
                env = checkEnvironment(args[1]);
            else
                env = new Environment();
            return macroexpand_1(form, env, LispThread.currentThread());
        }
    };

    // ### *gensym-counter*
    private static final Symbol _GENSYM_COUNTER_ =
        PACKAGE_CL.addExternalSymbol("*GENSYM-COUNTER*");
    static {
        _GENSYM_COUNTER_.setSymbolValue(Fixnum.ZERO);
        _GENSYM_COUNTER_.setSpecial(true);
    }

    // ### gensym
    private static final Primitive GENSYM = new Primitive("gensym") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            String prefix = "G";
            if (args.length == 1) {
                LispObject arg = args[0];
                if (arg instanceof Fixnum) {
                    int n = ((Fixnum)arg).getValue();
                    if (n < 0)
                        throw new ConditionThrowable(new TypeError(arg,
                                                                   "non-negative integer"));
                    StringBuffer sb = new StringBuffer(prefix);
                    sb.append(n);
                    return new Symbol(sb.toString());
                }
                if (arg instanceof Bignum) {
                    BigInteger n = ((Bignum)arg).getValue();
                    if (n.signum() < 0)
                        throw new ConditionThrowable(new TypeError(arg,
                                                                   "non-negative integer"));
                    StringBuffer sb = new StringBuffer(prefix);
                    sb.append(n.toString());
                    return new Symbol(sb.toString());
                }
                if (arg instanceof LispString)
                    prefix = ((LispString)arg).getValue();
                else
                    throw new ConditionThrowable(new TypeError(arg, "string or non-negative integer"));
            }
            return gensym(prefix);
        }
    };

    private static final Symbol gensym() throws ConditionThrowable
    {
        return gensym("G");
    }

    private static final Symbol gensym(String prefix) throws ConditionThrowable
    {
        LispObject oldValue;
        LispThread thread = LispThread.currentThread();
        Environment dynEnv = thread.getDynamicEnvironment();
        Binding binding =
            (dynEnv == null) ? null : dynEnv.getBinding(_GENSYM_COUNTER_);
        if (binding != null) {
            oldValue = binding.value;
            binding.value = oldValue.incr();
        } else {
            oldValue = _GENSYM_COUNTER_.getSymbolValue();
            _GENSYM_COUNTER_.setSymbolValue(oldValue.incr());
        }
        StringBuffer sb = new StringBuffer(prefix);
        sb.append(String.valueOf(oldValue));
        return new Symbol(sb.toString());
    }

    // ### string
    private static final Primitive1 STRING = new Primitive1("string") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return string(arg);
        }
    };

    // ### intern
    // intern string &optional package => symbol, status
    // status is one of :INHERITED, :EXTERNAL, :INTERNAL or NIL.
    private static final Primitive INTERN = new Primitive("intern") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            switch (args.length) {
                case 1: {
                    Package pkg =
                        (Package) _PACKAGE_.symbolValueNoThrow(thread);
                    return pkg.intern(LispString.getValue(args[0]), thread);
                }
                case 2: {
                    Package pkg = coerceToPackage(args[1]);
                    return pkg.intern(LispString.getValue(args[0]), thread);
                }
                default:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            }
        }
    };

    // ### unintern
    // unintern symbol &optional package => generalized-boolean
    private static final Primitive UNINTERN = new Primitive("unintern") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length == 0 || args.length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            Symbol symbol = checkSymbol(args[0]);
            Package pkg;
            if (args.length == 2)
                pkg = coerceToPackage(args[1]);
            else
                pkg = getCurrentPackage();
            return pkg.unintern(symbol);
        }
    };

    // ### find-package
    private static final Primitive1 FIND_PACKAGE =
        new Primitive1("find-package") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Package)
                return arg;
            if (arg instanceof LispString) {
                Package pkg =
                    Packages.findPackage(((LispString)arg).getValue());
                return pkg != null ? pkg : NIL;
            }
            if (arg instanceof Symbol) {
                Package pkg = Packages.findPackage(arg.getName());
                return pkg != null ? pkg : NIL;
            }
            if (arg instanceof LispCharacter) {
                String packageName =
                    String.valueOf(new char[] {((LispCharacter)arg).getValue()});
                Package pkg = Packages.findPackage(packageName);
                return pkg != null ? pkg : NIL;
            }
            return NIL;
        }
    };

    // ### %make-package
    // %make-package package-name nicknames use => package
    private static final Primitive3 _MAKE_PACKAGE =
        new Primitive3("%make-package", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            String packageName = javaString(first);
            Package pkg =
                Packages.findPackage(packageName);
            if (pkg != null)
                throw new ConditionThrowable(new LispError("package " + packageName +
                                                           " already exists"));
            LispObject nicknames = checkList(second);
            if (nicknames != NIL) {
                LispObject list = nicknames;
                while (list != NIL) {
                    String nick = javaString(list.car());
                    if (Packages.findPackage(nick) != null) {
                        throw new ConditionThrowable(new PackageError("a package named " + nick +
                                                                      " already exists"));
                    }
                    list = list.cdr();
                }
            }
            LispObject use = checkList(third);
            if (use != NIL) {
                LispObject list = use;
                while (list != NIL) {
                    LispObject obj = list.car();
                    if (obj instanceof Package)
                        ; // OK.
                    else {
                        String s = javaString(obj);
                        Package p = Packages.findPackage(s);
                        if (p == null)
                            throw new ConditionThrowable(new LispError(String.valueOf(obj)) +
                                                " is not the name of a package");
                    }
                    list = list.cdr();
                }
            }
            // Now create the package.
            pkg = Packages.createPackage(packageName);
            // Add the nicknames.
            while (nicknames != NIL) {
                String nick = javaString(nicknames.car());
                pkg.addNickname(nick);
                nicknames = nicknames.cdr();
            }
            // Create the use list.
            while (use != NIL) {
                LispObject obj = use.car();
                if (obj instanceof Package)
                    pkg.usePackage((Package)obj);
                else {
                    String s = javaString(obj);
                    Package p = Packages.findPackage(s);
                    if (p == null)
                        throw new ConditionThrowable(new LispError(String.valueOf(obj)) +
                                            " is not the name of a package");
                    pkg.usePackage(p);
                }
                use = use.cdr();
            }
            return pkg;
        }
    };

    // ### %in-package
    private static final Primitive1 _IN_PACKAGE =
        new Primitive1("%in-package", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            String packageName = javaString(arg);
            Package pkg = Packages.findPackage(packageName);
            if (pkg == null)
                throw new ConditionThrowable(new PackageError("package " + arg + " does not exist"));
            LispThread thread = LispThread.currentThread();
            Environment dynEnv = thread.getDynamicEnvironment();
            if (dynEnv != null) {
                Binding binding = dynEnv.getBinding(_PACKAGE_);
                if (binding != null) {
                    binding.value = pkg;
                    return pkg;
                }
            }
            // No dynamic binding.
            _PACKAGE_.setSymbolValue(pkg);
            return pkg;
        }
    };

    // ### use-package
    // use-package packages-to-use &optional package => t
    private static final Primitive USE_PACKAGE = new Primitive("use-package") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            Package pkg;
            if (args.length == 2)
                pkg = coerceToPackage(args[1]);
            else
                pkg = getCurrentPackage();
            if (args[0] instanceof Cons) {
                LispObject list = args[0];
                while (list != NIL) {
                    pkg.usePackage(coerceToPackage(list.car()));
                    list = list.cdr();
                }
            } else
                pkg.usePackage(coerceToPackage(args[0]));
            return T;
        }
    };

    // ### do-external-symbols
    // do-external-symbols (var [package [result-form]]) declaration* {tag | statement}*
    // => result*
    // Should be a macro.
    private static final SpecialOperator DO_EXTERNAL_SYMBOLS =
        new SpecialOperator("do-external-symbols") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            return doSymbols(args, env, true);
        }
    };

    // ### do-symbols
    // do-symbols (var [package [result-form]]) declaration* {tag | statement}*
    // => result*
    // Should be a macro.
    private static final SpecialOperator DO_SYMBOLS =
        new SpecialOperator("do-symbols") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            return doSymbols(args, env, false);
        }
    };

    private static final LispObject doSymbols(LispObject args, Environment env,
                                              boolean externalOnly)
        throws ConditionThrowable
    {
        LispObject bodyForm = args.cdr();
        args = args.car();
        Symbol var = checkSymbol(args.car());
        args = args.cdr();
        final LispThread thread = LispThread.currentThread();
        // Defaults.
        Package pkg = getCurrentPackage();
        LispObject resultForm = NIL;
        if (args != NIL) {
            pkg = coerceToPackage(eval(args.car(), env, thread));
            args = args.cdr();
            if (args != NIL)
                resultForm = args.car();
        }
        Environment oldDynEnv = thread.getDynamicEnvironment();
        final List list;
        if (externalOnly)
            list = pkg.getExternalSymbols();
        else
            list = pkg.getAccessibleSymbols();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Symbol symbol = (Symbol) it.next();
            Environment ext = new Environment(env);
            bind(var, symbol, ext);
            LispObject body = bodyForm;
            int depth = thread.getStackDepth();
            try {
                while (body != NIL) {
                    eval(body.car(), ext, thread);
                    body = body.cdr();
                }
            }
            catch (Return ret) {
                if (ret.getTag() == NIL) {
                    thread.setStackDepth(depth);
                    return ret.getResult();
                }
                throw ret;
            }
        }
        Environment ext = new Environment(env);
        bind(var, NIL, ext);
        LispObject result = eval(resultForm, ext, thread);
        thread.setDynamicEnvironment(oldDynEnv);
        return result;
    }

    // ### package-symbols
    private static final Primitive1 PACKAGE_SYMBOLS =
        new Primitive1("package-symbols", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).getSymbols();
        }
    };

    // ### package-internal-symbols
    private static final Primitive1 PACKAGE_INTERNAL_SYMBOLS =
        new Primitive1("package-internal-symbols", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).PACKAGE_INTERNAL_SYMBOLS();
        }
    };

    // ### package-external-symbols
    private static final Primitive1 PACKAGE_EXTERNAL_SYMBOLS =
        new Primitive1("package-external-symbols", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).PACKAGE_EXTERNAL_SYMBOLS();
        }
    };

    // ### package-inherited-symbols
    private static final Primitive1 PACKAGE_INHERITED_SYMBOLS =
        new Primitive1("package-inherited-symbols", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).PACKAGE_INHERITED_SYMBOLS();
        }
    };

    // ### export
    // export symbols &optional package
    private static final Primitive EXPORT =
        new Primitive("export") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length == 0 || args.length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            Package pkg;
            if (args.length == 2)
                pkg = coerceToPackage(args[1]);
            else
                pkg = (Package) _PACKAGE_.symbolValue();
            // args[0] can be a single symbol or a list.
            if (args[0] instanceof Cons) {
                for (LispObject list = args[0]; list != NIL; list = list.cdr())
                    pkg.export(checkSymbol(list.car()));
            } else
                pkg.export(checkSymbol(args[0]));
            return T;
        }
    };

    // ### find-symbol
    // find-symbol string &optional package => symbol, status
    private static final Primitive FIND_SYMBOL =
        new Primitive("find-symbol") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length == 0 || args.length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            String name = LispString.getValue(args[0]);
            Package pkg;
            if (args.length == 2)
                pkg = coerceToPackage(args[1]);
            else
                pkg = getCurrentPackage();
            return pkg.findSymbol(name);
        }
    };

    // ### fset
    private static final Primitive2 FSET =
        new Primitive2("fset", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            if (second instanceof LispString) {
                String className = ((LispString)second).getValue();
                if (className.endsWith(".class")) {
                    try {
                        JavaClassLoader loader = new JavaClassLoader();
                        Class c = loader.loadClassFromFile(className);
                        if (c != null) {
                            Class[] parameterTypes = new Class[0];
                            java.lang.reflect.Constructor constructor =
                                c.getConstructor(parameterTypes);
                            Object[] initargs = new Object[0];
                            LispObject obj =
                                (LispObject) constructor.newInstance(initargs);
                            symbol.setSymbolFunction(obj);
                            return obj;
                        }
                    }
                    catch (Throwable t) {
                        Debug.trace(t);
                    }
                }
                throw new ConditionThrowable(new LispError("unable to load ".concat(className)));
            }
            symbol.setSymbolFunction(second);
            return second;
        }
    };

    // ### %set-symbol-plist
    private static final Primitive2 _SET_SYMBOL_PLIST =
        new Primitive2("%set-symbol-plist", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkSymbol(first).setPropertyList(checkList(second));
            return second;
        }
    };

    // ### get
    // get symbol indicator &optional default => value
    private static final Primitive GET = new Primitive("get") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            Symbol symbol;
            LispObject indicator;
            LispObject defaultValue;
            switch (args.length) {
                case 2:
                    symbol = checkSymbol(args[0]);
                    indicator = args[1];
                    defaultValue = NIL;
                    break;
                case 3:
                    symbol = checkSymbol(args[0]);
                    indicator = args[1];
                    defaultValue = args[2];
                    break;
                default:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            }
            return get(symbol, indicator, defaultValue);
        }
    };

    // ### %put
    // %put symbol indicator value
    private static final Primitive3 _PUT =
        new Primitive3("%put", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            LispObject indicator = second;
            LispObject value = third;
            return put(symbol, indicator, value);
        }
    };

    // ### macrolet
    private static final SpecialOperator MACROLET =
        new SpecialOperator("macrolet") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            LispObject defs = checkList(args.car());
            final LispThread thread = LispThread.currentThread();
            LispObject result;
            if (defs != NIL) {
                Environment ext = new Environment(env);
                while (defs != NIL) {
                    LispObject def = checkList(defs.car());
                    Symbol symbol = checkSymbol(def.car());
                    LispObject lambdaList = def.cadr();
                    LispObject body = def.cddr();
                    LispObject block =
                        new Cons(Symbol.BLOCK, new Cons(symbol, body));
                    LispObject toBeApplied =
                        list3(Symbol.LAMBDA, lambdaList, block);
                    LispObject formArg = gensym("FORM-");
                    LispObject envArg = gensym("ENV-"); // Ignored.
                    LispObject expander =
                        list3(Symbol.LAMBDA, list2(formArg, envArg),
                              list3(Symbol.APPLY, toBeApplied,
                                    list2(Symbol.CDR, formArg)));
                    Closure expansionFunction =
                        new Closure(expander.cadr(), expander.cddr(), env);
                    MacroObject macroObject =
                        new MacroObject(expansionFunction);
                    ext.bindFunctional(symbol, macroObject);
                    defs = defs.cdr();
                }
                result = progn(args.cdr(), ext, thread);
            } else
                result = progn(args.cdr(), env, thread);
            return result;
        }
    };

    // ### tagbody
    private static final SpecialOperator TAGBODY =
        new SpecialOperator("tagbody") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            Binding tags = null;
            LispObject body = args;
            while (body != NIL) {
                LispObject current = body.car();
                body = body.cdr();
                if (current instanceof Cons)
                    continue;
                // It's a tag.
                tags = new Binding(current, body, tags);
            }
            final LispThread thread = LispThread.currentThread();
            final int depth = thread.getStackDepth();
            LispObject remaining = args;
            while (remaining != NIL) {
                LispObject current = remaining.car();
                if (current instanceof Cons) {
                    try {
                        // Handle GO inline if possible.
                        if (current.car() == Symbol.GO) {
                            LispObject code = null;
                            LispObject tag = current.cadr();
                            for (Binding binding = tags; binding != null; binding = binding.next) {
                                if (binding.symbol.eql(tag)) {
                                    code = binding.value;
                                    break;
                                }
                            }
                            if (code != null) {
                                remaining = code;
                                continue;
                            }
                            throw new Go(tag);
                        }
                        eval(current, env, thread);
                    }
                    catch (Go go) {
                        LispObject code = null;
                        LispObject tag = go.getTag();
                        for (Binding binding = tags; binding != null; binding = binding.next) {
                            if (binding.symbol.eql(tag)) {
                                code = binding.value;
                                break;
                            }
                        }
                        if (code != null) {
                            remaining = code;
                            thread.setStackDepth(depth);
                            continue;
                        }
                        throw go;
                    }
                }
                remaining = remaining.cdr();
            }
            thread.clearValues();
            return NIL;
        }
    };

    // ### go
    private static final SpecialOperator GO = new SpecialOperator("go") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() != 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            throw new Go(args.car());
        }
    };

    // ### block
    private static final SpecialOperator BLOCK = new SpecialOperator("block") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args == NIL)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject tag;
            if (args.car() == NIL)
                tag = NIL;
            else
                tag = checkSymbol(args.car());
            LispObject body = args.cdr();
            Block block = new Block(tag, body);
            Environment ext = new Environment(env);
            ext.addBlock(tag, block);
            LispObject result = NIL;
            final LispThread thread = LispThread.currentThread();
            final int depth = thread.getStackDepth();
            try {
                while (body != NIL) {
                    result = eval(body.car(), ext, thread);
                    body = body.cdr();
                }
                return result;
            }
            catch (Return ret) {
                if (ret.getBlock() != null) {
                    if (ret.getBlock() == block) {
                        thread.setStackDepth(depth);
                        return ret.getResult();
                    } else
                        throw ret;
                }
                if (ret.getTag() == tag) {
                    thread.setStackDepth(depth);
                    return ret.getResult();
                }
                throw ret;
            }
        }
    };

    // ### return-from
    private static final SpecialOperator RETURN_FROM =
        new SpecialOperator("return-from") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final int length = args.length();
            if (length < 1 || length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            Symbol symbol = checkSymbol(args.car());
            Block block = env.lookupBlock(symbol);
            if (block == null) {
                StringBuffer sb = new StringBuffer("no block named ");
                sb.append(symbol.getName());
                sb.append(" is currently visible");
                throw new ConditionThrowable(new LispError(sb.toString()));
            }
            LispObject result;
            if (length == 2)
                result = eval(args.cadr(), env, LispThread.currentThread());
            else
                result = NIL;
            throw new Return(symbol, block, result);
        }
    };

    // ### return
    // Should be a macro.
    private static final SpecialOperator RETURN =
        new SpecialOperator("return") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            switch (args.length()) {
                case 0:
                    throw new Return(NIL, NIL);
                case 1:
                    throw new Return(NIL,
                                     eval(args.car(), env,
                                          LispThread.currentThread()));
                default:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            }
        }
    };

    // ### catch
    private static final SpecialOperator CATCH = new SpecialOperator("catch") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject tag = eval(args.car(), env, thread);
            LispObject body = args.cdr();
            LispObject result = NIL;
            final int depth = thread.getStackDepth();
            try {
                while (body != NIL) {
                    result = eval(body.car(), env, thread);
                    body = body.cdr();
                }
                return result;
            }
            catch (Throw t) {
                if (t.getTag() == tag) {
                    thread.setStackDepth(depth);
                    return t.getResult();
                }
                throw t;
            }
            catch (Return ret) {
                throw ret;
            }
        }
    };

    // ### throw
    private static final SpecialOperator THROW = new SpecialOperator("throw") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() < 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject tag = eval(args.car(), env, thread);
            LispObject result = eval(args.cadr(), env, thread);
            throw new Throw(tag, result);
        }
    };

    // ### unwind-protect
    private static final SpecialOperator UNWIND_PROTECT =
        new SpecialOperator("unwind-protect") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject result;
            LispObject[] values;
            try {
                result = eval(args.car(), env, thread);
                values = thread.getValues();
            }
            finally {
                LispObject body = args.cdr();
                while (body != NIL) {
                    eval(body.car(), env, thread);
                    body = body.cdr();
                }
            }
            thread.setValues(values);
            return result;
        }
    };

    // ### eval-when
    private static final SpecialOperator EVAL_WHEN =
        new SpecialOperator("eval-when")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            return progn(args.cdr(), env, LispThread.currentThread());
        }
    };

    // ### function
    private static final SpecialOperator FUNCTION =
        new SpecialOperator("function")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispObject arg = args.car();
            if (arg instanceof Symbol) {
                LispObject functional = env.lookupFunctional(arg);
                if (functional instanceof Autoload) {
                    Autoload autoload = (Autoload) functional;
                    autoload.load();
                    functional = autoload.getSymbol().getSymbolFunction();
                }
                if (functional instanceof Function)
                    return functional;
                throw new ConditionThrowable(new UndefinedFunction(arg));
            }
            if (arg instanceof Cons) {
                if (arg.car() == Symbol.LAMBDA)
                    return new Closure(arg.cadr(), arg.cddr(), env);
                if (arg.car() == Symbol.SETF) {
                    LispObject f = get(checkSymbol(arg.cadr()),
                                       PACKAGE_SYS.intern("SETF-FUNCTION"));
                    if (f instanceof Function)
                        return f;
                }
            }
            throw new ConditionThrowable(new UndefinedFunction(arg));
        }
    };

    // ### setq
    private static final SpecialOperator SETQ = new SpecialOperator("setq") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            LispObject value = Symbol.NIL;
            final LispThread thread = LispThread.currentThread();
            while (args != NIL) {
                Symbol symbol = checkSymbol(args.car());
                args = args.cdr();
                value = eval(args.car(), env, thread);
                if (symbol.isSpecialVariable()) {
                    Environment dynEnv = thread.getDynamicEnvironment();
                    if (dynEnv != null) {
                        Binding binding = dynEnv.getBinding(symbol);
                        if (binding != null) {
                            binding.value = value;
                            args = args.cdr();
                            continue;
                        }
                    }
                    symbol.setSymbolValue(value);
                    args = args.cdr();
                    continue;
                }
                // Not special.
                Binding binding = env.getBinding(symbol);
                if (binding != null)
                    binding.value = value;
                else
                    symbol.setSymbolValue(value);
                args = args.cdr();
            }
            return value;
        }
    };

    // ### multiple-value-bind
    // multiple-value-bind (var*) values-form declaration* form*
    // Should be a macro.
    private static final SpecialOperator MULTIPLE_VALUE_BIND =
        new SpecialOperator("multiple-value-bind") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            LispObject vars = args.car();
            args = args.cdr();
            LispObject valuesForm = args.car();
            final LispThread thread = LispThread.currentThread();
            LispObject value = eval(valuesForm, env, thread);
            LispObject[] values = thread.getValues();
            if (values == null) {
                // eval() did not return multiple values.
                values = new LispObject[1];
                values[0] = value;
            }
            Environment oldDynEnv = thread.getDynamicEnvironment();
            Environment ext = new Environment(env);
            int i = 0;
            LispObject var = vars.car();
            while (var != NIL) {
                Symbol symbol = checkSymbol(var);
                if (i < values.length)
                    bind(symbol, values[i], ext);
                else
                    bind(symbol, NIL, ext);
                vars = vars.cdr();
                var = vars.car();
                ++i;
            }
            LispObject result = NIL;
            LispObject body = args.cdr();
            while (body != NIL) {
                result = eval(body.car(), ext, thread);
                body = body.cdr();
            }
            thread.setDynamicEnvironment(oldDynEnv);
            return result;
        }
    };

    // ### multiple-value-seq
    // multiple-value-setq vars form => result
    // Result is the primary value returned by the form.
    // Should be a macro.
    private static final SpecialOperator MULTIPLE_VALUE_SETQ =
        new SpecialOperator("multiple-value-setq") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() != 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject vars = args.car();
            LispObject form = args.cadr();
            final LispThread thread = LispThread.currentThread();
            LispObject result = eval(form, env, thread);
            LispObject[] values = thread.getValues();
            if (values == null) {
                // eval() did not return multiple values.
                values = new LispObject[1];
                values[0] = result;
            }
            final Environment dynEnv = thread.getDynamicEnvironment();
            final int limit = values.length;
            int i = 0;
            while (vars != NIL) {
                Symbol symbol = checkSymbol(vars.car());
                LispObject value = i < limit ? values[i] : NIL;
                ++i;
                if (symbol.isSpecialVariable()) {
                    if (dynEnv != null) {
                        Binding binding = dynEnv.getBinding(symbol);
                        if (binding != null) {
                            binding.value = value;
                            vars = vars.cdr();
                            continue;
                        }
                    }
                    symbol.setSymbolValue(value);
                    vars = vars.cdr();
                    continue;
                }
                // Not special.
                Binding binding = env.getBinding(symbol);
                if (binding != null)
                    binding.value = value;
                else
                    symbol.setSymbolValue(value);
                vars = vars.cdr();
            }
            thread.clearValues();
            return result;
        }
    };

    // ### multiple-value-prog1
    private static final SpecialOperator MULTIPLE_VALUE_PROG1 =
        new SpecialOperator("multiple-value-prog1") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject result = eval(args.car(), env, thread);
            LispObject[] values = thread.getValues();
            while ((args = args.cdr()) != NIL)
                eval(args.car(), env, thread);
            thread.setValues(values);
            return result;
        }
    };

    // ### multiple-value-call
    private static final SpecialOperator MULTIPLE_VALUE_CALL =
        new SpecialOperator("multiple-value-call") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject function;
            LispObject obj = eval(args.car(), env, thread);
            args = args.cdr();
            if (obj instanceof Symbol) {
                function = obj.getSymbolFunction();
                if (function == null)
                    throw new ConditionThrowable(new UndefinedFunction(obj));
            } else if (obj instanceof Function) {
                function = obj;
            } else
                throw new ConditionThrowable(new LispError(String.valueOf(obj) + " is not a function name"));
            ArrayList arrayList = new ArrayList();
            while (args != NIL) {
                LispObject form = args.car();
                LispObject result = eval(form, env, thread);
                LispObject[] values = thread.getValues();
                if (values != null) {
                    for (int i = 0; i < values.length; i++)
                        arrayList.add(values[i]);
                } else
                    arrayList.add(result);
                args = args.cdr();
            }
            LispObject[] argv = new LispObject[arrayList.size()];
            arrayList.toArray(argv);
            return funcall(function, argv, thread);
        }
    };

    // ### and
    // Should be a macro.
    private static final SpecialOperator AND = new SpecialOperator("and") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject result = T;
            while (args != NIL) {
                result = eval(args.car(), env, thread);
                if (result == NIL) {
                    if (args.cdr() != NIL) {
                        // Not the last form.
                        thread.clearValues();
                    }
                    break;
                }
                args = args.cdr();
            }
            return result;
        }
    };

    // ### or
    // Should be a macro.
    private static final SpecialOperator OR = new SpecialOperator("or") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            LispObject result = NIL;
            while (args != NIL) {
                result = eval(args.car(), env, thread);
                if (result != NIL) {
                    if (args.cdr() != NIL) {
                        // Not the last form.
                        thread.clearValues();
                    }
                    break;
                }
                args = args.cdr();
            }
            return result;
        }
    };

    // ### assert
    // Should be a macro.
    private static final SpecialOperator ASSERT =
        new SpecialOperator("assert") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() != 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (eval(args.car(), env, LispThread.currentThread()) == NIL)
                throw new ConditionThrowable(new LispError("assertion failed: " + args.car()));
            return NIL;
        }
    };

    // ### write-char
    // write-char character &optional output-stream => character
    private static final Primitive WRITE_CHAR =
        new Primitive("write-char") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final char c = LispCharacter.getValue(args[0]);
            final CharacterOutputStream out;
            if (args.length == 1)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[1]);
            out.writeChar(c);
            return args[0];
        }
    };

    // ### %write-string
    // write-string string output-stream start end => string
    private static final Primitive _WRITE_STRING =
        new Primitive("%write-string", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 4)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            String s = LispString.getValue(args[0]);
            CharacterOutputStream out = outSynonymOf(args[1]);
            int start = Fixnum.getValue(args[2]);
            int end = Fixnum.getValue(args[3]);
            out.writeString(s.substring(start, end));
            return args[0];
        }
    };

    // ### %write-newline
    // %write-newline output-stream => nil
    private static final Primitive1 _WRITE_NEWLINE =
        new Primitive1("%write-newline", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            outSynonymOf(arg).writeString(System.getProperty("line.separator"));
            return NIL;
        }
    };

    // ### finish-output
    // finish-output &optional output-stream => nil
    private static final Primitive FINISH_OUTPUT = new Primitive("finish-output") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            return flushOutput(args);
        }
    };

    // ### force-output
    // force-output &optional output-stream => nil
    private static final Primitive FORCE_OUTPUT = new Primitive("force-output") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            return flushOutput(args);
        }
    };

    private static final LispObject flushOutput(LispObject[] args)
        throws ConditionThrowable
    {
        final LispOutputStream out;
        if (args.length == 0)
            out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
        else if (args[0] instanceof LispOutputStream)
            out = (LispOutputStream) args[0];
        else if (args[0] instanceof TwoWayStream)
            out = ((TwoWayStream)args[0]).getOutputStream();
        else
            throw new ConditionThrowable(new TypeError(args[0], "output stream"));
        out.flushOutput();
        return NIL;
    }

    // ### clear-input
    // clear-input &optional input-stream => nil
    private static final Primitive CLEAR_INPUT = new Primitive("clear-input") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final CharacterInputStream in;
            if (args.length == 0)
                in = checkCharacterInputStream(_STANDARD_INPUT_.symbolValue());
            else
                in = inSynonymOf(args[0]);
            in.clearInput();
            return NIL;
        }
    };

    // ### close
    // close stream &key abort => result
    private static final Primitive CLOSE = new Primitive("close") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length == 0)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject abort = NIL; // Default.
            LispStream stream = checkStream(args[0]);
            if (length > 1) {
                if ((length - 1) % 2 != 0)
                    throw new ConditionThrowable(new ProgramError("odd number of keyword arguments"));
                if (length > 3)
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
                if (args[1] == Keyword.ABORT)
                    abort = args[2];
                else
                    throw new ConditionThrowable(new LispError(
                        "CLOSE: unrecognized keyword argument: " + args[1]));
            }
            return stream.close(abort);
        }
    };

    // ### multiple-value-list
    // multiple-value-list form => list
    // Evaluates form and creates a list of the multiple values it returns.
    // Should be a macro.
    private static final SpecialOperator MULTIPLE_VALUE_LIST =
        new SpecialOperator("multiple-value-list")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() != 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject result = eval(args.car(), env, thread);
            LispObject[] values = thread.getValues();
            thread.clearValues();
            if (values == null)
                return new Cons(result);
            LispObject list = NIL;
            for (int i = values.length; i-- > 0;)
                list = new Cons(values[i], list);
            return list;
        }
    };

    // ### nth-value
    // nth-value n form => object
    // Evaluates n and then form and returns the nth value returned by form, or
    // NIL if n >= number of values returned.
    // Should be a macro.
    private static final SpecialOperator NTH_VALUE =
        new SpecialOperator("nth-value")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() != 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            int n = Fixnum.getInt(eval(args.car(), env, thread));
            if (n < 0)
                n = 0;
            LispObject result = eval(args.cadr(), env, thread);
            LispObject[] values = thread.getValues();
            thread.clearValues();
            if (values == null) {
                // A single value was returned.
                return n == 0 ? result : NIL;
            }
            if (n < values.length)
                return values[n];
            return NIL;
        }
    };

    // ### write-byte
    // write-byte byte stream => byte
    private static final Primitive2 WRITE_BYTE = new Primitive2("write-byte")
    {
        public LispObject execute (LispObject first, LispObject second)
            throws ConditionThrowable
        {
            int n = Fixnum.getValue(first);
            if (n < 0 || n > 255)
                throw new ConditionThrowable(new TypeError(first, "unsigned byte"));
            final BinaryOutputStream out = checkBinaryOutputStream(second);
            out.writeByte(n);
            return first;
        }
    };

    // ### read-byte
    // read-byte stream &optional eof-error-p eof-value => byte
    private static final Primitive READ_BYTE = new Primitive("read-byte")
    {
        public LispObject execute (LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length < 1 || length > 3)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            final BinaryInputStream in = checkBinaryInputStream(args[0]);
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            return in.readByte(eofError, eofValue);
        }
    };

    // ### read-line
    // read-line &optional input-stream eof-error-p eof-value recursive-p
    // => line, missing-newline-p
    private static final Primitive READ_LINE = new Primitive("read-line")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length > 4)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            CharacterInputStream stream = null;
            if (length == 0)
                stream = getStandardInput();
            else if (args[0] instanceof CharacterInputStream)
                stream = (CharacterInputStream) args[0];
            else if (args[0] instanceof TwoWayStream) {
                LispInputStream in = ((TwoWayStream)args[0]).getInputStream();
                if (in instanceof CharacterInputStream)
                    stream = (CharacterInputStream) in;
            }
            if (stream == null)
                throw new ConditionThrowable(new TypeError(args[0], "character input stream"));
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            boolean recursive = length > 3 ? (args[3] != NIL) : false;
            return stream.readLine(eofError, eofValue);
        }
    };

    // ### %read-from-string
    // read-from-string string &optional eof-error-p eof-value &key start end
    // preserve-whitespace => object, position
    private static final Primitive _READ_FROM_STRING =
        new Primitive("%read-from-string", PACKAGE_SYS, false) {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 6)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            String s = LispString.getValue(args[0]);
            boolean eofError = args[1] != NIL;
            LispObject eofValue = args[2];
            LispObject start = args[3];
            LispObject end = args[4];
            boolean preserveWhitespace = args[5] != NIL;
            int startIndex, endIndex;
            if (start != NIL)
                startIndex = (int) Fixnum.getValue(start);
            else
                startIndex = 0;
            if (end != NIL)
                endIndex = (int) Fixnum.getValue(end);
            else
                endIndex = s.length();
            StringInputStream in =
                new StringInputStream(s, startIndex, endIndex);
            LispObject result;
            if (preserveWhitespace)
                result = in.readPreservingWhitespace(eofError, eofValue, false);
            else
                result = in.read(eofError, eofValue, false);
            LispObject[] values = new LispObject[2];
            values[0] = result;
            values[1] = new Fixnum(startIndex + in.getOffset());
            LispThread.currentThread().setValues(values);
            return result;
        }
    };

    private static final Primitive1 STANDARD_CHAR_P =
        new Primitive1("standard-char-p")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return checkCharacter(arg).isStandardChar();
        }
    };

    private static final Primitive1 GRAPHIC_CHAR_P =
        new Primitive1("graphic-char-p")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            char c = LispCharacter.getValue(arg);
            return (c >= ' ' && c < 127) ? T : NIL;
        }
    };

    private static final Primitive1 ALPHA_CHAR_P =
        new Primitive1("alpha-char-p")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            char c = LispCharacter.getValue(arg);
            return Character.isLetter(c) ? T : NIL;
        }
    };

    private static final Primitive1 NAME_CHAR = new Primitive1("name-char") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            String s = LispString.getValue(string(arg));
            int n = nameToChar(s);
            return n >= 0 ? LispCharacter.getInstance((char)n) : NIL;
        }
    };

    private static final Primitive1 CHAR_NAME = new Primitive1("char-name")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            char c = LispCharacter.getValue(arg);
            String name = null;
            switch (c) {
                case ' ':
                    name = "Space";
                    break;
                case '\n':
                    name = "Newline";
                    break;
                case '\t':
                    name = "Tab";
                    break;
                case '\r':
                    name = "Return";
                    break;
                case '\f':
                    name = "Page";
                    break;
                case '\b':
                    name = "Backspace";
                    break;
                default:
                    break;
            }
            return name != null ? new LispString(name) : NIL;
        }
    };

    private static final Primitive DIGIT_CHAR = new Primitive("digit-char")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final long radix;
            switch (args.length) {
                case 1:
                    radix = 10;
                    break;
                case 2:
                    radix = Fixnum.getValue(args[1]);
                    break;
                default:
                    throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            }
            long weight = Fixnum.getValue(args[0]);
            if (weight >= radix || weight >= 36)
                return NIL;
            if (weight < 10)
                return LispCharacter.getInstance((char)('0' + weight));
            return LispCharacter.getInstance((char)('A' + weight - 10));
        }
    };

    private static final Primitive1 _CALL_COUNT =
        new Primitive1("%call-count", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Fixnum(arg.getCallCount());
        }
    };

    private static final Primitive2 _SET_CALL_COUNT =
        new Primitive2("%set-call-count", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            first.setCallCount(Fixnum.getValue(second));
            return second;
        }
    };

    // ### get-dispatch-macro-character
    // get-dispatch-macro-character disp-char sub-char &optional readtable
    // => function
    private static final Primitive GET_DISPATCH_MACRO_CHARACTER =
        new Primitive("get-dispatch-macro-character") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 2 || args.length > 3)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            char dispChar = LispCharacter.getValue(args[0]);
            char subChar = LispCharacter.getValue(args[1]);
            Readtable readtable;
            if (args.length == 3)
                readtable = checkReadtable(args[2]);
            else
                readtable = getCurrentReadtable();
            return readtable.getDispatchMacroCharacter(dispChar, subChar);
        }
    };

    // ### set-dispatch-macro-character
    // set-dispatch-macro-character disp-char sub-char new-function &optional readtable
    // => t
    private static final Primitive SET_DISPATCH_MACRO_CHARACTER =
        new Primitive("set-dispatch-macro-character") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 3 || args.length > 4)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            char dispChar = LispCharacter.getValue(args[0]);
            char subChar = LispCharacter.getValue(args[1]);
            LispObject function = args[2];
            Readtable readtable;
            if (args.length == 4)
                readtable = checkReadtable(args[3]);
            else
                readtable = getCurrentReadtable();
            return readtable.setDispatchMacroCharacter(dispChar, subChar, function);
        }
    };

    // ### read
    // read &optional input-stream eof-error-p eof-value recursive-p => object
    private static final Primitive READ = new Primitive("read") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length > 4)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            CharacterInputStream stream =
                length > 0 ? checkCharacterInputStream(args[0]) : getStandardInput();
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            boolean recursive = length > 3 ? (args[3] != NIL) : false;
            return stream.read(eofError, eofValue, recursive);
        }
    };

    // ### read-char
    // read-char &optional input-stream eof-error-p eof-value recursive-p => char
    private static final Primitive READ_CHAR = new Primitive("read-char") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length > 4)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            CharacterInputStream stream =
                length > 0 ? checkCharacterInputStream(args[0]) : getStandardInput();
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            boolean recursive = length > 3 ? (args[3] != NIL) : false;
            return stream.readChar(eofError, eofValue);
        }
    };

    // ### unread-char
    // unread-char character &optional input-stream => nil
    private static final Primitive UNREAD_CHAR = new Primitive("unread-char") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            CharacterInputStream stream =
                length > 1 ? checkCharacterInputStream(args[1]) : getStandardInput();
            return stream.unreadChar(checkCharacter(args[0]));
        }
    };

    private static final Primitive2 _SET_LAMBDA_NAME =
        new Primitive2("%set-lambda-name", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first instanceof Function) {
                Function f = (Function) first;
                f.setLambdaName(second);
                return second;
            } else
                throw new ConditionThrowable(new TypeError(first, "function"));
        }
    };

    // Destructively alters the vector, changing its length to NEW-SIZE, which
    // must be less than or equal to its current length.
    // shrink-vector vector new-size => vector
    private static final Primitive2 SHRINK_VECTOR =
        new Primitive2("shrink-vector", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkVector(first).shrink(Fixnum.getInt(second));
            return first;
        }
    };

    // ### vector-subseq
    // vector-subseq vector start &optional end => subsequence
    private static final Primitive3 VECTOR_SUBSEQ =
        new Primitive3("vector-subseq", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(first);
            int start = Fixnum.getValue(second);
            int end = third != NIL ? Fixnum.getValue(third) : v.length();
            if (start > end) {
                StringBuffer sb = new StringBuffer("start (");
                sb.append(start);
                sb.append(") is greater than end (");
                sb.append(end);
                sb.append(')');
                throw new ConditionThrowable(new TypeError(sb.toString()));
            }
            return v.subseq(start, end);
        }
    };

    // ### random
    // random limit &optional random-state => random-number
    private static final Primitive RANDOM = new Primitive("random") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length < 1 || length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            Random random;
            if (length == 2)
                random = (Random) JavaObject.getObject(args[1]);
            else
                random = (Random) JavaObject.getObject(_RANDOM_STATE_.symbolValueNoThrow());
            if (args[0] instanceof Fixnum) {
                int limit = ((Fixnum)args[0]).getValue();
                if (limit > 0) {
                    int n = random.nextInt((int)limit);
                    return new Fixnum(n);
                }
            } else if (args[0] instanceof Bignum) {
                BigInteger limit = ((Bignum)args[0]).getValue();
                if (limit.signum() > 0) {
                    int bitLength = limit.bitLength();
                    BigInteger rand = new BigInteger(bitLength + 1, random);
                    BigInteger remainder = rand.remainder(limit);
                    return number(remainder);
                }
            } else if (args[0] instanceof LispFloat) {
                double limit = ((LispFloat)args[0]).getValue();
                if (limit > 0) {
                    double rand = random.nextDouble();
                    return new LispFloat(rand * limit);
                }
            }
            throw new ConditionThrowable(new TypeError(args[0], "positive integer or positive float"));
        }
    };

    // ### make-random-state
    private static final Primitive MAKE_RANDOM_STATE =
        new Primitive("make-random-state") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            // FIXME Ignore arguments (or lack thereof).
            return new JavaObject(new Random());
        }
    };

    // ### truncate
    private static final Primitive TRUNCATE = new Primitive("truncate") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length < 1 || length > 2)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject n = args[0];
            LispObject d = length == 1 ? Fixnum.ONE : args[1];
            if (n instanceof Fixnum)
                return ((Fixnum)n).truncate(d);
            if (n instanceof Bignum)
                return ((Bignum)n).truncate(d);
            if (n instanceof Ratio)
                return ((Ratio)n).truncate(d);
            if (n instanceof LispFloat)
                return ((LispFloat)n).truncate(d);
            throw new ConditionThrowable(new TypeError(n, "number"));
        }
    };

    // ### expt
    // expt base-number power-number => result
    public static final Primitive2 EXPT = new Primitive2("expt") {
        public LispObject execute(LispObject n, LispObject power)
            throws ConditionThrowable
        {
            if (power.zerop()) {
                if (power instanceof Fixnum) {
                    if (n instanceof LispFloat)
                        return LispFloat.ONE;
                    if (n instanceof Complex) {
                        if (((Complex)n).getRealPart() instanceof LispFloat)
                            return Complex.getInstance(LispFloat.ONE,
                                                       LispFloat.ZERO);
                    }
                    return Fixnum.ONE;
                }
                if (power instanceof LispFloat) {
                    return LispFloat.ONE;
                }
            }
            if (power instanceof Fixnum) {
                LispObject result = null;
                if (n instanceof LispFloat)
                    result = LispFloat.ONE;
                else
                    result = Fixnum.ONE;
                int count = ((Fixnum)power).getValue();
                if (count > 0) {
                    for (int i = count; i-- > 0;)
                        result = result.multiplyBy(n);
                } else if (count < 0) {
                    for (int i = -count; i-- > 0;)
                        result = result.divideBy(n);
                }
                return result;
            }
            if (power instanceof LispFloat) {
                if (n instanceof Fixnum) {
                    double d = Math.pow(((Fixnum)n).getValue(),
                                        ((LispFloat)power).getValue());
                    return new LispFloat(d);
                }
            }
            throw new ConditionThrowable(new LispError("EXPT: unsupported case"));
        }
    };

    // ### list
    private static final Primitive LIST = new Primitive("list") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Cons(arg);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new Cons(first, new Cons(second));
        }
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            return new Cons(first, new Cons(second, new Cons(third)));
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            LispObject result = NIL;
            for (int i = args.length; i-- > 0;)
                result = new Cons(args[i], result);
            return result;
        }
    };

    // ### list*
    private static final Primitive LIST_ = new Primitive("list*") {
        public LispObject execute() throws ConditionThrowable
        {
            throw new ConditionThrowable(new WrongNumberOfArgumentsException("LIST*"));
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new Cons(first, second);
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third) throws ConditionThrowable
        {
            return new Cons(first, new Cons(second, third));
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int i = args.length - 1;
            LispObject result = args[i];
            while (i-- > 0)
                result = new Cons(args[i], result);
            return result;
        }
    };

    // ### nreverse
    public static final Primitive1 NREVERSE = new Primitive1("nreverse") {
        public LispObject execute (LispObject arg) throws ConditionThrowable {
            if (arg instanceof AbstractVector) {
                ((AbstractVector)arg).nreverse();
                return arg;
            }
            LispObject list = checkList(arg);
            // Following code is from CLISP.
            if (list instanceof Cons) {
                LispObject list3 = list.cdr();
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
                        list.setCdr(list2);
                        list1.setCdr(list3);
                    }
                    LispObject h = list.car();
                    list.setCar(list3.car());
                    list3.setCar(h);
                }
            }
            return list;
        }
    };

    // ### nreconc
    // From CLISP.
    private static final Primitive2 NRECONC = new Primitive2("nreconc") {
        public LispObject execute(LispObject list, LispObject obj)
            throws ConditionThrowable
        {
            if (list instanceof Cons) {
                LispObject list3 = list.cdr();
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
                        list.setCdr(list2);
                        list1.setCdr(list3);
                    }
                    LispObject h = list.car();
                    list.setCar(list3.car());
                    list3.setCar(h);
                    list3.setCdr(obj);
                } else {
                    list.setCdr(obj);
                }
                return list;
            } else
                return obj;
        }
    };

    // ### reverse
    private static final Primitive1 REVERSE = new Primitive1("reverse") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof AbstractVector)
                return ((AbstractVector)arg).reverse();
            if (arg instanceof Cons) {
                LispObject result = NIL;
                while (arg != NIL) {
                    result = new Cons(arg.car(), result);
                    arg = arg.cdr();
                }
                return result;
            }
            if (arg == NIL)
                return NIL;
            throw new ConditionThrowable(new TypeError(arg, "proper sequence"));
        }
    };

    // ### %setelt
    // %setelt sequence index newval => newval
    private static final Primitive3 _SETELT = new Primitive3("%setelt") {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            if (first instanceof Cons) {
                int index = Fixnum.getValue(second);
                if (index < 0)
                    throw new ConditionThrowable(new TypeError());
                LispObject list = first;
                int i = 0;
                while (true) {
                    if (i == index) {
                        list.setCar(third);
                        return third;
                    }
                    list = list.cdr();
                    if (list == NIL)
                        throw new ConditionThrowable(new TypeError());
                    ++i;
                }
            } else if (first instanceof AbstractVector) {
                ((AbstractVector)first).set(Fixnum.getValue(second), third);
                return third;
            } else
                throw new ConditionThrowable(new TypeError(first, "sequence"));
        }
    };

//     (defun maptree (fun x)
//       (if (atom x)
//           (funcall fun x)
//           (let ((a (funcall fun (car x)))
//                 (d (maptree fun (cdr x))))
//             (if (and (eql a (car x)) (eql d (cdr x)))
//                 x
//                 (cons a d)))))

    // ### maptree
    private static final Primitive2 MAPTREE = new Primitive2("maptree") {
        public LispObject execute(LispObject fun, LispObject x)
            throws ConditionThrowable
        {
            if (x instanceof Cons) {
                LispObject a = fun.execute(x.car());
                // Recurse!
                LispObject d = execute(fun, x.cdr());
                if (a.eql(x.car()) && d.eql(x.cdr()))
                    return x;
                else
                    return new Cons(a, d);
            } else
                return fun.execute(x);
        }
    };

    // ### %make-list
    private static final Primitive2 _MAKE_LIST =
        new Primitive2("%make-list", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            int size = Fixnum.getValue(first);
            if (size < 0)
                throw new ConditionThrowable(new TypeError("MAKE-LIST: " + size +
                                                           " is not a valid list length"));
            LispObject result = NIL;
            for (int i = size; i-- > 0;)
                result = new Cons(second, result);
            return result;
        }
    };

    // memq item list &key key test test-not => tail
    private static final Primitive2 MEMQ = new Primitive2("memq") {
        public LispObject execute(LispObject item, LispObject list)
            throws ConditionThrowable
        {
            LispObject tail = checkList(list);
            while (tail != NIL) {
                if (item == tail.car())
                    return tail;
                tail = tail.cdr();
            }
            return NIL;
        }
    };

    // %member item list key test test-not => tail
    private static final Primitive _MEMBER =
        new Primitive("%member", PACKAGE_SYS, false) {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 5)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            LispObject item = args[0];
            LispObject tail = checkList(args[1]);
            LispObject key = args[2];
            if (key != NIL) {
                if (key instanceof Symbol)
                    key = key.getSymbolFunction();
                if (!(key instanceof Function))
                    throw new ConditionThrowable(new UndefinedFunction(args[2]));
            }
            LispObject test = args[3];
            LispObject testNot = args[4];
            if (test != NIL && testNot != NIL)
                throw new ConditionThrowable(new LispError("MEMBER: test and test-not both supplied"));
            if (test == NIL && testNot == NIL) {
                test = EQL;
            } else if (test != NIL) {
                if (test instanceof Symbol)
                    test = test.getSymbolFunction();
                if (!(test instanceof Function))
                    throw new ConditionThrowable(new UndefinedFunction(args[3]));
            } else if (testNot != NIL) {
                if (testNot instanceof Symbol)
                    testNot = testNot.getSymbolFunction();
                if (!(testNot instanceof Function))
                    throw new ConditionThrowable(new UndefinedFunction(args[3]));
            }
            if (key == NIL && test == EQL) {
                while (tail != NIL) {
                    if (item.eql(tail.car()))
                        return tail;
                    tail = tail.cdr();
                }
                return NIL;
            }
            while (tail != NIL) {
                LispObject candidate = tail.car();
                if (key != NIL)
                    candidate = key.execute(candidate);
                if (test != NIL) {
                    if (test.execute(item, candidate) == T)
                        return tail;
                } else if (testNot != NIL) {
                    if (testNot.execute(item, candidate) == NIL)
                        return tail;
                }
                tail = tail.cdr();
            }
            return NIL;
        }
    };

    // ### funcall-key
    // funcall-key function-or-nil element
    private static final Primitive2 FUNCALL_KEY =
        new Primitive2("funcall-key", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first != NIL)
                return funcall1(first, second, LispThread.currentThread());
            return second;
        }
    };

    // ### coerce-to-function
    private static final Primitive1 COERCE_TO_FUNCTION =
        new Primitive1("coerce-to-function", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToFunction(arg);
        }
    };

    // ### arglist
    private static final Primitive1 ARGLIST =
        new Primitive1("arglist", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Function function = coerceToFunction(arg);
            LispObject arglist = function.getArglist();
            LispObject[] values = new LispObject[2];
            if (arglist != null) {
                values[0] = arglist;
                values[1] = T;
            } else {
                values[0] = NIL;
                values[1] = NIL;
            }
            LispThread.currentThread().setValues(values);
            return values[0];
        }
    };

    private static final Primitive2 _SET_ARGLIST =
        new Primitive2("%set-arglist", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            coerceToFunction(first).setArglist(second);
            return second;
        }
    };

    // ### streamp
    private static final Primitive1 STREAMP = new Primitive1("streamp") {
        public LispObject execute(LispObject arg)
        {
            return arg instanceof LispStream ? T : NIL;
        }
    };

    // ### integerp
    private static final Primitive1 INTEGERP = new Primitive1("integerp") {
        public LispObject execute(LispObject arg)
        {
            return arg.INTEGERP();
        }
    };

    // ### realp
    private static final Primitive1 REALP = new Primitive1("realp") {
        public LispObject execute(LispObject arg)
        {
            return arg.REALP();
        }
    };

    // ### rationalp
    private static final Primitive1 RATIONALP = new Primitive1("rationalp") {
        public LispObject execute(LispObject arg)
        {
            return arg.RATIONALP();
        }
    };

    // ### complex
    private static final Primitive2 COMPLEX = new Primitive2("complex") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof LispFloat)
                return Complex.getInstance(arg, LispFloat.ZERO);
            if (arg.realp())
                return arg;
            throw new ConditionThrowable(new TypeError(arg, "real number"));
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return Complex.getInstance(first, second);
        }
    };

    // ### complexp
    private static final Primitive1 COMPLEXP = new Primitive1("complexp") {
        public LispObject execute(LispObject arg)
        {
            return arg.COMPLEXP();
        }
    };

    // ### numerator
    private static final Primitive1 NUMERATOR = new Primitive1("numerator") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.NUMERATOR();
        }
    };

    // ### denominator
    private static final Primitive1 DENOMINATOR = new Primitive1("denominator") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.DENOMINATOR();
        }
    };

    // ### realpart
    private static final Primitive1 REALPART = new Primitive1("realpart") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Complex)
                return ((Complex)arg).getRealPart();
            if (arg.numberp())
                return arg;
            throw new ConditionThrowable(new TypeError(arg, "number"));
        }
    };

    // ### imagpart
    private static final Primitive1 IMAGPART = new Primitive1("imagpart") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Complex)
                return ((Complex)arg).getImaginaryPart();
            return arg.multiplyBy(Fixnum.ZERO);
        }
    };

    // ### integer-length
    private static final Primitive1 INTEGER_LENGTH =
        new Primitive1("integer-length") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            BigInteger value;
            if (arg instanceof Fixnum)
                value = BigInteger.valueOf(((Fixnum)arg).getValue());
            else if (arg instanceof Bignum)
                value = ((Bignum)arg).getValue();
            else
                throw new ConditionThrowable(new TypeError(arg, "integer"));
            return new Fixnum(value.bitLength());
        }
    };

    // ### sqrt
    private static final Primitive1 SQRT =
        new Primitive1("sqrt") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Complex)
                throw new ConditionThrowable(new LispError("SQRT not implemented for complex numbers"));
            if (arg.minusp())
                return Complex.getInstance(new LispFloat(0),
                                           sqrt(Fixnum.ZERO.subtract(arg)));
            return sqrt(arg);
        }
    };

    private static final LispFloat sqrt(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new LispFloat(Math.sqrt(((Fixnum)obj).getValue()));
        if (obj instanceof Bignum)
            return new LispFloat(Math.sqrt(((Bignum)obj).floatValue()));
        if (obj instanceof Ratio)
            return new LispFloat(Math.sqrt(((Ratio)obj).floatValue()));
        if (obj instanceof LispFloat)
            return new LispFloat(Math.sqrt(((LispFloat)obj).getValue()));
        throw new ConditionThrowable(new TypeError(obj, "number"));
    }

    private static final Primitive LOG = new Primitive("log") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return log(arg);
        }
        public LispObject execute(LispObject number, LispObject base)
            throws ConditionThrowable
        {
            return log(number).divideBy(log(base));
        }
    };

    private static final LispFloat log(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new LispFloat(Math.log(((Fixnum)obj).getValue()));
        if (obj instanceof Bignum)
            return new LispFloat(Math.log(((Bignum)obj).floatValue()));
        if (obj instanceof Ratio)
            return new LispFloat(Math.log(((Ratio)obj).floatValue()));
        if (obj instanceof LispFloat)
            return new LispFloat(Math.log(((LispFloat)obj).getValue()));
        throw new ConditionThrowable(new TypeError(obj, "number"));
    }

    // ### gcd-2
    private static final Primitive2 GCD_2 =
        new Primitive2("gcd-2", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            BigInteger n1, n2;
            if (first instanceof Fixnum)
                n1 = BigInteger.valueOf(((Fixnum)first).getValue());
            else if (first instanceof Bignum)
                n1 = ((Bignum)first).getValue();
            else
                throw new ConditionThrowable(new TypeError(first, "integer"));
            if (second instanceof Fixnum)
                n2 = BigInteger.valueOf(((Fixnum)second).getValue());
            else if (second instanceof Bignum)
                n2 = ((Bignum)second).getValue();
            else
                throw new ConditionThrowable(new TypeError(second, "integer"));
            return number(n1.gcd(n2));
        }
    };

    // ### hashcode-to-string
    private static final Primitive1 HASHCODE_TO_STRING =
        new Primitive1("hashcode-to-string", PACKAGE_SYS, false) {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new LispString(Integer.toHexString(arg.hashCode()));
        }
    };

    static {
        new Primitives();
    }
}
