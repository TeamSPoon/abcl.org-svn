/*
 * TypeError.java
 *
 * Copyright (C) 2002-2004 Peter Graves
 * $Id: TypeError.java,v 1.25 2005-02-28 02:50:05 piso Exp $
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

public class TypeError extends LispError
{
    protected LispObject datum;
    protected LispObject expectedType;
    private String typeString;

    public TypeError()
    {
        datum = NIL;
        expectedType = NIL;
    }

    public TypeError(LispObject datum, LispObject expectedType)
    {
        this.datum = datum;
        this.expectedType = expectedType;
    }

    public TypeError(LispObject initArgs) throws ConditionThrowable
    {
        super(initArgs);
        LispObject datum = NIL;
        LispObject expectedType = NIL;
        LispObject first, second;
        while (initArgs != NIL) {
            first = initArgs.car();
            initArgs = initArgs.cdr();
            second = initArgs.car();
            initArgs = initArgs.cdr();
            if (first == Keyword.DATUM)
                datum = second;
            else if (first == Keyword.EXPECTED_TYPE)
                expectedType = second;
        }
        this.datum = datum;
        this.expectedType = expectedType;
        this.typeString = expectedType.writeToString();
    }

    public TypeError(String message)
    {
        super(message);
        datum = NIL;
        expectedType = NIL;
    }

    public TypeError(LispObject datum, String typeString)
    {
        this.datum = datum;
        expectedType = NIL;
        this.typeString = typeString;
    }

    public LispObject typeOf()
    {
        return Symbol.TYPE_ERROR;
    }

    public LispObject classOf()
    {
        return BuiltInClass.TYPE_ERROR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.TYPE_ERROR)
            return T;
        if (type == BuiltInClass.TYPE_ERROR)
            return T;
        return super.typep(type);
    }

    public String getMessage()
    {
        // FIXME
        try {
            final LispThread thread = LispThread.currentThread();
            final SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
            thread.bindSpecial(_PRINT_ESCAPE_, T);
            try {
                String s = super.getMessage();
                if (s != null)
                    return s;
                StringBuffer sb = new StringBuffer();
                String name = datum != null ? datum.writeToString() : null;
                String type = null;
                if (typeString != null)
                    type = typeString;
                else if (expectedType != null)
                    type = expectedType.writeToString();
                if (type != null) {
                    if (name != null) {
                        sb.append("The value ");
                        sb.append(name);
                    } else
                        sb.append("Value");
                    sb.append(" is not of type ");
                    sb.append(type);
                } else if (name != null) {
                    sb.append("Wrong type: ");
                    sb.append(name);
                }
                sb.append('.');
                return sb.toString();
            }
            catch (Throwable t) {
                // FIXME
                Debug.trace(t);
                return toString();
            }
            finally {
                thread.lastSpecialBinding = lastSpecialBinding;
            }
        }
        catch (Throwable t) {
            return toString();
        }
    }

    // ### type-error-datum
    private static final Primitive TYPE_ERROR_DATUM =
        new Primitive("type-error-datum", "condition")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof TypeError)
                return ((TypeError)arg).datum;
            return signal(new TypeError(arg, Symbol.TYPE_ERROR));
        }
    };

    // ### type-error-expected-type
    private static final Primitive TYPE_ERROR_EXPECTED_TYPE =
        new Primitive("type-error-expected-type", "condition")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof TypeError)
                return ((TypeError)arg).expectedType;
            return signal(new TypeError(arg, Symbol.TYPE_ERROR));
        }
    };
}
