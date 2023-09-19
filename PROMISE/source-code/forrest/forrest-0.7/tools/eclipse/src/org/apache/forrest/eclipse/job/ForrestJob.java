/*
* Copyright 2002-2005 The Apache Software Foundation or its licensors,
* as applicable.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.forrest.eclipse.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.forrest.eclipse.ForrestPlugin;
import org.apache.forrest.eclipse.preference.ForrestPreferences;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.Project;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

/**
 * An abstract Forrest job that provides utility methods for handling Eclipse based Forrest Jobs. 
 */
public abstract class ForrestJob extends Job {
	private static final String CONCURRENT_ANT_BUILDS = "Concurrent Ant builds are possible if you specify to build in a separate JRE.";

	private static final String VALIDATION_ERROR_MESSAGE = "Could not validate document";
	
	private static final String PLUGIN_INSTALL_FAILURE = "Unable to install required plugins";

	/**
	 * Logger for this class
	 */
	protected static final Logger logger = Logger.getLogger(ForrestJob.class);

	public static final int EXCEPTION_UNIDENTIFIED = 1001;

	public static final int EXCEPTION_VALIDATION = 1010;

	public static final int EXCEPTION_ANT_RUNNING = 1020;
	
	protected String workingDir;

	/**
	 * Create a new Forrest Job.
	 * @param name of the job
	 */
	public ForrestJob(String name) {
		super(name);

		ForrestPlugin plugin = ForrestPlugin.getDefault();
		URL urlPluginDir = plugin.getBundle().getEntry("/");
		Bundle bundle = Platform.getBundle(ForrestPlugin.ID);
		URL log4jConf = Platform.find(bundle, new Path("conf/log4j.xml"));
		DOMConfigurator.configure(log4jConf);
	}

	/**
	 * Run ant as a normal executable, that is wait for completion and retuen a
	 * status.
	 * 
	 * @param monitor -
	 *            the monitor to report execution progress
	 * @param cmdString -
	 *            the command string to pass to ant
	 * @return Status of the execution.
	 */
	protected IStatus runAnt(IProgressMonitor monitor, String cmdString) {
		if (logger.isDebugEnabled()) {
			logger.debug("runAnt(IProgressMonitor, String) - start");
		}

		IStatus status = Status.OK_STATUS;

		if (cmdString != null) {
			String fhome = ForrestPlugin.getDefault().getPluginPreferences()
					.getString(ForrestPreferences.FORREST_HOME);
			String antFile = fhome + File.separatorChar + "main"
					+ File.separatorChar + "forrest.build.xml";
			AntRunner runner = new AntRunner();
			runner.setCustomClasspath(getAntClasspath());
			runner.addBuildListener(AntBuildListener.class.getName());
			runner.setMessageOutputLevel(Project.MSG_INFO);
			try {
				runner.setBuildFileLocation(antFile);
				runner.setArguments(cmdString);
				logger.info("Running ANT with command string = " + cmdString);
				runner.run(monitor);
			} catch (CoreException e) {
				String userMsg;
				String errMsg = e.getMessage();
				if (errMsg.indexOf(VALIDATION_ERROR_MESSAGE) > 0) {
					String file = errMsg.substring(errMsg
							.indexOf(VALIDATION_ERROR_MESSAGE));
					userMsg = "Invalid XML Document: " + file;
					status = new Status(Status.ERROR, ForrestPlugin.ID,
							ForrestRunner.EXCEPTION_VALIDATION, userMsg, e);
				} else if (errMsg.endsWith(CONCURRENT_ANT_BUILDS)) {
					userMsg = "Can only run one Site operation at a time";
					status = new Status(Status.ERROR, ForrestPlugin.ID,
							EXCEPTION_ANT_RUNNING, userMsg, e);
				} else if (errMsg.indexOf(PLUGIN_INSTALL_FAILURE) >= 0) {
					userMsg = "Unable to install required server plugins";
					status = new Status(Status.ERROR, ForrestPlugin.ID,
							EXCEPTION_ANT_RUNNING, userMsg, e);
				} else {
					userMsg = "Forrest Server Exception";
					status = new Status(Status.ERROR, ForrestPlugin.ID,
							ForrestRunner.EXCEPTION_UNIDENTIFIED, userMsg, e);
				}
				logger.error("run(IProgressMonitor) - " + userMsg, e);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("runAnt(IProgressMonitor, String) - end");
		}
		return status;
	}
	
	private URL[] getAntClasspath() {
		Vector vctURLs = new Vector();
		String forrestHome = ForrestManager.FORREST_HOME;

		try {
			// FIXME: cache the classpath
            // add Forrest ANT jars
			vctURLs.add(new File(ForrestManager.FORREST_ANTTASK_CLASSES).toURL());
			ArrayList fileList = ForrestManager.getLibFiles(ForrestManager.FORREST_ANT);
			ListIterator itr = fileList.listIterator();
			while (itr.hasNext()) {
				vctURLs.add(((File)itr.next()).toURL());
			}
			
			// Add the path to the Plugin classes
			String className = this.getClass().getName();
			if (!className.startsWith("/")) {
				className = "/" + className;
			}
			className = className.replace('.', '/');			
			String classLocation = this.getClass().getClassLoader().getResource(className + ".class").toExternalForm();
			URL classURL = Platform.resolve(new URL(classLocation.substring(0, classLocation.indexOf(className))));
			vctURLs.add(classURL);
			
			// Add Plugin jars
			URL installURL = Platform.getBundle(ForrestPlugin.ID).getEntry("/");
			String location = Platform.resolve(installURL).getFile();
			fileList = ForrestManager.getLibFiles(location);
			itr = fileList.listIterator();
			while (itr.hasNext()) {
				vctURLs.add(((File)itr.next()).toURL());
			}
			
			// add Forrest jars
			File[] files = ForrestManager.getClasspathFiles();
			URL[] urls = new URL[files.length];
			for (int i = 0; i < files.length; i++) {
				urls[i] = files[i].toURL();
			}
			vctURLs.addAll(Arrays.asList(urls));
		} catch (FileNotFoundException e) {
			logger.error("getClasspathURLS()", e);

			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			logger.error("getClasspathURLS()", e);

			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("getClasspathURLS(), cannot resolve URL", e);
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		URL[] urls = new URL[vctURLs.size()];
		URL[] returnURLArray = (URL[]) vctURLs.toArray(urls);
		return returnURLArray;
	}
}