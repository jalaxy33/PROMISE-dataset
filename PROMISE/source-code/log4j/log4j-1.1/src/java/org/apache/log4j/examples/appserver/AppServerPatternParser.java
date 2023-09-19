package org.apache.log4j.examples.appserver;

import org.apache.log4j.*;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 *  Extend PatternParser to recognize additional conversion characters  
 *  suitable for use by application servers.  
 *  <p>
 *  <ul>
 *  <li>s - server name
 *  <li>h - hostname
 *  <li>b - component name (c and C already in use)
 *  <li>v - version name
 *  </ul>
 *  <p>
 *  
 *  @see org.apache.log4j.examples.MyPatternLayout
 *  @see org.apache.log4j.helpers.PatternParser
 *  @see org.apache.log4j.PatternLayout
 *
 *  @author Paul Glezen
 */
public class AppServerPatternParser extends PatternParser 
{
	/** Set to 'h'.*/
	protected static final char HOSTNAME_CHAR  = 'h';
	/** Set to 's'.*/
	protected static final char SERVER_CHAR    = 's';
	/** Set to 'b'.*/
	protected static final char COMPONENT_CHAR = 'b';
	/** Set to 'v'.*/
	protected static final char VERSION_CHAR   = 'v';

	/**
	 *  Create a parser with the provided pattern.
	 *
	 *  @param pattern a formatting pattern to parse
	 */
	public AppServerPatternParser(String pattern) 
	{
		super(pattern);
	}
    
	/**
	 *  Decide, based on the format character, which subtype of
	 *  <code>Converter</code> to instanciate.  The converter is
	 *  then added to the converter list using the
	 *  <code>addConverter</code> superclass method.
	 *  If the format character is not recognized, it is passed
	 *  to the superclass for interpretation.
	 */
   public void finalizeConverter(char formatChar) 
   {
		PatternConverter pc = null;
		switch( formatChar )
		{
			case HOSTNAME_CHAR:
				pc = new HostnamePatternConverter( formattingInfo );
				currentLiteral.setLength(0);
				addConverter( pc );
			   break;
			case SERVER_CHAR:
				pc = new ServerPatternConverter( formattingInfo );
				currentLiteral.setLength(0);
				addConverter( pc );
			   break;
			case COMPONENT_CHAR:
				pc = new ComponentPatternConverter( formattingInfo );
				currentLiteral.setLength(0);
				addConverter( pc );
			   break;
			case VERSION_CHAR:
				pc = new VersionPatternConverter( formattingInfo );
				currentLiteral.setLength(0);
				addConverter( pc );
			   break;
			default:
				super.finalizeConverter( formatChar );
      }
   }
  
   /**
	 *  The <code>AppServerPatternConverter</code> inner class factors
	 *  out all the duties of a converter except
	 */
	private static abstract class AppServerPatternConverter extends PatternConverter 
	{
		AppServerPatternConverter(FormattingInfo formattingInfo) 
		{
			super(formattingInfo);     
		}

		public String convert(LoggingEvent event) 
		{
			String result = null;
			AppServerLoggingEvent appEvent = null;

			if ( event instanceof AppServerLoggingEvent )
			{
				appEvent = (AppServerLoggingEvent) event;
				result = convert( appEvent );
			}
			return result;
		}

		public abstract String convert( AppServerLoggingEvent event );
	}

	private static class HostnamePatternConverter extends AppServerPatternConverter
	{
		HostnamePatternConverter( FormattingInfo formatInfo )
		{  super( formatInfo );  }

		public String convert( AppServerLoggingEvent event )
		{  return event.hostname;  }
	}

	private static class ServerPatternConverter extends AppServerPatternConverter
	{
		ServerPatternConverter( FormattingInfo formatInfo )
		{  super( formatInfo );  }

		public String convert( AppServerLoggingEvent event )
		{  return event.server;  }
	}

	private static class ComponentPatternConverter extends AppServerPatternConverter
	{
		ComponentPatternConverter( FormattingInfo formatInfo )
		{  super( formatInfo );  }

		public String convert( AppServerLoggingEvent event )
		{  return event.component;  }
	}

	private static class VersionPatternConverter extends AppServerPatternConverter
	{
		VersionPatternConverter( FormattingInfo formatInfo )
		{  super( formatInfo );  }

		public String convert( AppServerLoggingEvent event )
		{  return event.version;  }
	}
}
