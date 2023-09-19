package org.apache.log4j.examples.appserver;

import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.PatternLayout;

/**
 *  Extends <code>PatternLayout</code> to create subclass
 *  instances of <code>AppServerPatternParser</code> for
 *  parsing pattern strings.
 *
 *  @see AppServerPatternParser
 *  @see org.apache.log4j.helpers.PatternParser
 *  @see org.apache.log4j.PatternLayout
 *
 *  @author Paul Glezen
 */
public class AppServerPatternLayout extends PatternLayout {
  
  public AppServerPatternLayout() {
    this(DEFAULT_CONVERSION_PATTERN);
  }

  public AppServerPatternLayout(String pattern) {
    super(pattern);
  }
    
  public PatternParser createPatternParser(String pattern) {
    PatternParser result;
    if ( pattern == null )
      result = new AppServerPatternParser(DEFAULT_CONVERSION_PATTERN);
    else
      result = new AppServerPatternParser (pattern);
    
    return result;
  }
}
