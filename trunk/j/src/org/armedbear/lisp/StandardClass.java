/*
 * StandardClass.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: StandardClass.java,v 1.22 2005-06-21 18:42:14 piso Exp $
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

public class StandardClass extends SlotClass
{
    public StandardClass()
    {
        setClassLayout(new Layout(this, NIL, NIL));
    }

    public StandardClass(Symbol symbol, LispObject directSuperclasses)
    {
        super(symbol, directSuperclasses);
        setClassLayout(new Layout(this, NIL, NIL));
    }

    public LispObject typeOf()
    {
        return Symbol.STANDARD_CLASS;
    }

    public LispObject classOf()
    {
        return STANDARD_CLASS;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.STANDARD_CLASS)
            return T;
        if (type == STANDARD_CLASS)
            return T;
        return super.typep(type);
    }

    public String writeToString() throws ConditionThrowable
    {
        StringBuffer sb = new StringBuffer(Symbol.STANDARD_CLASS.writeToString());
        if (symbol != null) {
            sb.append(' ');
            sb.append(symbol.writeToString());
        }
        return unreadableString(sb.toString());
    }

    private static final StandardClass addStandardClass(Symbol name,
                                                        LispObject directSuperclasses)
    {
        StandardClass c = new StandardClass(name, directSuperclasses);
        addClass(name, c);
        return c;
    }

    public static final StandardClass STANDARD_OBJECT =
        addStandardClass(Symbol.STANDARD_OBJECT, list1(BuiltInClass.CLASS_T));

    public static final StandardClass STANDARD_CLASS =
        addStandardClass(Symbol.STANDARD_CLASS, list1(BuiltInClass.CLASS_T));

    public static final StandardClass CLASS =
        addStandardClass(Symbol.CLASS, list1(STANDARD_OBJECT));

    public static final StandardClass BUILT_IN_CLASS =
        addStandardClass(Symbol.BUILT_IN_CLASS, list1(CLASS));

    public static final StandardClass FORWARD_REFERENCED_CLASS =
        addStandardClass(Symbol.FORWARD_REFERENCED_CLASS, list1(CLASS));

    public static final StandardClass STRUCTURE_CLASS =
        addStandardClass(Symbol.STRUCTURE_CLASS, list1(CLASS));

    public static final StandardClass CONDITION =
        addStandardClass(Symbol.CONDITION, list1(STANDARD_OBJECT));

    public static final StandardClass SIMPLE_CONDITION =
        addStandardClass(Symbol.SIMPLE_CONDITION, list1(CONDITION));

    public static final StandardClass WARNING =
        addStandardClass(Symbol.WARNING, list1(CONDITION));

    public static final StandardClass SIMPLE_WARNING =
        addStandardClass(Symbol.SIMPLE_WARNING, list2(SIMPLE_CONDITION, WARNING));

    public static final StandardClass STYLE_WARNING =
        addStandardClass(Symbol.STYLE_WARNING, list1(WARNING));

    public static final StandardClass SERIOUS_CONDITION =
        addStandardClass(Symbol.SERIOUS_CONDITION, list1(CONDITION));

    public static final StandardClass STORAGE_CONDITION =
        addStandardClass(Symbol.STORAGE_CONDITION, list1(SERIOUS_CONDITION));

    public static final StandardClass ERROR =
        addStandardClass(Symbol.ERROR, list1(SERIOUS_CONDITION));

    public static final StandardClass ARITHMETIC_ERROR =
        addStandardClass(Symbol.ARITHMETIC_ERROR, list1(ERROR));

    public static final StandardClass CELL_ERROR =
        addStandardClass(Symbol.CELL_ERROR, list1(ERROR));

    public static final StandardClass CONTROL_ERROR =
        addStandardClass(Symbol.CONTROL_ERROR, list1(ERROR));

    public static final StandardClass FILE_ERROR =
        addStandardClass(Symbol.FILE_ERROR, list1(ERROR));

    public static final StandardClass DIVISION_BY_ZERO =
        addStandardClass(Symbol.DIVISION_BY_ZERO, list1(ARITHMETIC_ERROR));

    public static final StandardClass FLOATING_POINT_INEXACT =
        addStandardClass(Symbol.FLOATING_POINT_INEXACT, list1(ARITHMETIC_ERROR));

    public static final StandardClass FLOATING_POINT_INVALID_OPERATION =
        addStandardClass(Symbol.FLOATING_POINT_INVALID_OPERATION, list1(ARITHMETIC_ERROR));

    public static final StandardClass FLOATING_POINT_OVERFLOW =
        addStandardClass(Symbol.FLOATING_POINT_OVERFLOW, list1(ARITHMETIC_ERROR));

    public static final StandardClass FLOATING_POINT_UNDERFLOW =
        addStandardClass(Symbol.FLOATING_POINT_UNDERFLOW, list1(ARITHMETIC_ERROR));

    public static final StandardClass PROGRAM_ERROR =
        addStandardClass(Symbol.PROGRAM_ERROR, list1(ERROR));

    public static final StandardClass PACKAGE_ERROR =
        addStandardClass(Symbol.PACKAGE_ERROR, list1(ERROR));

    public static final StandardClass STREAM_ERROR =
        addStandardClass(Symbol.STREAM_ERROR, list1(ERROR));

    public static final StandardClass PARSE_ERROR =
        addStandardClass(Symbol.PARSE_ERROR, list1(ERROR));

    public static final StandardClass PRINT_NOT_READABLE =
        addStandardClass(Symbol.PRINT_NOT_READABLE, list1(ERROR));

    public static final StandardClass READER_ERROR =
        addStandardClass(Symbol.READER_ERROR, list2(PARSE_ERROR, STREAM_ERROR));

    public static final StandardClass END_OF_FILE =
        addStandardClass(Symbol.END_OF_FILE, list1(STREAM_ERROR));

    public static final StandardClass SIMPLE_ERROR =
        addStandardClass(Symbol.SIMPLE_ERROR, list2(SIMPLE_CONDITION, ERROR));

    public static final StandardClass TYPE_ERROR =
        addStandardClass(Symbol.TYPE_ERROR, list1(ERROR));

    public static final StandardClass SIMPLE_TYPE_ERROR =
        addStandardClass(Symbol.SIMPLE_TYPE_ERROR, list2(SIMPLE_CONDITION,
                                                         TYPE_ERROR));

    public static final StandardClass UNBOUND_SLOT =
        addStandardClass(Symbol.UNBOUND_SLOT, list1(CELL_ERROR));

    public static final StandardClass UNBOUND_VARIABLE =
        addStandardClass(Symbol.UNBOUND_VARIABLE, list1(CELL_ERROR));

    public static final StandardClass UNDEFINED_FUNCTION =
        addStandardClass(Symbol.UNDEFINED_FUNCTION, list1(CELL_ERROR));

    public static final StandardClass GENERIC_FUNCTION =
        addStandardClass(Symbol.GENERIC_FUNCTION, list2(BuiltInClass.FUNCTION, STANDARD_OBJECT));

    public static final StandardClass METHOD =
        addStandardClass(Symbol.METHOD, list1(STANDARD_OBJECT));

    public static final StandardClass STANDARD_METHOD =
        new StandardMethodClass();
    static {
        addClass(Symbol.STANDARD_METHOD, STANDARD_METHOD);
    }
    public static final StandardClass STANDARD_GENERIC_FUNCTION =
        new StandardGenericFunctionClass();
    static {
        addClass(Symbol.STANDARD_GENERIC_FUNCTION, STANDARD_GENERIC_FUNCTION);
    }

    public static final StandardClass SLOT_DEFINITION =
        new SlotDefinitionClass();
    static {
        addClass(Symbol.SLOT_DEFINITION, SLOT_DEFINITION);
    }

    public static void initializeStandardClasses()
    {
        ARITHMETIC_ERROR.setCPL(ARITHMETIC_ERROR, ERROR, SERIOUS_CONDITION,
                                CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        BUILT_IN_CLASS.setDirectSuperclass(CLASS);
        BUILT_IN_CLASS.setCPL(BUILT_IN_CLASS, CLASS, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        CELL_ERROR.setCPL(CELL_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                          STANDARD_OBJECT, BuiltInClass.CLASS_T);
        CLASS.setDirectSuperclass(BuiltInClass.CLASS_T);
        CLASS.setCPL(CLASS, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        CONDITION.setDirectSuperclass(STANDARD_OBJECT);
        CONDITION.setCPL(CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        CONDITION.setDirectSlots(
            list2(new SlotDefinition(Symbol.FORMAT_CONTROL,
                                     list1(PACKAGE_CL.intern("SIMPLE-CONDITION-FORMAT-CONTROL"))),
                  new SlotDefinition(Symbol.FORMAT_ARGUMENTS,
                                     list1(PACKAGE_CL.intern("SIMPLE-CONDITION-FORMAT-ARGUMENTS")))));
        CONTROL_ERROR.setCPL(CONTROL_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                             STANDARD_OBJECT, BuiltInClass.CLASS_T);
        DIVISION_BY_ZERO.setDirectSuperclass(ARITHMETIC_ERROR);
        DIVISION_BY_ZERO.setCPL(DIVISION_BY_ZERO, ARITHMETIC_ERROR, ERROR,
                                SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                                BuiltInClass.CLASS_T);
        END_OF_FILE.setDirectSuperclass(STREAM_ERROR);
        END_OF_FILE.setCPL(END_OF_FILE, STREAM_ERROR, ERROR, SERIOUS_CONDITION,
                           CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        ERROR.setDirectSuperclass(SERIOUS_CONDITION);
        ERROR.setCPL(ERROR, SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                     BuiltInClass.CLASS_T);
        FILE_ERROR.setCPL(FILE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                          STANDARD_OBJECT, BuiltInClass.CLASS_T);
        FLOATING_POINT_INEXACT.setDirectSuperclass(ARITHMETIC_ERROR);
        FLOATING_POINT_INEXACT.setCPL(FLOATING_POINT_INEXACT, ARITHMETIC_ERROR,
                                      ERROR, SERIOUS_CONDITION, CONDITION,
                                      STANDARD_OBJECT, BuiltInClass.CLASS_T);
        FLOATING_POINT_INVALID_OPERATION.setDirectSuperclass(ARITHMETIC_ERROR);
        FLOATING_POINT_INVALID_OPERATION.setCPL(FLOATING_POINT_INVALID_OPERATION,
                                                ARITHMETIC_ERROR, ERROR,
                                                SERIOUS_CONDITION, CONDITION,
                                                STANDARD_OBJECT, BuiltInClass.CLASS_T);
        FLOATING_POINT_OVERFLOW.setDirectSuperclass(ARITHMETIC_ERROR);
        FLOATING_POINT_OVERFLOW.setCPL(FLOATING_POINT_OVERFLOW, ARITHMETIC_ERROR,
                                       ERROR, SERIOUS_CONDITION, CONDITION,
                                       STANDARD_OBJECT, BuiltInClass.CLASS_T);
        FLOATING_POINT_UNDERFLOW.setDirectSuperclass(ARITHMETIC_ERROR);
        FLOATING_POINT_UNDERFLOW.setCPL(FLOATING_POINT_UNDERFLOW, ARITHMETIC_ERROR,
                                        ERROR, SERIOUS_CONDITION, CONDITION,
                                        STANDARD_OBJECT, BuiltInClass.CLASS_T);
        FORWARD_REFERENCED_CLASS.setDirectSuperclass(CLASS);
        FORWARD_REFERENCED_CLASS.setCPL(FORWARD_REFERENCED_CLASS, CLASS, BuiltInClass.CLASS_T);
        GENERIC_FUNCTION.setDirectSuperclasses(list2(BuiltInClass.FUNCTION, STANDARD_OBJECT));
        GENERIC_FUNCTION.setCPL(GENERIC_FUNCTION, STANDARD_OBJECT, BuiltInClass.FUNCTION,
                                BuiltInClass.CLASS_T);
        METHOD.setDirectSuperclass(STANDARD_OBJECT);
        METHOD.setCPL(METHOD, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        PACKAGE_ERROR.setCPL(PACKAGE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                             STANDARD_OBJECT, BuiltInClass.CLASS_T);
        PACKAGE_ERROR.setDirectSlots(
            list1(new SlotDefinition(Symbol.PACKAGE,
                                     list1(PACKAGE_CL.intern("PACKAGE-ERROR-PACKAGE")))));
        PARSE_ERROR.setCPL(PARSE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                           STANDARD_OBJECT, BuiltInClass.CLASS_T);
        PRINT_NOT_READABLE.setDirectSuperclass(ERROR);
        PRINT_NOT_READABLE.setCPL(PRINT_NOT_READABLE, ERROR, SERIOUS_CONDITION,
                                  CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        PROGRAM_ERROR.setCPL(PROGRAM_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                             STANDARD_OBJECT, BuiltInClass.CLASS_T);
        READER_ERROR.setCPL(READER_ERROR, PARSE_ERROR, STREAM_ERROR, ERROR,
                            SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                            BuiltInClass.CLASS_T);
        SERIOUS_CONDITION.setDirectSuperclass(CONDITION);
        SERIOUS_CONDITION.setCPL(SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                                 BuiltInClass.CLASS_T);
        SIMPLE_CONDITION.setDirectSuperclass(CONDITION);
        SIMPLE_CONDITION.setCPL(SIMPLE_CONDITION, CONDITION, STANDARD_OBJECT,
                                BuiltInClass.CLASS_T);
        SIMPLE_ERROR.setCPL(SIMPLE_ERROR, SIMPLE_CONDITION, ERROR,
                            SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                            BuiltInClass.CLASS_T);
        SIMPLE_TYPE_ERROR.setDirectSuperclasses(list2(SIMPLE_CONDITION, TYPE_ERROR));
        SIMPLE_TYPE_ERROR.setCPL(SIMPLE_TYPE_ERROR, SIMPLE_CONDITION,
                                 TYPE_ERROR, ERROR, SERIOUS_CONDITION,
                                 CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        SIMPLE_WARNING.setDirectSuperclasses(list2(SIMPLE_CONDITION, WARNING));
        SIMPLE_WARNING.setCPL(SIMPLE_WARNING, SIMPLE_CONDITION, WARNING,
                              CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        SLOT_DEFINITION.setDirectSuperclass(STANDARD_OBJECT);
        SLOT_DEFINITION.setCPL(SLOT_DEFINITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        STANDARD_CLASS.setDirectSuperclass(CLASS);
        STANDARD_CLASS.setCPL(STANDARD_CLASS, CLASS,
                              STANDARD_OBJECT, BuiltInClass.CLASS_T);
        STANDARD_GENERIC_FUNCTION.setDirectSuperclass(GENERIC_FUNCTION);
        STANDARD_GENERIC_FUNCTION.setCPL(STANDARD_GENERIC_FUNCTION,
                                         GENERIC_FUNCTION, STANDARD_OBJECT,
                                         BuiltInClass.FUNCTION, BuiltInClass.CLASS_T);
        STANDARD_METHOD.setDirectSuperclass(METHOD);
        STANDARD_METHOD.setCPL(STANDARD_METHOD, METHOD, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        STANDARD_OBJECT.setDirectSuperclass(BuiltInClass.CLASS_T);
        STANDARD_OBJECT.setCPL(STANDARD_OBJECT, BuiltInClass.CLASS_T);
        STORAGE_CONDITION.setDirectSuperclass(SERIOUS_CONDITION);
        STORAGE_CONDITION.setCPL(STORAGE_CONDITION, SERIOUS_CONDITION, CONDITION,
                                 STANDARD_OBJECT, BuiltInClass.CLASS_T);
        STREAM_ERROR.setCPL(STREAM_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                            STANDARD_OBJECT, BuiltInClass.CLASS_T);
        STRUCTURE_CLASS.setDirectSuperclass(CLASS);
        STRUCTURE_CLASS.setCPL(STRUCTURE_CLASS, CLASS, STANDARD_OBJECT,
                               BuiltInClass.CLASS_T);
        STYLE_WARNING.setDirectSuperclass(WARNING);
        STYLE_WARNING.setCPL(STYLE_WARNING, WARNING, CONDITION, STANDARD_OBJECT,
                             BuiltInClass.CLASS_T);
        TYPE_ERROR.setCPL(TYPE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                          STANDARD_OBJECT, BuiltInClass.CLASS_T);
        TYPE_ERROR.setDirectSlots(
            list2(new SlotDefinition(Symbol.DATUM,
                                     list1(PACKAGE_CL.intern("TYPE-ERROR-DATUM"))),
                  new SlotDefinition(Symbol.EXPECTED_TYPE,
                                     list1(PACKAGE_CL.intern("TYPE-ERROR-EXPECTED-TYPE")))));
        UNBOUND_SLOT.setCPL(UNBOUND_SLOT, CELL_ERROR, ERROR, SERIOUS_CONDITION,
                            CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
        UNBOUND_VARIABLE.setCPL(UNBOUND_VARIABLE, CELL_ERROR, ERROR,
                                SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                                BuiltInClass.CLASS_T);
        UNDEFINED_FUNCTION.setCPL(UNDEFINED_FUNCTION, CELL_ERROR, ERROR,
                                  SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                                  BuiltInClass.CLASS_T);
        WARNING.setDirectSuperclass(CONDITION);
        WARNING.setCPL(WARNING, CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);


        CONDITION.finalizeClassLayout();
        ERROR.finalizeClassLayout();
        PACKAGE_ERROR.finalizeClassLayout();
        SERIOUS_CONDITION.finalizeClassLayout();
        SIMPLE_CONDITION.finalizeClassLayout();
        SIMPLE_TYPE_ERROR.finalizeClassLayout();
        STORAGE_CONDITION.finalizeClassLayout();
        STYLE_WARNING.finalizeClassLayout();
        TYPE_ERROR.finalizeClassLayout();
        WARNING.finalizeClassLayout();
    }
}
