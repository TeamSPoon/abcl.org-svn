/*
 * StandardGenericFunction.java
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

import static org.armedbear.lisp.Lisp.*;

import java.util.concurrent.ConcurrentHashMap;

public final class StandardGenericFunction extends StandardObject
{
  LispObject function;

  int numberOfRequiredArgs;

  ConcurrentHashMap<CacheEntry,LispObject> cache;
  ConcurrentHashMap<LispObject,LispObject> slotCache;

  public StandardGenericFunction()
  {
    super(StandardClass.STANDARD_GENERIC_FUNCTION,
          StandardClass.STANDARD_GENERIC_FUNCTION.getClassLayout().getLength());
  }

  public StandardGenericFunction(String name, Package pkg, boolean exported,
                                 Function function, LispObject lambdaList,
                                 LispObject specializers)
  {
    this();
    Symbol symbol;
    if (exported)
      symbol = pkg.internAndExport(name.toUpperCase());
    else
      symbol = pkg.intern(name.toUpperCase());
    symbol.setSymbolFunction(this);
    this.function = function;
    slots[StandardGenericFunctionClass.SLOT_INDEX_NAME] = symbol;
    slots[StandardGenericFunctionClass.SLOT_INDEX_LAMBDA_LIST] =
      lambdaList;
    slots[StandardGenericFunctionClass.SLOT_INDEX_REQUIRED_ARGS] =
      lambdaList;
    numberOfRequiredArgs = lambdaList.length();
    slots[StandardGenericFunctionClass.SLOT_INDEX_INITIAL_METHODS] =
      NIL;
    StandardMethod method =
      new StandardMethod(this, function, lambdaList, specializers);
    slots[StandardGenericFunctionClass.SLOT_INDEX_METHODS] =
      list(method);
    slots[StandardGenericFunctionClass.SLOT_INDEX_METHOD_CLASS] =
      StandardClass.STANDARD_METHOD;
    slots[StandardGenericFunctionClass.SLOT_INDEX_METHOD_COMBINATION] =
      Symbol.STANDARD;
    slots[StandardGenericFunctionClass.SLOT_INDEX_ARGUMENT_PRECEDENCE_ORDER] =
      NIL;
    slots[StandardGenericFunctionClass.SLOT_INDEX_CLASSES_TO_EMF_TABLE] =
      NIL;
    slots[StandardGenericFunctionClass.SLOT_INDEX_DOCUMENTATION] = NIL;
  }

  void finalizeInternal()
  {
    cache = null;
  }

  @Override
  public LispObject typep(LispObject type)
  {
    if (type == Symbol.COMPILED_FUNCTION)
      {
        if (function != null)
          return function.typep(type);
        else
          return NIL;
      }
    if (type == Symbol.STANDARD_GENERIC_FUNCTION)
      return T;
    if (type == StandardClass.STANDARD_GENERIC_FUNCTION)
      return T;
    return super.typep(type);
  }

  public LispObject getGenericFunctionName()
  {
    return slots[StandardGenericFunctionClass.SLOT_INDEX_NAME];
  }

  public void setGenericFunctionName(LispObject name)
  {
    slots[StandardGenericFunctionClass.SLOT_INDEX_NAME] = name;
  }

  @Override
  public LispObject execute()
  {
    return function.execute();
  }

  @Override
  public LispObject execute(LispObject arg)
  {
    return function.execute(arg);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second)

  {
    return function.execute(first, second);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second,
                            LispObject third)

  {
    return function.execute(first, second, third);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth)

  {
    return function.execute(first, second, third, fourth);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth)

  {
    return function.execute(first, second, third, fourth,
                            fifth);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth, LispObject sixth)

  {
    return function.execute(first, second, third, fourth,
                            fifth, sixth);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth, LispObject sixth,
                            LispObject seventh)

  {
    return function.execute(first, second, third, fourth,
                            fifth, sixth, seventh);
  }

  @Override
  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth, LispObject sixth,
                            LispObject seventh, LispObject eighth)

  {
    return function.execute(first, second, third, fourth,
                            fifth, sixth, seventh, eighth);
  }

  @Override
  public LispObject execute(LispObject[] args)
  {
    return function.execute(args);
  }

  @Override
  public String printObject()
  {
    LispObject name = getGenericFunctionName();
    if (name != null)
      {
        StringBuilder sb = new StringBuilder();
        LispObject className;
        LispObject lispClass = getLispClass();
        if (lispClass instanceof LispClass)
          className = ((LispClass)lispClass).getName();
        else
          className = Symbol.CLASS_NAME.execute(lispClass);

        sb.append(className.princToString());
        sb.append(' ');
        sb.append(name.princToString());
        return unreadableString(sb.toString());
      }
    return super.printObject();
  }

  // Profiling.
  private int callCount;
  private int hotCount;

  @Override
  public final int getCallCount()
  {
    return callCount;
  }

  @Override
  public void setCallCount(int n)
  {
    callCount = n;
  }

  @Override
  public final void incrementCallCount()
  {
    ++callCount;
  }

    @Override
    public final int getHotCount()
    {
        return hotCount;
    }

    @Override
    public void setHotCount(int n)
    {
        hotCount = n;
    }

    @Override
    public final void incrementHotCount()
    {
        ++hotCount;
    }

    // AMOP (p. 216) specifies the following readers as generic functions:
  //   generic-function-argument-precedence-order
  //   generic-function-declarations
  //   generic-function-lambda-list
  //   generic-function-method-class
  //   generic-function-method-combination
  //   generic-function-methods
  //   generic-function-name

  // ### %generic-function-name
  private static final Primitive _GENERIC_FUNCTION_NAME =
    new Primitive("%generic-function-name", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_NAME];
      }
    };

  // ### %set-generic-function-name
  private static final Primitive _SET_GENERIC_FUNCTION_NAME =
    new Primitive("%set-generic-function-name", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_NAME] = second;
          return second;
      }
    };

  // ### %generic-function-lambda-list
  private static final Primitive _GENERIC_FUNCTION_LAMBDA_LIST =
    new Primitive("%generic-function-lambda-list", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_LAMBDA_LIST];
      }
    };

  // ### %set-generic-function-lambdaList
  private static final Primitive _SET_GENERIC_FUNCTION_LAMBDA_LIST =
    new Primitive("%set-generic-function-lambda-list", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_LAMBDA_LIST] = second;
          return second;
      }
    };

  // ### funcallable-instance-function funcallable-instance => function
  private static final Primitive FUNCALLABLE_INSTANCE_FUNCTION =
    new Primitive("funcallable-instance-function", PACKAGE_MOP, false,
                  "funcallable-instance")
    {
      @Override
      public LispObject execute(LispObject arg)

      {
          return checkStandardGenericFunction(arg).function;
      }
    };

  // ### set-funcallable-instance-function funcallable-instance function => unspecified
  // AMOP p. 230
  private static final Primitive SET_FUNCALLABLE_INSTANCE_FUNCTION =
    new Primitive("set-funcallable-instance-function", PACKAGE_MOP, true,
                  "funcallable-instance function")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).function = second;
          return second;
      }
    };

  // ### gf-required-args
  private static final Primitive GF_REQUIRED_ARGS =
    new Primitive("gf-required-args", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_REQUIRED_ARGS];
      }
    };

  // ### %set-gf-required-args
  private static final Primitive _SET_GF_REQUIRED_ARGS =
    new Primitive("%set-gf-required-args", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
        final StandardGenericFunction gf = checkStandardGenericFunction(first);
        gf.slots[StandardGenericFunctionClass.SLOT_INDEX_REQUIRED_ARGS] = second;
        gf.numberOfRequiredArgs = second.length();
        return second;
      }
    };

  // ### generic-function-initial-methods
  private static final Primitive GENERIC_FUNCTION_INITIAL_METHODS =
    new Primitive("generic-function-initial-methods", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_INITIAL_METHODS];
      }
    };

  // ### set-generic-function-initial-methods
  private static final Primitive SET_GENERIC_FUNCTION_INITIAL_METHODS =
    new Primitive("set-generic-function-initial-methods", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_INITIAL_METHODS] = second;
          return second;
      }
    };

  // ### generic-function-methods
  private static final Primitive GENERIC_FUNCTION_METHODS =
    new Primitive("generic-function-methods", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_METHODS];
      }
    };

  // ### set-generic-function-methods
  private static final Primitive SET_GENERIC_FUNCTION_METHODS =
    new Primitive("set-generic-function-methods", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_METHODS] = second;
          return second;
      }
    };

  // ### generic-function-method-class
  private static final Primitive GENERIC_FUNCTION_METHOD_CLASS =
    new Primitive("generic-function-method-class", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_METHOD_CLASS];
      }
    };

  // ### set-generic-function-method-class
  private static final Primitive SET_GENERIC_FUNCTION_METHOD_CLASS =
    new Primitive("set-generic-function-method-class", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_METHOD_CLASS] = second;
          return second;
      }
    };

  // ### generic-function-method-combination
  private static final Primitive GENERIC_FUNCTION_METHOD_COMBINATION =
    new Primitive("generic-function-method-combination", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_METHOD_COMBINATION];
      }
    };

  // ### set-generic-function-method-combination
  private static final Primitive SET_GENERIC_FUNCTION_METHOD_COMBINATION =
    new Primitive("set-generic-function-method-combination", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_METHOD_COMBINATION] 
	    = second;
          return second;
      }
    };

  // ### generic-function-argument-precedence-order
  private static final Primitive GENERIC_FUNCTION_ARGUMENT_PRECEDENCE_ORDER =
    new Primitive("generic-function-argument-precedence-order", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass
							 .SLOT_INDEX_ARGUMENT_PRECEDENCE_ORDER];
      }
    };

  // ### set-generic-function-argument-precedence-order
  private static final Primitive SET_GENERIC_FUNCTION_ARGUMENT_PRECEDENCE_ORDER =
    new Primitive("set-generic-function-argument-precedence-order", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first)
	    .slots[StandardGenericFunctionClass.SLOT_INDEX_ARGUMENT_PRECEDENCE_ORDER] = second;
          return second;
      }
    };

  // ### generic-function-classes-to-emf-table
  private static final Primitive GENERIC_FUNCTION_CLASSES_TO_EMF_TABLE =
    new Primitive("generic-function-classes-to-emf-table", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg)
	    .slots[StandardGenericFunctionClass.SLOT_INDEX_CLASSES_TO_EMF_TABLE];
      }
    };

  // ### set-generic-function-classes-to-emf-table
  private static final Primitive SET_GENERIC_FUNCTION_CLASSES_TO_EMF_TABLE =
    new Primitive("set-generic-function-classes-to-emf-table", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first)
	    .slots[StandardGenericFunctionClass.SLOT_INDEX_CLASSES_TO_EMF_TABLE] = second;
          return second;
      }
    };

  // ### generic-function-documentation
  private static final Primitive GENERIC_FUNCTION_DOCUMENTATION =
    new Primitive("generic-function-documentation", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          return checkStandardGenericFunction(arg).slots[StandardGenericFunctionClass.SLOT_INDEX_DOCUMENTATION];
      }
    };

  // ### set-generic-function-documentation
  private static final Primitive SET_GENERIC_FUNCTION_DOCUMENTATION =
    new Primitive("set-generic-function-documentation", PACKAGE_SYS, true)
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
          checkStandardGenericFunction(first).slots[StandardGenericFunctionClass.SLOT_INDEX_DOCUMENTATION] 
	    = second;
          return second;
      }
    };

  // ### %finalize-generic-function
  private static final Primitive _FINALIZE_GENERIC_FUNCTION =
    new Primitive("%finalize-generic-function", PACKAGE_SYS, true,
                  "generic-function")
    {
      @Override
      public LispObject execute(LispObject arg)
      {
          final StandardGenericFunction gf = checkStandardGenericFunction(arg);
          gf.finalizeInternal();        
          return T;
      }
    };

  // ### cache-emf
  private static final Primitive CACHE_EMF =
    new Primitive("cache-emf", PACKAGE_SYS, true, "generic-function args emf")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)

      {
        final StandardGenericFunction gf = checkStandardGenericFunction(first);
        LispObject args = second;
        LispObject[] array = new LispObject[gf.numberOfRequiredArgs];
        for (int i = gf.numberOfRequiredArgs; i-- > 0;)
          {
            array[i] = gf.getArgSpecialization(args.car());
            args = args.cdr();
          }
        CacheEntry specializations = new CacheEntry(array);
        ConcurrentHashMap<CacheEntry,LispObject> ht = gf.cache;
        if (ht == null)
            ht = gf.cache = new ConcurrentHashMap<CacheEntry,LispObject>();
        ht.put(specializations, third);
        return third;
      }
    };

  // ### get-cached-emf
  private static final Primitive GET_CACHED_EMF =
    new Primitive("get-cached-emf", PACKAGE_SYS, true, "generic-function args")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
        final StandardGenericFunction gf = checkStandardGenericFunction(first);
        LispObject args = second;
        LispObject[] array = new LispObject[gf.numberOfRequiredArgs];
        for (int i = gf.numberOfRequiredArgs; i-- > 0;)
          {
            array[i] = gf.getArgSpecialization(args.car());
            args = args.cdr();
          }
        CacheEntry specializations = new CacheEntry(array);
        ConcurrentHashMap<CacheEntry,LispObject> ht = gf.cache;
        if (ht == null)
          return NIL;
        LispObject emf = (LispObject) ht.get(specializations);
        return emf != null ? emf : NIL;
      }
    };

  /**
   * Returns an object representing generic function 
   * argument <tt>arg</tt> in a <tt>CacheEntry</tt>
   *
   * <p>In the simplest case, when this generic function
   * does not have EQL specialized methods, and therefore
   * only argument types are relevant for choosing
   * applicable methods, the value returned is the 
   * class of <tt>arg</tt>
   *
   * <p>If the function has EQL specialized methods: 
   *   - if <tt>arg</tt> is EQL to some of the EQL-specializers,
   *     a special object representing equality to that specializer
   *     is returned.
   *   - otherwise class of the <tt>arg</tt> is returned.
   *
   * <p>Note that we do not consider argument position, when
   * calculating arg specialization. In rare cases (when one argument
   * is eql-specialized to a symbol specifying class of another
   * argument) this may result in redundant cache entries caching the
   * same method. But the method cached is anyway correct for the
   * arguments (because in case of cache miss, correct method is
   * calculated by other code, which does not rely on
   * getArgSpecialization; and because EQL is true only for objects of
   * the same type, which guaranties that if a type-specialized
   * methods was chached by eql-specialization, all the cache hits
   * into this records will be from args of the conforming type).
   *
   * <p>Consider:
   * <pre><tt>
   * (defgeneric f (a b))
   *
   * (defmethod f (a (b (eql 'symbol)))
   *   "T (EQL 'SYMBOL)")
   *
   * (defmethod f ((a symbol) (b (eql 'symbol)))
   *   "SYMBOL (EQL 'SYMBOL)")
   *
   * (f 12 'symbol)
   * => "T (EQL 'SYMBOL)"
   *
   * (f 'twelve 'symbol)
   * => "SYMBOL (EQL 'SYMBOL)"
   *
   * (f 'symbol 'symbol)
   * => "SYMBOL (EQL 'SYMBOL)"
   *
   * </tt></pre>
   *
   * After the two above calls <tt>cache</tt> will contain three keys:
   * <pre>
   * { class FIXNUM, EqlSpecialization('SYMBOL) }
   * { class SYMBOL, EqlSpecialization('SYMBOL) }
   * { EqlSpecialization('SYMBOL), EqlSpecialization('SYMBOL) }.
   * </pre>
   */     
  LispObject getArgSpecialization(LispObject arg)
  {
    for (EqlSpecialization eqlSpecialization : eqlSpecializations)
      {
        if (eqlSpecialization.eqlTo.eql(arg))
          return eqlSpecialization;
      }
    return arg.classOf();
  }

  // ### %get-arg-specialization
  private static final Primitive _GET_ARG_SPECIALIZATION =
    new Primitive("%get-arg-specialization", PACKAGE_SYS, true, "generic-function arg")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
        final StandardGenericFunction gf = checkStandardGenericFunction(first);
        return gf.getArgSpecialization(second);
      }
    };

  // ### cache-slot-location
  private static final Primitive CACHE_SLOT_LOCATION =
    new Primitive("cache-slot-location", PACKAGE_SYS, true, "generic-function layout location")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)

      {
        final StandardGenericFunction gf = checkStandardGenericFunction(first);
        LispObject layout = second;
        LispObject location = third;
        ConcurrentHashMap<LispObject,LispObject> ht = gf.slotCache;
        if (ht == null)
          ht = gf.slotCache = new ConcurrentHashMap<LispObject,LispObject>();
        ht.put(layout, location);
        return third;
      }
    };

  // ### get-cached-slot-location
  private static final Primitive GET_CACHED_SLOT_LOCATION =
    new Primitive("get-cached-slot-location", PACKAGE_SYS, true, "generic-function layout")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second)

      {
        final StandardGenericFunction gf = checkStandardGenericFunction(first);
        LispObject layout = second;
        ConcurrentHashMap<LispObject,LispObject> ht = gf.slotCache;
        if (ht == null)
          return NIL;
        LispObject location = (LispObject) ht.get(layout);
        return location != null ? location : NIL;
      }
    };

  private static final StandardGenericFunction GENERIC_FUNCTION_NAME =
    new StandardGenericFunction("generic-function-name",
                                PACKAGE_MOP,
                                true,
                                _GENERIC_FUNCTION_NAME,
                                list(Symbol.GENERIC_FUNCTION),
                                list(StandardClass.STANDARD_GENERIC_FUNCTION));

  private static class CacheEntry
  {
    final LispObject[] array;

    CacheEntry(LispObject[] array)
    {
      this.array = array;
    }

    @Override
    public int hashCode()
    {
      int result = 0;
      for (int i = array.length; i-- > 0;)
        result ^= array[i].hashCode();
      return result;
    }

    @Override
    public boolean equals(Object object)
    {
      if (!(object instanceof CacheEntry))
        return false;
      final CacheEntry otherEntry = (CacheEntry) object;
      if (otherEntry.array.length != array.length)
        return false;
      final LispObject[] otherArray = otherEntry.array;
      for (int i = array.length; i-- > 0;)
        if (array[i] != otherArray[i])
          return false;
      return true;
    }
  }

  EqlSpecialization eqlSpecializations[] = new EqlSpecialization[0];

    // ### %init-eql-specializations
    private static final Primitive _INIT_EQL_SPECIALIZATIONS 
      = new Primitive("%init-eql-specializations", PACKAGE_SYS, true, 
		    "generic-function eql-specilizer-objects-list")
      {
        @Override
        public LispObject execute(LispObject first, LispObject second)

        {
          final StandardGenericFunction gf = checkStandardGenericFunction(first);
          LispObject eqlSpecializerObjects = second;
          gf.eqlSpecializations = new EqlSpecialization[eqlSpecializerObjects.length()];
          for (int i = 0; i < gf.eqlSpecializations.length; i++) {
	    gf.eqlSpecializations[i] = new EqlSpecialization(eqlSpecializerObjects.car());
	    eqlSpecializerObjects = eqlSpecializerObjects.cdr();
          }
          return NIL;
        }
      };

  private static class EqlSpecialization extends LispObject
  {
    public LispObject eqlTo;

    public EqlSpecialization(LispObject eqlTo)
    {
        this.eqlTo = eqlTo;
    }
  }
  
  public static final StandardGenericFunction checkStandardGenericFunction(LispObject obj)

  {
    if (obj instanceof StandardGenericFunction)
      return (StandardGenericFunction) obj;
    return (StandardGenericFunction) // Not reached.
      type_error(obj, Symbol.STANDARD_GENERIC_FUNCTION);
  }
}
