/*
 * make_condition.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: make_condition.java,v 1.5 2003-09-22 11:09:38 piso Exp $
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

// ### make-condition
public final class make_condition extends Primitive
{
    private make_condition(String name)
    {
        super(name);
    }

    // make-condition type &rest slot-initializations => condition
    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        if (args.length < 1)
            throw new ConditionThrowable(new WrongNumberOfArgumentsException(this));
        LispObject type = args[0];
        LispObject initArgs = NIL;
        for (int i = args.length; i-- > 1;)
            initArgs = new Cons(args[i], initArgs);
        if (type == Symbol.CONDITION)
            return new Condition(initArgs);
        if (type == Symbol.SIMPLE_CONDITION)
            return new SimpleCondition(initArgs);
        if (type == Symbol.ERROR)
            return new LispError(initArgs);
        if (type == Symbol.PACKAGE_ERROR)
            return new PackageError(initArgs);
        if (type == Symbol.SIMPLE_ERROR)
            return new SimpleError(initArgs);
        if (type == Symbol.UNBOUND_SLOT)
            return new UnboundSlot(initArgs);
        if (type == Symbol.UNBOUND_VARIABLE)
            return new UnboundVariable(initArgs);
        if (type == Symbol.UNDEFINED_FUNCTION)
            return new UndefinedFunction(initArgs);

//         if (type instanceof Symbol) {
//             LispClass cls = findClass(type);
//             if (cls instanceof StandardClass) {
//                 return ((StandardClass)cls).makeInstance(initArgs);
//             }
//         }

        throw new ConditionThrowable(new LispError("MAKE-CONDITION: unsupported type " + type));
    }

    private static final make_condition MAKE_CONDITION =
        new make_condition("make-condition");
}
