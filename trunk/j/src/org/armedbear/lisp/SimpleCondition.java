/*
 * SimpleCondition.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: SimpleCondition.java,v 1.13 2005-10-31 04:04:42 piso Exp $
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

public class SimpleCondition extends Condition
{
    public SimpleCondition() throws ConditionThrowable
    {
        setFormatControl(NIL);
        setFormatArguments(NIL);
    }

    public SimpleCondition(LispObject formatControl, LispObject formatArguments)
        throws ConditionThrowable
    {
        setFormatControl(formatControl);
        setFormatArguments(formatArguments);
    }

    public SimpleCondition(LispObject initArgs) throws ConditionThrowable
    {
        super(initArgs);
    }

    public SimpleCondition(String message)
    {
        super(message);
    }

    public LispObject typeOf()
    {
        return Symbol.SIMPLE_CONDITION;
    }

    public LispObject classOf()
    {
        return StandardClass.SIMPLE_CONDITION;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.SIMPLE_CONDITION)
            return T;
        if (type == StandardClass.SIMPLE_CONDITION)
            return T;
        return super.typep(type);
    }

    // ### simple-condition-format-control
    private static final Primitive SIMPLE_CONDITION_FORMAT_CONTROL =
        new Primitive("simple-condition-format-control", "condition")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).getInstanceSlotValue(Symbol.FORMAT_CONTROL);
            return signalTypeError(arg, Symbol.STANDARD_OBJECT);
        }
    };

    // ### simple-condition-format-arguments
    private static final Primitive SIMPLE_CONDITION_FORMAT_ARGUMENTS =
        new Primitive("simple-condition-format-arguments", "condition")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof StandardObject)
                return ((StandardObject)arg).getInstanceSlotValue(Symbol.FORMAT_ARGUMENTS);
            return signalTypeError(arg, Symbol.STANDARD_OBJECT);
        }
    };
}
