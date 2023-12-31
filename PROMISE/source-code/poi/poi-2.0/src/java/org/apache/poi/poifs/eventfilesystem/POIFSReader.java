
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

package org.apache.poi.poifs.eventfilesystem;

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.BlockList;
import org.apache.poi.poifs.storage.HeaderBlockReader;
import org.apache.poi.poifs.storage.RawDataBlockList;
import org.apache.poi.poifs.storage.SmallBlockTableReader;

/**
 * An event-driven reader for POIFS file systems. Users of this class
 * first create an instance of it, then use the registerListener
 * methods to register POIFSReaderListener instances for specific
 * documents. Once all the listeners have been registered, the read()
 * method is called, which results in the listeners being notified as
 * their documents are read.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POIFSReader
{
    private POIFSReaderRegistry registry;
    private boolean             registryClosed;

    /**
     * Create a POIFSReader
     */

    public POIFSReader()
    {
        registry       = new POIFSReaderRegistry();
        registryClosed = false;
    }

    /**
     * Read from an InputStream and process the documents we get
     *
     * @param stream the InputStream from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */

    public void read(final InputStream stream)
        throws IOException
    {
        registryClosed = true;

        // read the header block from the stream
        HeaderBlockReader header_block_reader = new HeaderBlockReader(stream);

        // read the rest of the stream into blocks
        RawDataBlockList  data_blocks         = new RawDataBlockList(stream);

        // set up the block allocation table (necessary for the
        // data_blocks to be manageable
        new BlockAllocationTableReader(header_block_reader.getBATCount(),
                                       header_block_reader.getBATArray(),
                                       header_block_reader.getXBATCount(),
                                       header_block_reader.getXBATIndex(),
                                       data_blocks);

        // get property table from the document
        PropertyTable properties =
            new PropertyTable(header_block_reader.getPropertyStart(),
                              data_blocks);

        // process documents
        processProperties(SmallBlockTableReader
            .getSmallDocumentBlocks(data_blocks, properties
                .getRoot(), header_block_reader
                    .getSBATStart()), data_blocks, properties.getRoot()
                        .getChildren(), new POIFSDocumentPath());
    }

    /**
     * Register a POIFSReaderListener for all documents
     *
     * @param listener the listener to be registered
     *
     * @exception NullPointerException if listener is null
     * @exception IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        if (registryClosed)
        {
            throw new IllegalStateException();
        }
        registry.registerListener(listener);
    }

    /**
     * Register a POIFSReaderListener for a document in the root
     * directory
     *
     * @param listener the listener to be registered
     * @param name the document name
     *
     * @exception NullPointerException if listener is null or name is
     *                                 null or empty
     * @exception IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener,
                                 final String name)
    {
        registerListener(listener, null, name);
    }

    /**
     * Register a POIFSReaderListener for a document in the specified
     * directory
     *
     * @param listener the listener to be registered
     * @param path the document path; if null, the root directory is
     *             assumed
     * @param name the document name
     *
     * @exception NullPointerException if listener is null or name is
     *                                 null or empty
     * @exception IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener,
                                 final POIFSDocumentPath path,
                                 final String name)
    {
        if ((listener == null) || (name == null) || (name.length() == 0))
        {
            throw new NullPointerException();
        }
        if (registryClosed)
        {
            throw new IllegalStateException();
        }
        registry.registerListener(listener,
                                  (path == null) ? new POIFSDocumentPath()
                                                 : path, name);
    }

    /**
     * read in files
     *
     * @param args names of the files
     *
     * @exception IOException
     */

    public static void main(String args[])
        throws IOException
    {
        if (args.length == 0)
        {
            System.err
                .println("at least one argument required: input filename(s)");
            System.exit(1);
        }

        // register for all
        for (int j = 0; j < args.length; j++)
        {
            POIFSReader         reader   = new POIFSReader();
            POIFSReaderListener listener = new SampleListener();

            reader.registerListener(listener);
            System.out.println("reading " + args[ j ]);
            FileInputStream istream = new FileInputStream(args[ j ]);

            reader.read(istream);
            istream.close();
        }
    }

    private void processProperties(final BlockList small_blocks,
                                   final BlockList big_blocks,
                                   final Iterator properties,
                                   final POIFSDocumentPath path)
        throws IOException
    {
        while (properties.hasNext())
        {
            Property property = ( Property ) properties.next();
            String   name     = property.getName();

            if (property.isDirectory())
            {
                POIFSDocumentPath new_path = new POIFSDocumentPath(path,
                                                 new String[]
                {
                    name
                });

                processProperties(
                    small_blocks, big_blocks,
                    (( DirectoryProperty ) property).getChildren(), new_path);
            }
            else
            {
                int      startBlock = property.getStartBlock();
                Iterator listeners  = registry.getListeners(path, name);

                if (listeners.hasNext())
                {
                    int           size     = property.getSize();
                    POIFSDocument document = null;

                    if (property.shouldUseSmallBlocks())
                    {
                        document =
                            new POIFSDocument(name, small_blocks
                                .fetchBlocks(startBlock), size);
                    }
                    else
                    {
                        document =
                            new POIFSDocument(name, big_blocks
                                .fetchBlocks(startBlock), size);
                    }
                    while (listeners.hasNext())
                    {
                        POIFSReaderListener listener =
                            ( POIFSReaderListener ) listeners.next();

                        listener.processPOIFSReaderEvent(
                            new POIFSReaderEvent(
                                new DocumentInputStream(document), path,
                                name));
                    }
                }
                else
                {

                    // consume the document's data and discard it
                    if (property.shouldUseSmallBlocks())
                    {
                        small_blocks.fetchBlocks(startBlock);
                    }
                    else
                    {
                        big_blocks.fetchBlocks(startBlock);
                    }
                }
            }
        }
    }

    private static class SampleListener
        implements POIFSReaderListener
    {

        /**
         * Constructor SampleListener
         */

        SampleListener()
        {
        }

        /**
         * Method processPOIFSReaderEvent
         *
         * @param event
         */

        public void processPOIFSReaderEvent(final POIFSReaderEvent event)
        {
            DocumentInputStream istream = event.getStream();
            POIFSDocumentPath   path    = event.getPath();
            String              name    = event.getName();

            try
            {
                byte[] data = new byte[ istream.available() ];

                istream.read(data);
                int pathLength = path.length();

                for (int k = 0; k < pathLength; k++)
                {
                    System.out.print("/" + path.getComponent(k));
                }
                System.out.println("/" + name + ": " + data.length
                                   + " bytes read");
            }
            catch (IOException ignored)
            {
            }
        }
    }   // end private class SampleListener
}       // end public class POIFSReader

