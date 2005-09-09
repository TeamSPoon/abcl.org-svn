/*
 * LogicalPathname.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: LogicalPathname.java,v 1.11 2005-09-09 19:36:38 piso Exp $
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

import java.util.HashMap;
import java.util.StringTokenizer;

public final class LogicalPathname extends Pathname
{
    private static final HashMap map = new HashMap();

    public LogicalPathname(String host, String rest) throws ConditionThrowable
    {
        this.host = new SimpleString(host);

        // "The device component of a logical pathname is always :UNSPECIFIC;
        // no other component of a logical pathname can be :UNSPECIFIC."
        device = Keyword.UNSPECIFIC;

        int semi = rest.lastIndexOf(';');
        if (semi >= 0) {
            // Directory.
            String d = rest.substring(0, semi + 1);
            directory = parseDirectory(d);
            rest = rest.substring(semi + 1);
        }

        int dot = rest.indexOf('.');
        if (dot >= 0) {
            String n = rest.substring(0, dot);
            if (n.equals("*"))
                name = Keyword.WILD;
            else
                name = new SimpleString(n.toUpperCase());
            rest = rest.substring(dot + 1);
            dot = rest.indexOf('.');
            if (dot >= 0) {
                String t = rest.substring(0, dot);
                if (t.equals("*"))
                    type = Keyword.WILD;
                else
                    type = new SimpleString(t.toUpperCase());
                // What's left is the version.
                String v = rest.substring(dot + 1);
                if (v.equals("*"))
                    version = Keyword.WILD;
                else if (v.equals("NEWEST") || v.equals("newest"))
                    version = Keyword.NEWEST;
                else
                    version = PACKAGE_CL.intern("PARSE-INTEGER").execute(new SimpleString(v));
            } else {
                String t = rest;
                if (t.equals("*"))
                    type = Keyword.WILD;
                else
                    type = new SimpleString(t.toUpperCase());
            }
        } else {
            String n = rest;
            if (n.equals("*"))
                name = Keyword.WILD;
            else
                name = new SimpleString(n.toUpperCase());
        }
    }

    private static final LispObject parseDirectory(String d)
        throws ConditionThrowable
    {
        LispObject result;
        if (d.charAt(0) == ';') {
            result = new Cons(Keyword.RELATIVE);
            d = d.substring(1);
        } else
            result = new Cons(Keyword.ABSOLUTE);
        StringTokenizer st = new StringTokenizer(d, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            LispObject obj;
            if (token.equals("*"))
                obj = Keyword.WILD;
            else if (token.equals("**"))
                obj = Keyword.WILD_INFERIORS;
            else if (token.equals("..")) {
                if (result.car() instanceof AbstractString) {
                    result = result.cdr();
                    continue;
                }
                obj= Keyword.UP;
            } else
                obj = new SimpleString(token.toUpperCase());
            result = new Cons(obj, result);
        }
        return result.nreverse();
    }

    public LispObject typeOf()
    {
        return Symbol.LOGICAL_PATHNAME;
    }

    public LispObject classOf()
    {
        return BuiltInClass.LOGICAL_PATHNAME;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.LOGICAL_PATHNAME)
            return T;
        if (type == BuiltInClass.LOGICAL_PATHNAME)
            return T;
        return super.typep(type);
    }

    protected String getDirectoryNamestring() throws ConditionThrowable
    {
        FastStringBuffer sb = new FastStringBuffer();
        // "If a pathname is converted to a namestring, the symbols NIL and
        // :UNSPECIFIC cause the field to be treated as if it were empty. That
        // is, both NIL and :UNSPECIFIC cause the component not to appear in
        // the namestring." 19.2.2.2.3.1
        if (directory != NIL) {
            LispObject temp = directory;
            LispObject part = temp.car();
            if (part == Keyword.ABSOLUTE)
                ;
            else if (part == Keyword.RELATIVE)
                sb.append(';');
            else
                signal(new FileError("Unsupported directory component " + part.writeToString() + ".",
                                     this));
            temp = temp.cdr();
            while (temp != NIL) {
                part = temp.car();
                if (part instanceof AbstractString)
                    sb.append(part.getStringValue());
                else if (part == Keyword.WILD)
                    sb.append('*');
                else if (part == Keyword.UP)
                    sb.append("..");
                else
                    signal(new FileError("Unsupported directory component " + part.writeToString() + ".",
                                         this));
                sb.append(';');
                temp = temp.cdr();
            }
        }
        return sb.toString();
    }

    public String writeToString() throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        boolean printReadably = (_PRINT_READABLY_.symbolValue(thread) != NIL);
        boolean printEscape = (_PRINT_ESCAPE_.symbolValue(thread) != NIL);
        FastStringBuffer sb = new FastStringBuffer();
        if (printReadably || printEscape)
            sb.append("#P\"");
        sb.append(host.getStringValue());
        sb.append(':');
        if (directory != NIL)
            sb.append(getDirectoryNamestring());
        sb.append(name.getStringValue());
        if (type != NIL) {
            sb.append('.');
            if (type == Keyword.WILD)
                sb.append('*');
            else
                sb.append(type.getStringValue());
        }
        if (version.integerp()) {
            sb.append('.');
            int base = Fixnum.getValue(_PRINT_BASE_.symbolValue(thread));
            if (version instanceof Fixnum)
                sb.append(Integer.toString(((Fixnum)version).value, base).toUpperCase());
            else if (version instanceof Bignum)
                sb.append(((Bignum)version).value.toString(base).toUpperCase());
        } else if (version == Keyword.WILD) {
            sb.append('*');
        }
        if (printReadably || printEscape)
            sb.append('"');
        return sb.toString();
    }

    // ### %make-logical-pathname namestring => logical-pathname
    private static final Primitive _MAKE_LOGICAL_PATHNAME =
        new Primitive("%make-logical-pathname", PACKAGE_SYS, true,
                      "namestring")
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            // Check for a logical pathname host.
            String s = arg.getStringValue();
            String h = getHostString(s);
            if (h != null && Pathname.LOGICAL_PATHNAME_TRANSLATIONS.get(new SimpleString(h)) != null) {
                // A defined logical pathname host.
                return new LogicalPathname(h, s.substring(s.indexOf(':') + 1));
            }
            return signal(new TypeError("Logical namestring does not specify a host: \"" + s + '"'));
        }
    };
}
