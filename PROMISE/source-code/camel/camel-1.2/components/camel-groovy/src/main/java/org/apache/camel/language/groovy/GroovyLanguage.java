/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.language.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.apache.camel.spi.Language;
import org.apache.camel.Expression;

/**
 * @version $Revision: 1.1 $
 */
public class GroovyLanguage implements Language  {

    public static GroovyExpression groovy(String expression) {
        return new GroovyLanguage().createExpression(expression);
    }

    public GroovyExpression createPredicate(String expression) {
        return createExpression(expression);
    }

    public GroovyExpression createExpression(String expression) {
        Class<Script> scriptType = parseExpression(expression);
        return new GroovyExpression(scriptType, expression);
    }

    protected Class<Script> parseExpression(String expression) {
        return new GroovyClassLoader().parseClass(expression);
    }
}
