/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.grails.ide.eclipse.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.grails.ide.eclipse.core.GrailsCoreActivator;

/**
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.2.0
 */
public class GrailsBuildSettingsHelper {

	public static String getBaseDir(IProject project) {
		if (project == null) {
			return null;
		}
		File baseDir = null;
		if (project.getRawLocation() != null) {
			baseDir = project.getRawLocation().toFile();
		}
		else if (project.getLocation() != null) {
			baseDir = project.getLocation().toFile();
		}
		try {
			return baseDir.getCanonicalPath();
		}
		catch (IOException e) {
			throw new GrailsBuildSettingsException(e);
		}
	}

	/**
	 * Retrieve contents of "application.properties" for a given Grails project.
	 * 
	 * @param project
	 */
	public static Properties getApplicationProperties(IProject project) {
		IFile eclipsePropFile = project.getFile("application.properties");
		File propFile = eclipsePropFile.getLocation().toFile();
		return getProps(propFile);
	}

	/**
	 * Save application.properties for a given Grails project
	 */
	public static void saveApplicationProperties(IProject project, Properties props) throws Exception {
		IFile eclipsePropFile = project.getFile("application.properties");
		File propFile = eclipsePropFile.getLocation().toFile();
		OutputStream out = new FileOutputStream(propFile);
		try {
			props.store(out, "Modified by GGTS");
		} finally {
			try {
				out.close();
			} catch (Throwable e) {
			}
			eclipsePropFile.refreshLocal(IResource.DEPTH_ZERO, null);
		}
	}

	/**
	 * Retrieve contents of "application.properties" for a given Grails project.
	 * 
	 * @param project
	 */
	public static Properties getApplicationProperties(File project) {
		File propFile = new File(project, "application.properties");
		return getProps(propFile);
	}
	
	private static Properties getProps(File propFile) {
		Properties props = new Properties();
		FileInputStream input = null;
		try {
			props.load(input = new FileInputStream(propFile));
			return props;
		} catch (FileNotFoundException e) {
			//Ignore... happens more or less expectedly when called on non-grails projects.
			return props;
		} catch (Exception e) {
			GrailsCoreActivator.log(e);
			return props; // Note this props is probably empty or only partially read.
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Persist contents of "application.properties" for a given Grails project.
	 * 
	 * @param project
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void setApplicationProperties(IProject project, Properties props) {
		IFile eclipsePropFile = project.getFile("application.properties");
		File propFile = eclipsePropFile.getLocation().toFile();
		FileOutputStream stream = null;
		try {
			try {
				props.store(stream = new FileOutputStream(propFile), "# grails meta data");
			} catch (Exception e) {
				GrailsCoreActivator.log("trouble saving grails meta data", e);
			}
		} finally {
			if (stream!=null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Read properties from file, change or add one property, then persist the change to the file.
	 */
	public static void setApplicationProperty(IProject project, String key, String value) {
		Properties props = getApplicationProperties(project);
		props.put(key, value);
		setApplicationProperties(project, props);
	}
}
