/*
 * Environment.java
 *
 * Copyright (C) 2002-2003 Peter Graves
 * $Id: Environment.java,v 1.8 2003-11-19 02:39:17 piso Exp $
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

public final class Environment extends LispObject
{
    private Binding vars;
    private Binding functions;
    private Binding blocks;

    public Environment() {}

    public Environment(Environment parent)
    {
        if (parent != null) {
            this.vars = parent.vars;
            this.functions = parent.functions;
            this.blocks = parent.blocks;
        }
    }

    public boolean isEmpty()
    {
        return (vars == null && functions == null);
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
        while (binding != null) {
            if (binding.symbol == symbol)
                return binding.value;
            binding = binding.next;
        }
        return null;
    }

    public Binding getBinding(LispObject symbol)
    {
        Binding binding = vars;
        while (binding != null) {
            if (binding.symbol == symbol)
                return binding;
            binding = binding.next;
        }
        return null;
    }

    // Functional bindings.
    public void bindFunctional(Symbol symbol, LispObject value)
    {
        functions = new Binding(symbol, value, functions);
    }

    public LispObject lookupFunctional(LispObject symbol)
        throws ConditionThrowable
    {
        Binding binding = functions;
        while (binding != null) {
            if (binding.symbol == symbol)
                return binding.value;
            binding = binding.next;
        }
        // Not found in environment.
        return symbol.getSymbolFunction();
    }

    public void addBlock(LispObject tag, Block block)
    {
        blocks = new Binding(tag, block, blocks);
    }

    public Block lookupBlock(LispObject symbol)
    {
        Binding binding = blocks;
        while (binding != null) {
            if (binding.symbol == symbol)
                return (Block) binding.value;
            binding = binding.next;
        }
        return null;
    }

    // Returns body with declarations removed.
    public LispObject processDeclarations(LispObject body)
        throws ConditionThrowable
    {
        while (body != NIL) {
            LispObject obj = body.car();
            if (obj instanceof Cons && obj.car() == Symbol.DECLARE) {
                LispObject decls = obj.cdr();
                while (decls != NIL) {
                    LispObject decl = decls.car();
                    if (decl instanceof Cons && decl.car() == Symbol.SPECIAL) {
                        LispObject vars = decl.cdr();
                        while (vars != NIL) {
                            Symbol var = checkSymbol(vars.car());
                            declareSpecial(var);
                            vars = vars.cdr();
                        }
                    }
                    decls = decls.cdr();
                }
                body = body.cdr();
            } else
                break;
        }
        return body;
    }

    public void declareSpecial(Symbol var)
    {
            vars = new Binding(var, null, vars);
            vars.specialp = true;
    }

    public boolean isDeclaredSpecial(Symbol var)
    {
        Binding binding = getBinding(var);
        return binding != null ? binding.specialp : false;
    }

    public String toString()
    {
        return unreadableString("ENVIRONMENT");
    }
}
