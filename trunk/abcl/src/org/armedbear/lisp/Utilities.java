/*
 * Utilities.java
 *
 * Copyright (C) 2003-2007 Peter Graves
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class Utilities extends Lisp
{
    public static final boolean isPlatformUnix;
    public static final boolean isPlatformWindows;

    static {
        String osName = System.getProperty("os.name");
        isPlatformUnix = osName.startsWith("Linux") ||
            osName.startsWith("Mac OS X") || osName.startsWith("Darwin") ||
            osName.startsWith("Solaris") ||
            osName.startsWith("SunOS") || osName.startsWith("AIX") ||
            osName.startsWith("FreeBSD") || osName.startsWith("OpenBSD") ||
            osName.startsWith("NetBSD");
        isPlatformWindows = osName.startsWith("Windows");
    }

    public static boolean isFilenameAbsolute(String filename)
    {
        final int length = filename.length();
        if (length > 0) {
            char c0 = filename.charAt(0);
            if (c0 == '\\' || c0 == '/')
                return true;
            if (length > 2) {
                if (isPlatformWindows) {
                    // Check for drive letter.
                    char c1 = filename.charAt(1);
                    if (c1 == ':') {
                        if (c0 >= 'a' && c0 <= 'z')
                            return true;
                        if (c0 >= 'A' && c0 <= 'Z')
                            return true;
                    }
                } else {
                    // Unix.
                    if (filename.equals("~") || filename.startsWith("~/"))
                        return true;
                }
            }
        }
        return false;
    }

    public static File getFile(Pathname pathname)
    {
        return getFile(pathname,
                       coerceToPathname(Symbol.DEFAULT_PATHNAME_DEFAULTS.symbolValue()));
    }

    public static File getFile(Pathname pathname, Pathname defaultPathname)

    {
        Pathname merged =
            Pathname.mergePathnames(pathname, defaultPathname, NIL);
        String namestring = merged.getNamestring();
        if (namestring != null)
            return new File(namestring);
        error(new FileError("Pathname has no namestring: " + merged.writeToString(),
                             merged));
        // Not reached.
        return null;
    }

    public static Pathname getDirectoryPathname(File file)

    {
        try {
            String namestring = file.getCanonicalPath();
            if (namestring != null && namestring.length() > 0) {
                if (namestring.charAt(namestring.length() - 1) != File.separatorChar)
                    namestring = namestring.concat(File.separator);
            }
            return new Pathname(namestring);
        }
        catch (IOException e) {
            error(new LispError(e.getMessage()));
            // Not reached.
            return null;
        }
    }
    
    public static byte[] getZippedZipEntryAsByteArray(ZipFile zipfile,
                                                      String entryName,
                                                      String subEntryName) 

  {
      ZipEntry entry = zipfile.getEntry(entryName);
      
      ZipInputStream stream = null;
      try {
          stream = new ZipInputStream(zipfile.getInputStream(entry));
      } 
      catch (IOException e) {
          Lisp.error(new FileError("Failed to open '" + entryName + "' in zipfile '"
                                   + zipfile + "': " + e.getMessage()));
      }
      //  XXX Cache the zipEntries somehow
      do {
          try { 
              entry = stream.getNextEntry();
          } catch (IOException e){
              Lisp.error(new FileError("Failed to seek for '" + subEntryName 
                                       + "' in '" 
                                       + zipfile.getName() + ":" + entryName + ".:"
                                       + e.getMessage()));
          }
      } while (!entry.getName().equals(subEntryName));
      
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int count;
        byte buf[] = new byte[1024];
        try {
            while ((count = stream.read(buf, 0, buf.length)) != -1) {
                buffer.write(buf, 0, count);
            }
        } catch (java.io.IOException e) {
          Lisp.error(new FileError("Failed to read compressed '"
                                   + subEntryName 
                                   + "' in '" 
                                   + zipfile.getName() + ":" + entryName + ":"
                                   + e.getMessage()));
        }
        return buffer.toByteArray();
    }
    
    public static InputStream getZippedZipEntryAsInputStream(ZipFile zipfile,
                                                             String entryName,
                                                             String subEntryName) 

  {
        return 
            new ByteArrayInputStream(Utilities
                                     .getZippedZipEntryAsByteArray(zipfile, entryName, 
                                                                   subEntryName));
  }
}

