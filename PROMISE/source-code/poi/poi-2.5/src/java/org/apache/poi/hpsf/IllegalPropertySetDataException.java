/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hpsf;

/**
 * <p>This exception is thrown when there is an illegal value set in a
 * {@link PropertySet}. For example, a {@link Variant#VT_BOOL} must
 * have a value of <code>-1 (true)</code> or <code>0 (false)</code>.
 * Any other value would trigger this exception. It supports a nested
 * "reason" throwable, i.e. an exception that caused this one to be
 * thrown.</p>
 *
 * @author Drew Varner(Drew.Varner atDomain sc.edu)
 * @version $Id: IllegalPropertySetDataException.java,v 1.3.4.1 2004/02/22 11:54:45 glens Exp $
 * @since 2002-05-26
 */
public class  IllegalPropertySetDataException extends HPSFRuntimeException
{

    public IllegalPropertySetDataException()
    {
        super();
    }



    public IllegalPropertySetDataException(final String msg)
    {
        super(msg);
    }



    public IllegalPropertySetDataException(final Throwable reason)
    {
        super(reason);
    }



    public IllegalPropertySetDataException(final String msg,
                                           final Throwable reason)
    {
        super(msg,reason);
    }

}
