/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.internal.content.XMLContentDescriber;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A content describer for detecting the name of the top-level project element.
 * <p>
 * If project top level element is found 
 *      then if:
 *          target subelements are found returns VALID
 *          default attribute is found returns VALID
 *      else:
 *          returns INDETERMINATE
 * else
 *      returns INVALID
 * </p>
 * 
 * @since 3.1
 */
public final class AntBuildfileContentDescriber extends XMLContentDescriber implements IExecutableExtension {

	/* (Intentionally not included in javadoc)
	 * Determines the validation status for the given contents.
	 * 
	 * @param contents the contents to be evaluated
	 * @return one of the following:<ul>
	 * <li><code>VALID</code></li>,
	 * <li><code>INVALID</code></li>,
	 * <li><code>INDETERMINATE</code></li>
	 * </ul>
	 * @throws IOException
	 */
	private int checkCriteria(InputSource contents) throws IOException {
		AntHandler antHandler = new AntHandler();
		try {
			if (!antHandler.parseContents(contents)) {
				return INVALID;
            }
		} catch (SAXException e) {
			// we may be handed any kind of contents... it is normal we fail to parse
			return INVALID;
		} catch (ParserConfigurationException e) {
			// some bad thing happened - force this describer to be disabled
			String message = InternalCoreAntMessages.getString("AntBuildfileContentDescriber.0"); //$NON-NLS-1$
            AntCorePlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, 0, message, e));
			throw new RuntimeException(message);
		}
		// Check to see if we matched our criteria.
		if (antHandler.hasRootProjectElement()) {
			if (antHandler.hasProjectDefaultAttribute() || antHandler.hasTargetElement()) {
                //project and default attribute or project and target element(s)
                return VALID;
            }
            //only a top level project element...maybe an Ant buildfile
            return INDETERMINATE;
        } 
			
		return INVALID;
	}

	/* (Intentionally not included in javadoc)
	 * @see IContentDescriber#describe(InputStream, IContentDescription)
	 */
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description) == INVALID) {
			return INVALID;
		}
		// super.describe will have consumed some chars, need to rewind		
		contents.reset();
		// Check to see if we matched our criteria.		
		return checkCriteria(new InputSource(contents));
	}

	/* (Intentionally not included in javadoc)
	 * @see IContentDescriber#describe(Reader, IContentDescription)
	 */
	public int describe(Reader contents, IContentDescription description) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description) == INVALID) {
			return INVALID;
		}
		// super.describe will have consumed some chars, need to rewind
		contents.reset();
		// Check to see if we matched our criteria.
		return checkCriteria(new InputSource(contents));
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    }
}