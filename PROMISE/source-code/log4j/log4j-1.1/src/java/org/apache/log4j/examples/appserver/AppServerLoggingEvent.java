package org.apache.log4j.examples.appserver;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

/**
 *  Represents logging events for application servers. When an affirmative
 *  logging decision is made, a <code>LoggingEvent</code> instance is
 *  created.  This sub-class of <code>LoggingEvent</code> provides
 *	 for a few additional attributes:
 *  <p>
 *  <ul>
 *  <li>hostname of event originator
 *  <li>server name of event originator
 *  <li>component name of event originator
 *  <li>component version of event originator
 *  </ul>
 *  <p>
 *  This class is used to add some application server related attributes
 *  to those attributes already provided by log4j.  It is instanciated by 
 *  <code>AppServerCategory.forcedLog</code> methods.
 *
 *  @author Paul Glezen
 */
public class AppServerLoggingEvent extends LoggingEvent
                                   implements java.io.Serializable 
{
	/** Hostname of machine from which this event originated. */
	public String hostname;

	/** Name of component from which this event originated.   */
	public String component;

	/** Name of server from which this event originated.  This
	    attribute may be more germane to CORBA/EJB environments. */
   public String server;

	/** Version name of server/component. */
	public String version;


  /**
   *  Instantiate an AppServerLoggingEvent from the supplied parameters.
   *  <p>
	*  All the fields of
   *  <code>AppServerLoggingEvent</code> are obtained from
	*  <code>AppServerCategory</code> or filled when actually needed.
   *  <p>
   *  @param fqnOfCategoryClass The Category class name.
   *  @param category  The category of this event.
   *  @param priority  The priority of this event.
   *  @param message   The message of this event.
   *  @param throwable The throwable of this event.  
	*/
	public AppServerLoggingEvent( String    fqnOfCategoryClass, 
				      AppServerCategory  category, 
				      Priority  priority, 
				      Object    message, 
				      Throwable throwable) 
	{
		super( fqnOfCategoryClass,
		       category,
		       priority,
		       message,
		       throwable );

		hostname  = category.getHostname();
		component = category.getComponent();
		server    = category.getServer();
		version   = category.getVersion();
	}
}
