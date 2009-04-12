/*
 * Environment.java
 *
 * Copyright (C) 2002-2006 Peter Graves
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

public final class Environment extends LispObject
{
  private Binding vars;
  private FunctionBinding lastFunctionBinding;
  private Binding blocks;
  private Binding tags;

  public Environment() {}

  public Environment(Environment parent)
  {
    if (parent != null)
      {
        vars = parent.vars;
        lastFunctionBinding = parent.lastFunctionBinding;
        blocks = parent.blocks;
        tags = parent.tags;
      }
  }

  // Construct a new Environment extending parent with the specified symbol-
  // value binding.
  public Environment(Environment parent, Symbol symbol, LispObject value)
  {
    if (parent != null)
      {
        vars = parent.vars;
        lastFunctionBinding = parent.lastFunctionBinding;
        blocks = parent.blocks;
        tags = parent.tags;
      }
    vars = new Binding(symbol, value, vars);
  }

  @Override
  public LispObject typeOf()
  {
    return Symbol.ENVIRONMENT;
  }

  @Override
  public LispObject classOf()
  {
    return BuiltInClass.ENVIRONMENT;
  }

  @Override
  public LispObject typep(LispObject type) throws ConditionThrowable
  {
    if (type == Symbol.ENVIRONMENT)
      return T;
    if (type == BuiltInClass.ENVIRONMENT)
      return T;
    return super.typep(type);
  }

  public boolean isEmpty()
  {
    if (lastFunctionBinding != null)
      return false;
    if (vars != null)
      {
        for (Binding binding = vars; binding != null; binding = binding.next)
          if (!binding.specialp)
            return false;
      }
    return true;
  }

  public void bind(Symbol symbol, LispObject value)
  {
    vars = new Binding(symbol, value, vars);
  }

  public void rebind(Symbol symbol, LispObject value)
  {
    Binding binding = getBinding(symbol);
    binding.value = value;
  }

  public LispObject lookup(LispObject symbol)
  {
    Binding binding = vars;
    while (binding != null)
      {
        if (binding.symbol == symbol)
          return binding.value;
        binding = binding.next;
      }
    return null;
  }

  public Binding getBinding(LispObject symbol)
  {
    Binding binding = vars;
    while (binding != null)
      {
        if (binding.symbol == symbol)
          return binding;
        binding = binding.next;
      }
    return null;
  }

  // Function bindings.
  public void addFunctionBinding(LispObject name, LispObject value)
  {
    lastFunctionBinding =
      new FunctionBinding(name, value, lastFunctionBinding);
  }

  public LispObject lookupFunction(LispObject name)
    throws ConditionThrowable
  {
    FunctionBinding binding = lastFunctionBinding;
    if (name instanceof Symbol)
      {
        while (binding != null)
          {
            if (binding.name == name)
              return binding.value;
            binding = binding.next;
          }
        // Not found in environment.
        return name.getSymbolFunction();
      }
    if (name instanceof Cons)
      {
        while (binding != null)
          {
            if (binding.name.equal(name))
              return binding.value;
            binding = binding.next;
          }
      }
    return null;
  }

  public void addBlock(LispObject tag, LispObject block)
  {
    blocks = new Binding(tag, block, blocks);
  }

  public LispObject lookupBlock(LispObject symbol)
  {
    Binding binding = blocks;
    while (binding != null)
      {
        if (binding.symbol == symbol)
          return binding.value;
        binding = binding.next;
      }
    return null;
  }

  public void addTagBinding(LispObject tag, LispObject code)
  {
    tags = new Binding(tag, code, tags);
  }

  public Binding getTagBinding(LispObject tag)
  {
    Binding binding = tags;
    while (binding != null)
      {
        if (binding.symbol.eql(tag))
          return binding;
        binding = binding.next;
      }
    return null;
  }

  // Returns body with declarations removed.
  public LispObject processDeclarations(LispObject body)
    throws ConditionThrowable
  {
    while (body != NIL)
      {
        LispObject obj = body.car();
        if (obj instanceof Cons && ((Cons)obj).car == Symbol.DECLARE)
          {
            LispObject decls = ((Cons)obj).cdr;
            while (decls != NIL)
              {
                LispObject decl = decls.car();
                if (decl instanceof Cons && ((Cons)decl).car == Symbol.SPECIAL)
                  {
                    LispObject names = ((Cons)decl).cdr;
                    while (names != NIL)
                      {
                        Symbol var = checkSymbol(names.car());
                        declareSpecial(var);
                        names = ((Cons)names).cdr;
                      }
                  }
                decls = ((Cons)decls).cdr;
              }
            body = ((Cons)body).cdr;
          }
        else
          break;
      }
    return body;
  }

  public void declareSpecial(Symbol var)
  {
    vars = new Binding(var, null, vars);
    vars.specialp = true;
  }

    /** Return true if a symbol is declared special.
     *
     * If there is no binding in the current (lexical) environment,
     * the current dynamic environment (thread) is checked.
     */
  public boolean isDeclaredSpecial(LispObject var)
  {
    Binding binding = getBinding(var);
    return (binding != null) ? binding.specialp :
        (LispThread.currentThread().getSpecialBinding(var) != null);
  }

  @Override
  public String writeToString() throws ConditionThrowable
  {
    return unreadableString(Symbol.ENVIRONMENT);
  }

  // ### make-environment
  public static final Primitive MAKE_ENVIRONMENT =
    new Primitive("make-environment", PACKAGE_SYS, true,
                  "&optional parent-environment")
    {
      @Override
      public LispObject execute()
      {
        return new Environment();
      }
      @Override
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        if (arg == NIL)
          return new Environment();
        return new Environment(checkEnvironment(arg));
      }
    };

  // ### environment-add-macro-definition
  public static final Primitive ENVIRONMENT_ADD_MACRO_DEFINITION =
    new Primitive("environment-add-macro-definition", PACKAGE_SYS, true,
                  "environment name expander")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)
        throws ConditionThrowable
      {
        Environment env = checkEnvironment(first);
        LispObject name = second;
        LispObject expander = third;
        env.addFunctionBinding(name, expander);
        return env;
      }
    };

  // ### environment-add-function-definition
  public static final Primitive ENVIRONMENT_ADD_FUNCTION_DEFINITION =
    new Primitive("environment-add-function-definition", PACKAGE_SYS, true,
                  "environment name lambda-expression")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)
        throws ConditionThrowable
      {
        checkEnvironment(first).addFunctionBinding(second, third);
        return first;
      }
    };

  // ### environment-add-symbol-binding
  public static final Primitive ENVIRONMENT_ADD_SYMBOL_BINDING =
    new Primitive("environment-add-symbol-binding", PACKAGE_SYS, true,
                  "environment symbol value")
    {
      @Override
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)
        throws ConditionThrowable
      {
        checkEnvironment(first).bind(checkSymbol(second), third);
        return first;
      }
    };
    
  // ### empty-environment-p
  private static final Primitive EMPTY_ENVIRONMENT_P =
    new Primitive("empty-environment-p", PACKAGE_SYS, true, "environment")
    {
      @Override
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
          return checkEnvironment(arg).isEmpty() ? T : NIL;
      }
    };

  // ### environment-variables
  private static final Primitive ENVIRONMENT_VARS =
    new Primitive("environment-variables", PACKAGE_SYS, true, "environment")
    {
      @Override
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
            Environment env = checkEnvironment(arg);
            LispObject result = NIL;
            for (Binding binding = env.vars; binding != null; binding = binding.next)
              if (!binding.specialp)
                result = result.push(new Cons(binding.symbol, binding.value));
            return result.nreverse();
      }
    };
}
