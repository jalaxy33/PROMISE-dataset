
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.PrintSetupRecord;

/**
 * Used to modify the print setup.
 * <P>
 * Paper size constants have been added for the ones I have access
 * to.  They follow as:<br>
 *  public static final short LETTER_PAPERSIZE 	          = 1;<br>
 *  public static final short LEGAL_PAPERSIZE 		  = 5;<br>
 *  public static final short EXECUTIVE_PAPERSIZE 	  = 7;<br>
 *  public static final short A4_PAPERSIZE 	  	  = 9;<br>
 *  public static final short A5_PAPERSIZE 		  = 11;<br>
 *  public static final short ENVELOPE_10_PAPERSIZE 	  = 20;<br>
 *  public static final short ENVELOPE_DL_PAPERSIZE 	  = 27;<br>
 *  public static final short ENVELOPE_CS_PAPERSIZE 	  = 28;<br>
 *  public static final short ENVELOPE_MONARCH_PAPERSIZE  = 37;<br>
 * <P>
 * @author Shawn Laubach (slaubach at apache dot org) */
public class HSSFPrintSetup extends Object {    
    public static final short LETTER_PAPERSIZE 	          = 1;
    public static final short LEGAL_PAPERSIZE 		  = 5;
    public static final short EXECUTIVE_PAPERSIZE 	  = 7;
    public static final short A4_PAPERSIZE 	  	  = 9;
    public static final short A5_PAPERSIZE 		  = 11;
    public static final short ENVELOPE_10_PAPERSIZE 	  = 20;
    public static final short ENVELOPE_DL_PAPERSIZE 	  = 27;
    public static final short ENVELOPE_CS_PAPERSIZE 	  = 28;
    public static final short ENVELOPE_MONARCH_PAPERSIZE  = 37;
    PrintSetupRecord printSetupRecord;
    
    /**    
     * Constructor.  Takes the low level print setup record.    
     * @param printSetupRecord the low level print setup record    
     */
    protected HSSFPrintSetup(PrintSetupRecord printSetupRecord) {
	this.printSetupRecord = printSetupRecord;
    }    
    
    /**    
     * Set the paper size.    
     * @param size the paper size.    
     */
    public void setPaperSize(short size)    {
	printSetupRecord.setPaperSize(size);
    }    
    
    /**    
     * Set the scale.    
     * @param scale the scale to use    
     */
    public void setScale(short scale)    {
	printSetupRecord.setScale(scale);
    }    
    
    /**    
     * Set the page numbering start.    
     * @param start the page numbering start    
     */
    public void setPageStart(short start)    {
	printSetupRecord.setPageStart(start);
    }    
    
    /**    
     * Set the number of pages wide to fit the sheet in    
     * @param width the number of pages    
     */
    public void setFitWidth(short width)    {
	printSetupRecord.setFitWidth(width);
    }    
    
    /**    
     * Set the number of pages high to fit the sheet in    
     * @param height the number of pages    
     */
    public void setFitHeight(short height)    {
	printSetupRecord.setFitHeight(height);
    }    
    
    /**    
     * Sets the options flags.  Not advisable to do it directly.    
     * @param options The bit flags for the options    
     */
    public void setOptions(short options)    {
	printSetupRecord.setOptions(options);
    }    
    
    /**    
     * Set whether to go left to right or top down in ordering    
     * @param ltor left to right    
     */
    public void setLeftToRight(boolean ltor)    {
	printSetupRecord.setLeftToRight(ltor);
    }    
    
    /**    
     * Set whether to print in landscape    
     * @param ls landscape    
     */
    public void setLandscape(boolean ls)    {
	printSetupRecord.setLandscape(!ls);
    }    
    
    /**    
     * Valid settings.  I'm not for sure.    
     * @param valid Valid    
     */
    public void setValidSettings(boolean valid)    {
	printSetupRecord.setValidSettings(valid);
    }    
    
    /**    
     * Set whether it is black and white    
     * @param mono Black and white    
     */
    public void setNoColor(boolean mono)    {
	printSetupRecord.setNoColor(mono);
    }    
    
    /**    
     * Set whether it is in draft mode    
     * @param d draft    
     */
    public void setDraft(boolean d)    {
	printSetupRecord.setDraft(d);
    }    
    
    /**    
     * Print the include notes    
     * @param printnotes print the notes    
     */
    public void setNotes(boolean printnotes)    {
	printSetupRecord.setNotes(printnotes);
    }    
    
    /**    
     * Set no orientation. ?    
     * @param orientation Orientation.    
     */
    public void setNoOrientation(boolean orientation)    {
	printSetupRecord.setNoOrientation(orientation);
    }    
    
    /**    
     * Set whether to use page start    
     * @param page Use page start    
     */
    public void setUsePage(boolean page)    {
	printSetupRecord.setUsePage(page);
    }    
    
    /**    
     * Sets the horizontal resolution.    
     * @param resolution horizontal resolution    
     */
    public void setHResolution(short resolution)    {
	printSetupRecord.setHResolution(resolution);
    }    
    
    /**    
     * Sets the vertical resolution.    
     * @param resolution vertical resolution    
     */
    public void setVResolution(short resolution)    {
	printSetupRecord.setVResolution(resolution);
    }    
    
    /**    
     * Sets the header margin.    
     * @param headermargin header margin    
     */
    public void setHeaderMargin(double headermargin)    {
	printSetupRecord.setHeaderMargin(headermargin);
    }    
    
    /**    
     * Sets the footer margin.    
     * @param footermargin footer margin    
     */
    public void setFooterMargin(double footermargin)    {
	printSetupRecord.setFooterMargin(footermargin);
    }    
    
    /**    
     * Sets the number of copies.    
     * @param copies number of copies    
     */
    public void setCopies(short copies)    {
	printSetupRecord.setCopies(copies);
    }    
    
    /**    
     * Returns the paper size.    
     * @return paper size    
     */
    public short getPaperSize()    {
        return printSetupRecord.getPaperSize();
    }    
    
    /**    
     * Returns the scale.    
     * @return scale    
     */
    public short getScale()    {
        return printSetupRecord.getScale();
    }    
    
    /**    
     * Returns the page start.    
     * @return page start    
     */
    public short getPageStart()    {
        return printSetupRecord.getPageStart();
    }    
    
    /**    
     * Returns the number of pages wide to fit sheet in.    
     * @return number of pages wide to fit sheet in    
     */
    public short getFitWidth()    {
        return printSetupRecord.getFitWidth();
    }    
    
    /**    
     * Returns the number of pages high to fit the sheet in.    
     * @return number of pages high to fit the sheet in    
     */
    public short getFitHeight()    {
        return printSetupRecord.getFitHeight();
    }    
    
    /**    
     * Returns the bit flags for the options.    
     * @return bit flags for the options    
     */
    public short getOptions()    {
        return printSetupRecord.getOptions();
    }    
    
    /**    
     * Returns the left to right print order.    
     * @return left to right print order    
     */
    public boolean getLeftToRight()    {
        return printSetupRecord.getLeftToRight();
    }    
    
    /**    
     * Returns the landscape mode.    
     * @return landscape mode    
     */
    public boolean getLandscape()    {
        return !printSetupRecord.getLandscape();
    }    
    
    /**    
     * Returns the valid settings.    
     * @return valid settings    
     */
    public boolean getValidSettings()    {
        return printSetupRecord.getValidSettings();
    }    
    
    /**    
     * Returns the black and white setting.    
     * @return black and white setting    
     */
    public boolean getNoColor()    {
        return printSetupRecord.getNoColor();
    }    
    
    /**    
     * Returns the draft mode.    
     * @return draft mode    
     */
    public boolean getDraft()    {
        return printSetupRecord.getDraft();
    }    
    
    /**    
     * Returns the print notes.    
     * @return print notes    
     */
    public boolean getNotes()    {
        return printSetupRecord.getNotes();
    }    
    
    /**    
     * Returns the no orientation.    
     * @return no orientation    
     */
    public boolean getNoOrientation()    {
        return printSetupRecord.getNoOrientation();
    }    
    
    /**    
     * Returns the use page numbers.    
     * @return use page numbers    
     */
    public boolean getUsePage()    {
        return printSetupRecord.getUsePage();
    }    
    
    /**    
     * Returns the horizontal resolution.    
     * @return horizontal resolution    
     */
    public short getHResolution()    {
        return printSetupRecord.getHResolution();
    }    
    
    /**    
     * Returns the vertical resolution.    
     * @return vertical resolution    
     */
    public short getVResolution()    {
        return printSetupRecord.getVResolution();
    }    
    
    /**    
     * Returns the header margin.    
     * @return header margin    
     */
    public double getHeaderMargin()    {
        return printSetupRecord.getHeaderMargin();
    }    
    
    /**    
     * Returns the footer margin.    
     * @return footer margin    
     */
    public double getFooterMargin()    {
        return printSetupRecord.getFooterMargin();
    }    
    
    /**    
     * Returns the number of copies.    
     * @return number of copies    
     */
    public short getCopies()    {
        return printSetupRecord.getCopies();
    }
}