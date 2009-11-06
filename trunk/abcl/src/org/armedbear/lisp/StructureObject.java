/*
 * StructureObject.java
 *
 * Copyright (C) 2003-2006 Peter Graves
 * $Id$
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
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.armedbear.lisp;

public final class StructureObject extends LispObject
{
  private final StructureClass structureClass;
  private final LispObject[] slots;

  public StructureObject(Symbol symbol, LispObject[] slots)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    this.slots = slots;
  }

  public StructureObject(Symbol symbol, LispObject obj0)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    LispObject[] slots = new LispObject[1];
    slots[0] = obj0;
    this.slots = slots;
  }

  public StructureObject(Symbol symbol, LispObject obj0, LispObject obj1)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    LispObject[] slots = new LispObject[2];
    slots[0] = obj0;
    slots[1] = obj1;
    this.slots = slots;
  }

  public StructureObject(Symbol symbol, LispObject obj0, LispObject obj1,
                         LispObject obj2)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    LispObject[] slots = new LispObject[3];
    slots[0] = obj0;
    slots[1] = obj1;
    slots[2] = obj2;
    this.slots = slots;
  }

  public StructureObject(Symbol symbol, LispObject obj0, LispObject obj1,
                         LispObject obj2, LispObject obj3)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    LispObject[] slots = new LispObject[4];
    slots[0] = obj0;
    slots[1] = obj1;
    slots[2] = obj2;
    slots[3] = obj3;
    this.slots = slots;
  }

  public StructureObject(Symbol symbol, LispObject obj0, LispObject obj1,
                         LispObject obj2, LispObject obj3, LispObject obj4)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    LispObject[] slots = new LispObject[5];
    slots[0] = obj0;
    slots[1] = obj1;
    slots[2] = obj2;
    slots[3] = obj3;
    slots[4] = obj4;
    this.slots = slots;
  }

  public StructureObject(Symbol symbol, LispObject obj0, LispObject obj1,
                         LispObject obj2, LispObject obj3, LispObject obj4,
                         LispObject obj5)

  {
    structureClass = (StructureClass) LispClass.findClass(symbol); // Might return null.
    LispObject[] slots = new LispObject[6];
    slots[0] = obj0;
    slots[1] = obj1;
    slots[2] = obj2;
    slots[3] = obj3;
    slots[4] = obj4;
    slots[5] = obj5;
    this.slots = slots;
  }

  public StructureObject(StructureObject obj)
  {
    this.structureClass = obj.structureClass;
    slots = new LispObject[obj.slots.length];
    for (int i = slots.length; i-- > 0;)
      slots[i] = obj.slots[i];
  }

  @Override
  public LispObject typeOf()
  {
    return structureClass.getSymbol();
  }

  @Override
  public LispObject classOf()
  {
    return structureClass;
  }

  @Override
  public LispObject getParts()
  {
    LispObject result = NIL;
    result = result.push(new Cons("class", structureClass));
    LispObject effectiveSlots = structureClass.getSlotDefinitions();
    LispObject[] effectiveSlotsArray = effectiveSlots.copyToArray();
    Debug.assertTrue(effectiveSlotsArray.length == slots.length);
    for (int i = 0; i < slots.length; i++)
      {
        SimpleVector slotDefinition = (SimpleVector) effectiveSlotsArray[i];
        LispObject slotName = slotDefinition.AREF(1);
        result = result.push(new Cons(slotName, slots[i]));
      }
    return result.nreverse();
  }

  @Override
  public LispObject typep(LispObject type)
  {
    if (type instanceof StructureClass)
      return memq(type, structureClass.getCPL()) ? T : NIL;
    if (type == structureClass.getSymbol())
      return T;
    if (type == Symbol.STRUCTURE_OBJECT)
      return T;
    if (type == BuiltInClass.STRUCTURE_OBJECT)
      return T;
    if (type instanceof Symbol)
      {
        LispClass c = LispClass.findClass((Symbol)type);
        if (c != null)
          return memq(c, structureClass.getCPL()) ? T : NIL;
      }
    return super.typep(type);
  }

  @Override
  public boolean equalp(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj instanceof StructureObject)
      {
        StructureObject o = (StructureObject) obj;
        if (structureClass != o.structureClass)
          return false;
        for (int i = 0; i < slots.length; i++)
          {
            if (!slots[i].equalp(o.slots[i]))
              return false;
          }
        return true;
      }
    return false;
  }

  @Override
  public LispObject getSlotValue_0()
  {
    try
      {
        return slots[0];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return badIndex(0);
      }
  }

  @Override
  public LispObject getSlotValue_1()
  {
    try
      {
        return slots[1];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return badIndex(1);
      }
  }

  @Override
  public LispObject getSlotValue_2()
  {
    try
      {
        return slots[2];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return badIndex(2);
      }
  }

  @Override
  public LispObject getSlotValue_3()
  {
    try
      {
        return slots[3];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return badIndex(3);
      }
  }

  @Override
  public LispObject getSlotValue(int index)
  {
    try
      {
        return slots[index];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return badIndex(index);
      }
  }

  @Override
  public int getFixnumSlotValue(int index)
  {
    try
      {
        return Fixnum.getValue(slots[index]);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index);
        // Not reached.
        return 0;
      }
  }

  @Override
  public boolean getSlotValueAsBoolean(int index)
  {
    try
      {
        return slots[index] != NIL ? true : false;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index);
        // Not reached.
        return false;
      }
  }

  @Override
  public void setSlotValue_0(LispObject value)

  {
    try
      {
        slots[0] = value;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(0);
      }
  }

  @Override
  public void setSlotValue_1(LispObject value)

  {
    try
      {
        slots[1] = value;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(1);
      }
  }

  @Override
  public void setSlotValue_2(LispObject value)

  {
    try
      {
        slots[2] = value;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(2);
      }
  }

  @Override
  public void setSlotValue_3(LispObject value)

  {
    try
      {
        slots[3] = value;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(3);
      }
  }

  @Override
  public void setSlotValue(int index, LispObject value)

  {
    try
      {
        slots[index] = value;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index);
      }
  }

  private LispObject badIndex(int n)
  {
    FastStringBuffer sb = new FastStringBuffer("Invalid slot index ");
    sb.append(Fixnum.getInstance(n).writeToString());
    sb.append(" for ");
    sb.append(writeToString());
    return error(new LispError(sb.toString()));
  }

  @Override
  public final int psxhash()
  {
    return psxhash(4);
  }

  @Override
  public final int psxhash(int depth)
  {
    int result = mix(structureClass.sxhash(), 7814971);
    if (depth > 0)
      {
        int limit = slots.length;
        if (limit > 4)
          limit = 4;
        for (int i = 0; i < limit; i++)
          result = mix(slots[i].psxhash(depth - 1), result);
      }
    return result & 0x7fffffff;
  }

  @Override
  public String writeToString()
  {
    try
      {
        final LispThread thread = LispThread.currentThread();
        // FIXME
        if (typep(Symbol.RESTART) != NIL)
          {
            Symbol PRINT_RESTART = PACKAGE_SYS.intern("PRINT-RESTART");
            LispObject fun = PRINT_RESTART.getSymbolFunction();
            StringOutputStream stream = new StringOutputStream();
            thread.execute(fun, this, stream);
            return stream.getString().getStringValue();
          }
        if (_PRINT_STRUCTURE_.symbolValue(thread) == NIL)
          return unreadableString(structureClass.getSymbol().writeToString());
        int maxLevel = Integer.MAX_VALUE;
        LispObject printLevel = Symbol.PRINT_LEVEL.symbolValue(thread);
        if (printLevel instanceof Fixnum)
          maxLevel = ((Fixnum)printLevel).value;
        LispObject currentPrintLevel =
          _CURRENT_PRINT_LEVEL_.symbolValue(thread);
        int currentLevel = Fixnum.getValue(currentPrintLevel);
        if (currentLevel >= maxLevel && slots.length > 0)
          return "#";
        FastStringBuffer sb = new FastStringBuffer("#S(");
        sb.append(structureClass.getSymbol().writeToString());
        if (currentLevel < maxLevel)
          {
            LispObject effectiveSlots = structureClass.getSlotDefinitions();
            LispObject[] effectiveSlotsArray = effectiveSlots.copyToArray();
            Debug.assertTrue(effectiveSlotsArray.length == slots.length);
            final LispObject printLength = Symbol.PRINT_LENGTH.symbolValue(thread);
            final int limit;
            if (printLength instanceof Fixnum)
              limit = Math.min(slots.length, ((Fixnum)printLength).value);
            else
              limit = slots.length;
            final boolean printCircle =
              (Symbol.PRINT_CIRCLE.symbolValue(thread) != NIL);
            for (int i = 0; i < limit; i++)
              {
                sb.append(' ');
                SimpleVector slotDefinition = (SimpleVector) effectiveSlotsArray[i];
                // FIXME AREF(1)
                LispObject slotName = slotDefinition.AREF(1);
                Debug.assertTrue(slotName instanceof Symbol);
                sb.append(':');
                sb.append(((Symbol)slotName).name.getStringValue());
                sb.append(' ');
                if (printCircle)
                  {
                    StringOutputStream stream = new StringOutputStream();
                    thread.execute(Symbol.OUTPUT_OBJECT.getSymbolFunction(),
                                   slots[i], stream);
                    sb.append(stream.getString().getStringValue());
                  }
                else
                  sb.append(slots[i].writeToString());
              }
            if (limit < slots.length)
              sb.append(" ...");
          }
        sb.append(')');
        return sb.toString();
      }
    catch (StackOverflowError e)
      {
        error(new StorageCondition("Stack overflow."));
        return null; // Not reached.
      }
  }

  // ### structure-object-p object => generalized-boolean
  private static final Primitive STRUCTURE_OBJECT_P =
    new Primitive("structure-object-p", PACKAGE_SYS, true, "object")
    {
      @Override
      public LispObject execute(LispObject arg)
      {
        return arg instanceof StructureObject ? T : NIL;
      }
    };

  // ### structure-length instance => length
  private static final Primitive STRUCTURE_LENGTH =
    new Primitive("structure-length", PACKAGE_SYS, true, "instance")
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          if (arg instanceof StructureObject)
            return Fixnum.getInstance(((StructureObject)arg).slots.length);
        return type_error(arg, Symbol.STRUCTURE_OBJECT);
      }
    };

  // ### structure-ref instance index => value
  private static final Primitive STRUCTURE_REF =
    new Primitive("structure-ref", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
    if (first instanceof StructureObject)
        try
          {
            return ((StructureObject)first).slots[Fixnum.getValue(second)];
          }
        catch (ArrayIndexOutOfBoundsException e)
          {
            // Shouldn't happen.
            return error(new LispError("Internal error."));
          }      
      return type_error(first, Symbol.STRUCTURE_OBJECT);
      }
    };

  // ### structure-set instance index new-value => new-value
  private static final Primitive STRUCTURE_SET =
    new Primitive("structure-set", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)

      {
          
            if (first instanceof StructureObject)
                try
                  {
                    ((StructureObject)first).slots[Fixnum.getValue(second)] = third;
                    return third;
                  }
                catch (ArrayIndexOutOfBoundsException e)
                  {
                    // Shouldn't happen.
                    return error(new LispError("Internal error."));
                  }      
              return type_error(first, Symbol.STRUCTURE_OBJECT);
              }      
    };

  // ### make-structure
  private static final Primitive MAKE_STRUCTURE =
    new Primitive("make-structure", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          return new StructureObject(checkSymbol(first), second);
      }
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)

      {
          return new StructureObject(checkSymbol(first), second, third);
      }
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third, LispObject fourth)

      {
          return new StructureObject(checkSymbol(first), second, third, fourth);
      }
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third, LispObject fourth,
                                LispObject fifth)

      {
          return new StructureObject(checkSymbol(first), second, third, fourth,
                  fifth);
      }
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third, LispObject fourth,
                                LispObject fifth, LispObject sixth)

      {
          return new StructureObject(checkSymbol(first), second, third, fourth,
                  fifth, sixth);
      }
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third, LispObject fourth,
                                LispObject fifth, LispObject sixth,
                                LispObject seventh)

      {
          return new StructureObject(checkSymbol(first), second, third, fourth,
                  fifth, sixth, seventh);
      }
    };

  // ### %make-structure name slot-values => object
  private static final Primitive _MAKE_STRUCTURE =
    new Primitive("%make-structure", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          return new StructureObject(checkSymbol(first), second.copyToArray());
      }
    };

  // ### copy-structure structure => copy
  private static final Primitive COPY_STRUCTURE =
    new Primitive(Symbol.COPY_STRUCTURE, "structure")
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          if (arg instanceof StructureObject)
            return new StructureObject((StructureObject)arg);
          return type_error(arg, Symbol.STRUCTURE_OBJECT);
      }
    };
}
