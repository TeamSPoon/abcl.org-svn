/*
 * Lisp.java
 *
 * Copyright (C) 2002-2003 Peter Graves
 * $Id: Lisp.java,v 1.165 2003-10-16 14:33:49 piso Exp $
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
import java.util.Random;

public abstract class Lisp
{
    // Packages.
    public static final Package PACKAGE_CL =
        Packages.createPackage("COMMON-LISP", 1024);
    public static final Package PACKAGE_CL_USER =
        Packages.createPackage("COMMON-LISP-USER", 1024);
    public static final Package PACKAGE_SYS =
        Packages.createPackage("SYSTEM");
    public static final Package PACKAGE_TPL =
        Packages.createPackage("TOP-LEVEL");
    public static final Package PACKAGE_EXT =
        Packages.createPackage("EXTENSIONS");
    public static final Package PACKAGE_JAVA =
        Packages.createPackage("JAVA");
    static {
        try {
            PACKAGE_CL.addNickname("CL");
            PACKAGE_CL_USER.addNickname("CL-USER");
            PACKAGE_CL_USER.usePackage(PACKAGE_CL);
            PACKAGE_CL_USER.usePackage(PACKAGE_EXT);
            PACKAGE_CL_USER.usePackage(PACKAGE_JAVA);
            PACKAGE_SYS.addNickname("SYS");
            PACKAGE_SYS.usePackage(PACKAGE_CL);
            PACKAGE_SYS.usePackage(PACKAGE_EXT);
            PACKAGE_TPL.addNickname("TPL");
            PACKAGE_TPL.usePackage(PACKAGE_CL);
            PACKAGE_TPL.usePackage(PACKAGE_EXT);
            PACKAGE_EXT.addNickname("EXT");
            PACKAGE_EXT.usePackage(PACKAGE_CL);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    public static final Package PACKAGE_KEYWORD =
        Packages.createPackage("KEYWORD", 1024);

    static {
        PACKAGE_CL.addInitialExports(Exports.COMMON_LISP_SYMBOL_NAMES);
    }

    // ### nil
    public static final LispObject NIL = new Nil(PACKAGE_CL);

    // End-of-file marker.
    public static final LispObject EOF = new LispObject();

    // Functional types.
    static final int FTYPE_SPECIAL_OPERATOR = 1;
    static final int FTYPE_MACRO            = 2;
    static final int FTYPE_AUTOLOAD         = 3;

    // argv must not be null!
    public static final LispObject funcall(LispObject fun, LispObject[] argv,
                                           LispThread thread)
        throws ConditionThrowable
    {
        if (fun instanceof Autoload) {
            Autoload autoload = (Autoload) fun;
            autoload.load();
            fun = autoload.getSymbol().getSymbolFunction();
        }
        if (debug)
            thread.pushStackFrame(fun, argv);
        thread.clearValues();
        LispObject result;
        if (profiling)
            fun.incrementCallCount();
        switch (argv.length) {
            case 0:
                result = fun.execute();
                break;
            case 1:
                result = fun.execute(argv[0]);
                break;
            case 2:
                result = fun.execute(argv[0], argv[1]);
                break;
            case 3:
                result = fun.execute(argv[0], argv[1], argv[2]);
                break;
            default:
                result = fun.execute(argv);
                break;
        }
        if (debug)
            thread.popStackFrame();
        return result;
    }

    public static final LispObject funcall0(LispObject fun, LispThread thread)
        throws ConditionThrowable
    {
        if (fun instanceof Autoload) {
            Autoload autoload = (Autoload) fun;
            autoload.load();
            fun = autoload.getSymbol().getSymbolFunction();
        }
        if (debug) {
            LispObject[] argv = new LispObject[0];
            thread.pushStackFrame(fun, argv);
        }
        thread.clearValues();
        LispObject result;
        if (profiling)
            fun.incrementCallCount();
        result = fun.execute();
        if (debug)
            thread.popStackFrame();
        return result;
    }

    public static final LispObject funcall1(LispObject fun, LispObject arg,
                                            LispThread thread)
        throws ConditionThrowable
    {
        if (fun instanceof Autoload) {
            Autoload autoload = (Autoload) fun;
            autoload.load();
            fun = autoload.getSymbol().getSymbolFunction();
        }
        if (debug) {
            LispObject[] argv = new LispObject[1];
            argv[0] = arg;
            thread.pushStackFrame(fun, argv);
        }
        thread.clearValues();
        LispObject result;
        if (profiling)
            fun.incrementCallCount();
        result = fun.execute(arg);
        if (debug)
            thread.popStackFrame();
        return result;
    }

    public static final LispObject funcall2(LispObject fun, LispObject first,
                                            LispObject second, LispThread thread)
        throws ConditionThrowable
    {
        if (fun instanceof Autoload) {
            Autoload autoload = (Autoload) fun;
            autoload.load();
            fun = autoload.getSymbol().getSymbolFunction();
        }
        if (debug) {
            LispObject[] argv = new LispObject[2];
            argv[0] = first;
            argv[1] = second;
            thread.pushStackFrame(fun, argv);
        }
        thread.clearValues();
        LispObject result;
        if (profiling)
            fun.incrementCallCount();
        result = fun.execute(first, second);
        if (debug)
            thread.popStackFrame();
        return result;
    }

    public static final LispObject funcall3(LispObject fun, LispObject first,
                                            LispObject second, LispObject third,
                                            LispThread thread)
        throws ConditionThrowable
    {
        if (fun instanceof Autoload) {
            Autoload autoload = (Autoload) fun;
            autoload.load();
            fun = autoload.getSymbol().getSymbolFunction();
        }
        if (debug) {
            LispObject[] argv = new LispObject[3];
            argv[0] = first;
            argv[1] = second;
            argv[2] = third;
            thread.pushStackFrame(fun, argv);
        }
        thread.clearValues();
        LispObject result;
        if (profiling)
            fun.incrementCallCount();
        result = fun.execute(first, second, third);
        if (debug)
            thread.popStackFrame();
        return result;
    }

    public static final LispObject macroexpand(LispObject form,
                                               final Environment env,
                                               final LispThread thread)
        throws ConditionThrowable
    {
        LispObject expanded = NIL;
        while (true) {
            form = macroexpand_1(form, env, thread);
            LispObject[] values = thread.getValues();
            if (values[1] == NIL) {
                values[1] = expanded;
                return form;
            }
            expanded = T;
        }
    }

    public static final LispObject macroexpand_1(final LispObject form,
                                                 final Environment env,
                                                 final LispThread thread)
        throws ConditionThrowable
    {
        LispObject[] results = new LispObject[2];
        if (form instanceof Cons) {
            LispObject car = form.car();
            if (car instanceof Symbol) {
                LispObject obj = env.lookupFunctional(car);
                if (obj instanceof Autoload) {
                    Autoload autoload = (Autoload) obj;
                    autoload.load();
                    obj = autoload.getSymbol().getSymbolFunction();
                }
                if (obj instanceof SpecialOperator)
                    obj = Primitives.get((Symbol)car,
                        Symbol.MACROEXPAND_MACRO, NIL);
                if (obj instanceof MacroObject) {
                    LispObject expander = ((MacroObject)obj).getExpander();
                    if (profiling)
                        expander.incrementCallCount();
                    results[0] = expander.execute(form, env);
                    results[1] = T;
                    thread.setValues(results);
                    return results[0];
                }
            }
        }
        // Not a macro.
        results[0] = form;
        results[1] = NIL;
        thread.setValues(results);
        return results[0];
    }

    private static boolean debug = false;

    private static final Primitive1 INTERACTIVE_EVAL =
        new Primitive1("interactive-eval", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject object) throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            final Environment environment = new Environment();
            Symbol.MINUS.setSymbolValue(object);
            LispObject result;
            try {
                result = eval(object, environment, thread);
            }
            catch (StackOverflowError e) {
                if (debug) {
                    Symbol savedBacktrace = intern("*SAVED-BACKTRACE*", PACKAGE_EXT);
                    savedBacktrace.setSymbolValue(thread.backtraceAsList(0));
                }
                throw new ConditionThrowable(new LispError("stack overflow"));
            }
            catch (ConditionThrowable t) {
                if (debug) {
                    Symbol savedBacktrace = intern("*SAVED-BACKTRACE*", PACKAGE_EXT);
                    savedBacktrace.setSymbolValue(thread.backtraceAsList(0));
                }
                throw t;
            }
            Debug.assertTrue(result != null);
            Symbol.STAR_STAR_STAR.setSymbolValue(Symbol.STAR_STAR.getSymbolValue());
            Symbol.STAR_STAR.setSymbolValue(Symbol.STAR.getSymbolValue());
            Symbol.STAR.setSymbolValue(result);
            Symbol.PLUS_PLUS_PLUS.setSymbolValue(Symbol.PLUS_PLUS.getSymbolValue());
            Symbol.PLUS_PLUS.setSymbolValue(Symbol.PLUS.getSymbolValue());
            Symbol.PLUS.setSymbolValue(Symbol.MINUS.getSymbolValue());
            LispObject[] values = thread.getValues();
            Symbol.SLASH_SLASH_SLASH.setSymbolValue(Symbol.SLASH_SLASH.getSymbolValue());
            Symbol.SLASH_SLASH.setSymbolValue(Symbol.SLASH.getSymbolValue());
            if (values != null) {
                LispObject slash = NIL;
                for (int i = values.length; i-- > 0;)
                    slash = new Cons(values[i], slash);
                Symbol.SLASH.setSymbolValue(slash);
            } else {
                Symbol.SLASH.setSymbolValue(new Cons(result));
            }
            return result;
        }
    };

    public static final LispObject eval(final LispObject obj,
                                        final Environment env,
                                        final LispThread thread)
        throws ConditionThrowable
    {
        thread.clearValues();
        if (thread.isDestroyed())
            throw new ThreadDestroyed();
        if (obj instanceof Symbol) {
            LispObject result = null;
            if (obj.isSpecialVariable()) {
                result = thread.lookupSpecial(obj);
            } else
                result = env.lookup(obj);
            if (result == null) {
                result = obj.getSymbolValue();
                if (result == null)
                    throw new ConditionThrowable(new UnboundVariable(obj));
            }
            return result;
        } else if (obj instanceof Cons) {
            LispObject first = obj.car();
            if (first instanceof Symbol) {
                LispObject fun = env.lookupFunctional(first);
                if (fun == null)
                    throw new ConditionThrowable(new UndefinedFunction(first));
                switch (fun.getFunctionalType()) {
                    case FTYPE_SPECIAL_OPERATOR: {
                        if (profiling)
                            fun.incrementCallCount();
                        // Don't eval args!
                        return fun.execute(obj.cdr(), env);
                    }
                    case FTYPE_MACRO:
                        return eval(macroexpand(obj, env, thread), env, thread);
                    case FTYPE_AUTOLOAD: {
                        Autoload autoload = (Autoload) fun;
                        autoload.load();
                        return eval(obj, env, thread);
                    }
                    default: {
                        if (debug)
                            return funcall(fun,
                                           evalList(obj.cdr(), env, thread),
                                           thread);
                        if (profiling)
                            fun.incrementCallCount();
                        LispObject args = obj.cdr();
                        if (args == NIL)
                            return fun.execute();
                        LispObject arg1 = args.car();
                        args = args.cdr();
                        if (args == NIL)
                            return fun.execute(thread.value(eval(arg1, env, thread)));
                        LispObject arg2 = args.car();
                        args = args.cdr();
                        if (args == NIL)
                            return fun.execute(eval(arg1, env, thread),
                                               thread.value(eval(arg2, env, thread)));
                        LispObject arg3 = args.car();
                        args = args.cdr();
                        if (args == NIL)
                            return fun.execute(eval(arg1, env, thread),
                                               eval(arg2, env, thread),
                                               thread.value(eval(arg3, env, thread)));
                        // More than 3 arguments.
                        final int length = args.length() + 3;
                        LispObject[] results = new LispObject[length];
                        results[0] = eval(arg1, env, thread);
                        results[1] = eval(arg2, env, thread);
                        results[2] = eval(arg3, env, thread);
                        for (int i = 3; i < length; i++) {
                            results[i] = eval(args.car(), env, thread);
                            args = args.cdr();
                        }
                        thread.clearValues();
                        return fun.execute(results);
                    }
                }
            } else {
                LispObject args = obj.cdr();
                if (!args.listp())
                    throw new ConditionThrowable(new TypeError(args, "list"));
                LispObject funcar = first.car();
                LispObject rest = first.cdr();
                Symbol symbol = checkSymbol(funcar);
                if (symbol == Symbol.LAMBDA) {
                    Closure closure = new Closure(rest.car(), rest.cdr(), env);
                    return closure.execute(evalList(args, env, thread));
                } else
                    throw new ConditionThrowable(new ProgramError("illegal function object: " + first));
            }
        } else
            return obj;
    }

    private static final LispObject[] evalList(LispObject exps,
                                               Environment env,
                                               LispThread thread)
        throws ConditionThrowable
    {
        final int length = exps.length();
        LispObject[] results = new LispObject[length];
        for (int i = 0; i < length; i++) {
            results[i] = eval(exps.car(), env, thread);
            exps = exps.cdr();
        }
        // Ignore multiple values!
        thread.clearValues();
        return results;
    }

    public static final LispObject progn(LispObject body, Environment env,
                                         LispThread thread)
        throws ConditionThrowable
    {
        LispObject result = NIL;
        while (body != NIL) {
            result = eval(body.car(), env, thread);
            body = body.cdr();
        }
        return result;
    }

    // Environment wrappers.
    public static final void bind(Symbol symbol, LispObject value,
                                  Environment env)
    {
        if (symbol.isSpecialVariable())
            LispThread.currentThread().bindSpecial(symbol, value);
        else
            env.bind(symbol, value);
    }

    public static final void rebind(Symbol symbol, LispObject value,
                                    Environment env)
    {
        if (symbol.isSpecialVariable()) {
            Environment dynEnv =
                LispThread.currentThread().getDynamicEnvironment();
            Debug.assertTrue(dynEnv != null);
            dynEnv.rebind(symbol, value);
        } else
            env.rebind(symbol, value);
    }

    public static final void bindSpecialVariable(Symbol symbol,
                                                 LispObject value)
    {
        Debug.assertTrue(symbol.isSpecialVariable());
        LispThread.currentThread().bindSpecial(symbol, value);
    }

    public static final LispObject setSpecialVariable(Symbol symbol,
                                                      LispObject value)
    {
        Environment dynEnv =
            LispThread.currentThread().getDynamicEnvironment();
        if (dynEnv != null) {
            Binding binding = dynEnv.getBinding(symbol);
            if (binding != null) {
                binding.value = value;
                return value;
            }
        }
        symbol.setSymbolValue(value);
        return value;
    }

    public static final Cons list1(LispObject obj1)
    {
        return new Cons(obj1);
    }

    public static final Cons list2(LispObject obj1, LispObject obj2)
    {
        return new Cons(obj1, new Cons(obj2));
    }

    public static final Cons list3(LispObject obj1, LispObject obj2,
                                   LispObject obj3)
    {
        return new Cons(obj1, new Cons(obj2, new Cons(obj3)));
    }

    public static final Cons list4(LispObject obj1, LispObject obj2,
                                   LispObject obj3, LispObject obj4)
    {
        return new Cons(obj1,
                        new Cons(obj2,
                                 new Cons(obj3,
                                          new Cons(obj4))));
    }

    public static final Cons list5(LispObject obj1, LispObject obj2,
                                   LispObject obj3, LispObject obj4,
                                   LispObject obj5)
    {
        return new Cons(obj1,
                        new Cons(obj2,
                                 new Cons(obj3,
                                          new Cons(obj4,
                                                   new Cons(obj5)))));
    }

    public static final Cons list6(LispObject obj1, LispObject obj2,
                                   LispObject obj3, LispObject obj4,
                                   LispObject obj5, LispObject obj6)
    {
        return new Cons(obj1,
                        new Cons(obj2,
                                 new Cons(obj3,
                                          new Cons(obj4,
                                                   new Cons(obj5,
                                                            new Cons(obj6))))));
    }

    public static final Cons list7(LispObject obj1, LispObject obj2,
                                   LispObject obj3, LispObject obj4,
                                   LispObject obj5, LispObject obj6,
                                   LispObject obj7)
    {
        return new Cons(obj1,
                        new Cons(obj2,
                                 new Cons(obj3,
                                          new Cons(obj4,
                                                   new Cons(obj5,
                                                            new Cons(obj6,
                                                                     new Cons(obj7)))))));
    }

    // Used by jvm compiler.
    public static final LispObject multipleValueList(LispObject result)
    {
        LispThread thread = LispThread.currentThread();
        LispObject[] values = thread.getValues();
        thread.clearValues();
        if (values == null)
            return new Cons(result);
        LispObject list = NIL;
        for (int i = values.length; i-- > 0;)
            list = new Cons(values[i], list);
        return list;
    }

    public static Symbol checkSymbol(LispObject obj) throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (Symbol) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "symbol"));
        }
    }

    public static final Cons checkCons(LispObject obj) throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (Cons) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "cons"));
        }
    }

    public static final LispObject checkList(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        if (obj.listp())
            return obj;
        throw new ConditionThrowable(new TypeError(obj, "list"));
    }

    public static final AbstractArray checkArray(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (AbstractArray) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "array"));
        }
    }

    public static final AbstractVector checkVector(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (AbstractVector) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "vector"));
        }
    }

    public static final LispString checkString(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (LispString) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "string"));
        }
    }

    public static final LispString string(LispObject arg) throws ConditionThrowable
    {
        if (arg instanceof LispString)
            return (LispString) arg;
        if (arg instanceof Symbol)
            return new LispString(arg.getName());
        if (arg instanceof LispCharacter)
            return new LispString(((LispCharacter)arg).getValue());
        throw new ConditionThrowable(new TypeError(String.valueOf(arg) +
                                                   " cannot be coerced to a string"));
    }

    public static final String javaString(LispObject arg) throws ConditionThrowable
    {
        if (arg instanceof LispString)
            return ((LispString)arg).getValue();
        if (arg instanceof Symbol)
            return arg.getName();
        if (arg instanceof LispCharacter)
            return String.valueOf(new char[] {((LispCharacter)arg).getValue()});
        throw new ConditionThrowable(new TypeError(String.valueOf(arg) +
                                                   " cannot be coerced to a string"));
    }

    public static final LispObject number(long n)
    {
        if (n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE)
            return new Fixnum((int)n);
        return new Bignum(n);
    }

    private static final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    public static final LispObject number(BigInteger numerator,
                                          BigInteger denominator)
        throws ConditionThrowable
    {
        if (denominator.signum() == 0)
            throw new ConditionThrowable(new DivisionByZero());
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        if (!gcd.equals(BigInteger.ONE)) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
        if (denominator.equals(BigInteger.ONE))
            return number(numerator);
        return new Ratio(numerator, denominator);
    }

    public static final LispObject number(BigInteger n)
    {
        if (n.compareTo(INT_MIN) >= 0 && n.compareTo(INT_MAX) <= 0)
            return new Fixnum(n.intValue());
        return new Bignum(n);
    }

    public static final LispObject values(LispObject first, LispObject second)
    {
        LispObject[] values = new LispObject[2];
        values[0] = first;
        values[1] = second;
        LispThread.currentThread().setValues(values);
        return first;
    }

    public static final LispObject values(LispObject[] args)
    {
        if (args.length == 1) {
            LispThread.currentThread().clearValues();
            return args[0];
        }
        LispThread.currentThread().setValues(args);
        return args.length > 0 ? args[0] : NIL;
    }

    public static final LispObject readObjectFromString(String s)
    {
        try {
            return new StringInputStream(s).read(true, NIL, false);
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static final int nameToChar(String s)
    {
        String lower = s.toLowerCase();
        if (lower.equals("space"))
            return ' ';
        if (lower.equals("tab"))
            return '\t';
        if (lower.equals("newline"))
            return '\n';
        if (lower.equals("linefeed"))
            return '\n';
        if (lower.equals("return"))
            return '\r';
        if (lower.equals("page"))
            return '\f';
        if (lower.equals("null"))
            return 0;
        if (lower.equals("backspace"))
            return '\b';
        // Unknown.
        return -1;
    }

    public static final LispObject getUpgradedArrayElementType(LispObject type)
    {
        if (type == Symbol.CHARACTER || type == Symbol.BASE_CHAR || type == Symbol.STANDARD_CHAR)
            return Symbol.CHARACTER;
        if (type == Symbol.BIT)
            return Symbol.BIT;
        if (type == NIL)
            return Symbol.CHARACTER;
        return T;
    }

    public static final LispCharacter checkCharacter(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (LispCharacter) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "character"));
        }
    }

    public static final Package checkPackage(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (Package) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "package"));
        }
    }

    public static final Function checkFunction(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (Function) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "function"));
        }
    }

    public static final LispStream checkStream(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (LispStream) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "stream"));
        }
    }

    public static final CharacterInputStream checkCharacterInputStream(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        if (obj instanceof CharacterInputStream)
            return (CharacterInputStream) obj;
        if (obj instanceof TwoWayStream) {
            LispInputStream in = ((TwoWayStream)obj).getInputStream();
            if (in instanceof CharacterInputStream)
                return (CharacterInputStream) in;
        }
        throw new ConditionThrowable(new TypeError(obj, "character input stream"));
    }

    public static final CharacterOutputStream checkCharacterOutputStream(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        if (obj instanceof CharacterOutputStream)
            return (CharacterOutputStream) obj;
        if (obj instanceof TwoWayStream) {
            LispOutputStream out = ((TwoWayStream)obj).getOutputStream();
            if (out instanceof CharacterOutputStream)
                return (CharacterOutputStream) out;
        }
        throw new ConditionThrowable(new TypeError(obj, "character output stream"));
    }

    public static final BinaryInputStream checkBinaryInputStream(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        if (obj instanceof BinaryInputStream)
            return (BinaryInputStream) obj;
        if (obj instanceof TwoWayStream) {
            LispInputStream in = ((TwoWayStream)obj).getInputStream();
            if (in instanceof BinaryInputStream)
                return (BinaryInputStream) in;
        }
        throw new ConditionThrowable(new TypeError(obj, "binary input stream"));
    }

    public static final BinaryOutputStream checkBinaryOutputStream(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        if (obj instanceof BinaryOutputStream)
            return (BinaryOutputStream) obj;
        if (obj instanceof TwoWayStream) {
            LispOutputStream out = ((TwoWayStream)obj).getOutputStream();
            if (out instanceof BinaryOutputStream)
                return (BinaryOutputStream) out;
        }
        throw new ConditionThrowable(new TypeError(obj, "binary output stream"));
    }

    public static final CharacterInputStream inSynonymOf(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == T)
            return checkCharacterInputStream(_TERMINAL_IO_.symbolValue());
        if (obj == NIL)
            return checkCharacterInputStream(_STANDARD_INPUT_.symbolValue());
        if (obj instanceof CharacterInputStream)
            return (CharacterInputStream) obj;
        if (obj instanceof TwoWayStream) {
            LispInputStream in = ((TwoWayStream)obj).getInputStream();
            if (in instanceof CharacterInputStream)
                return (CharacterInputStream) in;
        }
        throw new ConditionThrowable(new TypeError(obj, "character input stream"));
    }

    public static final CharacterOutputStream outSynonymOf(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == T)
            return checkCharacterOutputStream(_TERMINAL_IO_.symbolValue());
        if (obj == NIL)
            return checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValue());
        if (obj instanceof CharacterOutputStream)
            return (CharacterOutputStream) obj;
        if (obj instanceof TwoWayStream) {
            LispOutputStream out = ((TwoWayStream)obj).getOutputStream();
            if (out instanceof CharacterOutputStream)
                return (CharacterOutputStream) out;
        }
        throw new ConditionThrowable(new TypeError(obj, "character output stream"));
    }

    public static final Readtable checkReadtable(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (Readtable) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "readtable"));
        }
    }

    public static final Environment checkEnvironment(LispObject obj)
        throws ConditionThrowable
    {
        if (obj == null)
            throw new NullPointerException();
        try {
            return (Environment) obj;
        }
        catch (ClassCastException e) {
            throw new ConditionThrowable(new TypeError(obj, "environment"));
        }
    }

    public static final Function coerceToFunction(LispObject obj)
        throws ConditionThrowable
    {
        if (obj instanceof Function)
            return (Function) obj;
        if (obj instanceof Symbol) {
            LispObject fun = obj.getSymbolFunction();
            if (fun instanceof Function)
                return (Function) fun;
        } else if (obj instanceof Cons && obj.car() == Symbol.LAMBDA)
            return new Closure(obj.cadr(), obj.cddr(), new Environment());
        throw new ConditionThrowable(new UndefinedFunction(obj));
    }

    // Returns package or throws exception.
    public static final Package coerceToPackage(LispObject obj)
        throws ConditionThrowable
    {
        if (obj instanceof Package)
            return (Package) obj;
        Package pkg = Packages.findPackage(javaString(obj));
        if (pkg != null)
            return pkg;
        throw new ConditionThrowable(new PackageError(obj + " is not the name of a package"));
    }

    // Property lists.
    public static final LispObject get(Symbol symbol, LispObject indicator,
                                       LispObject defaultValue)
        throws ConditionThrowable
    {
        LispObject result = get(symbol, indicator);
        return result != null ? result : defaultValue;
    }

    // Returns null if there is no property with the specified indicator.
    public static final LispObject get(Symbol symbol, LispObject indicator)
        throws ConditionThrowable
    {
        LispObject list = checkList(symbol.getPropertyList());
        while (list != NIL) {
            LispObject obj = list.car();
            if (obj.eql(indicator))
                return list.cadr();
            list = list.cddr();
        }
        return null;
    }

    public static final LispObject put(Symbol symbol, LispObject indicator,
                                       LispObject value)
        throws ConditionThrowable
    {
        LispObject list = checkList(symbol.getPropertyList());
        while (list != NIL) {
            if (list.car().eql(indicator)) {
                // Found it!
                LispObject rest = list.cdr();
                rest.setCar(value);
                return value;
            }
            list = list.cddr();
        }
        // Not found.
        symbol.setPropertyList(new Cons(indicator,
                                        new Cons(value,
                                                 symbol.getPropertyList())));
        return value;
    }

    public static final LispObject remprop(Symbol symbol, LispObject indicator)
        throws ConditionThrowable
    {
        LispObject list = checkList(symbol.getPropertyList());
        LispObject prev = null;
        while (list != NIL) {
            if (!(list.cdr() instanceof Cons))
                throw new ConditionThrowable(new ProgramError(String.valueOf(symbol) +
                                                              " has an odd number of items in its property list"));
            if (list.car().eql(indicator)) {
                // Found it!
                if (prev != null) {
                    prev.setCdr(list.cddr());
                } else {
                    symbol.setPropertyList(list.cddr());
                }
                return T;
            }
            prev = list;
            list = list.cddr();
        }
        // Not found.
        return NIL;
    }

    public static final Symbol intern(String name, Package pkg)
    {
        return pkg.intern(name);
    }

    // Used by jvm compiler.
    public static final Symbol internInPackage(String name, String packageName)
        throws ConditionThrowable
    {
        Package pkg = Packages.findPackage(packageName);
        if (pkg == null)
            throw new ConditionThrowable(new LispError(packageName + " is not the name of a package"));
        return pkg.intern(name);
    }

    public static final Symbol export(String name, Package pkg)
    {
        Symbol symbol = pkg.intern(name);
        try {
            pkg.export(symbol); // FIXME Inefficient!
        }
        catch (ConditionThrowable t) {
            Debug.trace(t);
        }
        return symbol;
    }

    public static final Symbol internSpecial(String name, Package pkg,
        LispObject value)
    {
        Symbol symbol = pkg.intern(name);
        symbol.setSpecial(true);
        symbol.setSymbolValue(value);
        return symbol;
    }

    public static final Symbol exportSpecial(String name, Package pkg,
        LispObject value)
    {
        Symbol symbol = pkg.intern(name);
        try {
            pkg.export(symbol); // FIXME Inefficient!
        }
        catch (ConditionThrowable t) {
            Debug.trace(t);
        }
        symbol.setSpecial(true);
        symbol.setSymbolValue(value);
        return symbol;
    }

    public static final Symbol exportConstant(String name, Package pkg,
        LispObject value)
    {
        Symbol symbol = pkg.intern(name);
        try {
            pkg.export(symbol); // FIXME Inefficient!
        }
        catch (ConditionThrowable t) {
            Debug.trace(t);
        }
        symbol.setConstant(true);
        symbol.setSymbolValue(value);
        return symbol;
    }

    public static Symbol _DEFAULT_PATHNAME_DEFAULTS_ =
        exportSpecial("*DEFAULT-PATHNAME-DEFAULTS*", PACKAGE_CL,
            new LispString(System.getProperty("user.dir")));

    public static final Symbol _PACKAGE_ =
        exportSpecial("*PACKAGE*", PACKAGE_CL, PACKAGE_CL_USER);

    public static final Package getCurrentPackage()
    {
        return (Package) _PACKAGE_.symbolValueNoThrow();
    }

    private static CharacterInputStream stdin =
        new CharacterInputStream(System.in);

    private static CharacterOutputStream stdout =
        new CharacterOutputStream(System.out);

    public static final Symbol _STANDARD_INPUT_ =
        exportSpecial("*STANDARD-INPUT*", PACKAGE_CL, stdin);

    public static final Symbol _STANDARD_OUTPUT_ =
        exportSpecial("*STANDARD-OUTPUT*", PACKAGE_CL, stdout);

    public static final Symbol _ERROR_OUTPUT_ =
        exportSpecial("*ERROR-OUTPUT*", PACKAGE_CL, stdout);

    public static final Symbol _TRACE_OUTPUT_ =
        exportSpecial("*TRACE-OUTPUT*", PACKAGE_CL, stdout);

    public static final Symbol _TERMINAL_IO_ =
        exportSpecial("*TERMINAL-IO*", PACKAGE_CL,
                      new TwoWayStream(stdin, stdout));

    public static final Symbol _QUERY_IO_ =
        exportSpecial("*QUERY-IO*", PACKAGE_CL,
                      new TwoWayStream(stdin, stdout));

    public static final Symbol _DEBUG_IO_ =
        exportSpecial("*DEBUG-IO*", PACKAGE_CL,
                      new TwoWayStream(stdin, stdout));

    public void resetIO(CharacterInputStream in, CharacterOutputStream out)
    {
        stdin = in;
        stdout = out;
        _STANDARD_INPUT_.setSymbolValue(stdin);
        _STANDARD_OUTPUT_.setSymbolValue(stdout);
        _ERROR_OUTPUT_.setSymbolValue(stdout);
        _TRACE_OUTPUT_.setSymbolValue(stdout);
        _TERMINAL_IO_.setSymbolValue(new TwoWayStream(stdin, stdout));
        _QUERY_IO_.setSymbolValue(new TwoWayStream(stdin, stdout));
        _DEBUG_IO_.setSymbolValue(new TwoWayStream(stdin, stdout));
    }

    public static final TwoWayStream getTerminalIO()
    {
        return (TwoWayStream) _TERMINAL_IO_.symbolValueNoThrow();
    }

    public static final CharacterInputStream getStandardInput()
    {
        return (CharacterInputStream) _STANDARD_INPUT_.symbolValueNoThrow();
    }

    public static final CharacterOutputStream getStandardOutput() throws ConditionThrowable
    {
        return checkCharacterOutputStream(_STANDARD_OUTPUT_.symbolValueNoThrow());
    }

    public static final CharacterOutputStream getTraceOutput()
    {
        return (CharacterOutputStream) _TRACE_OUTPUT_.symbolValueNoThrow();
    }

    public static final Symbol _READTABLE_ =
        exportSpecial("*READTABLE*", PACKAGE_CL, new Readtable());

    public static final Readtable getCurrentReadtable()
    {
        return (Readtable) _READTABLE_.symbolValueNoThrow();
    }

    public static final Symbol _READ_SUPPRESS_ =
        exportSpecial("*READ-SUPPRESS*", PACKAGE_CL, NIL);

    public static final Symbol _DEBUGGER_HOOK_ =
        exportSpecial("*DEBUGGER-HOOK*", PACKAGE_CL, NIL);

    public static final Symbol MOST_POSITIVE_FIXNUM =
        exportConstant("MOST-POSITIVE-FIXNUM", PACKAGE_CL,
            new Fixnum(Integer.MAX_VALUE));

    public static final Symbol MOST_NEGATIVE_FIXNUM =
        exportConstant("MOST-NEGATIVE-FIXNUM", PACKAGE_CL,
            new Fixnum(Integer.MIN_VALUE));

    public static void exit()
    {
        Interpreter interpreter = Interpreter.getInstance();
        if (interpreter != null)
            interpreter.kill();
    }

    public static final Primitive0 DEBUG =
        new Primitive0("%debug", PACKAGE_SYS, false) {
        public LispObject execute() throws ConditionThrowable
        {
            debug = true;
            return LispThread.currentThread().nothing();
        }
    };

    public static final Primitive0 NODEBUG =
        new Primitive0("%nodebug", PACKAGE_SYS, false) {
        public LispObject execute() throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            if (debug) {
                debug = false;
                thread.resetStack();
            }
            return thread.nothing();
        }
    };

    private static boolean profiling;

    // ### start-profiler
    public static final Primitive0 START_PROFILER =
        new Primitive0("start-profiler", PACKAGE_EXT, true) {
        public LispObject execute() throws ConditionThrowable
        {
            CharacterOutputStream out = getStandardOutput();
            out.freshLine();
            if (!profiling) {
                Package[] packages = Packages.getAllPackages();
                for (int i = 0; i < packages.length; i++) {
                    Package pkg = packages[i];
                    Symbol[] symbols = pkg.symbols();
                    for (int j = 0; j < symbols.length; j++) {
                        Symbol symbol = symbols[j];
                        LispObject f = symbol.getSymbolFunction();
                        if (f != null)
                            f.setCallCount(0);
                    }
                }
                out.writeLine("; Profiling started.");
                out.flushOutput();
                profiling = true;
            } else {
                out.writeLine("; Profiling already enabled.");
                out.flushOutput();
            }
            return LispThread.currentThread().nothing();
        }
    };

    // ### stop-profiler
    public static final Primitive0 STOP_PROFILER =
        new Primitive0("stop-profiler", PACKAGE_EXT, true) {
        public LispObject execute() throws ConditionThrowable
        {
            CharacterOutputStream out = getStandardOutput();
            out.freshLine();
            if (profiling) {
                profiling = false;
                out.writeLine("; Profiling stopped.");
            } else
                out.writeLine("; Profiling not enabled.");
            out.flushOutput();
            return LispThread.currentThread().nothing();
        }
    };

    // ### t
    public static final Symbol T = PACKAGE_CL.addExternalSymbol("T");
    static {
        T.setSymbolValue(T);
        T.setConstant(true);
    }

    // ### *modules*
    public static final Symbol _MODULES_ =
        exportSpecial("*MODULES*", PACKAGE_CL, NIL);

    // ### *load-verbose*
    public static final Symbol _LOAD_VERBOSE_ =
        exportSpecial("*LOAD-VERBOSE*", PACKAGE_CL, NIL);

    // ### *load-print*
    public static final Symbol _LOAD_PRINT_ =
        exportSpecial("*LOAD-PRINT*", PACKAGE_CL, NIL);

    // ### *load-truename*
    public static final Symbol _LOAD_TRUENAME_ =
        exportSpecial("*LOAD-TRUENAME*", PACKAGE_CL, NIL);

    // ### *load-depth*
    // internal symbol
    public static final Symbol _LOAD_DEPTH_ =
        internSpecial("*LOAD-DEPTH*", PACKAGE_SYS, new Fixnum(0));

    // ### *autoload-verbose*
    // internal symbol
    public static final Symbol _AUTOLOAD_VERBOSE_ =
        internSpecial("*AUTOLOAD-VERBOSE*", PACKAGE_SYS, NIL);

    // Printer control variables.
    public static final Symbol _PRINT_ARRAY_ =
        exportSpecial("*PRINT-ARRAY*", PACKAGE_CL, T);

    public static final Symbol _PRINT_BASE_ =
        exportSpecial("*PRINT-BASE*", PACKAGE_CL, new Fixnum(10));

    public static final Symbol _PRINT_CASE_ =
        exportSpecial("*PRINT-CASE*", PACKAGE_CL, Keyword.UPCASE);

    public static final Symbol _PRINT_CIRCLE_ =
        exportSpecial("*PRINT-CIRCLE*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_ESCAPE_ =
        exportSpecial("*PRINT-ESCAPE*", PACKAGE_CL, T);

    public static final Symbol _PRINT_GENSYM_ =
        exportSpecial("*PRINT-GENSYM*", PACKAGE_CL, T);

    public static final Symbol _PRINT_LENGTH_ =
        exportSpecial("*PRINT-LENGTH*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_LEVEL_ =
        exportSpecial("*PRINT-LEVEL*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_LINES_ =
        exportSpecial("*PRINT-LINES*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_MISER_WIDTH_ =
        exportSpecial("*PRINT-MISER-WIDTH*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_PPRINT_DISPATCH_ =
        exportSpecial("*PRINT-PPRINT-DISPATCH*", PACKAGE_CL, NIL); // FIXME

    public static final Symbol _PRINT_PRETTY_ =
        exportSpecial("*PRINT-PRETTY*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_RADIX_ =
        exportSpecial("*PRINT-RADIX*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_READABLY_ =
        exportSpecial("*PRINT-READABLY*", PACKAGE_CL, NIL);

    public static final Symbol _PRINT_RIGHT_MARGIN_ =
        exportSpecial("*PRINT-RIGHT-MARGIN*", PACKAGE_CL, NIL);

    public static final Symbol _RANDOM_STATE_ =
        exportSpecial("*RANDOM-STATE*", PACKAGE_CL,
                      new JavaObject(new Random()));

    public static final Symbol STAR = exportSpecial("*", PACKAGE_CL, NIL);
    public static final Symbol STAR_STAR =
        exportSpecial("**", PACKAGE_CL, NIL);
    public static final Symbol STAR_STAR_STAR =
        exportSpecial("***", PACKAGE_CL, NIL);

    public static final Symbol MINUS = exportSpecial("-", PACKAGE_CL, NIL);

    public static final Symbol PLUS = exportSpecial("+", PACKAGE_CL, NIL);
    public static final Symbol PLUS_PLUS =
        exportSpecial("++", PACKAGE_CL, NIL);
    public static final Symbol PLUS_PLUS_PLUS =
        exportSpecial("+++", PACKAGE_CL, NIL);

    public static final Symbol SLASH = exportSpecial("/", PACKAGE_CL, NIL);
    public static final Symbol SLASH_SLASH =
        exportSpecial("//", PACKAGE_CL, NIL);
    public static final Symbol SLASH_SLASH_SLASH =
        exportSpecial("///", PACKAGE_CL, NIL);

    public static final Symbol PI =
        exportConstant("PI", PACKAGE_CL,
                       new LispFloat((double)3.141592653589793));

    public static final Symbol SHORT_FLOAT_EPSILON =
        exportConstant("SHORT-FLOAT-EPSILON", PACKAGE_CL,
                       new LispFloat((double)1.1102230246251568E-16));

    public static final Symbol SINGLE_FLOAT_EPSILON =
        exportConstant("SINGLE-FLOAT-EPSILON", PACKAGE_CL,
                       new LispFloat((double)1.1102230246251568E-16));

    public static final Symbol DOUBLE_FLOAT_EPSILON =
        exportConstant("DOUBLE-FLOAT-EPSILON", PACKAGE_CL,
                       new LispFloat((double)1.1102230246251568E-16));

    public static final Symbol LONG_FLOAT_EPSILON =
        exportConstant("LONG-FLOAT-EPSILON", PACKAGE_CL,
                       new LispFloat((double)1.1102230246251568E-16));

    public static final Symbol SHORT_FLOAT_NEGATIVE_EPSILON =
        exportConstant("SHORT-FLOAT-NEGATIVE-EPSILON", PACKAGE_CL,
                       new LispFloat((double)5.551115123125784E-17));

    public static final Symbol SINGLE_FLOAT_NEGATIVE_EPSILON =
        exportConstant("SINGLE-FLOAT-NEGATIVE-EPSILON", PACKAGE_CL,
                       new LispFloat((double)5.551115123125784E-17));

    public static final Symbol DOUBLE_FLOAT_NEGATIVE_EPSILON =
        exportConstant("DOUBLE-FLOAT-NEGATIVE-EPSILON", PACKAGE_CL,
                       new LispFloat((double)5.551115123125784E-17));

    public static final Symbol LONG_FLOAT_NEGATIVE_EPSILON =
        exportConstant("LONG-FLOAT-NEGATIVE-EPSILON", PACKAGE_CL,
                       new LispFloat((double)5.551115123125784E-17));

    public static final Symbol MOST_POSITIVE_SHORT_FLOAT =
        exportConstant("MOST-POSITIVE-SHORT-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MAX_VALUE));

    public static final Symbol MOST_POSITIVE_SINGLE_FLOAT =
        exportConstant("MOST-POSITIVE-SINGLE-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MAX_VALUE));

    public static final Symbol MOST_POSITIVE_DOUBLE_FLOAT =
        exportConstant("MOST-POSITIVE-DOUBLE-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MAX_VALUE));

    public static final Symbol MOST_POSITIVE_LONG_FLOAT =
        exportConstant("MOST-POSITIVE-LONG-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MAX_VALUE));

    public static final Symbol LEAST_POSITIVE_SHORT_FLOAT =
        exportConstant("LEAST-POSITIVE-SHORT-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_SINGLE_FLOAT =
        exportConstant("LEAST-POSITIVE-SINGLE-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_DOUBLE_FLOAT =
        exportConstant("LEAST-POSITIVE-DOUBLE-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_LONG_FLOAT =
        exportConstant("LEAST-POSITIVE-LONG-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_NORMALIZED_SHORT_FLOAT =
        exportConstant("LEAST-POSITIVE-NORMALIZED-SHORT-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_NORMALIZED_SINGLE_FLOAT =
        exportConstant("LEAST-POSITIVE-NORMALIZED-SINGLE-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_NORMALIZED_DOUBLE_FLOAT =
        exportConstant("LEAST-POSITIVE-NORMALIZED-DOUBLE-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol LEAST_POSITIVE_NORMALIZED_LONG_FLOAT =
        exportConstant("LEAST-POSITIVE-NORMALIZED-LONG-FLOAT", PACKAGE_CL,
                       new LispFloat(Double.MIN_VALUE));

    public static final Symbol MOST_NEGATIVE_SHORT_FLOAT =
        exportConstant("MOST-NEGATIVE-SHORT-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MAX_VALUE));

    public static final Symbol MOST_NEGATIVE_SINGLE_FLOAT =
        exportConstant("MOST-NEGATIVE-SINGLE-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MAX_VALUE));

    public static final Symbol MOST_NEGATIVE_DOUBLE_FLOAT =
        exportConstant("MOST-NEGATIVE-DOUBLE-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MAX_VALUE));

    public static final Symbol MOST_NEGATIVE_LONG_FLOAT =
        exportConstant("MOST-NEGATIVE-LONG-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MAX_VALUE));

    public static final Symbol LEAST_NEGATIVE_SHORT_FLOAT =
        exportConstant("LEAST-NEGATIVE-SHORT-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_SINGLE_FLOAT =
        exportConstant("LEAST-NEGATIVE-SINGLE-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_DOUBLE_FLOAT =
        exportConstant("LEAST-NEGATIVE-DOUBLE-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_LONG_FLOAT =
        exportConstant("LEAST-NEGATIVE-LONG-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_NORMALIZED_SHORT_FLOAT =
        exportConstant("LEAST-NEGATIVE-NORMALIZED-SHORT-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_NORMALIZED_SINGLE_FLOAT =
        exportConstant("LEAST-NEGATIVE-NORMALIZED-SINGLE-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_NORMALIZED_DOUBLE_FLOAT =
        exportConstant("LEAST-NEGATIVE-NORMALIZED-DOUBLE-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    public static final Symbol LEAST_NEGATIVE_NORMALIZED_LONG_FLOAT =
        exportConstant("LEAST-NEGATIVE-NORMALIZED-LONG-FLOAT", PACKAGE_CL,
                       new LispFloat(- Double.MIN_VALUE));

    private static final void loadClass(String className)
    {
        try {
            Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static {
        loadClass("org.armedbear.lisp.Primitives");
        loadClass("org.armedbear.lisp.SpecialOperators");
        loadClass("org.armedbear.lisp.Extensions");
        loadClass("org.armedbear.lisp.Java");
        loadClass("org.armedbear.lisp.CompiledFunction");
        loadClass("org.armedbear.lisp.Autoload");
        loadClass("org.armedbear.lisp.AutoloadMacro");
        loadClass("org.armedbear.lisp.cxr");
        loadClass("org.armedbear.lisp.Do");
        loadClass("org.armedbear.lisp.dolist");
        loadClass("org.armedbear.lisp.dotimes");
    }
}
