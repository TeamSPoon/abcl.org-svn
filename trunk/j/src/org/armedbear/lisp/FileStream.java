/*
 * FileStream.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: FileStream.java,v 1.11 2004-03-10 01:56:15 piso Exp $
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public final class FileStream extends Stream
{
    private static final String lineSeparator = System.getProperty("line.separator");

    private final RandomAccessFile raf;
    private final Pathname pathname;
    private final int bytesPerUnit;

    public FileStream(Pathname pathname, String namestring,
                      LispObject elementType, LispObject direction,
                      LispObject ifExists)
        throws IOException
    {
        File file = new File(namestring);
        String mode = null;
        if (direction == Keyword.INPUT) {
            mode = "r";
            isInputStream = true;
        } else if (direction == Keyword.OUTPUT) {
            mode = "rw";
            isOutputStream = true;
        } else if (direction == Keyword.IO) {
            mode = "rw";
            isInputStream = true;
            isOutputStream = true;
        }
        Debug.assertTrue(mode != null);
        raf = new RandomAccessFile(file, mode);
        // ifExists is ignored unless we have an output stream.
        if (isOutputStream) {
            if (ifExists == Keyword.OVERWRITE)
                raf.seek(0);
            else if (ifExists == Keyword.APPEND)
                raf.seek(raf.length());
            else
                raf.setLength(0);
        }
        this.pathname = pathname;
        this.elementType = elementType;
        if (elementType == Symbol.CHARACTER || elementType == Symbol.BASE_CHAR) {
            isCharacterStream = true;
            bytesPerUnit = 1;
        } else {
            isBinaryStream = true;
            int width;
            try {
                width = Fixnum.getValue(elementType.cadr());
            }
            catch (ConditionThrowable t) {
                width = 8;
            }
            bytesPerUnit = width / 8;
        }
    }

    public LispObject typeOf()
    {
        return Symbol.FILE_STREAM;
    }

    public LispClass classOf()
    {
        return BuiltInClass.FILE_STREAM;
    }

    public LispObject typep(LispObject typeSpecifier) throws ConditionThrowable
    {
        if (typeSpecifier == Symbol.FILE_STREAM)
            return T;
        if (typeSpecifier == BuiltInClass.FILE_STREAM)
            return T;
        return super.typep(typeSpecifier);
    }

    public Pathname getPathname()
    {
        return pathname;
    }

    public LispObject listen() throws ConditionThrowable
    {
        try {
            return raf.getFilePointer() < raf.length() ? T : NIL;
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
            // Not reached.
            return NIL;
        }
    }

    public LispObject fileLength() throws ConditionThrowable
    {
        String namestring = pathname.getNamestring();
        if (namestring == null)
            return signal(new SimpleError("Pathname has no namestring: " +
                                          pathname + '.'));
        File file = new File(namestring);
        long length = file.length(); // in 8-bit bytes
        if (isCharacterStream)
            return number(length);
        // "For a binary file, the length is measured in units of the
        // element type of the stream."
        return number(length / bytesPerUnit);
    }

    // Returns -1 at end of file.
    protected int _readChar() throws ConditionThrowable
    {
        try {
            return raf.read();
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
            // Not reached.
            return -1;
        }
    }

    protected void _unreadChar(int n) throws ConditionThrowable
    {
        try {
            long pos = raf.getFilePointer();
            if (pos > 0)
                raf.seek(pos - 1);
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    protected boolean _charReady() throws ConditionThrowable
    {
        return true;
    }

    public void _writeChar(char c) throws ConditionThrowable
    {
        try {
            raf.write((byte)c);
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    public void _writeChars(char[] chars, int start, int end)
        throws ConditionThrowable
    {
        _writeString(new String(chars, start, end - start));
    }

    public void _writeString(String s) throws ConditionThrowable
    {
        try {
            raf.writeBytes(s);
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    public void _writeLine(String s) throws ConditionThrowable
    {
        try {
            raf.writeBytes(s);
            raf.writeBytes(lineSeparator);
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    // Reads an 8-bit byte.
    public int _readByte() throws ConditionThrowable
    {
        try {
            return raf.read(); // Reads an 8-bit byte.
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
            // Not reached.
            return -1;
        }
    }

    // Writes an 8-bit byte.
    public void _writeByte(int n) throws ConditionThrowable
    {
        try {
            raf.write((byte)n); // Writes an 8-bit byte.
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    public void _finishOutput() throws ConditionThrowable
    {
    }

    public void _clearInput() throws ConditionThrowable
    {
        try {
            raf.seek(raf.length());
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    protected long _getFilePosition() throws ConditionThrowable
    {
        try {
            long pos = raf.getFilePointer();
            return pos / bytesPerUnit;
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
            // Not reached.
            return -1;
        }
    }

    protected boolean _setFilePosition(LispObject arg) throws ConditionThrowable
    {
        try {
            long pos;
            if (arg == Keyword.START)
                pos = 0;
            else if (arg == Keyword.END)
                pos = raf.length();
            else {
                long n = Fixnum.getValue(arg); // FIXME arg might be a bignum
                pos = n * bytesPerUnit;
            }
            raf.seek(pos);
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
        return true;
    }

    public void _close() throws ConditionThrowable
    {
        try {
            raf.close();
            setOpen(false);
        }
        catch (IOException e) {
            signal(new StreamError(this, e));
        }
    }

    public String toString()
    {
        return unreadableString("FILE-STREAM");
    }

    // ### make-file-stream pathname element-type direction => stream
    private static final Primitive MAKE_FILE_STREAM =
        new Primitive("make-file-stream", PACKAGE_SYS, false,
                      "pathname element-type direction if-exists")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length != 4)
                signal(new WrongNumberOfArgumentsException(this));
            Pathname pathname = Pathname.coerceToPathname(args[0]);
            LispObject elementType = args[1];
            LispObject direction = args[2];
            LispObject ifExists = args[3];
            if (direction != Keyword.INPUT && direction != Keyword.OUTPUT &&
                direction != Keyword.IO)
                signal(new LispError("Direction must be :INPUT, :OUTPUT, or :IO."));
            String namestring = pathname.getNamestring();
            if (namestring == null)
                return NIL;
            try {
                return new FileStream(pathname, namestring, elementType,
                                      direction, ifExists);
            }
            catch (FileNotFoundException e) {
                return NIL;
            }
            catch (IOException e) {
                return signal(new StreamError(null, e));
            }
        }
    };
}
