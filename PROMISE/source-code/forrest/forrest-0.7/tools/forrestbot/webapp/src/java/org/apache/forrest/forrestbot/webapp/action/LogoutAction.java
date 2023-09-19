/*
* Copyright 2002-2004 The Apache Software Foundation or its licensors,
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
package org.apache.forrest.forrestbot.webapp.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.forrest.forrestbot.webapp.Constants;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public final class LogoutAction extends BaseAction {
	private static Logger log = Logger.getLogger(LogoutAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		super.execute(mapping, form, request, response);

		// session
		request.getSession(true).removeAttribute("auth");
		
		// request & application for good measure
		request.removeAttribute("auth");
		request.getSession(true).getServletContext().removeAttribute("auth");
		
		return mapping.findForward(Constants.FORWARD_NAME_SUCCESS);
	}
}
