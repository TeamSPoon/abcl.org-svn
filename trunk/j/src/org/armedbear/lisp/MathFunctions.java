/*
 * Math.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: MathFunctions.java,v 1.2 2004-02-12 12:11:26 piso Exp $
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

public final class MathFunctions extends Lisp
{
    // ### sin
    private static final Primitive1 SIN = new Primitive1("sin", "radians")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return sin(arg);
        }
    };

    private static LispObject sin(LispObject arg) throws ConditionThrowable
    {
        if (arg.realp())
            return new LispFloat(Math.sin(LispFloat.coerceToFloat(arg).getValue()));
        if (arg instanceof Complex) {
            LispObject n = arg.multiplyBy(Complex.getInstance(Fixnum.ZERO,
                                                              Fixnum.ONE));
            LispObject result = exp(n);
            result = result.subtract(exp(n.multiplyBy(Fixnum.MINUS_ONE)));
            return result.divideBy(Fixnum.TWO.multiplyBy(Complex.getInstance(Fixnum.ZERO,
                                                                             Fixnum.ONE)));
        }
        return signal(new TypeError(arg, Symbol.NUMBER));
    }

    // ### cos
    private static final Primitive1 COS = new Primitive1("cos", "radians")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return cos(arg);
        }
    };

    private static LispObject cos(LispObject arg) throws ConditionThrowable
    {
        if (arg.realp())
            return new LispFloat(Math.cos(LispFloat.coerceToFloat(arg).getValue()));
        if (arg instanceof Complex) {
            LispObject n = arg.multiplyBy(Complex.getInstance(Fixnum.ZERO,
                                                              Fixnum.ONE));
            LispObject result = exp(n);
            result = result.add(exp(n.multiplyBy(Fixnum.MINUS_ONE)));
            return result.divideBy(Fixnum.TWO);
        }
        return signal(new TypeError(arg, "number"));
    }

    // ### tan
    private static final Primitive1 TAN = new Primitive1("tan", "radians")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return tan(arg);
        }
    };

    private static LispObject tan(LispObject arg) throws ConditionThrowable
    {
        return sin(arg).divideBy(cos(arg));
    }

    // ### atan
    private static final Primitive ATAN =
        new Primitive("atan", "number1 &optional number2")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return atan(arg);
        }

        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            return atan(first.divideBy(second));
        }
    };

    private static LispObject atan(LispObject arg) throws ConditionThrowable
    {
        if (arg instanceof Complex) {
            LispObject im = ((Complex)arg).getImaginaryPart();
            if (im.zerop())
                return Complex.getInstance(atan(((Complex)arg).getRealPart()),
                                           im);
            LispObject result = arg.multiplyBy(arg);
            result = result.add(Fixnum.ONE);
            result = Fixnum.ONE.divideBy(result);
            result = sqrt(result);
            LispObject n = Complex.getInstance(Fixnum.ZERO, Fixnum.ONE);
            n = n.multiplyBy(arg);
            n = n.add(Fixnum.ONE);
            result = n.multiplyBy(result);
            result = log(result);
            result = result.multiplyBy(Complex.getInstance(Fixnum.ZERO, Fixnum.MINUS_ONE));
            return result;
        }
        return new LispFloat(Math.atan(LispFloat.coerceToFloat(arg).getValue()));
    }

    // ### exp
    private static final Primitive1 EXP = new Primitive1("exp", "number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return exp(arg);
        }
    };

    private static LispObject exp(LispObject arg) throws ConditionThrowable
    {
        if (arg.realp()) {  // return real
            LispFloat argf = LispFloat.coerceToFloat(arg);
            return new LispFloat(Math.exp(argf.getValue()));
        } else if (arg instanceof Complex) {
            Complex argc = (Complex)arg;
            double re = LispFloat.coerceToFloat(argc.getRealPart()).getValue();
            double im = LispFloat.coerceToFloat(argc.getImaginaryPart()).getValue();
            LispFloat resX = new LispFloat(Math.exp(re) * Math.cos(im));
            LispFloat resY = new LispFloat(Math.exp(re) * Math.sin(im));
            return Complex.getInstance(resX, resY);
        }
        return signal(new TypeError(arg, "number"));
    }

    // ### sqrt
    private static final Primitive1 SQRT = new Primitive1("sqrt", "number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return sqrt(arg);
        }
    };

    private static final LispObject sqrt(LispObject obj) throws ConditionThrowable
    {
        if (obj.realp() && !obj.minusp()) {  // returning real
            LispFloat f = LispFloat.coerceToFloat(obj);
            return new LispFloat(Math.sqrt(f.getValue()));
        } else {  // returning Complex
            if (obj.realp()) {
                return Complex.getInstance(new LispFloat(0),
                                           sqrt(Fixnum.ZERO.subtract(obj)));
            } else if (obj instanceof Complex) {
                return exp(log(obj).divideBy(Fixnum.TWO));
            }
        }
        signal(new TypeError(obj, "number"));
        return NIL;
    }

    // ### log
    private static final Primitive LOG = new Primitive("log", "number &optional base")
    {
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

    private static final LispObject log(LispObject obj) throws ConditionThrowable
    {
        if (obj.realp() && !obj.minusp()) {  // real value
            if (obj instanceof Fixnum)
                return new LispFloat(Math.log(((Fixnum)obj).getValue()));
            if (obj instanceof Bignum)
                return new LispFloat(Math.log(((Bignum)obj).floatValue()));
            if (obj instanceof Ratio)
                return new LispFloat(Math.log(((Ratio)obj).floatValue()));
            if (obj instanceof LispFloat)
                return new LispFloat(Math.log(((LispFloat)obj).getValue()));
        } else { // returning Complex
            LispFloat re, im, phase, abs;
            if (obj.realp() && obj.minusp()) {
                re = LispFloat.coerceToFloat(obj);
                abs = new LispFloat(Math.abs(re.getValue()));
                phase = new LispFloat(Math.PI);
                return Complex.getInstance(new LispFloat(Math.log(abs.getValue())), phase);
            } else if (obj instanceof Complex) {
                re = LispFloat.coerceToFloat(((Complex)obj).getRealPart());
                im = LispFloat.coerceToFloat(((Complex)obj).getImaginaryPart());
                phase = new LispFloat(Math.atan2(im.getValue(), re.getValue()));  // atan(y/x)
                abs = (LispFloat)((Complex)obj).ABS();
                return Complex.getInstance(new LispFloat(Math.log(abs.getValue())), phase);
            }
        }
        signal(new TypeError(obj, "number"));
        return NIL;
    }
}
