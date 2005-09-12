/*
 * Pathname.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: Pathname.java,v 1.86 2005-09-12 23:30:03 piso Exp $
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

public class Pathname extends LispObject
{
    protected LispObject host = NIL;
    protected LispObject device = NIL;
    protected LispObject directory = NIL;
    protected LispObject name = NIL;

    // A string, NIL, :WILD or :UNSPECIFIC.
    protected LispObject type = NIL;

    // A positive integer, or NIL, :WILD, :UNSPECIFIC, or :NEWEST.
    protected LispObject version = NIL;

    private String namestring;

    protected Pathname()
    {
    }

    public Pathname(String s) throws ConditionThrowable
    {
        init(s);
    }

    public Pathname(URL url) throws ConditionThrowable
    {
        String protocol = url.getProtocol();
        if ("jar".equals(protocol)) {
            String s = url.getPath();
            if (s.startsWith("file:")) {
                int index = s.indexOf("!/");
                String container = s.substring(5, index);
                if (Utilities.isPlatformWindows()) {
                    if (container.length() > 0 && container.charAt(0) == '/')
                        container = container.substring(1);
                }
                device = new Pathname(container);
                s = s.substring(index + 1);
                Pathname p = new Pathname(s);
                directory = p.directory;
                name = p.name;
                type = p.type;
                return;
            }
        } else if ("file".equals(protocol)) {
            String s = url.getPath();
            if (s != null && s.startsWith("file:")) {
                init(s.substring(5));
                return;
            }
        }
        signal(new LispError("Unsupported URL: \"" + url.toString() + '"'));
    }

    private final void init(String s) throws ConditionThrowable
    {
        if (s == null)
            return;
        if (Utilities.isPlatformWindows())
            s = s.replace('/', '\\');
        // Jar file support.
        int bang = s.indexOf("!/");
        if (bang >= 0) {
            Pathname container = new Pathname(s.substring(0, bang));
            LispObject containerType = container.type;
            if (containerType instanceof AbstractString) {
                if (containerType.getStringValue().equalsIgnoreCase("jar")) {
                    device = container;
                    s = s.substring(bang + 1);
                    Pathname p = new Pathname(s);
                    directory = p.directory;
                    name = p.name;
                    type = p.type;
                    return;
                }
            }
        }
        if (Utilities.isPlatformUnix()) {
            if (s.equals("~"))
                s = System.getProperty("user.home").concat("/");
            else if (s.startsWith("~/"))
                s = System.getProperty("user.home").concat(s.substring(1));
        }
        namestring = s;
        if (s.equals(".") || s.equals("./")) {
            directory = new Cons(Keyword.RELATIVE);
            return;
        }
        if (s.equals("..") || s.equals("../")) {
            directory = list2(Keyword.RELATIVE, Keyword.BACK);
            return;
        }
        if (Utilities.isPlatformWindows()) {
            if (s.length() >= 2 && s.charAt(1) == ':') {
                device = new SimpleString(s.charAt(0));
                s = s.substring(2);
            }
        }
        String d = null;
        // Find last file separator char.
        if (Utilities.isPlatformWindows()) {
            for (int i = s.length(); i-- > 0;) {
                char c = s.charAt(i);
                if (c == '/' || c == '\\') {
                    d = s.substring(0, i + 1);
                    s = s.substring(i + 1);
                    break;
                }
            }
        } else {
            for (int i = s.length(); i-- > 0;) {
                if (s.charAt(i) == '/') {
                    d = s.substring(0, i + 1);
                    s = s.substring(i + 1);
                    break;
                }
            }
        }
        if (d != null) {
            if (s.equals("..")) {
                d = d.concat(s);
                s = "";
            }
            directory = parseDirectory(d);
        }
        if (s.startsWith(".")) {
            name = new SimpleString(s);
            return;
        }
        int index = s.lastIndexOf('.');
        String n = null;
        String t = null;
        if (index > 0) {
            n = s.substring(0, index);
            t = s.substring(index + 1);
        } else if (s.length() > 0)
            n = s;
        if (n != null) {
            if (n.equals("*"))
                name = Keyword.WILD;
            else
                name = new SimpleString(n);
        }
        if (t != null) {
            if (t.equals("*"))
                type = Keyword.WILD;
            else
                type = new SimpleString(t);
        }
    }

    private static final LispObject parseDirectory(String d)
        throws ConditionThrowable
    {
        if (d.equals("/") || (Utilities.isPlatformWindows() && d.equals("\\")))
            return new Cons(Keyword.ABSOLUTE);
        LispObject result;
        if (d.startsWith("/") || (Utilities.isPlatformWindows() && d.startsWith("\\")))
            result = new Cons(Keyword.ABSOLUTE);
        else
            result = new Cons(Keyword.RELATIVE);
        StringTokenizer st = new StringTokenizer(d, "/\\");
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
                obj = new SimpleString(token);
            result = new Cons(obj, result);
        }
        return result.nreverse();
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject parts = NIL;
        parts = parts.push(new Cons("HOST", host));
        parts = parts.push(new Cons("DEVICE", device));
        parts = parts.push(new Cons("DIRECTORY", directory));
        parts = parts.push(new Cons("NAME", name));
        parts = parts.push(new Cons("TYPE", type));
        parts = parts.push(new Cons("VERSION", version));
        return parts.nreverse();
    }

    public LispObject typeOf()
    {
        return Symbol.PATHNAME;
    }

    public LispObject classOf()
    {
        return BuiltInClass.PATHNAME;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.PATHNAME)
            return T;
        if (type == BuiltInClass.PATHNAME)
            return T;
        return super.typep(type);
    }

    public final LispObject getDevice()
    {
        return device;
    }

    public String getNamestring() throws ConditionThrowable
    {
        if (namestring != null)
            return namestring;
        if (name == NIL && type != NIL) {
            Debug.assertTrue(namestring == null);
            return null;
        }
        if (directory instanceof AbstractString)
            Debug.assertTrue(false);
        if (!validateDirectory(directory, false))
            return null;
        FastStringBuffer sb = new FastStringBuffer();
        if (host != NIL) {
            Debug.assertTrue(host instanceof AbstractString);
            sb.append(host.getStringValue());
            sb.append(':');
        }
        sb.append(getDirectoryNamestring());
        if (name instanceof AbstractString)
            sb.append(name.getStringValue());
        else if (name == Keyword.WILD)
            sb.append('*');
        if (type != NIL) {
            sb.append('.');
            if (type instanceof AbstractString)
                sb.append(type.getStringValue());
            else if (type == Keyword.WILD)
                sb.append('*');
            else
                Debug.assertTrue(false);
        }
        if (this instanceof LogicalPathname) {
            if (version.integerp()) {
                sb.append('.');
                int base = Fixnum.getValue(_PRINT_BASE_.symbolValue());
                if (version instanceof Fixnum)
                    sb.append(Integer.toString(((Fixnum)version).value, base).toUpperCase());
                else if (version instanceof Bignum)
                    sb.append(((Bignum)version).value.toString(base).toUpperCase());
            } else if (version == Keyword.WILD) {
                sb.append(".*");
            }
        }
        return namestring = sb.toString();
    }

    protected String getDirectoryNamestring() throws ConditionThrowable
    {
        FastStringBuffer sb = new FastStringBuffer();
        // "If a pathname is converted to a namestring, the symbols NIL and
        // :UNSPECIFIC cause the field to be treated as if it were empty. That
        // is, both NIL and :UNSPECIFIC cause the component not to appear in
        // the namestring." 19.2.2.2.3.1
        if (device == NIL)
            ;
        else if (device == Keyword.UNSPECIFIC)
            ;
        else if (device instanceof AbstractString) {
            sb.append(device.getStringValue());
            sb.append(':');
        } else if (device instanceof Pathname) {
            sb.append(((Pathname)device).getNamestring());
            sb.append("!");
        } else
            Debug.assertTrue(false);
        final char separatorChar;
        if (device instanceof Pathname)
            separatorChar = '/'; // Jar file.
        else
            separatorChar = File.separatorChar;
        if (directory != NIL) {
            LispObject temp = directory;
            LispObject part = temp.car();
            if (part == Keyword.ABSOLUTE)
                sb.append(separatorChar);
            else if (part == Keyword.RELATIVE)
                ;
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
                else if (part == Keyword.WILD_INFERIORS)
                    sb.append("**");
                else if (part == Keyword.UP)
                    sb.append("..");
                else
                    signal(new FileError("Unsupported directory component " + part.writeToString() + ".",
                                         this));
                sb.append(separatorChar);
                temp = temp.cdr();
            }
        }
        return sb.toString();
    }

    public boolean equal(LispObject obj) throws ConditionThrowable
    {
        if (this == obj)
            return true;
        if (obj instanceof Pathname) {
            Pathname p = (Pathname) obj;
            if (Utilities.isPlatformWindows()) {
                if (!host.equalp(p.host))
                    return false;
                if (!device.equalp(p.device))
                    return false;
                if (!directory.equalp(p.directory))
                    return false;
                if (!name.equalp(p.name))
                    return false;
                if (!type.equalp(p.type))
                    return false;
                // Ignore version component.
                //if (!version.equalp(p.version))
                //    return false;
            } else {
                // Unix.
                if (!host.equal(p.host))
                    return false;
                if (!device.equal(p.device))
                    return false;
                if (!directory.equal(p.directory))
                    return false;
                if (!name.equal(p.name))
                    return false;
                if (!type.equal(p.type))
                    return false;
                // Ignore version component.
                //if (!version.equal(p.version))
                //    return false;
            }
            return true;
        }
        return false;
    }

    public boolean equalp(LispObject obj) throws ConditionThrowable
    {
        return equal(obj);
    }

    public int sxhash()
    {
        return ((host.sxhash() ^
                 device.sxhash() ^
                 directory.sxhash() ^
                 name.sxhash() ^
                 type.sxhash() ^
                 version.sxhash()) & 0x7fffffff);
    }

    public String writeToString() throws ConditionThrowable
    {
        try {
            final LispThread thread = LispThread.currentThread();
            boolean printReadably = (_PRINT_READABLY_.symbolValue(thread) != NIL);
            boolean printEscape = (_PRINT_ESCAPE_.symbolValue(thread) != NIL);
            boolean useNamestring;
            String s = null;
            try {
                s = getNamestring();
            }
            catch (Throwable t) {}
            if (s != null) {
                useNamestring = true;
                if (printReadably) {
                    // We have a namestring. Check for non-NIL values of pathname
                    // components that can't be read from the namestring.
                    if (host != NIL || version != NIL) {
                        useNamestring = false;
                    } else if (Utilities.isPlatformWindows()) {
                        if (version != NIL)
                            useNamestring = false;
                    }
                }
            } else
                useNamestring = false;
            FastStringBuffer sb = new FastStringBuffer();
            if (useNamestring) {
                if (printReadably || printEscape)
                    sb.append("#P\"");
                final int limit = s.length();
                for (int i = 0; i < limit; i++) {
                    char c = s.charAt(i);
                    if (c == '\"' || c == '\\')
                        sb.append('\\');
                    sb.append(c);
                }
                if (printReadably || printEscape)
                    sb.append('"');
            } else {
                sb.append("#P(");
                if (host != NIL) {
                    sb.append(":HOST ");
                    sb.append(host.writeToString());
                    sb.append(' ');
                }
                if (device != NIL) {
                    sb.append(":DEVICE ");
                    sb.append(device.writeToString());
                    sb.append(' ');
                }
                if (directory != NIL) {
                    sb.append(":DIRECTORY ");
                    sb.append(directory.writeToString());
                    sb.append(" ");
                }
                if (name != NIL) {
                    sb.append(":NAME ");
                    sb.append(name.writeToString());
                    sb.append(' ');
                }
                if (type != NIL) {
                    sb.append(":TYPE ");
                    sb.append(type.writeToString());
                    sb.append(' ');
                }
                if (version != NIL) {
                    sb.append(":VERSION ");
                    sb.append(version.writeToString());
                    sb.append(' ');
                }
                if (sb.charAt(sb.length() - 1) == ' ')
                    sb.setLength(sb.length() - 1);
                sb.append(')');
            }
            return sb.toString();
        }
        catch (ConditionThrowable t) {
            return unreadableString("PATHNAME");
        }
    }

    // A logical host is represented as the string that names it.
    // (defvar *logical-pathname-translations* (make-hash-table :test 'equal))
    public static EqualHashTable LOGICAL_PATHNAME_TRANSLATIONS =
        new EqualHashTable(64, NIL, NIL);

    private static final Symbol _LOGICAL_PATHNAME_TRANSLATIONS_ =
        exportSpecial("*LOGICAL-PATHNAME-TRANSLATIONS*", PACKAGE_SYS,
                      LOGICAL_PATHNAME_TRANSLATIONS);

    public static Pathname parseNamestring(String s)
        throws ConditionThrowable
    {
        return new Pathname(s);
    }

    public static Pathname parseNamestring(AbstractString namestring)
        throws ConditionThrowable
    {
        // Check for a logical pathname host.
        String s = namestring.getStringValue();
        String h = getHostString(s);
        if (h != null && LOGICAL_PATHNAME_TRANSLATIONS.get(new SimpleString(h)) != null) {
            // A defined logical pathname host.
            return new LogicalPathname(h, s.substring(s.indexOf(':') + 1));
        }
        return new Pathname(s);
    }

    public static Pathname parseNamestring(AbstractString namestring,
                                           AbstractString host)
        throws ConditionThrowable
    {
        // Look for a logical pathname host in the namestring.
        String s = namestring.getStringValue();
        String h = getHostString(s);
        if (h != null) {
            if (!h.equals(host.getStringValue())) {
                signal(new LispError("Host in " + s +
                                     " does not match requested host " +
                                     host.getStringValue()));
                // Not reached.
                return null;
            }
            // Remove host prefix from namestring.
            s = s.substring(s.indexOf(':') + 1);
        }
        if (LOGICAL_PATHNAME_TRANSLATIONS.get(host) != null) {
            // A defined logical pathname host.
            return new LogicalPathname(host.getStringValue(), s);
        }
        signal(new LispError(host.writeToString() + " is not defined as a logical pathname host."));
        // Not reached.
        return null;
    }

    // "one or more uppercase letters, digits, and hyphens"
    protected static String getHostString(String s)
    {
        int colon = s.indexOf(':');
        if (colon >= 0)
            return s.substring(0, colon).toUpperCase();
        else
            return null;
    }

    public static Pathname coerceToPathname(LispObject arg)
        throws ConditionThrowable
    {
        if (arg instanceof Pathname)
            return (Pathname) arg;
        if (arg instanceof AbstractString)
            return parseNamestring((AbstractString)arg);
        if (arg instanceof FileStream)
            return ((FileStream)arg).getPathname();
        signalTypeError(arg, list4(Symbol.OR, Symbol.PATHNAME,
                                   Symbol.STRING, Symbol.FILE_STREAM));
        // Not reached.
        return null;
    }

    private static final void checkCaseArgument(LispObject arg)
        throws ConditionThrowable
    {
        if (arg != Keyword.COMMON && arg != Keyword.LOCAL)
            signalTypeError(arg, list3(Symbol.MEMBER, Keyword.COMMON,
                                       Keyword.LOCAL));
    }

    // ### %pathname-host
    private static final Primitive _PATHNAME_HOST =
        new Primitive("%pathname-host", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkCaseArgument(second);
            return coerceToPathname(first).host;
        }
    };

    // ### %pathname-device
    private static final Primitive _PATHNAME_DEVICE =
        new Primitive("%pathname-device", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkCaseArgument(second);
            return coerceToPathname(first).device;
        }
    };

    // ### %pathname-directory
    private static final Primitive _PATHNAME_DIRECTORY =
        new Primitive("%pathname-directory", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkCaseArgument(second);
            return coerceToPathname(first).directory;
        }
    };

    // ### %pathname-name
    private static final Primitive _PATHNAME_NAME =
        new Primitive("%pathname-name", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkCaseArgument(second);
            return coerceToPathname(first).name;
        }
    };

    // ### %pathname-type
    private static final Primitive _PATHNAME_TYPE =
        new Primitive("%pathname-type", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            checkCaseArgument(second);
            return coerceToPathname(first).type;
        }
    };

    // ### pathname-version
    private static final Primitive PATHNAME_VERSION =
        new Primitive("pathname-version", "pathname")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPathname(arg).version;
        }
    };

    // ### namestring
    // namestring pathname => namestring
    private static final Primitive NAMESTRING =
        new Primitive("namestring", "pathname")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Pathname pathname = coerceToPathname(arg);
            String namestring = pathname.getNamestring();
            if (namestring == null)
                signal(new SimpleError("Pathname has no namestring: " +
                                       pathname.writeToString()));
            return new SimpleString(namestring);
        }
    };

    // ### directory-namestring
    // directory-namestring pathname => namestring
    private static final Primitive DIRECTORY_NAMESTRING =
        new Primitive("directory-namestring", "pathname")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return new SimpleString(coerceToPathname(arg).getDirectoryNamestring());
        }
    };

    // ### pathname pathspec => pathname
    private static final Primitive PATHNAME =
        new Primitive("pathname", "pathspec")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPathname(arg);
        }
    };

    // ### coerce-to-pathname thing &optional host => pathname
    private static final Primitive COERCE_TO_PATHNAME =
        new Primitive("coerce-to-pathname", PACKAGE_SYS, true,
                      "thing &optional host")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return coerceToPathname(arg);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (second == NIL)
                return coerceToPathname(first);
            // FIXME Support other types for first argument (and verify that
            // hosts match).
            if (first instanceof AbstractString) {
                AbstractString namestring = (AbstractString) first;
                final AbstractString host;
                try {
                    host = (AbstractString) second;
                }
                catch (ClassCastException e) {
                    return signalTypeError(second, Symbol.STRING);
                }
                return parseNamestring(namestring, host);
            }
            return signal(new LispError("COERCE-TO-PATHNAME: unsupported case."));
        }
    };

    // ### make-pathname
    private static final Primitive MAKE_PATHNAME =
        new Primitive("make-pathname",
                      "&key host device directory name type version defaults case")
    {
        public LispObject execute(LispObject[] args)
            throws ConditionThrowable
        {
            return _makePathname(args);
        }
    };

    // Used by the #p reader.
    public static final Pathname makePathname(LispObject args)
        throws ConditionThrowable
    {
        return _makePathname(args.copyToArray());
    }

    private static final Pathname _makePathname(LispObject[] args)
        throws ConditionThrowable
    {
        if (args.length % 2 != 0)
            signal(new ProgramError("Odd number of keyword arguments."));
        Pathname p = new Pathname();
        Pathname defaults = null;
        boolean deviceSupplied = false;
        boolean nameSupplied = false;
        boolean typeSupplied = false;
        for (int i = 0; i < args.length; i += 2) {
            LispObject key = args[i];
            LispObject value = args[i+1];
            if (key == Keyword.HOST) {
                p.host = value;
            } else if (key == Keyword.DEVICE) {
                p.device = value;
                deviceSupplied = true;
            } else if (key == Keyword.DIRECTORY) {
                if (value instanceof AbstractString)
                    p.directory = list2(Keyword.ABSOLUTE, value);
                else if (value == Keyword.WILD)
                    p.directory = list2(Keyword.ABSOLUTE, Keyword.WILD);
                else
                    p.directory = value;
            } else if (key == Keyword.NAME) {
                p.name = value;
                nameSupplied = true;
            } else if (key == Keyword.TYPE) {
                p.type = value;
                typeSupplied = true;
            } else if (key == Keyword.VERSION) {
                p.version = value;
            } else if (key == Keyword.DEFAULTS) {
                defaults = coerceToPathname(value);
            } else if (key == Keyword.CASE) {
                ; // Ignored.
            }
        }
        if (defaults != null) {
            // Ignore host.
            p.directory = mergeDirectories(p.directory, defaults.directory);
            if (!deviceSupplied)
                p.device = defaults.device;
            if (!nameSupplied)
                p.name = defaults.name;
            if (!typeSupplied)
                p.type = defaults.type;
        }
        return p;
    }

    private static final boolean validateDirectory(LispObject obj,
                                                   boolean signalError)
        throws ConditionThrowable
    {
        LispObject temp = obj;
        while (temp != NIL) {
            LispObject first = temp.car();
            temp = temp.cdr();
            if (first == Keyword.ABSOLUTE || first == Keyword.WILD_INFERIORS) {
                LispObject second = temp.car();
                if (second == Keyword.UP || second == Keyword.BACK) {
                    if (signalError) {
                        FastStringBuffer sb = new FastStringBuffer();
                        sb.append(first.writeToString());
                        sb.append(" may not be followed immediately by ");
                        sb.append(second.writeToString());
                        sb.append('.');
                        signal(new FileError(sb.toString()));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // ### pathnamep
    private static final Primitive PATHNAMEP =
        new Primitive("pathnamep", "object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return arg instanceof Pathname ? T : NIL;
        }
    };

    // ### user-homedir-pathname
    // user-homedir-pathname &optional host => pathname
    private static final Primitive USER_HOMEDIR_PATHNAME =
        new Primitive("user-homedir-pathname", "&optional host")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            switch (args.length) {
                case 0: {
                    String s = System.getProperty("user.home");
                    // For compatibility with SBCL and ACL (and maybe other
                    // Lisps), we want the namestring of a directory to end
                    // with a file separator character.
                    if (!s.endsWith(File.separator))
                        s = s.concat(File.separator);
                    return new Pathname(s);
                }
                case 1:
                    return NIL;
                default:
                    return signal(new WrongNumberOfArgumentsException(this));
            }
        }
    };

    // ### list-directory
    private static final Primitive LIST_DIRECTORY =
        new Primitive("list-directory", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Pathname pathname = Pathname.coerceToPathname(arg);
            LispObject result = NIL;
            String s = pathname.getNamestring();
            if (s != null) {
                File f = new File(s);
                if (f.isDirectory()) {
                    File[] files = f.listFiles();
                    try {
                        for (int i = files.length; i-- > 0;) {
                            File file = files[i];
                            Pathname p;
                            if (file.isDirectory())
                                p = Utilities.getDirectoryPathname(file);
                            else
                                p = new Pathname(file.getCanonicalPath());
                            result = new Cons(p, result);
                        }
                    }
                    catch (IOException e) {
                        return signal(new FileError("Unable to list directory " + pathname.writeToString() + ".",
                                                    pathname));
                    }
                }
            }
            return result;
        }
    };

    public boolean isWild() throws ConditionThrowable
    {
        if (host == Keyword.WILD || host == Keyword.WILD_INFERIORS)
            return true;
        if (device == Keyword.WILD || device == Keyword.WILD_INFERIORS)
            return true;
        if (directory instanceof Cons) {
            if (memq(Keyword.WILD, directory))
                return true;
            if (memq(Keyword.WILD_INFERIORS, directory))
                return true;
        }
        if (name == Keyword.WILD || name == Keyword.WILD_INFERIORS)
            return true;
        if (type == Keyword.WILD || type == Keyword.WILD_INFERIORS)
            return true;
        if (version == Keyword.WILD || version == Keyword.WILD_INFERIORS)
            return true;
        return false;
    }

    // ### %wild-pathname-p
    private static final Primitive _WILD_PATHNAME_P =
        new Primitive("%wild-pathname-p", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Pathname pathname = Pathname.coerceToPathname(first);
            if (second == NIL)
                return pathname.isWild() ? T : NIL;
            if (second == Keyword.DIRECTORY) {
                if (pathname.directory instanceof Cons) {
                    if (memq(Keyword.WILD, pathname.directory))
                        return T;
                    if (memq(Keyword.WILD_INFERIORS, pathname.directory))
                        return T;
                }
                return NIL;
            }
            LispObject value;
            if (second == Keyword.HOST)
                value = pathname.host;
            else if (second == Keyword.DEVICE)
                value = pathname.device;
            else if (second == Keyword.NAME)
                value = pathname.name;
            else if (second == Keyword.TYPE)
                value = pathname.type;
            else if (second == Keyword.VERSION)
                value = pathname.version;
            else
                return signal(new ProgramError("Unrecognized keyword " +
                                               second.writeToString() + "."));
            if (value == Keyword.WILD || value == Keyword.WILD_INFERIORS)
                return T;
            else
                return NIL;
        }
    };

    // ### merge-pathnames
    private static final Primitive MERGE_PATHNAMES =
        new Primitive("merge-pathnames",
                      "pathname &optional default-pathname default-version")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Pathname pathname = coerceToPathname(arg);
            Pathname defaultPathname =
                coerceToPathname(_DEFAULT_PATHNAME_DEFAULTS_.symbolValue());
            LispObject defaultVersion = Keyword.NEWEST;
            return mergePathnames(pathname, defaultPathname, defaultVersion);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            Pathname pathname = coerceToPathname(first);
            Pathname defaultPathname =
                coerceToPathname(second);
            LispObject defaultVersion = Keyword.NEWEST;
            return mergePathnames(pathname, defaultPathname, defaultVersion);
        }
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third)
            throws ConditionThrowable
        {
            Pathname pathname = coerceToPathname(first);
            Pathname defaultPathname =
                coerceToPathname(second);
            LispObject defaultVersion = third;
            return mergePathnames(pathname, defaultPathname, defaultVersion);
        }
    };

    public static final Pathname mergePathnames(Pathname pathname,
                                                Pathname defaultPathname,
                                                LispObject defaultVersion)
        throws ConditionThrowable
    {
        if (pathname instanceof LogicalPathname || defaultPathname instanceof LogicalPathname)
            signal(new LispError("Bad place for a logical pathname."));
        Pathname p = new Pathname();
        if (pathname.host != NIL)
            p.host = pathname.host;
        else
            p.host = defaultPathname.host;
        if (pathname.device != NIL)
            p.device = pathname.device;
        else
            p.device = defaultPathname.device;
        p.directory =
            mergeDirectories(pathname.directory, defaultPathname.directory);
        if (pathname.name != NIL)
            p.name = pathname.name;
        else
            p.name = defaultPathname.name;
        if (pathname.type != NIL)
            p.type = pathname.type;
        else
            p.type = defaultPathname.type;
        if (pathname.version != NIL)
            p.version = pathname.version;
        else if (pathname.name instanceof AbstractString)
            p.version = defaultVersion;
        else if (defaultPathname.version != NIL)
            p.version = defaultPathname.version;
        else
            p.version = defaultVersion;
        return p;
    }

    private static final LispObject mergeDirectories(LispObject dir,
                                                     LispObject defaultDir)
        throws ConditionThrowable
    {
        if (dir == NIL)
            return defaultDir;
        if (dir.car() == Keyword.RELATIVE && defaultDir != NIL) {
            LispObject result = NIL;
            while (defaultDir != NIL) {
                result = new Cons(defaultDir.car(), result);
                defaultDir = defaultDir.cdr();
            }
            dir = dir.cdr(); // Skip :RELATIVE.
            while (dir != NIL) {
                result = new Cons(dir.car(), result);
                dir = dir.cdr();
            }
            LispObject[] array = result.copyToArray();
            for (int i = 0; i < array.length - 1; i++) {
                if (array[i] == Keyword.BACK) {
                    if (array[i+1] instanceof AbstractString || array[i+1] == Keyword.WILD) {
                        array[i] = null;
                        array[i+1] = null;
                    }
                }
            }
            result = NIL;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null)
                    result = new Cons(array[i], result);
            }
            return result;
        }
        return dir;
    }

    public static final LispObject truename(LispObject arg,
                                            boolean errorIfDoesNotExist)
        throws ConditionThrowable
    {
        final Pathname pathname = Pathname.coerceToPathname(arg);
        if (pathname.isWild())
            return signal(new FileError("Bad place for a wild pathname.",
                                        pathname));
        final Pathname defaultedPathname =
            mergePathnames(pathname,
                           Pathname.coerceToPathname(_DEFAULT_PATHNAME_DEFAULTS_.symbolValue()),
                           NIL);
        final String namestring = defaultedPathname.getNamestring();
        if (namestring == null)
            return signal(new FileError("Pathname has no namestring: " + defaultedPathname.writeToString(),
                                        defaultedPathname));
        final File file = new File(namestring);
        if (file.isDirectory())
            return Utilities.getDirectoryPathname(file);
        if (file.exists()) {
            try {
                return new Pathname(file.getCanonicalPath());
            }
            catch (IOException e) {
                return signal(new LispError(e.getMessage()));
            }
        }
        if (errorIfDoesNotExist) {
            StringBuffer sb = new StringBuffer("The file ");
            sb.append(defaultedPathname.writeToString());
            sb.append(" does not exist.");
            return signal(new FileError(sb.toString(), defaultedPathname));
        }
        return NIL;
    }

    // ### mkdir
    private static final Primitive MKDIR =
        new Primitive("mkdir", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            final Pathname pathname = Pathname.coerceToPathname(arg);
            if (pathname.isWild())
                signal(new FileError("Bad place for a wild pathname.", pathname));
            Pathname defaultedPathname =
                mergePathnames(pathname,
                               Pathname.coerceToPathname(_DEFAULT_PATHNAME_DEFAULTS_.symbolValue()),
                               NIL);
            File file = Utilities.getFile(defaultedPathname);
            return file.mkdir() ? T : NIL;
        }
    };

    // ### rename-file filespec new-name => defaulted-new-name, old-truename, new-truename
    public static final Primitive RENAME_FILE =
        new Primitive("rename-file", "filespec new-name")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            final Pathname filespec = (Pathname) truename(first, true);
            Pathname newName = coerceToPathname(second);
            if (newName.isWild())
                signal(new FileError("Bad place for a wild pathname.", newName));
            newName = mergePathnames(newName, filespec, NIL);
            File source = new File(filespec.getNamestring());
            File destination = new File(newName.getNamestring());
            if (Utilities.isPlatformWindows()) {
                if (destination.isFile())
                    destination.delete();
            }
            if (!source.renameTo(destination))
                return signal(new FileError("Unable to rename " +
                                            filespec.writeToString() +
                                            " to " + newName.writeToString() +
                                            "."));
            LispThread.currentThread().setValues(newName, filespec,
                                                 truename(newName, true));
            return newName;
        }
    };

    // ### file-namestring pathname => namestring
    private static final Primitive FILE_NAMESTRING =
        new Primitive("file-namestring", "pathname")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Pathname p = coerceToPathname(arg);
            StringBuffer sb = new StringBuffer();
            if (p.name instanceof AbstractString)
                sb.append(p.name.getStringValue());
            else if (p.name == Keyword.WILD)
                sb.append('*');
            else
                signal(new SimpleError("Pathname has no name component: " +
                                       p.writeToString() + "."));
            if (p.type instanceof AbstractString) {
                sb.append('.');
                sb.append(p.type.getStringValue());
            } else if (p.type == Keyword.WILD)
                sb.append(".*");
            return new SimpleString(sb);
        }
    };

    // ### host-namestring pathname => namestring
    private static final Primitive HOST_NAMESTRING =
        new Primitive("host-namestring", "pathname")
    {
        public LispObject execute(LispObject arg)
        {
            return NIL;
        }
    };

    static {
        try {
            LispObject obj = _DEFAULT_PATHNAME_DEFAULTS_.getSymbolValue();
            _DEFAULT_PATHNAME_DEFAULTS_.setSymbolValue(coerceToPathname(obj));
        }
        catch (Throwable t) {
            Debug.trace(t);
        }
    }
}
