/*
 * Primitives.java
 *
 * Copyright (C) 2002-2004 Peter Graves
 * $Id: Primitives.java,v 1.574 2004-02-19 01:35:12 piso Exp $
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

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class Primitives extends Lisp
{
    // ### *
    public static final Primitive MULTIPLY = new Primitive("*","&rest numbers")
    {
        public LispObject execute()
        {
            return Fixnum.ONE;
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg.numberp())
                return arg;
            signal(new TypeError(arg, "number"));
            return NIL;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.multiplyBy(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            LispObject result = Fixnum.ONE;
            for (int i = 0; i < args.length; i++)
                result = result.multiplyBy(args[i]);
            return result;
        }
    };

    // ### /
    public static final Primitive DIVIDE = new Primitive("/","numerator &rest denominators")
    {
        public LispObject execute() throws ConditionThrowable
        {
            signal(new WrongNumberOfArgumentsException("/"));
            return NIL;
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return Fixnum.ONE.divideBy(arg);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.divideBy(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            LispObject result = args[0];
            for (int i = 1; i < args.length; i++)
                result = result.divideBy(args[i]);
            return result;
        }
    };

    // ### min
    public static final Primitive MIN = new Primitive("min","&rest reals")
    {
        public LispObject execute() throws ConditionThrowable
        {
            signal(new WrongNumberOfArgumentsException("min"));
            return NIL;
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg.realp())
                return arg;
            signal(new TypeError(arg, "real number"));
            return NIL;
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            LispObject result = args[0];
            if (!result.realp())
                signal(new TypeError(result, "real number"));
            for (int i = 1; i < args.length; i++) {
                if (args[i].isLessThan(result))
                    result = args[i];
            }
            return result;
        }
    };


    // ### max
    public static final Primitive MAX = new Primitive("max","&rest reals")
    {
        public LispObject execute() throws ConditionThrowable
        {
            signal(new WrongNumberOfArgumentsException("max"));
            return NIL;
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg.realp())
                return arg;
            signal(new TypeError(arg, "real number"));
            return NIL;
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            LispObject result = args[0];
            if (!result.realp())
                signal(new TypeError(result, "real number"));
            for (int i = 1; i < args.length; i++) {
                if (args[i].isGreaterThan(result))
                    result = args[i];
            }
            return result;
        }
    };

    // ### identity
    private static final Primitive1 IDENTITY = new Primitive1("identity","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg;
        }
    };

    // ### compiled-function-p
    private static final Primitive1 COMPILED_FUNCTION_P =
        new Primitive1("compiled-function-p","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.typep(Symbol.COMPILED_FUNCTION);
        }
    };

    // ### consp
    private static final Primitive1 CONSP = new Primitive1("consp","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg instanceof Cons ? T : NIL;
        }
    };

    // ### listp
    private static final Primitive1 LISTP = new Primitive1("listp","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.LISTP();
        }
    };

    // ### abs
    private static final Primitive1 ABS = new Primitive1("abs","number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.ABS();
        }
    };

    // ### arrayp
    private static final Primitive1 ARRAYP = new Primitive1("arrayp","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg instanceof AbstractArray ? T : NIL;
        }
    };

    // ### array-has-fill-pointer-p
    private static final Primitive1 ARRAY_HAS_FILL_POINTER_P =
        new Primitive1("array-has-fill-pointer-p","array")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof AbstractVector)
                return ((AbstractVector)arg).getFillPointer() >= 0 ? T : NIL;
            if (arg instanceof DisplacedArray)
                return ((DisplacedArray)arg).getFillPointer() >= 0 ? T : NIL;
            if (arg instanceof AbstractArray)
                return NIL;
            signal(new TypeError(arg, "array"));
            return NIL;
        }
    };

    // ### vectorp
    private static final Primitive1 VECTORP = new Primitive1("vectorp","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.VECTORP();
        }
    };

    // ### simple-vector-p
    private static final Primitive1 SIMPLE_VECTOR_P =
        new Primitive1("simple-vector-p","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.typep(Symbol.SIMPLE_VECTOR);
        }
    };

    // ### bit-vector-p
    private static final Primitive1 BIT_VECTOR_P =
        new Primitive1("bit-vector-p","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.BIT_VECTOR_P();
        }
    };

    // ### simple-bit-vector-p
    private static final Primitive1 SIMPLE_BIT_VECTOR_P =
        new Primitive1("simple-bit-vector-p","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.typep(Symbol.SIMPLE_BIT_VECTOR);
        }
    };

    // ### eval
    private static final Primitive1 EVAL = new Primitive1("eval","form")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return eval(arg, new Environment(), LispThread.currentThread());
        }
    };

    // ### eq
    private static final Primitive2 EQ = new Primitive2("eq","x y")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first == second ? T : NIL;
        }
    };

    // ### eql
    private static final Primitive2 EQL = new Primitive2("eql","x y")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.eql(second) ? T : NIL;
        }
    };

    // ### equal
    private static final Primitive2 EQUAL = new Primitive2("equal","x y")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.equal(second) ? T : NIL;
        }
    };

    // ### equalp
    private static final Primitive2 EQUALP = new Primitive2("equalp","x y")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.equalp(second) ? T : NIL;
        }
    };

    // ### values
    private static final Primitive VALUES = new Primitive("values","&rest object")
    {
        public LispObject execute()
        {
            return LispThread.currentThread().setValues();
        }
        public LispObject execute(LispObject arg)
        {
            return LispThread.currentThread().setValues(arg);
        }
        public LispObject execute(LispObject first, LispObject second)
        {
            return LispThread.currentThread().setValues(first, second);
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
        {
            return LispThread.currentThread().setValues(first, second, third);
        }
        public LispObject execute(LispObject[] args)
        {
            return LispThread.currentThread().setValues(args);
        }
    };

    // ### values-list
    // values-list list => element*
    // Returns the elements of the list as multiple values.
    private static final Primitive1 VALUES_LIST = new Primitive1("values-list","list")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return LispThread.currentThread().setValues(arg.copyToArray());
        }
    };

    // ### cons
    private static final Primitive2 CONS = new Primitive2("cons","object-1 object-2")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new Cons(first, second);
        }
    };

    // ### length
    private static final Primitive1 LENGTH = new Primitive1("length","sequence")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.LENGTH();
        }
    };

    // ### elt
    private static final Primitive2 ELT = new Primitive2("elt","sequence index")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.elt(Fixnum.getValue(second));
        }
    };

    // ### atom
    private static final Primitive1 ATOM = new Primitive1("atom","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg instanceof Cons ? NIL : T;
        }
    };

    // ### constantp
    private static final Primitive CONSTANTP = new Primitive("constantp","form &optional environment")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.CONSTANTP();
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.CONSTANTP();
        }
    };

    // ### functionp
    private static final Primitive1 FUNCTIONP = new Primitive1("functionp","object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return (arg instanceof Function || arg instanceof GenericFunction) ? T : NIL;
        }
    };

    // ### special-operator-p
    private static final Primitive1 SPECIAL_OPERATOR_P =
        new Primitive1("special-operator-p","symbol")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.getSymbolFunction() instanceof SpecialOperator ? T : NIL;
        }
    };

    // ### symbolp
    private static final Primitive1 SYMBOLP = new Primitive1("symbolp","object") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.SYMBOLP();
        }
    };

    // ### endp
    private static final Primitive1 ENDP = new Primitive1("endp","list") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.ENDP();
        }
    };

    // ### null
    private static final Primitive1 NULL = new Primitive1("null","object") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg == NIL ? T : NIL;
        }
    };

    // ### not
    private static final Primitive1 NOT = new Primitive1("not","x") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg == NIL ? T : NIL;
        }
    };

    // ### plusp
    private static final Primitive1 PLUSP = new Primitive1("plusp","real") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.PLUSP();
        }
    };

    // ### minusp
    private static final Primitive1 MINUSP = new Primitive1("minusp","real") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.MINUSP();
        }
    };

    // ### zerop
    private static final Primitive1 ZEROP = new Primitive1("zerop","number") {
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

    // ### symbol-value
    private static final Primitive1 SYMBOL_VALUE = new Primitive1("symbol-value","symbol")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            final Symbol symbol = checkSymbol(arg);
            LispObject value =
                LispThread.currentThread().lookupSpecial(symbol);
            if (value == null) {
                value = symbol.symbolValue();
                if (value instanceof SymbolMacro)
                    signal(new LispError(arg + " has no dynamic value"));
            }
            return value;
        }
    };

    // ### set
    // set symbol value => value
    private static final Primitive2 SET = new Primitive2("set","symbol value")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            Environment dynEnv =
                LispThread.currentThread().getDynamicEnvironment();
            if (dynEnv != null) {
                Binding binding = dynEnv.getBinding(symbol);
                if (binding != null) {
                    binding.value = second;
                    return second;
                }
            }
            symbol.setSymbolValue(second);
            return second;
        }
    };

    // ### rplaca
    private static final Primitive2 RPLACA = new Primitive2("rplaca","cons object")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
                first.setCar(second);
                return first;
        }
    };

    // ### rplacd
    private static final Primitive2 RPLACD = new Primitive2("rplacd","cons object")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
                first.setCdr(second);
                return first;
        }
    };

    // ### +
    private static final Primitive ADD = new Primitive("+","&rest numbers")
    {
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

    // ### 1+
    private static final Primitive1 ONE_PLUS = new Primitive1("1+","number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.incr();
        }
    };

    // ### -
    private static final Primitive SUBTRACT = new Primitive("-","minuend &rest subtrahends")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.subtract(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            switch (args.length) {
                case 0:
                    signal(new WrongNumberOfArgumentsException("-"));
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

    // ### 1-
    private static final Primitive1 ONE_MINUS = new Primitive1("1-","number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.decr();
        }
    };

    // ### when
    private static final SpecialOperator WHEN = new SpecialOperator("when") {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args == NIL)
                signal(new WrongNumberOfArgumentsException(this));
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
                signal(new WrongNumberOfArgumentsException(this));
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
            outSynonymOf(second)._writeString(String.valueOf(first));
            return first;
        }
    };

    // ### %write-to-string
    // %write-to-string object => string
    private static final Primitive1 _WRITE_TO_STRING =
        new Primitive1("%write-to-string", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new LispString(String.valueOf(arg));
        }
    };

    // ### princ
    // princ object &optional output-stream => object
    private static final Primitive PRINC = new Primitive("princ","object &optional output-stream") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 2)
                signal(new WrongNumberOfArgumentsException(this));
            final Stream out;
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
        new Primitive1("princ-to-string","object") {
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
    private static final Primitive PRIN1 = new Primitive("prin1","object &optional output-stream") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Stream out =
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
        new Primitive1("prin1-to-string","object") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new LispString(String.valueOf(arg));
        }
    };

    // ### print
    // print object &optional output-stream => object
    // PRINT is just like PRIN1 except that the printed representation of
    // object is preceded by a newline and followed by a space.
    private static final Primitive1 PRINT = new Primitive1("print","object &optional output-stream") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Stream out =
                checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            out.terpri();
            out.prin1(arg);
            out._writeString(" ");
            return arg;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Stream out = outSynonymOf(second);
            out.terpri();
            out.prin1(first);
            out._writeString(" ");
            return first;
        }
    };

    // ### terpri
    // terpri &optional output-stream => nil
    private static final Primitive TERPRI = new Primitive("terpri","&optional output-stream") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                signal(new WrongNumberOfArgumentsException(this));
            final Stream out;
            if (args.length == 0)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[0]);
            return out.terpri();
        }
    };

    // ### fresh-line
    // fresh-line &optional output-stream => generalized-boolean
    private static final Primitive FRESH_LINE = new Primitive("fresh-line","&optional output-stream") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                signal(new WrongNumberOfArgumentsException(this));
            Stream out;
            if (args.length == 0)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[0]);
            return out.freshLine();
        }
    };

    // ### boundp
    // Determines only whether a symbol has a value in the global environment;
    // any lexical bindings are ignored.
    private static final Primitive1 BOUNDP = new Primitive1("boundp","symbol")
    {
        public LispObject execute(LispObject obj) throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(obj);
            // PROGV: "If too few values are supplied, the remaining symbols
            // are bound and then made to have no value." So BOUNDP must
            // explicitly check for a binding with no value.
            Environment dynEnv =
                LispThread.currentThread().getDynamicEnvironment();
            if (dynEnv != null) {
                Binding binding = dynEnv.getBinding(symbol);
                if (binding != null)
                    return binding.value != null ? T : NIL;
            }
            // No binding.
            return symbol.getSymbolValue() != null ? T : NIL;
        }
    };

    // ### fboundp
    private static final Primitive1 FBOUNDP = new Primitive1("fboundp","name")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Symbol)
                return arg.getSymbolFunction() != null ? T : NIL;
            if (arg instanceof Cons && arg.car() == Symbol.SETF) {
                LispObject f = get(checkSymbol(arg.cadr()),
                                   PACKAGE_SYS.intern("SETF-FUNCTION"));
                return f != null ? T : NIL;
            }
            signal(new TypeError(arg, "valid function name"));
            return NIL;
        }
    };

    // ### fmakunbound
    private static final Primitive1 FMAKUNBOUND = new Primitive1("fmakunbound","name")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Symbol) {
                ((Symbol)arg).setSymbolFunction(null);
            } else if (arg instanceof Cons && arg.car() == Symbol.SETF) {
                remprop(checkSymbol(arg.cadr()),
                        PACKAGE_SYS.intern("SETF-FUNCTION"));
            } else
                signal(new TypeError(arg, "valid function name"));
            return arg;
        }
    };

    // ### remprop
    private static final Primitive2 REMPROP = new Primitive2("remprop","symbol indicator")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return remprop(checkSymbol(first), second);
        }
    };

    // ### append
    public static final Primitive APPEND = new Primitive("append","&rest lists") {
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
    private static final Primitive NCONC = new Primitive("nconc","&rest lists") {
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
                            signal(new TypeError(list, "list"));
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
    private static final Primitive EQUALS = new Primitive("=","&rest numbers") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isEqualTo(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length < 1)
                signal(new WrongNumberOfArgumentsException(this));
            final LispObject obj = array[0];
            for (int i = 1; i < length; i++) {
                if (array[i].isNotEqualTo(obj))
                    return NIL;
            }
            return T;
        }
    };

    // Returns true if no two numbers are the same; otherwise returns false.
    private static final Primitive NOT_EQUALS = new Primitive("/=","&rest numbers") {
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
                signal(new WrongNumberOfArgumentsException(this));
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
    private static final Primitive LESS_THAN = new Primitive("<","&rest numbers") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isLessThan(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length < 1)
                signal(new WrongNumberOfArgumentsException(this));
            for (int i = 1; i < length; i++) {
                if (array[i].isLessThanOrEqualTo(array[i-1]))
                    return NIL;
            }
            return T;
        }
    };

    // ### <=
    private static final Primitive LE = new Primitive("<=","&rest numbers") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isLessThanOrEqualTo(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            switch (array.length) {
                case 0:
                    signal(new WrongNumberOfArgumentsException(this));
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
    private static final Primitive GREATER_THAN = new Primitive(">","&rest numbers") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.isGreaterThan(second) ? T : NIL;
        }
        public LispObject execute(LispObject[] array) throws ConditionThrowable
        {
            final int length = array.length;
            if (length < 1)
                signal(new WrongNumberOfArgumentsException(this));
            for (int i = 1; i < length; i++) {
                if (array[i].isGreaterThanOrEqualTo(array[i-1]))
                    return NIL;
            }
            return T;
        }
    };

    // ### >=
    private static final Primitive GE = new Primitive(">=","&rest numbers") {
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
                    signal(new WrongNumberOfArgumentsException(this));
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
    // Redefined properly in assoc.lisp.
    private static final Primitive ASSOC = new Primitive("assoc","item alist &key key test test-not")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 2)
                signal(new WrongNumberOfArgumentsException(this));
            LispObject item = args[0];
            LispObject alist = args[1];
            while (alist != NIL) {
                LispObject cons = alist.car();
                if (cons instanceof Cons) {
                    if (cons.car().eql(item))
                        return cons;
                } else if (cons != NIL)
                    signal(new TypeError(cons, "list"));
                alist = alist.cdr();
            }
            return NIL;
        }
    };

    // ### nth
    // nth n list => object
    private static final Primitive2 NTH = new Primitive2("nth", "n list")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            int index = Fixnum.getValue(first);
            if (index < 0)
                signal(new TypeError("NTH: invalid index " + index + "."));
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

    // ### %set-nth
    // %setnth n list new-object => new-object
    private static final Primitive3 _SET_NTH =
        new Primitive3("%set-nth", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            int index = Fixnum.getValue(first);
            if (index < 0)
                signal(new TypeError("(SETF NTH): invalid index " + index + "."));
            int i = 0;
            while (true) {
                if (i == index) {
                    second.setCar(third);
                    return third;
                }
                second = second.cdr();
                if (second == NIL) {
                    return signal(new LispError("(SETF NTH): the index " +
                                                index + "is too large."));
                }
                ++i;
            }
        }
    };

    // ### nthcdr
    private static final Primitive2 NTHCDR = new Primitive2("nthcdr", "n list")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            final int index = Fixnum.getValue(first);
            if (index < 0)
                signal(new TypeError("NTHCDR: invalid index " + index + "."));
            for (int i = 0; i < index; i++) {
                second = second.cdr();
                if (second == NIL)
                    return NIL;
            }
            return second;
        }
    };

    // ### error
    private static final Primitive ERROR = new Primitive("error", "datum &rest arguments")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1) {
                signal(new WrongNumberOfArgumentsException(this));
                return NIL;
            }
            LispObject datum = args[0];
            if (datum instanceof Condition) {
                signal((Condition)datum);
                return NIL;
            }
            if (datum instanceof Symbol) {
                LispObject initArgs = NIL;
                for (int i = 1; i < args.length; i++)
                    initArgs = new Cons(args[i], initArgs);
                initArgs = initArgs.nreverse();
                Condition condition;
                if (datum == Symbol.FILE_ERROR)
                    condition = new FileError(initArgs);
                else if (datum == Symbol.PACKAGE_ERROR)
                    condition = new PackageError(initArgs);
                else if (datum == Symbol.PARSE_ERROR)
                    condition = new ParseError(initArgs);
                else if (datum == Symbol.PROGRAM_ERROR)
                    condition = new ProgramError(initArgs);
                else if (datum == Symbol.SIMPLE_CONDITION)
                    condition = new SimpleCondition(initArgs);
                else if (datum == Symbol.SIMPLE_WARNING)
                    condition = new SimpleWarning(initArgs);
                else if (datum == Symbol.UNBOUND_SLOT)
                    condition = new UnboundSlot(initArgs);
                else if (datum == Symbol.WARNING)
                    condition = new Warning(initArgs);
                else if (datum == Symbol.SIMPLE_ERROR)
                    condition = new SimpleError(initArgs);
                else if (datum == Symbol.SIMPLE_TYPE_ERROR)
                    condition = new SimpleTypeError(initArgs);
                else if (datum == Symbol.CONTROL_ERROR)
                    condition = new ControlError(initArgs);
                else if (datum == Symbol.TYPE_ERROR)
                    condition = new TypeError(initArgs);
                else if (datum == Symbol.UNDEFINED_FUNCTION)
                    condition = new UndefinedFunction(initArgs);
                else
                    // Default.
                    condition = new  SimpleError(initArgs);
                signal(condition);
                return NIL;
            }
            // Default is SIMPLE-ERROR.
            LispObject formatControl = args[0];
            LispObject formatArguments = NIL;
            for (int i = 1; i < args.length; i++)
                formatArguments = new Cons(args[i], formatArguments);
            formatArguments = formatArguments.nreverse();
            signal(new SimpleError(formatControl, formatArguments));
            return NIL;
        }
    };

    // ### signal
    private static final Primitive SIGNAL =
        new Primitive("signal", "datum &rest arguments")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
            if (args[0] instanceof Condition)
                throw new ConditionThrowable((Condition)args[0]);
            throw new ConditionThrowable (new SimpleCondition());
        }
    };

    // ### format
    private static final Primitive FORMAT =
        new Primitive("format", "destination control-string &rest args")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            return _FORMAT.execute(args);
        }
    };

    private static final Primitive _FORMAT =
        new Primitive("%format", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 2)
                signal(new WrongNumberOfArgumentsException(this));
            LispObject destination = args[0];
            // Copy remaining arguments.
            LispObject[] _args = new LispObject[args.length-1];
            for (int i = 0; i < _args.length; i++)
                _args[i] = args[i+1];
            String s = _format(_args);
            if (destination == T) {
                checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue())._writeString(s);
                return NIL;
            }
            if (destination == NIL)
                return new LispString(s);
            if (destination instanceof TwoWayStream) {
                Stream out = ((TwoWayStream)destination).getOutputStream();
                if (out instanceof Stream) {
                    ((Stream)out)._writeString(s);
                    return NIL;
                }
                signal(new TypeError(destination, "character output stream"));
            }
            if (destination instanceof Stream) {
                ((Stream)destination)._writeString(s);
                return NIL;
            }
            // Destination can also be a string with a fill pointer.
//             signal(new LispError("FORMAT: not implemented"));
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
        LispObject formatControl = args[0];
        LispObject formatArguments = NIL;
        for (int i = 1; i < args.length; i++)
            formatArguments = new Cons(args[i], formatArguments);
        formatArguments = formatArguments.nreverse();
        return format(formatControl, formatArguments);
    }

    // ### %defun
    // %defun name arglist body &optional environment => name
    private static final Primitive _DEFUN = new Primitive("%defun", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 3 || args.length > 4) {
                signal(new WrongNumberOfArgumentsException(this));
                return NIL;
            }
            LispObject first = args[0];
            LispObject second = args[1];
            LispObject third = args[2];
            Environment env;
            if (args.length == 4 && args[3] != NIL)
                env = checkEnvironment(args[3]);
            else
                env = new Environment();
            final Symbol symbol;
            if (first instanceof Symbol) {
                symbol = checkSymbol(first);
                if (symbol.getSymbolFunction() instanceof SpecialOperator) {
                    String message =
                        symbol.getName() + " is a special operator and may not be redefined.";
                    return signal(new ProgramError(message));
                }
            } else if (first instanceof Cons && first.car() == Symbol.SETF) {
                symbol = checkSymbol(first.cadr());
            } else
                return signal(new TypeError(first, "valid function name"));
            LispObject arglist = checkList(second);
            LispObject body = checkList(third);
            if (body.car() instanceof LispString && body.cdr() != NIL) {
                // Documentation.
                if (first instanceof Symbol)
                    symbol.setFunctionDocumentation(body.car());
                else
                    ; // FIXME Support documentation for SETF functions!
                body = body.cdr();
            }
            LispObject decls = NIL;
            while (body.car() instanceof Cons && body.car().car() == Symbol.DECLARE) {
                decls = new Cons(body.car(), decls);
                body = body.cdr();
            }
            body = new Cons(symbol, body);
            body = new Cons(Symbol.BLOCK, body);
            body = new Cons(body, NIL);
            while (decls != NIL) {
                body = new Cons(decls.car(), body);
                decls = decls.cdr();
            }
            final String name;
            if (first instanceof Symbol)
                name = symbol.getName();
            else
                name = null;
            Closure closure = new Closure(name, arglist, body, env);
            closure.setArglist(arglist);
            if (first instanceof Symbol)
                symbol.setSymbolFunction(closure);
            else
                // SETF function
                put(symbol, PACKAGE_SYS.intern("SETF-FUNCTION"), closure);
            return first;
        }
    };

    // ### macro-function
    // Need to support optional second argument specifying environment.
    private static final Primitive MACRO_FUNCTION =
        new Primitive("macro-function", "symbol &optional environment")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            LispObject obj = arg.getSymbolFunction();
            if (obj instanceof AutoloadMacro) {
                ((AutoloadMacro)obj).load();
                obj = arg.getSymbolFunction();
            }
            if (obj instanceof MacroObject)
                return ((MacroObject)obj).getExpander();
            if (obj instanceof SpecialOperator) {
                LispObject macroObject =
                    get((Symbol)arg, Symbol.MACROEXPAND_MACRO, NIL);
                if (macroObject instanceof MacroObject)
                    return ((MacroObject)macroObject).getExpander();
                return NIL;
            }
            return NIL;
        }
    };

    // ### defmacro
    private static final SpecialOperator DEFMACRO = new SpecialOperator("defmacro")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(args.car());
            LispObject lambdaList = checkList(args.cadr());
            LispObject body = args.cddr();
            LispObject block = new Cons(Symbol.BLOCK, new Cons(symbol, body));
            LispObject toBeApplied =
                list2(Symbol.FUNCTION, list3(Symbol.LAMBDA, lambdaList, block));
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
	    macroObject.setArglist(lambdaList);
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
                signal(new TypeError(third, "string"));
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
        new Primitive3("%defconstant", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            if (third instanceof LispString)
                symbol.setVariableDocumentation(third);
            else if (third != NIL)
                signal(new TypeError(third, "string"));
            symbol.setSymbolValue(second);
            symbol.setSpecial(true);
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
    private static final SpecialOperator CASE = new SpecialOperator("case")
    {
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
    private static final SpecialOperator ECASE = new SpecialOperator("ecase")
    {
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
                    if (key.eql(candidate))
                        match = true;
                }
                if (match) {
                    return progn(clause.cdr(), env, thread);
                }
                args = args.cdr();
            }
            signal(new TypeError("ECASE: no match for " + key));
            return NIL;
        }
    };

    // ### upgraded-array-element-type
    // upgraded-array-element-type typespec &optional environment
    // => upgraded-typespec
    private static final Primitive UPGRADED_ARRAY_ELEMENT_TYPE =
        new Primitive("upgraded-array-element-type", "typespec &optional environment") {
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
        new Primitive1("array-rank", "array") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Fixnum(checkArray(arg).getRank());
        }
    };

    // ### array-dimensions
    // array-dimensions array => dimensions
    // Returns a list of integers. Fill pointer (if any) is ignored.
    private static final Primitive1 ARRAY_DIMENSIONS =
        new Primitive1("array-dimensions", "array") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return checkArray(arg).getDimensions();
        }
    };

    // ### array-dimension
    // array-dimension array axis-number => dimension
    private static final Primitive2 ARRAY_DIMENSION =
        new Primitive2("array-dimension", "array axis-number") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return new Fixnum(checkArray(first).getDimension(Fixnum.getValue(second)));
        }
    };

    // ### array-total-size
    // array-total-size array => size
    private static final Primitive1 ARRAY_TOTAL_SIZE =
        new Primitive1("array-total-size","array") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new Fixnum(checkArray(arg).getTotalSize());
        }
    };


    // ### array-element-type
    // array-element-type array => typespec
    private static final Primitive1 ARRAY_ELEMENT_TYPE =
        new Primitive1("array-element-type", "array")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return checkArray(arg).getElementType();
        }
    };

    // ### adjustable-array-p
    private static final Primitive1 ADJUSTABLE_ARRAY_P =
        new Primitive1("adjustable-array-p", "array")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof AbstractArray)
                return T;
            return signal(new TypeError(arg, Symbol.ARRAY));
        }
    };

    // ### array-in-bounds-p
    // array-in-bounds-p array &rest subscripts => generalized-boolean
    private static final Primitive ARRAY_IN_BOUNDS_P =
        new Primitive("array-in-bounds-p", "array &rest subscripts")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                signal(new WrongNumberOfArgumentsException(this));
            AbstractArray array = checkArray(args[0]);
            int rank = array.getRank();
            if (rank != args.length - 1) {
                StringBuffer sb = new StringBuffer("ARRAY-IN-BOUNDS-P: ");
                sb.append("wrong number of subscripts (");
                sb.append(args.length - 1);
                sb.append(") for array of rank ");
                sb.append(rank);
                signal(new ProgramError(sb.toString()));
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
                    signal(new TypeError(arg, "integer"));
            }
            return T;
        }
    };

    // ### %array-row-major-index
    // %array-row-major-index array subscripts => index
    private static final Primitive2 _ARRAY_ROW_MAJOR_INDEX =
        new Primitive2("%array-row-major-index", PACKAGE_SYS, false)
    {
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
    private static final Primitive AREF =
        new Primitive("aref", "array &rest subscripts")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            AbstractArray array = checkArray(arg);
            if (array.getRank() == 0)
                return array.getRowMajor(0);
            StringBuffer sb = new StringBuffer("AREF: ");
            sb.append("wrong number of subscripts (0) for array of rank ");
            sb.append(array.getRank());
            signal(new ProgramError(sb.toString()));
            return NIL;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return first.AREF(second);
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1)
                signal(new WrongNumberOfArgumentsException(this));
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
            signal(new ProgramError(sb.toString()));
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
                    signal(new ProgramError());
                sum += n * lastSize;
            } else if (subscript instanceof Bignum) {
                signal(new ProgramError());
            } else
                signal(new TypeError(subscript, "integer"));
        }
        return sum;
    }

    // ### row-major-aref
    // row-major-aref array index => element
    private static final Primitive2 ROW_MAJOR_AREF =
        new Primitive2("row-major-aref","array index") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return checkArray(first).getRowMajor(Fixnum.getValue(second));
        }
    };

    // ### %set-row-major-aref
    // %set-row-major-aref array index new-value => new-value
    private static final Primitive3 _SET_ROW_MAJOR_AREF =
        new Primitive3("%set-row-major-aref", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((AbstractArray)first).setRowMajor(Fixnum.getValue(second), third);
                return third;
            }
            catch (ClassCastException e) {
                signal(new TypeError(first, "array"));
                return NIL;
            }
        }
    };

    // ### vector
    private static final Primitive VECTOR = new Primitive("vector","&rest objects") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            return new Vector(args);
        }
    };

    // ### %vset
    // %vset vector index new-value => new-value
    private static final Primitive3 _VSET =
        new Primitive3("%vset", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                ((AbstractVector)first).set(Fixnum.getValue(second), third);
                return third;
            }
            catch (ClassCastException e) {
                signal(new TypeError(first, "vector"));
                return NIL;
            }
        }
    };

    // ### svref
    // svref simple-vector index => element
    private static final Primitive2 SVREF = new Primitive2("svref","simple-vector index") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(first);
            if (!v.isSimpleVector())
                signal(new TypeError(first, "simple vector"));
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
                signal(new TypeError(first, "simple vector"));
            int i = v.checkIndex(second);
            v.set(i, third);
            return third;
        }
    };

    // ### fill-pointer
    private static final Primitive1 FILL_POINTER =
        new Primitive1("fill-pointer", "vector")
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            int fillPointer = -1;
            if (arg instanceof AbstractVector)
                fillPointer = ((AbstractVector)arg).getFillPointer();
            else if (arg instanceof DisplacedArray)
                fillPointer = ((DisplacedArray)arg).getFillPointer();
            if (fillPointer >= 0)
                return new Fixnum(fillPointer);
            if (arg instanceof AbstractArray)
                return ((AbstractArray)arg).noFillPointer();
            else
                return signal(new TypeError(arg, Symbol.ARRAY));
        }
    };

    // ### %set-fill-pointer
    private static final Primitive2 _SET_FILL_POINTER =
        new Primitive2("%set-fill-pointer", PACKAGE_SYS, false) {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(first);
            if (v.getFillPointer() < 0)
                v.noFillPointer();
            v.setFillPointer(second);
            return second;
        }
    };

    // ### vector-push
    // vector-push new-element vector => index-of-new-element
    private static final Primitive2 VECTOR_PUSH =
        new Primitive2("vector-push","new-element vector") {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            AbstractVector v = checkVector(second);
            int fillPointer = v.getFillPointer();
            if (fillPointer < 0)
                v.noFillPointer();
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
        new Primitive("vector-push-extend",
                      "new-element vector &optional extension")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            try {
                return ((AbstractVector)second).vectorPushExtend(first);
            }
            catch (ClassCastException e) {
                return signal(new TypeError(second, Symbol.VECTOR));
            }
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            try {
                return ((AbstractVector)second).vectorPushExtend(first, third);
            }
            catch (ClassCastException e) {
                return signal(new TypeError(second, Symbol.VECTOR));
            }
        }
    };

    // ### vector-pop
    // vector-pop vector => element
    private static final Primitive1 VECTOR_POP = new Primitive1("vector-pop", "vector")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            AbstractVector v = checkVector(arg);
            int fillPointer = v.getFillPointer();
            if (fillPointer < 0)
                v.noFillPointer();
            if (fillPointer == 0)
                signal(new LispError("nothing left to pop"));
            int newFillPointer = v.checkIndex(fillPointer - 1);
            LispObject element = v.get(newFillPointer);
            v.setFillPointer(newFillPointer);
            return element;
        }
    };

    // ### type-of
    private static final Primitive1 TYPE_OF = new Primitive1("type-of", "object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.typeOf();
        }
    };

    // ### class-of
    private static final Primitive1 CLASS_OF = new Primitive1("class-of", "object")
    {
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
        new Primitive1("function-lambda-expression", "function")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            final LispObject value1, value2;
            Function function = checkFunction(arg);
            String name = function.getName();
            final LispObject value3 = name != null ? new LispString(name) : NIL;
            if (function instanceof Closure) {
                Closure closure = (Closure) function;
                LispObject expr = closure.getBody();
                expr = new Cons(closure.getParameterList(), expr);
                expr = new Cons(Symbol.LAMBDA, expr);
                value1 = expr;
                Environment env = closure.getEnvironment();
                if (env == null || env.isEmpty())
                    value2 = NIL;
                else
                    value2 = env; // Return environment as closure-p.
            } else
                value1 = value2 = NIL;
            return LispThread.currentThread().setValues(value1, value2, value3);
        }
    };

    // ### funcall
    // This needs to be public for LispAPI.java.
    public static final Primitive FUNCALL =
        new Primitive("funcall", "function &rest args")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return funcall0(requireFunction(arg), LispThread.currentThread());
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return funcall1(requireFunction(first), second,
                            LispThread.currentThread());
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            return funcall2(requireFunction(first), second, third,
                            LispThread.currentThread());
        }
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1) {
                signal(new WrongNumberOfArgumentsException(this));
                return NIL;
            }
            LispObject fun = requireFunction(args[0]);
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
        private LispObject requireFunction(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Function || arg instanceof GenericFunction)
                return arg;
            if (arg instanceof Symbol) {
                LispObject function = arg.getSymbolFunction();
                if (function instanceof Function || function instanceof GenericFunction)
                    return function;
                return signal(new UndefinedFunction(arg));
            }
            return signal(new TypeError(arg, list3(Symbol.OR, Symbol.FUNCTION,
                                                   Symbol.SYMBOL)));
        }
    };

    // ### apply
    public static final Primitive APPLY =
        new Primitive("apply", "function &rest args")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            LispObject spread = checkList(second);
            LispObject fun = first;
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (fun instanceof Function || fun instanceof GenericFunction) {
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
            signal(new TypeError(fun, "function"));
            return NIL;
        }
        public LispObject execute(final LispObject[] args) throws ConditionThrowable
        {
            final int numArgs = args.length;
            if (numArgs < 2)
                signal(new WrongNumberOfArgumentsException(this));
            LispObject spread = checkList(args[numArgs - 1]);
            LispObject fun = args[0];
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (fun instanceof Function || fun instanceof GenericFunction) {
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
            signal(new TypeError(fun, "function"));
            return NIL;
        }
    };

    // ### mapcar
    private static final Primitive MAPCAR = new Primitive("mapcar","function &rest lists")
    {
        public LispObject execute(LispObject op, LispObject list)
            throws ConditionThrowable
        {
            LispObject fun;
            if (op instanceof Symbol)
                fun = op.getSymbolFunction();
            else
                fun = op;
            if (fun instanceof Function || fun instanceof GenericFunction) {
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
            signal(new UndefinedFunction(op));
            return NIL;
        }
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            // First argument must be a function.
            LispObject fun = first;
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (!(fun instanceof Function || fun instanceof GenericFunction))
                signal(new UndefinedFunction(first));
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
                signal(new WrongNumberOfArgumentsException(this));
            // First argument must be a function.
            LispObject fun = args[0];
            if (fun instanceof Symbol)
                fun = fun.getSymbolFunction();
            if (!(fun instanceof Function || fun instanceof GenericFunction))
                signal(new UndefinedFunction(args[0]));
            // Remaining arguments must be lists.
            int commonLength = -1;
            for (int i = 1; i < numArgs; i++) {
                if (!args[i].listp())
                    signal(new TypeError(args[i], "list"));
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
    private static final Primitive MACROEXPAND = new Primitive("macroexpand","form &optional env") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length < 1 || length > 2)
                signal(new WrongNumberOfArgumentsException(this));
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
    private static final Primitive MACROEXPAND_1 = new Primitive("macroexpand-1","form &optional env")
    {
        public LispObject execute(LispObject form) throws ConditionThrowable
        {
            return macroexpand_1(form,
                                 new Environment(),
                                 LispThread.currentThread());
        }
        public LispObject execute(LispObject form, LispObject env)
            throws ConditionThrowable
        {
            return macroexpand_1(form,
                                 env != NIL ? checkEnvironment(env) : new Environment(),
                                 LispThread.currentThread());
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
    private static final Primitive GENSYM = new Primitive("gensym","&optional x")
    {
        public LispObject execute() throws ConditionThrowable
        {
            return gensym("G");
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            String prefix = "G";
            if (arg instanceof Fixnum) {
                int n = ((Fixnum)arg).getValue();
                if (n < 0)
                    signal(new TypeError(arg,
                                                               "non-negative integer"));
                StringBuffer sb = new StringBuffer(prefix);
                sb.append(n);
                return new Symbol(sb.toString());
            }
            if (arg instanceof Bignum) {
                BigInteger n = ((Bignum)arg).getValue();
                if (n.signum() < 0)
                    signal(new TypeError(arg,
                                                               "non-negative integer"));
                StringBuffer sb = new StringBuffer(prefix);
                sb.append(n.toString());
                return new Symbol(sb.toString());
            }
            if (arg instanceof LispString)
                prefix = ((LispString)arg).getValue();
            else
                signal(new TypeError(arg, "string or non-negative integer"));
            return gensym(prefix);
        }
    };

    private static final Symbol gensym(String prefix) throws ConditionThrowable
    {
        LispThread thread = LispThread.currentThread();
        Environment dynEnv = thread.getDynamicEnvironment();
        Binding binding =
            (dynEnv == null) ? null : dynEnv.getBinding(_GENSYM_COUNTER_);
        LispObject oldValue;
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
    private static final Primitive1 STRING = new Primitive1("string","x") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return string(arg);
        }
    };

    // ### intern
    // intern string &optional package => symbol, status
    // status is one of :INHERITED, :EXTERNAL, :INTERNAL or NIL.
    private static final Primitive INTERN =
        new Primitive("intern"," string &optional package")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            String s;
            try {
                s = ((LispString)arg).getValue();
            }
            catch (ClassCastException e) {
                if (arg instanceof NilVector) {
                    if (arg.length() == 0)
                        s = "";
                    else
                        return ((NilVector)arg).accessError();
                } else
                    return signal(new TypeError(arg, Symbol.STRING));
            }
            final LispThread thread = LispThread.currentThread();
            Package pkg =
                (Package) _PACKAGE_.symbolValueNoThrow(thread);
            return pkg.intern(s, thread);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            String s;
            try {
                s = ((LispString)first).getValue();
            }
            catch (ClassCastException e) {
                if (first instanceof NilVector) {
                    if (first.length() == 0)
                        s = "";
                    else
                        return ((NilVector)first).accessError();
                } else
                    return signal(new TypeError(first, Symbol.STRING));
            }
            Package pkg = coerceToPackage(second);
            return pkg.intern(s, LispThread.currentThread());
        }
    };

    // ### unintern
    // unintern symbol &optional package => generalized-boolean
    private static final Primitive UNINTERN =
        new Primitive("unintern", "symbol &optional package")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length == 0 || args.length > 2)
                signal(new WrongNumberOfArgumentsException(this));
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
        new Primitive1("find-package","name") {
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
                signal(new LispError("package " + packageName +
                                                           " already exists"));
            LispObject nicknames = checkList(second);
            if (nicknames != NIL) {
                LispObject list = nicknames;
                while (list != NIL) {
                    String nick = javaString(list.car());
                    if (Packages.findPackage(nick) != null) {
                        signal(new PackageError("a package named " + nick +
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
                        if (p == null) {
                            signal(new LispError(String.valueOf(obj) +
                                                 " is not the name of a package"));
                            return NIL;
                        }
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
                    if (p == null) {
                        signal(new LispError(String.valueOf(obj) +
                                             " is not the name of a package"));
                        return NIL;
                    }
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
                signal(new PackageError("package " + arg + " does not exist"));
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
    private static final Primitive USE_PACKAGE = new Primitive("use-package","packages-to-use &optional package") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 2)
                signal(new WrongNumberOfArgumentsException(this));
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

    // ### package-symbols
    private static final Primitive1 PACKAGE_SYMBOLS =
        new Primitive1("package-symbols", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).getSymbols();
        }
    };

    // ### package-internal-symbols
    private static final Primitive1 PACKAGE_INTERNAL_SYMBOLS =
        new Primitive1("package-internal-symbols", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).PACKAGE_INTERNAL_SYMBOLS();
        }
    };

    // ### package-external-symbols
    private static final Primitive1 PACKAGE_EXTERNAL_SYMBOLS =
        new Primitive1("package-external-symbols", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).PACKAGE_EXTERNAL_SYMBOLS();
        }
    };

    // ### package-inherited-symbols
    private static final Primitive1 PACKAGE_INHERITED_SYMBOLS =
        new Primitive1("package-inherited-symbols", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPackage(arg).PACKAGE_INHERITED_SYMBOLS();
        }
    };

    // ### export
    // export symbols &optional package
    private static final Primitive EXPORT =
        new Primitive("export","symbols &optional package") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length == 0 || args.length > 2)
                signal(new WrongNumberOfArgumentsException(this));
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
        new Primitive("find-symbol","string &optional package") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length == 0 || args.length > 2)
                signal(new WrongNumberOfArgumentsException(this));
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
    private static final Primitive2 FSET = new Primitive2("fset", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkSymbol(first).setSymbolFunction(second);
            return second;
        }
    };

    // ### %set-symbol-plist
    private static final Primitive2 _SET_SYMBOL_PLIST =
        new Primitive2("%set-symbol-plist", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkSymbol(first).setPropertyList(checkList(second));
            return second;
        }
    };

    // ### getf
    // getf plist indicator &optional default => value
    private static final Primitive GETF =
        new Primitive("getf", "plist indicator &optional default")
    {
        public LispObject execute(LispObject plist, LispObject indicator)
            throws ConditionThrowable
        {
            return getf(plist, indicator, NIL);
        }
        public LispObject execute(LispObject plist, LispObject indicator,
                                  LispObject defaultValue)
            throws ConditionThrowable
        {
            return getf(plist, indicator, defaultValue);
        }
    };

    // ### get
    // get symbol indicator &optional default => value
    private static final Primitive GET =
        new Primitive("get", "symbol indicator &optional default")
    {
        public LispObject execute(LispObject symbol, LispObject indicator)
            throws ConditionThrowable
        {
            return get(checkSymbol(symbol), indicator, NIL);
        }
        public LispObject execute(LispObject symbol, LispObject indicator,
                                  LispObject defaultValue)
            throws ConditionThrowable
        {
            return get(checkSymbol(symbol), indicator, defaultValue);
        }
    };

    // ### %put
    // %put symbol indicator value => value
    private static final Primitive3 _PUT = new Primitive3("%put", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject symbol, LispObject indicator,
                                  LispObject value)
            throws ConditionThrowable
        {
            return put(checkSymbol(symbol), indicator, value);
        }
    };

    // ### macrolet
    private static final SpecialOperator MACROLET = new SpecialOperator("macrolet")
    {
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
    private static final SpecialOperator TAGBODY = new SpecialOperator("tagbody")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            Environment ext = new Environment(env);
            LispObject body = args;
            while (body != NIL) {
                LispObject current = body.car();
                body = body.cdr();
                if (current instanceof Cons)
                    continue;
                // It's a tag.
                ext.addTagBinding(current, body);
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
                            LispObject tag = current.cadr();
                            Binding binding = ext.getTagBinding(tag);
                            if (binding != null && binding.value != null) {
                                remaining = binding.value;
                                continue;
                            }
                            throw new Go(tag);
                        }
                        eval(current, ext, thread);
                    }
                    catch (Go go) {
                        LispObject tag = go.getTag();
                        Binding binding = ext.getTagBinding(tag);
                        if (binding != null && binding.value != null) {
                            remaining = binding.value;
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
    private static final SpecialOperator GO = new SpecialOperator("go")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() != 1)
                signal(new WrongNumberOfArgumentsException(this));
            Binding binding = env.getTagBinding(args.car());
            if (binding == null)
                return signal(new ControlError("no tag named " + args.car() +
                                               " is currently visible"));
            throw new Go(args.car());
        }
    };

    // ### block
    private static final SpecialOperator BLOCK = new SpecialOperator("block")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args == NIL)
                signal(new WrongNumberOfArgumentsException(this));
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
    private static final SpecialOperator RETURN_FROM = new SpecialOperator("return-from")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            final int length = args.length();
            if (length < 1 || length > 2)
                signal(new WrongNumberOfArgumentsException(this));
            Symbol symbol = checkSymbol(args.car());
            Block block = env.lookupBlock(symbol);
            if (block == null) {
                StringBuffer sb = new StringBuffer("no block named ");
                sb.append(symbol.getName());
                sb.append(" is currently visible");
                signal(new LispError(sb.toString()));
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
    private static final SpecialOperator RETURN = new SpecialOperator("return")
    {
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
                    signal(new WrongNumberOfArgumentsException(this));
                    return NIL;
            }
        }
    };

    // ### catch
    private static final SpecialOperator CATCH = new SpecialOperator("catch")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() < 1)
                signal(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject tag = eval(args.car(), env, thread);
            thread.pushCatchTag(tag);
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
            finally {
                thread.popCatchTag();
            }
        }
    };

    // ### throw
    private static final SpecialOperator THROW = new SpecialOperator("throw")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() < 2)
                signal(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject tag = eval(args.car(), env, thread);
            LispObject result = eval(args.cadr(), env, thread);
            if (thread.isValidCatchTag(tag))
                throw new Throw(tag, result);
            else
                return signal(new ControlError("attempt to throw to the non-existent tag " + tag));
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
            if (values != null)
                thread.setValues(values);
            else
                thread.clearValues();
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
            LispObject body = args.cdr();
            final LispThread thread = LispThread.currentThread();
            LispObject value = eval(valuesForm, env, thread);
            LispObject[] values = thread.getValues();
            if (values == null) {
                // eval() did not return multiple values.
                values = new LispObject[1];
                values[0] = value;
            }
            // Process declarations.
            LispObject specials = NIL;
            while (body != NIL) {
                LispObject obj = body.car();
                if (obj instanceof Cons && obj.car() == Symbol.DECLARE) {
                    LispObject decls = obj.cdr();
                    while (decls != NIL) {
                        LispObject decl = decls.car();
                        if (decl instanceof Cons && decl.car() == Symbol.SPECIAL) {
                            LispObject declvars = decl.cdr();
                            while (declvars != NIL) {
                                specials = new Cons(declvars.car(), specials);
                                declvars = declvars.cdr();
                            }
                        }
                        decls = decls.cdr();
                    }
                    body = body.cdr();
                } else
                    break;
            }
            final Environment oldDynEnv = thread.getDynamicEnvironment();
            final Environment ext = new Environment(env);
            int i = 0;
            LispObject var = vars.car();
            while (var != NIL) {
                Symbol sym = checkSymbol(var);
                LispObject val = i < values.length ? values[i] : NIL;
                if (specials != NIL && memq(sym, specials)) {
                    thread.bindSpecial(sym, val);
                    ext.declareSpecial(sym);
                } else if (sym.isSpecialVariable()) {
                    thread.bindSpecial(sym, val);
                } else
                    ext.bind(sym, val);
                vars = vars.cdr();
                var = vars.car();
                ++i;
            }
            LispObject result = NIL;
            try {
                while (body != NIL) {
                    result = eval(body.car(), ext, thread);
                    body = body.cdr();
                }
            }
            finally {
                thread.setDynamicEnvironment(oldDynEnv);
            }
            return result;
        }
    };

    // ### multiple-value-prog1
    private static final SpecialOperator MULTIPLE_VALUE_PROG1 =
        new SpecialOperator("multiple-value-prog1")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() == 0)
                signal(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject result = eval(args.car(), env, thread);
            LispObject[] values = thread.getValues();
            while ((args = args.cdr()) != NIL)
                eval(args.car(), env, thread);
            if (values != null)
                thread.setValues(values);
            else
                thread.clearValues();
            return result;
        }
    };

    // ### multiple-value-call
    private static final SpecialOperator MULTIPLE_VALUE_CALL =
        new SpecialOperator("multiple-value-call")
    {
        public LispObject execute(LispObject args, Environment env)
            throws ConditionThrowable
        {
            if (args.length() == 0)
                signal(new WrongNumberOfArgumentsException(this));
            final LispThread thread = LispThread.currentThread();
            LispObject function;
            LispObject obj = eval(args.car(), env, thread);
            args = args.cdr();
            if (obj instanceof Symbol) {
                function = obj.getSymbolFunction();
                if (function == null)
                    signal(new UndefinedFunction(obj));
            } else if (obj instanceof Function) {
                function = obj;
            } else {
                signal(new LispError(String.valueOf(obj) + " is not a function name"));
                return NIL;
            }
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

    // ### write-char
    // write-char character &optional output-stream => character
    private static final Primitive WRITE_CHAR =
        new Primitive("write-char", "character &optional output-stream")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 1 || args.length > 2)
                signal(new WrongNumberOfArgumentsException(this));
            final char c = LispCharacter.getValue(args[0]);
            final Stream out;
            if (args.length == 1)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else
                out = outSynonymOf(args[1]);
            out._writeChar(c);
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
                signal(new WrongNumberOfArgumentsException(this));
            String s = args[0].getStringValue();
            Stream out = outSynonymOf(args[1]);
            int start = Fixnum.getValue(args[2]);
            int end = Fixnum.getValue(args[3]);
            out._writeString(s.substring(start, end));
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
            outSynonymOf(arg)._writeString(System.getProperty("line.separator"));
            return NIL;
        }
    };

    // ### finish-output
    // finish-output &optional output-stream => nil
    private static final Primitive FINISH_OUTPUT =
        new Primitive("finish-output", "&optional output-stream")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                signal(new WrongNumberOfArgumentsException(this));
            return finishOutput(args);
        }
    };

    // ### force-output
    // force-output &optional output-stream => nil
    private static final Primitive FORCE_OUTPUT =
        new Primitive("force-output", "&optional output-stream")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                signal(new WrongNumberOfArgumentsException(this));
            return finishOutput(args);
        }
    };

    private static final LispObject finishOutput(LispObject[] args)
        throws ConditionThrowable
    {
        Stream out = null;
        if (args.length == 0)
            out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
        else {
            LispObject arg = args[0];
            if (arg == T)
                out = checkCharacterOutputStream(_TERMINAL_IO_.symbolValue());
            else if (arg == NIL)
                out = checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
            else if (arg instanceof Stream) {
                Stream stream = (Stream) arg;
                if (stream instanceof TwoWayStream)
                    out = ((TwoWayStream)arg).getOutputStream();
                else if (stream.isOutputStream())
                    out = stream;
            }
            if (out == null)
                signal(new TypeError(arg, "output stream"));
        }
        return out.finishOutput();
    }

    // ### clear-input
    // clear-input &optional input-stream => nil
    private static final Primitive CLEAR_INPUT =
        new Primitive("clear-input", "&optional input-stream")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length > 1)
                signal(new WrongNumberOfArgumentsException(this));
            final Stream in;
            if (args.length == 0)
                in = checkCharacterInputStream(_STANDARD_INPUT_.symbolValue());
            else
                in = inSynonymOf(args[0]);
            in.clearInput();
            return NIL;
        }
    };

    // ### clear-output
    // clear-output &optional output-stream => nil
    // "If any of these operations does not make sense for output-stream, then
    // it does nothing."
    private static final Primitive CLEAR_OUTPUT =
        new Primitive("clear-output", "&optional output-stream")
    {
        public LispObject execute() throws ConditionThrowable
        {
            // Default is standard output.
            return NIL;
        }
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg == T)
                return NIL; // *TERMINAL-IO*
            if (arg == NIL)
                return NIL; // *STANDARD-OUTPUT*
            if (arg instanceof Stream) {
                Stream stream = (Stream) arg;
                if (stream instanceof TwoWayStream) {
                    Stream out = ((TwoWayStream)stream).getOutputStream();
                    if (out.isOutputStream())
                        return NIL;
                }
                if (stream.isOutputStream())
                    return NIL;
            }
            return signal(new TypeError(arg, "output stream"));
        }
    };

    // ### close
    // close stream &key abort => result
    private static final Primitive CLOSE =
        new Primitive("close", "stream &key abort")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length == 0)
                signal(new WrongNumberOfArgumentsException(this));
            LispObject abort = NIL; // Default.
            Stream stream = checkStream(args[0]);
            if (length > 1) {
                if ((length - 1) % 2 != 0)
                    signal(new ProgramError("odd number of keyword arguments"));
                if (length > 3)
                    signal(new WrongNumberOfArgumentsException(this));
                if (args[1] == Keyword.ABORT)
                    abort = args[2];
                else
                    signal(new ProgramError("Unrecognized keyword argument " +
                                            args[1] + "."));
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
                signal(new WrongNumberOfArgumentsException(this));
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
                signal(new WrongNumberOfArgumentsException(this));
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

    // ### write-8-bits
    // write-8-bits byte stream => byte
    private static final Primitive2 WRITE_8_BITS =
        new Primitive2("write-8-bits", PACKAGE_SYS, false, "byte stream")
    {
        public LispObject execute (LispObject first, LispObject second)
            throws ConditionThrowable
        {
            int n = Fixnum.getValue(first);
            if (n < 0 || n > 255)
                signal(new TypeError(first,
                                     list2(Symbol.UNSIGNED_BYTE, new Fixnum(8))));
            final Stream out = checkBinaryOutputStream(second);
            out._writeByte(n);
            return first;
        }
    };

    // ### read-8-bits
    // read-8-bits stream &optional eof-error-p eof-value => byte
    private static final Primitive READ_8_BITS =
        new Primitive("read-8-bits", PACKAGE_SYS, false,
                      "stream &optional eof-error-p eof-value")
    {
        public LispObject execute (LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length < 1 || length > 3)
                signal(new WrongNumberOfArgumentsException(this));
            final Stream in = checkBinaryInputStream(args[0]);
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            return in.readByte(eofError, eofValue);
        }
    };

    // ### read-line
    // read-line &optional input-stream eof-error-p eof-value recursive-p
    // => line, missing-newline-p
    private static final Primitive READ_LINE =
        new Primitive("read-line",
                      "&optional input-stream eof-error-p eof-value recursive-p")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length > 4)
                signal(new WrongNumberOfArgumentsException(this));
            Stream stream = null;
            if (length == 0)
                stream = getStandardInput();
            else if (args[0] instanceof TwoWayStream)
                stream = ((TwoWayStream)args[0]).getInputStream();
            else if (args[0] instanceof Stream)
                stream = (Stream) args[0];
            if (stream == null)
                signal(new TypeError(args[0], "character input stream"));
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
        new Primitive("%read-from-string", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length < 6)
                signal(new WrongNumberOfArgumentsException(this));
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
            return LispThread.currentThread().setValues(result,
                                                        new Fixnum(in.getOffset()));
        }
    };

    private static final Primitive1 STANDARD_CHAR_P =
        new Primitive1("standard-char-p","character")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return checkCharacter(arg).isStandardChar();
        }
    };

    private static final Primitive1 GRAPHIC_CHAR_P =
        new Primitive1("graphic-char-p","char")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            char c = LispCharacter.getValue(arg);
            return (c >= ' ' && c < 127) ? T : NIL;
        }
    };

    private static final Primitive1 ALPHA_CHAR_P =
        new Primitive1("alpha-char-p","character")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            char c = LispCharacter.getValue(arg);
            return Character.isLetter(c) ? T : NIL;
        }
    };

    private static final Primitive1 NAME_CHAR = new Primitive1("name-char","name") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            String s = LispString.getValue(string(arg));
            int n = nameToChar(s);
            return n >= 0 ? LispCharacter.getInstance((char)n) : NIL;
        }
    };

    private static final Primitive1 CHAR_NAME = new Primitive1("char-name","character")
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

    private static final Primitive DIGIT_CHAR = new Primitive("digit-char","weight &optional radix")
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
                    signal(new WrongNumberOfArgumentsException(this));
                    return NIL;
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

    // ### read
    // read &optional input-stream eof-error-p eof-value recursive-p => object
    private static final Primitive READ = new Primitive("read","&optional input-stream eof-error-p eof-value recursive-p") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length > 4)
                signal(new WrongNumberOfArgumentsException(this));
            Stream stream =
                length > 0 ? checkCharacterInputStream(args[0]) : getStandardInput();
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            boolean recursive = length > 3 ? (args[3] != NIL) : false;
            return stream.read(eofError, eofValue, recursive);
        }
    };

    // ### read-char
    // read-char &optional input-stream eof-error-p eof-value recursive-p => char
    private static final Primitive READ_CHAR =
        new Primitive("read-char",
                      "&optional input-stream eof-error-p eof-value recursive-p")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length > 4)
                signal(new WrongNumberOfArgumentsException(this));
            Stream stream =
                length > 0 ? checkCharacterInputStream(args[0]) : getStandardInput();
            boolean eofError = length > 1 ? (args[1] != NIL) : true;
            LispObject eofValue = length > 2 ? args[2] : NIL;
            boolean recursive = length > 3 ? (args[3] != NIL) : false;
            return stream.readChar(eofError, eofValue);
        }
    };

    // ### unread-char
    // unread-char character &optional input-stream => nil
    private static final Primitive UNREAD_CHAR =
        new Primitive("unread-char", "character &optional input-stream")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return getStandardInput().unreadChar(checkCharacter(arg));
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Stream stream = checkCharacterInputStream(second);
            return stream.unreadChar(checkCharacter(first));
        }
    };

    private static final Primitive2 _SET_LAMBDA_NAME =
        new Primitive2("%set-lambda-name", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first instanceof Function) {
                Function f = (Function) first;
                f.setLambdaName(second);
                return second;
            } else {
                signal(new TypeError(first, "function"));
                return NIL;
            }
        }
    };

    // Destructively alters the vector, changing its length to NEW-SIZE, which
    // must be less than or equal to its current length.
    // shrink-vector vector new-size => vector
    private static final Primitive2 SHRINK_VECTOR =
        new Primitive2("shrink-vector", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkVector(first).shrink(Fixnum.getInt(second));
            return first;
        }
    };

    // ### subseq
    // subseq sequence start &optional end
    private static final Primitive SUBSEQ =
        new Primitive("subseq", "sequence start &optional end")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            final int start = Fixnum.getValue(second);
            if (start < 0) {
                StringBuffer sb = new StringBuffer("Bad start index (");
                sb.append(start);
                sb.append(") for SUBSEQ.");
                signal(new TypeError(sb.toString()));
            }
            if (first.listp())
                return list_subseq(first, start, -1);
            if (first.vectorp()) {
                AbstractVector v = (AbstractVector) first;
                return v.subseq(start, v.length());
            }
            return signal(new TypeError(first, Symbol.SEQUENCE));
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            final int start = Fixnum.getValue(second);
            if (start < 0) {
                StringBuffer sb = new StringBuffer("Bad start index (");
                sb.append(start);
                sb.append(").");
                signal(new TypeError(sb.toString()));
            }
            int end;
            if (third != NIL) {
                end = Fixnum.getValue(third);
                if (start > end) {
                    StringBuffer sb = new StringBuffer("Start index (");
                    sb.append(start);
                    sb.append(") is greater than end index (");
                    sb.append(end);
                    sb.append(") for SUBSEQ.");
                    signal(new TypeError(sb.toString()));
                }
            } else
                end = -1;
            if (first.listp())
                return list_subseq(first, start, end);
            if (first.vectorp()) {
                AbstractVector v = (AbstractVector) first;
                if (end < 0)
                    end = v.length();
                return v.subseq(start, end);
            }
            return signal(new TypeError(first, Symbol.SEQUENCE));
        }
    };

    private static final LispObject list_subseq(LispObject list, int start,
                                                int end)
        throws ConditionThrowable
    {
        int index = 0;
        LispObject result = NIL;
        while (list != NIL) {
            if (end >= 0 && index == end)
                return result.nreverse();
            if (index++ >= start)
                result = new Cons(list.car(), result);
            list = list.cdr();
        }
        return result.nreverse();
    }

    // ### truncate
    private static final Primitive TRUNCATE = new Primitive("truncate","number &optional divisor") {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            final int length = args.length;
            if (length < 1 || length > 2) {
                signal(new WrongNumberOfArgumentsException(this));
                return NIL;
            }
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
            signal(new TypeError(n, "number"));
            return NIL;
        }
    };

    // ### expt
    // expt base-number power-number => result
    public static final Primitive2 EXPT = new Primitive2("expt", "base-number power-number")
    {
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
            if (power instanceof Ratio) {
                if (n instanceof Fixnum) {
                    double d = Math.pow(((Fixnum)n).getValue(),
                                        ((Ratio)power).floatValue());
                    return new LispFloat(d);
                }
            }
            signal(new LispError("EXPT: unsupported case"));
            return NIL;
        }
    };

    // ### list
    private static final Primitive LIST = new Primitive("list", "&rest objects")
    {
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
    private static final Primitive LIST_ = new Primitive("list*", "&rest objects")
    {
        public LispObject execute() throws ConditionThrowable
        {
            signal(new WrongNumberOfArgumentsException("LIST*"));
            return NIL;
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
    public static final Primitive1 NREVERSE = new Primitive1("nreverse", "sequence")
    {
        public LispObject execute (LispObject arg) throws ConditionThrowable
        {
            return arg.nreverse();
        }
    };

    // ### nreconc
    // Adapted from CLISP.
    private static final Primitive2 NRECONC = new Primitive2("nreconc", "list tail")
    {
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
                } else if (list3 == NIL) {
                    list.setCdr(obj);
                } else
                    signal(new TypeError(list3, Symbol.LIST));
                return list;
            } else
                return obj;
        }
    };

    // ### reverse
    private static final Primitive1 REVERSE = new Primitive1("reverse", "sequence")
    {
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
            signal(new TypeError(arg, "proper sequence"));
            return NIL;
        }
    };

    // ### %set-elt
    // %setelt sequence index newval => newval
    private static final Primitive3 _SET_ELT =
        new Primitive3("%set-elt", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
            LispObject third) throws ConditionThrowable
        {
            if (first instanceof AbstractVector) {
                ((AbstractVector)first).set(Fixnum.getValue(second), third);
                return third;
            }
            if (first instanceof Cons) {
                int index = Fixnum.getValue(second);
                if (index < 0)
                    signal(new TypeError());
                LispObject list = first;
                int i = 0;
                while (true) {
                    if (i == index) {
                        list.setCar(third);
                        return third;
                    }
                    list = list.cdr();
                    if (list == NIL)
                        signal(new TypeError());
                    ++i;
                }
            }
            signal(new TypeError(first, "sequence"));
            return NIL;
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
    private static final Primitive2 MAPTREE =
        new Primitive2("maptree", PACKAGE_SYS, false)
    {
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
                signal(new TypeError(String.valueOf(size) + " is not a valid list length."));
            LispObject result = NIL;
            for (int i = size; i-- > 0;)
                result = new Cons(second, result);
            return result;
        }
    };

    // ### %member
    // %member item list key test test-not => tail
    private static final Primitive _MEMBER =
        new Primitive("%member", PACKAGE_SYS, false) {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 5)
                signal(new WrongNumberOfArgumentsException(this));
            LispObject item = args[0];
            LispObject tail = checkList(args[1]);
            LispObject key = args[2];
            if (key != NIL) {
                if (key instanceof Symbol)
                    key = key.getSymbolFunction();
                if (!(key instanceof Function || key instanceof GenericFunction))
                    signal(new UndefinedFunction(args[2]));
            }
            LispObject test = args[3];
            LispObject testNot = args[4];
            if (test != NIL && testNot != NIL)
                signal(new LispError("MEMBER: test and test-not both supplied"));
            if (test == NIL && testNot == NIL) {
                test = EQL;
            } else if (test != NIL) {
                if (test instanceof Symbol)
                    test = test.getSymbolFunction();
                if (!(test instanceof Function || test instanceof GenericFunction))
                    signal(new UndefinedFunction(args[3]));
            } else if (testNot != NIL) {
                if (testNot instanceof Symbol)
                    testNot = testNot.getSymbolFunction();
                if (!(testNot instanceof Function || testNot instanceof GenericFunction))
                    signal(new UndefinedFunction(args[3]));
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
        new Primitive1("coerce-to-function", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToFunction(arg);
        }
    };

    // ### make-closure lambda-form environment => closure
    private static final Primitive2 MAKE_CLOSURE =
        new Primitive2("make-closure", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first instanceof Cons && first.car() == Symbol.LAMBDA) {
                final Environment env;
                if (second == NIL)
                    env = new Environment();
                else
                    env = checkEnvironment(second);
                return new Closure(first.cadr(), first.cddr(), env);
            }
            return signal(new TypeError("Argument to MAKE-CLOSURE is not a lambda form."));
        }
    };

    // ### streamp
    private static final Primitive1 STREAMP = new Primitive1("streamp", "object")
    {
        public LispObject execute(LispObject arg)
        {
            return arg instanceof Stream ? T : NIL;
        }
    };

    // ### integerp
    private static final Primitive1 INTEGERP = new Primitive1("integerp", "object")
    {
        public LispObject execute(LispObject arg)
        {
            return arg.INTEGERP();
        }
    };

    // ### evenp
    private static final Primitive1 EVENP = new Primitive1("evenp", "integer")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.EVENP();
        }
    };

    // ### oddp
    private static final Primitive1 ODDP = new Primitive1("oddp", "integer")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.ODDP();
        }
    };

    // ### numberp
    private static final Primitive1 NUMBERP = new Primitive1("numberp", "object")
    {
        public LispObject execute(LispObject arg)
        {
            return arg.NUMBERP();
        }
    };

    // ### realp
    private static final Primitive1 REALP = new Primitive1("realp", "object")
    {
        public LispObject execute(LispObject arg)
        {
            return arg.REALP();
        }
    };

    // ### rationalp
    private static final Primitive1 RATIONALP = new Primitive1("rationalp","object") {
        public LispObject execute(LispObject arg)
        {
            return arg.RATIONALP();
        }
    };

    // ### complex
    private static final Primitive2 COMPLEX = new Primitive2("complex","realpart &optional imagpart") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof LispFloat)
                return Complex.getInstance(arg, LispFloat.ZERO);
            if (arg.realp())
                return arg;
            signal(new TypeError(arg, "real number"));
            return NIL;
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return Complex.getInstance(first, second);
        }
    };

    // ### complexp
    private static final Primitive1 COMPLEXP = new Primitive1("complexp","object") {
        public LispObject execute(LispObject arg)
        {
            return arg.COMPLEXP();
        }
    };

    // ### numerator
    private static final Primitive1 NUMERATOR = new Primitive1("numerator","rational") {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.NUMERATOR();
        }
    };

    // ### denominator
    private static final Primitive1 DENOMINATOR = new Primitive1("denominator","rational")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg.DENOMINATOR();
        }
    };

    // ### realpart
    private static final Primitive1 REALPART = new Primitive1("realpart","number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Complex)
                return ((Complex)arg).getRealPart();
            if (arg.numberp())
                return arg;
            signal(new TypeError(arg, "number"));
            return NIL;
        }
    };

    // ### imagpart
    private static final Primitive1 IMAGPART = new Primitive1("imagpart", "number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Complex)
                return ((Complex)arg).getImaginaryPart();
            return arg.multiplyBy(Fixnum.ZERO);
        }
    };

    // ### integer-length
    private static final Primitive1 INTEGER_LENGTH =
        new Primitive1("integer-length", "integer")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            BigInteger value;
            if (arg instanceof Fixnum)
                value = BigInteger.valueOf(((Fixnum)arg).getValue());
            else if (arg instanceof Bignum)
                value = ((Bignum)arg).getValue();
            else {
                signal(new TypeError(arg, "integer"));
                return NIL;
            }
            return new Fixnum(value.bitLength());
        }
    };

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
            else {
                signal(new TypeError(first, "integer"));
                return NIL;
            }
            if (second instanceof Fixnum)
                n2 = BigInteger.valueOf(((Fixnum)second).getValue());
            else if (second instanceof Bignum)
                n2 = ((Bignum)second).getValue();
            else {
                signal(new TypeError(second, "integer"));
                return NIL;
            }
            return number(n1.gcd(n2));
        }
    };

    // ### hashcode-to-string
    private static final Primitive1 HASHCODE_TO_STRING =
        new Primitive1("hashcode-to-string", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new LispString(Integer.toHexString(System.identityHashCode(arg)));
        }
    };

    // ### mkdir
    private static final Primitive1 MKDIR =
        new Primitive1("mkdir", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof LispString)
                return new File(((LispString)arg).getValue()).mkdir() ? T : NIL;
            else
                return signal(new TypeError(arg, Symbol.STRING));
        }
    };

    static {
        new Primitives();
    }
}
