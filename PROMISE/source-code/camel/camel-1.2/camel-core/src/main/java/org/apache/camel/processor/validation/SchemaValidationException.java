/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.processor.validation;

import java.util.List;

import org.xml.sax.SAXParseException;

import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;

/**
 * A Schema validation exception occurred
 * 
 * @version $Revision: $
 */
public class SchemaValidationException extends ValidationException {
    private final Object schema;
    private final List<SAXParseException> fatalErrors;
    private final List<SAXParseException> errors;
    private final List<SAXParseException> warnings;

    public SchemaValidationException(Exchange exchange, Object schema, List<SAXParseException> fatalErrors,
                                     List<SAXParseException> errors, List<SAXParseException> warnings) {
        super(exchange, message(schema, fatalErrors, errors, warnings));
        this.schema = schema;
        this.fatalErrors = fatalErrors;
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Returns the schema that failed
     * 
     * @return the schema that failed
     */
    public Object getSchema() {
        return schema;
    }

    /**
     * Returns the validation errors
     * 
     * @return the validation errors
     */
    public List<SAXParseException> getErrors() {
        return errors;
    }

    /**
     * Returns the fatal validation errors
     * 
     * @return the fatal validation errors
     */
    public List<SAXParseException> getFatalErrors() {
        return fatalErrors;
    }

    /**
     * Returns the validation warnings
     * 
     * @return the validation warnings
     */
    public List<SAXParseException> getWarnings() {
        return warnings;
    }

    protected static String message(Object schema, List<SAXParseException> fatalErrors,
                                    List<SAXParseException> errors, List<SAXParseException> warnings) {
        StringBuffer buffer = new StringBuffer("Validation failed for: " + schema);
        if (!fatalErrors.isEmpty()) {
            buffer.append(" fatal errors: ");
            buffer.append(fatalErrors);
        }
        if (!errors.isEmpty()) {
            buffer.append(" errors: ");
            buffer.append(errors);
        }
        return buffer.toString();
    }
}
