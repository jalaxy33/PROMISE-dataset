/*
 * Copyright 1999-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.forrest.locationmap.lm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.selection.Selector;


/**
 * Locationmap select statement.
 */
public final class SelectNode extends AbstractNode {
    
    // the containing LocatorNode
    private final LocatorNode m_ln;
    
    // the selector that does the work
    private Selector m_selector;
    
    // the type of selector for this node
    private String m_type;
    
    // the locations to test against
    private AbstractNode[] m_nodes;
    
    
    public SelectNode(LocatorNode ln, ServiceManager manager) {
        super(manager);
        m_ln = ln;
    }
    
    public void build(Configuration configuration) throws ConfigurationException {
        
        super.build(configuration);
        
        // get the selector
        m_type = configuration.getAttribute("type",m_ln.getDefaultSelector());
        try {
            final ServiceSelector selectors = (ServiceSelector) super.m_manager.lookup(Selector.ROLE + "Selector");
            m_selector = (Selector) selectors.select(m_type);
        } catch (ServiceException e) {
            final String message = "Unable to get Selector of type " + m_type;
            throw new ConfigurationException(message,e);
        }
        
        // build the child nodes
        final Configuration[] children = configuration.getChildren();
        final List nodes = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            AbstractNode node = null;
            String name = children[i].getName();
            if (name.equals("location")) {
                node = new LocationNode(m_ln, super.m_manager);
            } 
            else if (name.equals("match")) {
                node = new MatchNode(m_ln, super.m_manager);
            }
            else if (name.equals("select")) {
                node = new SelectNode(m_ln, super.m_manager);
            }
            else if (!name.equals("parameter")) {
                final String message = "Unknown select node child:" + name;
                throw new ConfigurationException(message);
            }
            if (node != null) {
                node.enableLogging(getLogger());
                node.build(children[i]);
                nodes.add(node);
            }
        }
        m_nodes = (AbstractNode[]) nodes.toArray(new AbstractNode[nodes.size()]);
    }
    
    public String locate(Map om, InvokeContext context) throws Exception {
        
        Parameters parameters = resolveParameters(context,om);
        for (int i = 0; i < m_nodes.length; i++) {
            String location = m_nodes[i].locate(om,context);
            if (m_selector.select(location,om,parameters)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("selected: " + location);
                }
                return location;
            }
        }
        return null;
    }
}