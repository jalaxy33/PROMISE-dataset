<%--
  Copyright 2002-2004 The Apache Software Foundation or its licensors,
  as applicable.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean-el" prefix="bean-el" %>

<html:errors/>

<logic:messagesNotPresent> 
	<bean-el:message key="execute.started" arg0="${project}"/>
</logic:messagesNotPresent>
<br/>
<html:link page="/summary.do"><bean:message key="back.to.main"/></html:link>
<br/>
<html:link page="/viewlog.do" paramId="project" paramName="project"><bean-el:message key="view.log" arg0="${project}"/></html:link>
