/*****************************************************************************
 *                                                                           *
 *  This file is part of the BeanShell Java Scripting distribution.          *
 *  Documentation and updates may be found at http://www.beanshell.org/      *
 *                                                                           *
 *  Sun Public License Notice:                                               *
 *                                                                           *
 *  The contents of this file are subject to the Sun Public License Version  *
 *  1.0 (the "License"); you may not use this file except in compliance with *
 *  the License. A copy of the License is available at http://www.sun.com    * 
 *                                                                           *
 *  The Original Code is BeanShell. The Initial Developer of the Original    *
 *  Code is Pat Niemeyer. Portions created by Pat Niemeyer are Copyright     *
 *  (C) 2000.  All Rights Reserved.                                          *
 *                                                                           *
 *  GNU Public License Notice:                                               *
 *                                                                           *
 *  Alternatively, the contents of this file may be used under the terms of  *
 *  the GNU Lesser General Public License (the "LGPL"), in which case the    *
 *  provisions of LGPL are applicable instead of those above. If you wish to *
 *  allow use of your version of this file only under the  terms of the LGPL *
 *  and not to allow others to use your version of this file under the SPL,  *
 *  indicate your decision by deleting the provisions above and replace      *
 *  them with the notice and other provisions required by the LGPL.  If you  *
 *  do not delete the provisions above, a recipient may use your version of  *
 *  this file under either the SPL or the LGPL.                              *
 *                                                                           *
 *  Patrick Niemeyer (pat@pat.net)                                           *
 *  Author of Learning Java, O'Reilly & Associates                           *
 *  http://www.pat.net/~pat/                                                 *
 *                                                                           *
 *****************************************************************************/

package bsh.classpath;

import java.net.*;
import java.util.*;
import java.io.*;
import bsh.BshClassManager;

/**
	One of the things BshClassLoader does is to address a deficiency in
	URLClassLoader that prevents us from specifying individual classes
	via URLs.
*/
public class BshClassLoader extends URLClassLoader 
{
	public BshClassLoader( URL [] bases ) {
		super(bases);
	}

	public BshClassLoader( BshClassPath bcp ) {
		super( bcp.getPathComponents() );
	}

	/**
		For use by children
	*/
	protected BshClassLoader() { 
		super( new URL [] { } );
	}

	public void addURL( URL url ) {
		super.addURL( url );
	}

	/**
		This modification allows us to reload classes which are in the 
		Java VM user classpath.  We search first rather than delegate to
		the parent classloader (or bootstrap path) first.
	*/
	public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        Class c = null;

		/*
			Check first for classes loaded through this loader.
			The VM will not allow a class to be loaded twice.
		*/
        c = findLoadedClass(name);
		if ( c != null )
			return c;

		/*
			Try to find the class using our classloading mechanism.
			Note: I wish we didn't have to catch the exception here... slow
		*/
		try {
			c = findClass( name );
		} catch ( ClassNotFoundException e ) { }

		/*
			Checks parent or bootstrap path 
			unfortunately also tries findClass() again if those fail
			also we don't use parent... right?
		if ( c == null )
			return super.loadClass( name, resolve );
		*/
		if ( c == null )
			throw new ClassNotFoundException("here in loaClass");

		if ( resolve )
			resolveClass( c );

		return c;
	}

	/**
		Find the correct source for the class...

		Try designated loader if any
		Try our URLClassLoader paths if any
		Try base loader if any
		Try system ???
	*/
	// add some caching for not found classes?
	public Class findClass( String name ) throws ClassNotFoundException {

		// Should we try to load the class ourselves or delegate?
		// look for overlay loader

		ClassLoader cl = 	
			BshClassManager.getClassManager().getLoaderForClass( name );

		Class c;

		// If there is a designated loader and it's not us delegate to it
		if ( cl != null && cl != this )
			try {
				return cl.loadClass( name );
			} catch ( ClassNotFoundException e ) {
				throw new ClassNotFoundException(
					"Designated loader could not find class: "+e );
			}

		// Let URLClassLoader try any paths it may have
		if ( getURLs().length > 0 )
			try {
				return super.findClass(name);
			} catch ( ClassNotFoundException e ) { }


		// If there is a baseLoader and it's not us delegate to it
		BshClassManager bcm = BshClassManager.getClassManager();
		cl = bcm.getBaseLoader();

		if ( cl != null && cl != this )
			try {
				return cl.loadClass( name );
			} catch ( ClassNotFoundException e ) { }
		
		// Try system loader
		return bcm.plainClassForName( name );
	}

	/*
		The superclass does something like this

        c = findLoadedClass(name);
        if null
            try
                if parent not null
                    c = parent.loadClass(name, false);
                else
                    c = findBootstrapClass(name);
            catch ClassNotFoundException 
                c = findClass(name);
	*/

}
