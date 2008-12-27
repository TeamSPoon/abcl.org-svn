/*
 * JavaObject.java
 *
 * Copyright (C) 2002-2005 Peter Graves
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

public final class JavaObject extends LispObject
{
    private final Object obj;

    public JavaObject(Object obj)
    {
        this.obj = obj;
    }

    @Override
    public LispObject typeOf()
    {
        return Symbol.JAVA_OBJECT;
    }

    @Override
    public LispObject classOf()
    {
        return BuiltInClass.JAVA_OBJECT;
    }

    @Override
    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.JAVA_OBJECT)
            return T;
        if (type == BuiltInClass.JAVA_OBJECT)
            return T;
        return super.typep(type);
    }

    public final Object getObject()
    {
        return obj;
    }

    @Override
    public Object javaInstance()
    {
        return obj;
    }

    @Override
    public Object javaInstance(Class c)
    {
        return javaInstance();
    }

    public static final Object getObject(LispObject o)
        throws ConditionThrowable
    {
        try {
	    return ((JavaObject)o).obj;
        }
        catch (ClassCastException e) {
            type_error(o, Symbol.JAVA_OBJECT);
            // Not reached.
            return null;
        }
    }

    @Override
    public final boolean equal(LispObject other)
    {
        if (this == other)
            return true;
        if (other instanceof JavaObject)
            return (obj == ((JavaObject)other).obj);
        return false;
    }

    @Override
    public final boolean equalp(LispObject other)
    {
        return equal(other);
    }

    @Override
    public int sxhash()
    {
        return obj == null ? 0 : (obj.hashCode() & 0x7ffffff);
    }

    @Override
    public String writeToString() throws ConditionThrowable
    {
        if (obj instanceof ConditionThrowable)
            return obj.toString();
        final FastStringBuffer sb =
            new FastStringBuffer(Symbol.JAVA_OBJECT.writeToString());
        sb.append(' ');
        sb.append(obj == null ? "null" : obj.getClass().getName());
        return unreadableString(sb.toString());
    }

    // ### describe-java-object
    private static final Primitive DESCRIBE_JAVA_OBJECT =
        new Primitive("describe-java-object", PACKAGE_JAVA, true)
    {
        @Override
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (!(first instanceof JavaObject))
                return type_error(first, Symbol.JAVA_OBJECT);
            final Stream stream;
            try {
                stream = (Stream) second;
            }
            catch (ClassCastException e) {
                return type_error(second, Symbol.STREAM);
            }
            final JavaObject javaObject = (JavaObject) first;
            final Object obj = javaObject.getObject();
            final FastStringBuffer sb =
                new FastStringBuffer(javaObject.writeToString());
            sb.append(" is an object of type ");
            sb.append(Symbol.JAVA_OBJECT.writeToString());
            sb.append(".");
            sb.append(System.getProperty("line.separator"));
            sb.append("The wrapped Java object is ");
            if (obj == null) {
                sb.append("null.");
            } else {
                sb.append("an ");
                final Class c = obj.getClass();
                String className = c.getName();
                if (c.isArray()) {
                    sb.append("array of ");
                    if (className.startsWith("[L") && className.endsWith(";")) {
                        className = className.substring(1, className.length() - 1);
                        sb.append(className);
                        sb.append(" objects");
                    } else if (className.startsWith("[") && className.length() > 1) {
                        char descriptor = className.charAt(1);
                        final String type;
                        switch (descriptor) {
                            case 'B': type = "bytes"; break;
                            case 'C': type = "chars"; break;
                            case 'D': type = "doubles"; break;
                            case 'F': type = "floats"; break;
                            case 'I': type = "ints"; break;
                            case 'J': type = "longs"; break;
                            case 'S': type = "shorts"; break;
                            case 'Z': type = "booleans"; break;
                            default:
                                type = "unknown type";
                        }
                        sb.append(type);
                    }
                    sb.append(" with ");
                    final int length = java.lang.reflect.Array.getLength(obj);
                    sb.append(length);
                    sb.append(" element");
                    if (length != 1)
                        sb.append('s');
                    sb.append('.');
                } else {
                    sb.append("instance of ");
                    sb.append(className);
                    sb.append(':');
                    sb.append(System.getProperty("line.separator"));
                    sb.append("  \"");
                    sb.append(obj.toString());
                    sb.append('"');
                }
            }
            stream._writeString(sb.toString());
            return LispThread.currentThread().nothing();
        }
    };
}
