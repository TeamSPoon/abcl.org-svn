/*
 * assq.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: assq.java,v 1.3 2004-11-03 15:27:23 piso Exp $
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

// ### assq item alist => entry
public final class assq extends Primitive
{
    private assq()
    {
        super("assq", PACKAGE_EXT);
    }

    public LispObject execute(LispObject item, LispObject alist)
        throws ConditionThrowable
    {
        while (alist != NIL) {
            LispObject cons = alist.car();
            if (cons instanceof Cons) {
                if (cons.car() == item)
                    return cons;
            } else if (cons != NIL)
                return signal(new TypeError(cons, "list"));
            alist = alist.cdr();
        }
        return NIL;
    }

    private static final assq ASSQ = new assq();
}
