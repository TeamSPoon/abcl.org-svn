/*
 * HashTable.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: HashTable.java,v 1.53 2005-11-08 14:49:41 piso Exp $
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

public abstract class HashTable extends LispObject
{
    protected final LispObject rehashSize;
    protected final LispObject rehashThreshold;

    // The rounded product of the capacity and the load factor. When the number
    // of elements exceeds the threshold, the implementation calls rehash().
    protected int threshold;

    protected static final float loadFactor = 0.75f;

    // Array containing the actual key-value mappings.
    protected HashEntry[] buckets;

    // The number of key-value pairs.
    protected int count;

    protected HashTable(int size, LispObject rehashSize,
                        LispObject rehashThreshold)
    {
        this.rehashSize = rehashSize;
        this.rehashThreshold = rehashThreshold;
        buckets = new HashEntry[size];
        threshold = (int) (size * loadFactor);
    }

    protected static int calculateInitialCapacity(int size)
    {
        int capacity = 1;
        while (capacity < size)
            capacity <<= 1;
        return capacity;
    }

    public final LispObject getRehashSize()
    {
        return rehashSize;
    }

    public final LispObject getRehashThreshold()
    {
        return rehashThreshold;
    }

    public int getSize()
    {
        return buckets.length;
    }

    public int getCount()
    {
        return count;
    }

    public abstract Symbol getTest();

    public LispObject typeOf()
    {
        return Symbol.HASH_TABLE;
    }

    public LispObject classOf()
    {
        return BuiltInClass.HASH_TABLE;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.HASH_TABLE)
            return T;
        if (type == BuiltInClass.HASH_TABLE)
            return T;
        return super.typep(type);
    }

    public boolean equalp(LispObject obj) throws ConditionThrowable
    {
        if (this == obj)
            return true;
        if (obj instanceof HashTable) {
            HashTable ht = (HashTable) obj;
            if (count != ht.count)
                return false;
            if (getTest() != ht.getTest())
                return false;
            LispObject entries = ENTRIES();
            while (entries != NIL) {
                LispObject entry = entries.car();
                LispObject key = entry.car();
                LispObject value = entry.cdr();
                if (!value.equalp(ht.get(key)))
                    return false;
                entries = entries.cdr();
            }
            return true;
        }
        return false;
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject parts = NIL;
        for (int i = 0; i < buckets.length; i++) {
            HashEntry e = buckets[i];
            while (e != null) {
                parts = parts.push(new Cons("KEY [bucket " + i + "]", e.key));
                parts = parts.push(new Cons("VALUE", e.value));
                e = e.next;
            }
        }
        return parts.nreverse();
    }

    public synchronized void clear()
    {
        for (int i = buckets.length; i-- > 0;)
            buckets[i] = null;
        count = 0;
    }

    // gethash key hash-table &optional default => value, present-p
    public synchronized LispObject gethash(LispObject key)
        throws ConditionThrowable
    {
        LispObject value = get(key);
        final LispObject presentp;
        if (value == null)
            value = presentp = NIL;
        else
            presentp = T;
        return LispThread.currentThread().setValues(value, presentp);
    }

    // gethash key hash-table &optional default => value, present-p
    public synchronized LispObject gethash(LispObject key,
                                           LispObject defaultValue)
        throws ConditionThrowable
    {
        LispObject value = get(key);
        final LispObject presentp;
        if (value == null) {
            value = defaultValue;
            presentp = NIL;
        } else
            presentp = T;
        return LispThread.currentThread().setValues(value, presentp);
    }

    public synchronized LispObject puthash(LispObject key, LispObject newValue)
        throws ConditionThrowable
    {
        put(key, newValue);
        return newValue;
    }

    // remhash key hash-table => generalized-boolean
    public synchronized LispObject remhash(LispObject key)
        throws ConditionThrowable
    {
        // A value in a Lisp hash table can never be null, so...
        return remove(key) != null ? T : NIL;
    }

    public String writeToString() throws ConditionThrowable
    {
        FastStringBuffer sb = new FastStringBuffer(getTest().writeToString());
        sb.append(" hash table, ");
        sb.append(count);
        if (count == 1)
            sb.append(" entry");
        else
            sb.append(" entries");
        sb.append(", ");
        sb.append(buckets.length);
        sb.append(" buckets");
        return unreadableString(sb.toString());
    }

    public abstract LispObject get(LispObject key);

    public abstract void put(LispObject key, LispObject value)
        throws ConditionThrowable;

    public abstract LispObject remove(LispObject key) throws ConditionThrowable;

    protected abstract void rehash();

    // Returns a list of (key . value) pairs.
    public LispObject ENTRIES()
    {
        LispObject list = NIL;
        for (int i = buckets.length; i-- > 0;) {
            HashEntry e = buckets[i];
            while (e != null) {
                list = new Cons(new Cons(e.key, e.value), list);
                e = e.next;
            }
        }
        return list;
    }

    public LispObject MAPHASH(LispObject function) throws ConditionThrowable
    {
        for (int i = buckets.length; i-- > 0;) {
            HashEntry e = buckets[i];
            while (e != null) {
                function.execute(e.key, e.value);
                e = e.next;
            }
        }
        return NIL;
    }

    protected static class HashEntry
    {
        LispObject key;
        LispObject value;
        HashEntry next;

        HashEntry(LispObject key, LispObject value)
        {
            this.key = key;
            this.value = value;
        }
    }

    // For EQUALP hash tables.
    public int psxhash()
    {
        long result = 2062775257; // Chosen at random.
        result = mix(result, count);
        result = mix(result, getTest().sxhash());
        return (int) (result & 0x7fffffff);
    }
}
