/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache" and "Apache Software Foundation" must
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.poi.hpsf.littleendian;

/**
 * <p>A data item in the little-endian format. Little-endian means
 * that lower bytes come before higher bytes.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id: LittleEndian.java,v 1.1 2002/02/14 04:00:59 mjohnson Exp $
 * @since 2002-02-09
 */
public abstract class LittleEndian
{

    /* This class could be optimized by not copying the bytes, but
     * instead maintaining just references to the original byte
     * arrays. However, before implementing this it should be
     * investigated whether it is worth the while. */


    /**
     * <p>The bytes making out the little-endian field. They are in
     * correct order, i.e. high-endian.</p>
     */
    protected byte[] bytes;



    /**
     * <p>Creates a {@link LittleEndian} and reads its value from a
     * byte array.</p>
     *
     * @param src The byte array to read from.
     *
     * @param offset The offset of the first byte to read.
     */
    public LittleEndian(final byte[] src, final int offset)
    {
        read(src, offset);
    }



    /**
     * <p>Returns the bytes making out the little-endian field in
     * big-endian order.
     </p> */
    public byte[] getBytes()
    {
        return bytes;
    }



    /**
     * <p>Reads the little-endian field from a byte array.</p>
     *
     * @param src The byte array to read from
     *
     * @param offset The offset within the <var>src</var> byte array
     */
    public byte[] read(final byte[] src, final int offset)
    {
        final int length = length();
        bytes = new byte[length];
        for (int i = 0; i < length; i++)
            bytes[i] = src[offset + length - 1 - i];
        return bytes;
    }


    /**
     * <p>Returns the number of bytes of this little-endian field.</p>
     */
    public abstract int length();

}
