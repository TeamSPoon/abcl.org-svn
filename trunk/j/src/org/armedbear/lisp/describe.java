/*
 * describe.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: describe.java,v 1.3 2003-07-27 18:50:00 piso Exp $
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

public final class describe extends Lisp
{
    // ### describe
    // Need to support optional second argument specifying output stream.
    private static final Primitive DESCRIBE = new Primitive("describe") {
        public LispObject execute(LispObject[] args) throws Condition
        {
            if (args.length != 1)
                throw new WrongNumberOfArgumentsException(this);
            LispObject obj = args[0];
            StringBuffer sb = new StringBuffer(String.valueOf(obj));
            if (obj instanceof Symbol) {
                Symbol symbol = (Symbol) obj;
                LispObject pkg = symbol.getPackage();
                sb.append(" is an ");
                if (pkg == NIL)
                    sb.append("uninterned");
                else if (((Package)pkg).findExternalSymbol(symbol.getName()) == symbol)
                    sb.append("external");
                else
                    sb.append("internal");
                sb.append(" symbol");
                if (pkg != NIL) {
                    sb.append(" in the ");
                    sb.append(pkg.getName());
                    sb.append(" package");
                }
                sb.append(".\n");
                if (symbol.isSpecialVariable()) {
                    sb.append("It is a special variable; ");
                    LispObject value = symbol.getSymbolValue();
                    if (value != null) {
                        sb.append("its value is ");
                        sb.append(value);
                    } else
                        sb.append("no current value");
                    sb.append(".\n");
                }
                LispObject function = symbol.getSymbolFunction();
                if (function != null) {
                    sb.append("Its function binding is ");
                    sb.append(function);
                    sb.append(".\n");
                    if (function instanceof Function) {
                        LispObject arglist = ((Function)function).getArglist();
                        if (arglist != null) {
                            sb.append("Function argument list:\n  ");
                            if (arglist instanceof LispString)
                                sb.append(((LispString)arglist).getValue());
                            else
                                sb.append(arglist);
                            sb.append('\n');
                        }
                    }
                    LispObject documentation =
                        symbol.getFunctionDocumentation();
                    if (documentation instanceof LispString) {
                        sb.append("Function documentation:\n  ");
                        sb.append(((LispString)documentation).getValue());
                        sb.append('\n');
                    }
                }
                LispObject plist = symbol.getPropertyList();
                if (plist != NIL) {
                    sb.append("Its property list has these indicator/value pairs:\n");
                    LispObject[] array = plist.copyToArray();
                    for (int i = 0; i < array.length; i += 2) {
                        LispObject indicator = array[i];
                        LispObject value = array[i+1];
                        sb.append("  ");
                        sb.append(indicator);
                        sb.append(' ');
                        sb.append(value);
                        sb.append('\n');
                    }
                }
            }
            CharacterOutputStream out = getStandardOutput();
            out.freshLine();
            out.writeString(sb.toString());
            out.freshLine();
            return LispThread.currentThread().nothing();
        }
    };
}
