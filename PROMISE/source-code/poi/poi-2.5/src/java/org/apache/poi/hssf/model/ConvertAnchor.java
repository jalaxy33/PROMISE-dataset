/* ====================================================================
   Copyright 2004   Apache Software Foundation

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

package org.apache.poi.hssf.model;

import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.hssf.usermodel.HSSFAnchor;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFChildAnchor;

/**
 * $Id: ConvertAnchor.java,v 1.1.2.2 2004/02/22 11:54:46 glens Exp $
 */
public class ConvertAnchor
{
    public static EscherRecord createAnchor( HSSFAnchor userAnchor )
    {
        if (userAnchor instanceof HSSFClientAnchor)
        {
            HSSFClientAnchor a = (HSSFClientAnchor) userAnchor;

            EscherClientAnchorRecord anchor = new EscherClientAnchorRecord();
            anchor.setRecordId( EscherClientAnchorRecord.RECORD_ID );
            anchor.setOptions( (short) 0x0000 );
            anchor.setFlag( (short) 0 );
            anchor.setCol1( (short) Math.min(a.getCol1(), a.getCol2()) );
            anchor.setDx1( (short) Math.min(a.getDx1(), a.getDx2()) );
            anchor.setRow1( (short) Math.min(a.getRow1(), a.getRow2()) );
            anchor.setDy1( (short) Math.min(a.getDy1(), a.getDy2()) );

            anchor.setCol2( (short) Math.max(a.getCol1(), a.getCol2()) );
            anchor.setDx2( (short) Math.max(a.getDx1(), a.getDx2()) );
            anchor.setRow2( (short) Math.max(a.getRow1(), a.getRow2()) );
            anchor.setDy2( (short) Math.max(a.getDy1(), a.getDy2() ) );
            return anchor;
        }
        else
        {
            HSSFChildAnchor a = (HSSFChildAnchor) userAnchor;
            EscherChildAnchorRecord anchor = new EscherChildAnchorRecord();
            anchor.setRecordId( EscherChildAnchorRecord.RECORD_ID );
            anchor.setOptions( (short) 0x0000 );
            anchor.setDx1( (short) Math.min(a.getDx1(), a.getDx2()) );
            anchor.setDy1( (short) Math.min(a.getDy1(), a.getDy2()) );
            anchor.setDx2( (short) Math.max(a.getDx2(), a.getDx1()) );
            anchor.setDy2( (short) Math.max(a.getDy2(), a.getDy1()) );
            return anchor;
        }
    }

}
