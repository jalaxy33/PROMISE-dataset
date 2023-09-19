/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.mediators.bsf;

import com.sun.phobos.script.javascript.RhinoScriptEngineFactory;
import com.sun.script.groovy.GroovyScriptEngineFactory;
import com.sun.script.jruby.JRubyScriptEngineFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.bsf.xml.XMLHelper;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.mozilla.javascript.Context;

import javax.activation.DataHandler;
import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Synapse mediator that calls a function in any scripting language supported by the BSF.
 * The ScriptMediator supports scripts specified in-line or those loaded through a registry
 * <p/>
 * <pre>
 *    &lt;script [key=&quot;entry-key&quot;]
 *      [function=&quot;script-function-name&quot;] language="javascript|groovy|ruby"&gt
 *      (text | xml)?
 *    &lt;/script&gt;
 * </pre>
 * <p/>
 * <p/>
 * The function is an optional attribute defining the name of the script function to call,
 * if not specified it defaults to a function named 'mediate'. The function takes a single
 * parameter which is the Synapse MessageContext. The function may return a boolean, if it
 * does not then true is assumed.
 */
public class ScriptMediator extends AbstractMediator {

    /**
     * The name of the variable made available to the scripting language to access the message
     */
    private static final String MC_VAR_NAME = "mc";

    /**
     * Name of the JavaScript language
     */
    private static final String JAVA_SCRIPT = "js";

    /**
     * The registry entry key for a script loaded from the registry
     * Handle both static and dynamic(Xpath) Keys
     */
    private Value key;
    /**
     * The language of the script code
     */
    private String language;
    /**
     * The map of included scripts; key = registry entry key, value = script source
     */
    private final Map<Value, Object> includes;
    /**
     * The optional name of the function to be invoked, defaults to mediate
     */
    private String function = "mediate";
    /**
     * The source code of the script
     */
    private String scriptSourceCode;
    /**
     * The BSF engine created to process each message through the script
     */
    protected ScriptEngine scriptEngine;
    /**
     * Does the ScriptEngine support multi-threading
     */
    private boolean multiThreadedEngine;
    /**
     * The compiled script. Only used for inline scripts
     */
    private CompiledScript compiledScript;
    /**
     * The Invokable script. Only used for external scripts
     */
    private Invocable invocableScript;
    /**
     * The BSF helper to convert between the XML representations used by Java
     * and the scripting language
     */
    private XMLHelper xmlHelper;

    /**
     * Lock used to ensure thread-safe lookup of the object from the registry
     */
    private final Object resourceLock = new Object();

    /**
     * Store the class loader from properties
     */
    private ClassLoader loader;

    /**
     * Create a script mediator for the given language and given script source
     *
     * @param language         the BSF language
     * @param scriptSourceCode the source code of the script
     */
    public ScriptMediator(String language, String scriptSourceCode,ClassLoader classLoader) {
        this.language = language;
        this.scriptSourceCode = scriptSourceCode;
        this.setLoader(classLoader);
        this.includes = new TreeMap<Value, Object>();
        initInlineScript();
    }

    /**
     * Create a script mediator for the given language and given script entry key and function
     *
     * @param language       the BSF language
     * @param includeKeysMap Include script keys
     * @param key            the registry entry key to load the script
     * @param function       the function to be invoked
     */
    public ScriptMediator(String language, Map<Value, Object> includeKeysMap,
                          Value key, String function,ClassLoader classLoader) {
        this.language = language;
        this.key = key;
        this.setLoader(classLoader);
        this.includes = includeKeysMap;
        if (function != null) {
            this.function = function;
        }
        initScriptEngine();
        if (!(scriptEngine instanceof Invocable)) {
            throw new SynapseException("Script engine is not an Invocable" +
                    " engine for language: " + language);
        }
        invocableScript = (Invocable) scriptEngine;
    }

    /**
     * Perform Script mediation
     *
     * @param synCtx the Synapse message context
     * @return the boolean result from the script invocation
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Script mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Scripting language : " + language + " source " +
                    (key == null ? ": specified inline " : " loaded with key : " + key) +
                    (function != null ? " function : " + function : ""));
        }

        boolean returnValue;
        if (multiThreadedEngine) {
            returnValue = invokeScript(synCtx);
        } else {
            // TODO: change to use a pool of script engines (requires an update to BSF)
            synchronized (scriptEngine.getClass()) {
                returnValue = invokeScript(synCtx);
            }
        }

        if (synLog.isTraceTraceEnabled()) {
            synLog.traceTrace("Result message after execution of script : " + synCtx.getEnvelope());
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : Script mediator return value : " + returnValue);
        }

        return returnValue;
    }

    private boolean invokeScript(MessageContext synCtx) {
        boolean returnValue;
        try {

            //if the engine is Rhino then needs to set the class loader specifically
            if(language.equals("js")){
                Context cx = Context.enter();
                cx.setApplicationClassLoader(this.loader);
            }

            Object returnObject;
            if (key != null) {
                returnObject = mediateWithExternalScript(synCtx);
            } else {
                returnObject = mediateForInlineScript(synCtx);
            }
            returnValue = !(returnObject != null && returnObject instanceof Boolean)
                    || (Boolean) returnObject;

        } catch (ScriptException e) {
            handleException("The script engine returned an error executing the " +
                    (key == null ? "inlined " : "external ") + language + " script" +
                    (key != null ? " : " + key : "") +
                    (function != null ? " function " + function : ""), e, synCtx);
            returnValue = false;
        } catch (NoSuchMethodException e) {
            handleException("The script engine returned a NoSuchMethodException executing the " +
                    "external " + language + " script" + " : " + key +
                    (function != null ? " function " + function : ""), e, synCtx);
            returnValue = false;
        }  finally {
                if(language.equals("js")){
                          Context.exit();
                     }
    }

        return returnValue;
    }

    /**
     * Mediation implementation when the script to be executed should be loaded from the registry
     *
     * @param synCtx the message context
     * @return script result
     * @throws ScriptException       For any errors , when compile, run the script
     * @throws NoSuchMethodException If the function is not defined in the script
     */
    private Object mediateWithExternalScript(MessageContext synCtx)
            throws ScriptException, NoSuchMethodException {
        prepareExternalScript(synCtx);
        ScriptMessageContext scriptMC = new ScriptMessageContext(synCtx, xmlHelper);
        return invocableScript.invokeFunction(function, new Object[]{scriptMC});        
    }

    /**
     * Perform mediation with static inline script of the given scripting language
     *
     * @param synCtx message context
     * @return true, or the script return value
     * @throws ScriptException For any errors , when compile , run the script
     */
    private Object mediateForInlineScript(MessageContext synCtx) throws ScriptException {

        ScriptMessageContext scriptMC = new ScriptMessageContext(synCtx, xmlHelper);

        Bindings bindings = scriptEngine.createBindings();
        bindings.put(MC_VAR_NAME, scriptMC);

        Object response;
        if (compiledScript != null) {
            response = compiledScript.eval(bindings);
        } else {
            response = scriptEngine.eval(scriptSourceCode, bindings);
        }

        return response;

    }

    /**
     * Initialise the Mediator for the inline script
     */
    protected void initInlineScript() {
        try {
            initScriptEngine();

            if (scriptEngine instanceof Compilable) {
                if (log.isDebugEnabled()) {
                    log.debug("Script engine supports Compilable interface, " +
                            "compiling script code..");
                }
                compiledScript = ((Compilable) scriptEngine).compile(scriptSourceCode);
            } else {
                // do nothing. If the script engine doesn't support Compilable then
                // the inline script will be evaluated on each invocation
                if (log.isDebugEnabled()) {
                    log.debug("Script engine does not support the Compilable interface, " +
                            "in-lined script would be evaluated on each invocation..");
                }
            }

        } catch (ScriptException e) {
            throw new SynapseException("Exception initializing inline script", e);
        }
    }

    /**
     * Prepares the mediator for the invocation of an external script
     *
     * @param synCtx MessageContext script
     * @throws ScriptException For any errors , when compile the script
     */
    protected synchronized void prepareExternalScript(MessageContext synCtx)
            throws ScriptException {

        // TODO: only need this synchronized method for dynamic registry entries. If there was a way
        // to access the registry entry during mediator initialization then for non-dynamic entries
        // this could be done just the once during mediator initialization.

        // Derive actual key from xpath expression or get static key
        String generatedScriptKey = key.evaluateValue(synCtx);
        Entry entry = synCtx.getConfiguration().getEntryDefinition(generatedScriptKey);
        boolean needsReload = (entry != null) && entry.isDynamic() &&
                (!entry.isCached() || entry.isExpired());
        synchronized (resourceLock) {
            if (scriptSourceCode == null || needsReload) {
                Object o = synCtx.getEntry(generatedScriptKey);
                if (o instanceof OMElement) {
                    scriptSourceCode = ((OMElement) (o)).getText();
                    scriptEngine.eval(scriptSourceCode);
                } else if (o instanceof String) {
                    scriptSourceCode = (String) o;
                    scriptEngine.eval(scriptSourceCode);
                } else if (o instanceof OMText) {

                    DataHandler dataHandler = (DataHandler) ((OMText) o).getDataHandler();
                    if (dataHandler != null) {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(
                                    new InputStreamReader(dataHandler.getInputStream()));
                            scriptEngine.eval(reader);

                        } catch (IOException e) {
                            handleException("Error in reading script as a stream ", e, synCtx);
                        } finally {

                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    handleException("Error in closing input stream ", e, synCtx);
                                }
                            }

                        }
                    }
                }

            }
        }

        // load <include /> scripts; reload each script if needed
        for (Value includeKey : includes.keySet()) {

            String includeSourceCode = (String) includes.get(includeKey);

            String generatedKey = includeKey.evaluateValue(synCtx);

            Entry includeEntry = synCtx.getConfiguration().getEntryDefinition(generatedKey);
            boolean includeEntryNeedsReload = (includeEntry != null) && includeEntry.isDynamic()
                    && (!includeEntry.isCached() || includeEntry.isExpired());
            synchronized (resourceLock) {
                if (includeSourceCode == null || includeEntryNeedsReload) {
                    log.debug("Re-/Loading the include script with key " + includeKey);
                    Object o = synCtx.getEntry(generatedKey);
                    if (o instanceof OMElement) {
                        includeSourceCode = ((OMElement) (o)).getText();
                        scriptEngine.eval(includeSourceCode);
                    } else if (o instanceof String) {
                        includeSourceCode = (String) o;
                        scriptEngine.eval(includeSourceCode);
                    } else if (o instanceof OMText) {

                        DataHandler dataHandler = (DataHandler) ((OMText) o).getDataHandler();
                        if (dataHandler != null) {
                            BufferedReader reader = null;
                            try {
                                reader = new BufferedReader(
                                        new InputStreamReader(dataHandler.getInputStream()));
                                scriptEngine.eval(reader);

                            } catch (IOException e) {
                                handleException("Error in reading script as a stream ", e, synCtx);
                            } finally {

                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e) {
                                        handleException("Error in closing input" +
                                                " stream ", e, synCtx);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void initScriptEngine() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing script mediator for language : " + language);
        }

        ScriptEngineManager manager = new ScriptEngineManager();
        manager.registerEngineExtension("js", new RhinoScriptEngineFactory());
        manager.registerEngineExtension("groovy", new GroovyScriptEngineFactory());
        manager.registerEngineExtension("rb", new JRubyScriptEngineFactory());

        this.scriptEngine = manager.getEngineByExtension(language);
        if (scriptEngine == null) {
            handleException("No script engine found for language: " + language);
        }
        //Invoking a custom Helper class since there is an api change in rhino17 for js
        if (language.equalsIgnoreCase(JAVA_SCRIPT)) {
            xmlHelper = new JavaScriptXmlHelper();
        } else {
            xmlHelper = XMLHelper.getArgHelper(scriptEngine);
        }

        this.multiThreadedEngine = scriptEngine.getFactory().getParameter("THREADING") != null;
        log.debug("Script mediator for language : " + language +
                " supports multithreading? : " + multiThreadedEngine);
    }

    public String getLanguage() {
        return language;
    }

    public Value getKey() {
        return key;
    }

    public String getFunction() {
        return function;
    }

    public String getScriptSrc() {
        return scriptSourceCode;
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public Map<Value, Object> getIncludeMap() {
        return includes;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

}
