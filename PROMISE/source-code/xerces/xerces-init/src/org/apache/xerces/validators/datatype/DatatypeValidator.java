/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.validators.datatype;

import java.util.Hashtable;
import java.util.Locale;

/**
 *
 * DataTypeValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * Note: there is no support for facets in this API, since we are trying to convince
 * W3C to remove facets from the data type spec.
 *
 * @author Ted Leung
 * @version
 */

public interface DatatypeValidator {
	
	public static final String MININCLUSIVE = "minInclusive";
	public static final String MINEXCLUSIVE = "minExclusive";
	public static final String MAXINCLUSIVE = "maxInclusive";
	public static final String MAXEXCLUSIVE = "maxExclusive";
	public static final String MINABSOLUTEVALUE = "minAbsoluteValue";
	public static final String MAXABSOLUTEVALUE = "maxAbsoluteValue";
	
	public static final String PRECISION = "precision";
	public static final String SCALE = "scale";
	
	public static final String LENGTH = "length";
	public static final String MAXLENGTH = "maxLength";
	public static final String ENUMERATION = "enumeration";
	public static final String LITERAL = "literal";
	public static final String LEXICALREPRESENTATION = "lexicalRepresentation";
	public static final String LEXICAL = "lexical";
	public static final String ENCODING = "encoding";
	
	/**
     * validate that a string matches a datatype
     *
     * validate returns true or false depending on whether the string content is an
     * instance of the data type represented by this validator.
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  invalid according to the rules for the validators
     */
	public void validate(String content) throws InvalidDatatypeValueException;
	
	/**
	 * set the facets for this datatype
	 *
	 * setFacets is responsible for ensuring that the supplied facets do not contradict each
	 * other
	 *
	 * @param facets A hashtable where facet names are keys and facet values are stored
	 *        in the hashtable.  Usually facet values are strings, except for the 
	 *        enumeration facet.  The value for this facet is a Vector of strings, one
	 *        per enumeration value
	 *
	 * @exception throws UnknownFacetException
	 * @exception throws IllegalFacetException
	 * @exception throws IllegalFacetValueException
	 */
	public void setFacets(Hashtable facets) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException; 
	
	/**
	 * set the base type for this datatype
	 *
	 * @param base the validator for this type's base type
	 *
	 */
	public void setBasetype(DatatypeValidator base);


	
    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale);
}
