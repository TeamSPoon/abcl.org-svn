/*
 * Load.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: Load.java,v 1.103 2005-05-25 16:59:09 piso Exp $
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class Load extends Lisp
{
    public static final LispObject load(String filename)
        throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        return load(new Pathname(filename),
                    filename,
                    _LOAD_VERBOSE_.symbolValue(thread) != NIL,
                    _LOAD_PRINT_.symbolValue(thread) != NIL,
                    true);
    }

    public static final LispObject load(Pathname pathname,
                                        final String filename,
                                        boolean verbose,
                                        boolean print,
                                        boolean ifDoesNotExist)
        throws ConditionThrowable
    {
        File file = null;
        boolean isFile = false;
        if (Utilities.isFilenameAbsolute(filename)) {
            file = new File(filename);
            if (file != null) {
                isFile = file.isFile();
                if (!isFile) {
                    String extension = getExtension(filename);
                    if (extension == null) {
                        // No extension specified. Try appending ".lisp".
                        file = new File(filename.concat(".lisp"));
                        isFile = file.isFile();
                    }
                }
            }
        } else {
            // Filename is not absolute.
            String dir =
                Pathname.coerceToPathname(_DEFAULT_PATHNAME_DEFAULTS_.symbolValue()).getNamestring();
            file = new File(dir, filename);
            if (file != null) {
                isFile = file.isFile();
                if (!isFile) {
                    String extension = getExtension(filename);
                    if (extension == null) {
                        // No extension specified. Try appending ".lisp".
                        file = new File(dir, filename.concat(".lisp"));
                        isFile = file.isFile();
                    }
                }
            }
        }
        if (!isFile) {
            if (ifDoesNotExist)
                return signal(new FileError("File not found: " + filename,
                                            pathname));
            else
                return NIL;
        }
        String truename = filename;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            truename = file.getCanonicalPath();
        }
        catch (FileNotFoundException e) {
            if (ifDoesNotExist)
                return signal(new FileError("File not found: " + filename,
                                            pathname));
            else
                return NIL;
        }
        catch (IOException e) {
            return signal(new LispError(e.getMessage()));
        }
        try {
            return loadFileFromStream(null, truename,
                                      new Stream(in, Symbol.CHARACTER),
                                      verbose, print, false);
        }
        catch (FaslVersionMismatch e) {
            StringBuffer sb = new StringBuffer("Incorrect fasl version: ");
            sb.append(truename);
            return signal(new SimpleError(sb.toString()));
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                return signal(new LispError(e.getMessage()));
            }
        }
    }

    public static final LispObject loadSystemFile(String filename)
        throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        return loadSystemFile(filename,
                              _LOAD_VERBOSE_.symbolValue(thread) != NIL,
                              _LOAD_PRINT_.symbolValue(thread) != NIL,
                              false);
    }

    public static final LispObject loadSystemFile(String filename, boolean auto)
        throws ConditionThrowable
    {
        LispThread thread = LispThread.currentThread();
        if (auto) {
            SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
            thread.bindSpecial(_READTABLE_,
                               Readtable._STANDARD_READTABLE_.symbolValue(thread));
            thread.bindSpecial(_PACKAGE_, PACKAGE_CL_USER);
            try {
                return loadSystemFile(filename,
                                      _AUTOLOAD_VERBOSE_.symbolValue(thread) != NIL,
                                      _LOAD_PRINT_.symbolValue(thread) != NIL,
                                      auto);
            }
            finally {
                thread.lastSpecialBinding = lastSpecialBinding;
            }
        } else {
            return loadSystemFile(filename,
                                  _LOAD_VERBOSE_.symbolValue(thread) != NIL,
                                  _LOAD_PRINT_.symbolValue(thread) != NIL,
                                  auto);
        }
    }

    public static final LispObject loadSystemFile(final String filename,
                                                  boolean verbose,
                                                  boolean print,
                                                  boolean auto)
        throws ConditionThrowable
    {
        final int ARRAY_SIZE = 2;
        String[] candidates = new String[ARRAY_SIZE];
        String extension = getExtension(filename);
        if (extension == null) {
            // No extension specified.
            candidates[0] = filename + '.' + COMPILE_FILE_TYPE;
            candidates[1] = filename.concat(".lisp");
        } else if (extension.equals(".abcl")) {
            candidates[0] = filename;
            candidates[1] =
                filename.substring(0, filename.length() - 5).concat(".lisp");
        } else
            candidates[0] = filename;
        InputStream in = null;
        Pathname pathname = null;
        String truename = null;
        for (int i = 0; i < ARRAY_SIZE; i++) {
            String s = candidates[i];
            if (s == null)
                break;
            final String dir = Site.getLispHome();
            if (dir != null) {
                File file = new File(dir, s);
                if (file.isFile()) {
                    try {
                        in = new FileInputStream(file);
                        truename = file.getCanonicalPath();
                    }
                    catch (IOException e) {
                        in = null;
                    }
                }
            } else {
                URL url = Lisp.class.getResource(s);
                if (url != null) {
                    try {
                        in = url.openStream();
                        if ("jar".equals(url.getProtocol()))
                            pathname = new Pathname(url);
                        truename = getPath(url);
                    }
                    catch (IOException e) {
                        in = null;
                    }
                }
            }
            if (in != null) {
                final LispThread thread = LispThread.currentThread();
                final SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
                thread.bindSpecial(_WARN_ON_REDEFINITION_, NIL);
                try {
                    return loadFileFromStream(pathname, truename,
                                              new Stream(in, Symbol.CHARACTER),
                                              verbose, print, auto);
                }
                catch (FaslVersionMismatch e) {
                    StringBuffer sb =
                        new StringBuffer("; Incorrect fasl version: ");
                    sb.append(truename);
                    System.err.println(sb.toString());
                }
                finally {
                    thread.lastSpecialBinding = lastSpecialBinding;
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        return signal(new LispError(e.getMessage()));
                    }
                }
            }
        }
        return signal(new LispError("file not found: " + filename));
    }

    // ### *fasl-source*
    // internal symbol
    public static final Symbol _FASL_SOURCE_ =
        internSpecial("*FASL-SOURCE*", PACKAGE_SYS, NIL);

    // ### *fasl-version*
    // internal symbol
    private static final Symbol _FASL_VERSION_ =
        exportConstant("*FASL-VERSION*", PACKAGE_SYS, new Fixnum(27));

    // ### *fasl-anonymous-package*
    // internal symbol
    public static final Symbol _FASL_ANONYMOUS_PACKAGE_ =
        internSpecial("*FASL-ANONYMOUS-PACKAGE*", PACKAGE_SYS, NIL);

    // ### init-fasl
    private static final Primitive INIT_FASL =
        new Primitive("init-fasl", PACKAGE_SYS, true, "&key version")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first == Keyword.VERSION) {
                if (second.eql(_FASL_VERSION_.getSymbolValue())) {
                    // OK
                    final LispThread thread = LispThread.currentThread();
                    Readtable readtable = new Readtable(NIL);
                    readtable.setDispatchMacroCharacter('#', ':', FASL_SHARP_COLON);
                    thread.bindSpecial(_READTABLE_, readtable);
                    thread.bindSpecial(_FASL_ANONYMOUS_PACKAGE_, NIL);
                    thread.bindSpecial(_FASL_SOURCE_, NIL);
                    return T;
                }
            }
            throw new FaslVersionMismatch(second);
        }
    };

    private static final LispObject loadFileFromStream(LispObject pathname,
                                                       String truename,
                                                       Stream in,
                                                       boolean verbose,
                                                       boolean print,
                                                       boolean auto)
        throws ConditionThrowable
    {
        long start = System.currentTimeMillis();
        final LispThread thread = LispThread.currentThread();
        final SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
        // "LOAD binds *READTABLE* and *PACKAGE* to the values they held before
        // loading the file."
        thread.bindSpecialToCurrentValue(_READTABLE_);
        thread.bindSpecialToCurrentValue(_PACKAGE_);
        int loadDepth = Fixnum.getValue(_LOAD_DEPTH_.symbolValue(thread));
        thread.bindSpecial(_LOAD_DEPTH_, new Fixnum(++loadDepth));
        // Compiler policy.
        thread.bindSpecialToCurrentValue(_SPEED_);
        thread.bindSpecialToCurrentValue(_SPACE_);
        thread.bindSpecialToCurrentValue(_SAFETY_);
        thread.bindSpecialToCurrentValue(_DEBUG_);
        final String prefix = getLoadVerbosePrefix(loadDepth);
        try {
            if (pathname == null && truename != null)
                pathname = Pathname.parseNamestring(truename);
            thread.bindSpecial(_LOAD_PATHNAME_,
                               pathname != null ? pathname : NIL);
            thread.bindSpecial(_LOAD_TRUENAME_,
                               pathname != null ? pathname : NIL);
            if (verbose) {
                Stream out = getStandardOutput();
                out.freshLine();
                out._writeString(prefix);
                out._writeString(auto ? " Autoloading " : " Loading ");
                out._writeString(truename != null ? truename : "stream");
                out._writeLine(" ...");
                out._finishOutput();
                LispObject result = loadStream(in, print);
                long elapsed = System.currentTimeMillis() - start;
                out.freshLine();
                out._writeString(prefix);
                out._writeString(auto ? " Autoloaded " : " Loaded ");
                out._writeString(truename != null ? truename : "stream");
                out._writeString(" (");
                out._writeString(String.valueOf(((float)elapsed)/1000));
                out._writeLine(" seconds)");
                out._finishOutput();
                return result;
            } else
                return loadStream(in, print);
        }
        finally {
            thread.lastSpecialBinding = lastSpecialBinding;
        }
    }

    public static String getLoadVerbosePrefix(int loadDepth)
    {
        StringBuffer sb = new StringBuffer(";");
        for (int i = loadDepth - 1; i-- > 0;)
            sb.append(' ');
        return sb.toString();
    }

    // ### fasl-sharp-colon
    public static final DispatchMacroFunction FASL_SHARP_COLON =
        new DispatchMacroFunction("sharp-colon", PACKAGE_SYS, false,
                                  "stream sub-char numarg")
    {
        public LispObject execute(Stream stream, char c, int n)
            throws ConditionThrowable
        {
            LispThread thread = LispThread.currentThread();
            Symbol symbol = (Symbol) stream.readSymbol();
            LispObject pkg = _FASL_ANONYMOUS_PACKAGE_.symbolValue(thread);
            if (pkg == NIL) {
                thread.bindSpecial(_FASL_ANONYMOUS_PACKAGE_,
                                   pkg = new Package());
            }
            symbol = ((Package)pkg).intern(symbol.getName());
            symbol.setPackage(NIL);
            return symbol;
        }
    };

    private static final LispObject loadStream(Stream in, boolean print)
        throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
        thread.bindSpecial(_LOAD_STREAM_, in);
        SpecialBinding sourcePositionBinding =
            new SpecialBinding(_SOURCE_POSITION_, Fixnum.ZERO,
                               thread.lastSpecialBinding);
        thread.lastSpecialBinding = sourcePositionBinding;
        try {
            final Environment env = new Environment();
            while (true) {
                sourcePositionBinding.value = new Fixnum(in.getOffset());
                LispObject obj = in.read(false, EOF, true);
                if (obj == EOF)
                    break;
                LispObject result = eval(obj, env, thread);
                if (print) {
                    Stream out = getStandardOutput();
                    out._writeLine(result.writeToString());
                    out._finishOutput();
                }
            }
            return T;
        }
        finally {
            thread.lastSpecialBinding = lastSpecialBinding;
        }
    }

    // Returns extension including leading '.'
    private static final String getExtension(String filename)
    {
        int index = filename.lastIndexOf('.');
        if (index < 0)
            return null;
        if (index < filename.lastIndexOf(File.separatorChar))
            return null; // Last dot was in path part of filename.
        return filename.substring(index);
    }

    private static final String getPath(URL url)
    {
        if (url != null) {
            String path = url.getPath();
            if (path != null) {
                if (Utilities.isPlatformWindows()) {
                    if (path.length() > 0 && path.charAt(0) == '/')
                        path = path.substring(1);
                }
                return path;
            }
        }
        return null;
    }

    // ### %load filespec verbose print if-does-not-exist => generalized-boolean
    private static final Primitive _LOAD =
        new Primitive("%load", PACKAGE_SYS, false,
                      "filespec verbose print if-does-not-exist")
    {
        public LispObject execute(LispObject filespec, LispObject verbose,
                                  LispObject print, LispObject ifDoesNotExist)
            throws ConditionThrowable
        {
            if (filespec instanceof Stream) {
                if (((Stream)filespec).isOpen()) {
                    LispObject pathname;
                    if (filespec instanceof FileStream)
                        pathname = ((FileStream)filespec).getPathname();
                    else
                        pathname = NIL;
                    String truename;
                    if (pathname instanceof Pathname)
                        truename = ((Pathname)pathname).getNamestring();
                    else
                        truename = null;
                    return loadFileFromStream(pathname,
                                              truename,
                                              (Stream) filespec,
                                              verbose != NIL,
                                              print != NIL,
                                              false);
                }
                // If stream is closed, fall through...
            }
            Pathname pathname = Pathname.coerceToPathname(filespec);
            return load(pathname,
                        pathname.getNamestring(),
                        verbose != NIL,
                        print != NIL,
                        ifDoesNotExist != NIL);
        }
    };

    // ### load-system-file
    private static final Primitive LOAD_SYSTEM_FILE =
        new Primitive("load-system-file", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            return loadSystemFile(arg.getStringValue(),
                                  _LOAD_VERBOSE_.symbolValue(thread) != NIL,
                                  _LOAD_PRINT_.symbolValue(thread) != NIL,
                                  false);
        }
    };

    private static class FaslVersionMismatch extends Error
    {
        private final LispObject version;

        public FaslVersionMismatch(LispObject version)
        {
            this.version = version;
        }

        public LispObject getVersion()
        {
            return version;
        }
    }
}
