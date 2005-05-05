/*
 * create_new_file.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: create_new_file.java,v 1.4 2005-05-05 14:25:40 piso Exp $
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

// ### create-new-file
public final class create_new_file extends Primitive
{
    private create_new_file()
    {
        super("create-new-file", PACKAGE_SYS, false, "pathname");
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        Pathname pathname = Pathname.coerceToPathname(arg);
        String namestring = pathname.getNamestring();
        if (namestring == null)
            signal(new FileError("Pathname has no namestring: " + pathname.writeToString(),
                                 pathname));
        try {
            return new File(namestring).createNewFile() ? T : NIL;
        }
        catch (IOException e) {
            return signal(new StreamError(null, e));
        }
    }

    private static final Primitive CREATE_NEW_FILE = new create_new_file();
}
