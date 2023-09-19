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

package org.apache.synapse.transport.vfs;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import java.io.IOException;

/**
 * axis2.xml - transport definition
 *  <transportSender name="file" class="org.apache.synapse.transport.vfs.VFSTransportSender">
 *      <parameter name="transport.vfs.Locking">enable|disable</parameter> ?
 *  </transportSender>
 */
public class VFSTransportSender extends AbstractTransportSender implements ManagementSupport {

    /** The VFS file system manager */
    private FileSystemManager fsManager = null;

    /**
     * By default file locking in VFS transport is turned on at a global level
     *
     * NOTE: DO NOT USE THIS FLAG, USE PollTableEntry#isFileLockingEnabled() TO CHECK WHETHER
     * FILE LOCKING IS ENABLED
     */
    private boolean globalFileLockingFlag = true;

    /**
     * The public constructor
     */
    public VFSTransportSender() {
        log = LogFactory.getLog(VFSTransportSender.class);
    }

    /**
     * Initialize the VFS file system manager and be ready to send messages
     * @param cfgCtx the axis2 configuration context
     * @param transportOut the transport-out description
     * @throws AxisFault on error
     */
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut)
            throws AxisFault {

        super.init(cfgCtx, transportOut);
        try {
            StandardFileSystemManager fsm = new StandardFileSystemManager();
            fsm.setConfiguration(getClass().getClassLoader().getResource("providers.xml"));
            fsm.init();
            fsManager = fsm;
            Parameter lckFlagParam = transportOut.getParameter(VFSConstants.TRANSPORT_FILE_LOCKING);
            if (lckFlagParam != null) {
                String strLockingFlag = lckFlagParam.getValue().toString();
                // by-default enabled, if explicitly specified as "disable" make it disable
                if (VFSConstants.TRANSPORT_FILE_LOCKING_DISABLED.equals(strLockingFlag)) {
                    globalFileLockingFlag = false;
                }
            }
        } catch (FileSystemException e) {
            handleException("Error initializing the file transport : " + e.getMessage(), e);
        }
    }

    /**
     * Send the given message over the VFS transport
     *
     * @param msgCtx the axis2 message context
     * @throws AxisFault on error
     */
    public void sendMessage(MessageContext msgCtx, String targetAddress,
                            OutTransportInfo outTransportInfo) throws AxisFault {

        if (waitForSynchronousResponse(msgCtx)) {
            throw new AxisFault("The VFS transport doesn't support synchronous responses. " +
                    "Please use the appropriate (out only) message exchange pattern.");
        }

        VFSOutTransportInfo vfsOutInfo = null;

        if (targetAddress != null) {
            vfsOutInfo = new VFSOutTransportInfo(targetAddress, globalFileLockingFlag);
        } else if (outTransportInfo != null && outTransportInfo instanceof VFSOutTransportInfo) {
            vfsOutInfo = (VFSOutTransportInfo) outTransportInfo;
        }

        if (vfsOutInfo != null) {
            FileObject replyFile = null;
            try {
                boolean wasError = true;
                int retryCount = 0;
                int maxRetryCount = vfsOutInfo.getMaxRetryCount();
                long reconnectionTimeout = vfsOutInfo.getReconnectTimeout();
                boolean append = vfsOutInfo.isAppend();
                boolean isUseTempFile = vfsOutInfo.isUseTempFile();
                String tempTargetFileName, actualTargetFileName = null;

                while (wasError) {
                    try {
                        retryCount++;
                        replyFile = fsManager.resolveFile(vfsOutInfo.getOutFileURI());
                    
                        if (replyFile == null) {
                            log.error("replyFile is null");
                            throw new FileSystemException("replyFile is null");
                        }
                        wasError = false;
                                        
                    } catch (FileSystemException e) {
                        log.error("cannot resolve replyFile", e);
                        if(maxRetryCount <= retryCount) {
                            handleException("cannot resolve replyFile repeatedly: "
                                    + e.getMessage(), e);
                        }
                    }
                
                    if (wasError) {
                        try {
                            Thread.sleep(reconnectionTimeout);
                        } catch (InterruptedException e2) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                
                if (replyFile.exists()) {
                    if (replyFile.getType() == FileType.FOLDER) {
                        if (isUseTempFile) {
                            tempTargetFileName = VFSUtils.generateTempFileName();
                            actualTargetFileName = VFSUtils.getFileName(msgCtx, vfsOutInfo);
                        } else {
                            tempTargetFileName = VFSUtils.getFileName(msgCtx, vfsOutInfo);
                        }
                        // we need to write a file containing the message to this folder
                        FileObject responseFile = fsManager.resolveFile(replyFile, tempTargetFileName);

                        // if file locking is not disabled acquire the lock
                        // before uploading the file
                        if (vfsOutInfo.isFileLockingEnabled()) {
                            acquireLockForSending(responseFile, vfsOutInfo);
                            if (!responseFile.exists()) {
                                responseFile.createFile();
                            }
                            populateResponseFile(responseFile, msgCtx,append, true);
                            VFSUtils.releaseLock(fsManager, responseFile);
                        } else {
                            if (!responseFile.exists()) {
                                responseFile.createFile();
                            }
                            populateResponseFile(responseFile, msgCtx,append, false);
                        }

                        if (isUseTempFile) {
                            responseFile.moveTo(fsManager.resolveFile(replyFile, actualTargetFileName));
                        }

                    } else if (replyFile.getType() == FileType.FILE) {
                        if (isUseTempFile) {
                            tempTargetFileName = VFSUtils.generateTempFileName();
                            actualTargetFileName = replyFile.getURL().toString();
                            replyFile = getTempFileObject(fsManager, replyFile, tempTargetFileName);
                        }

                        // if file locking is not disabled acquire the lock
                        // before uploading the file
                        if (vfsOutInfo.isFileLockingEnabled()) {
                            acquireLockForSending(replyFile, vfsOutInfo);
                            populateResponseFile(replyFile, msgCtx, append, true);
                            VFSUtils.releaseLock(fsManager, replyFile);
                        } else {
                            populateResponseFile(replyFile, msgCtx, append, false);
                        }

                        if (isUseTempFile) {
                            replyFile.moveTo(fsManager.resolveFile(actualTargetFileName));
                        }
                    } else {
                        handleException("Unsupported reply file type : " + replyFile.getType() +
                                " for file : " + vfsOutInfo.getOutFileURI());
                    }
                } else {
                    if (isUseTempFile) {
                        tempTargetFileName = VFSUtils.generateTempFileName();
                        actualTargetFileName = replyFile.getURL().toString();
                        replyFile = getTempFileObject(fsManager, replyFile, tempTargetFileName);
                    }

                    // if file locking is not disabled acquire the lock before uploading the file
                    if (vfsOutInfo.isFileLockingEnabled()) {
                        acquireLockForSending(replyFile, vfsOutInfo);
                        replyFile.createFile();
                        populateResponseFile(replyFile, msgCtx, append, true);
                        VFSUtils.releaseLock(fsManager, replyFile);
                    } else {
                        replyFile.createFile();
                        populateResponseFile(replyFile, msgCtx, append, false);
                    }

                    if (isUseTempFile) {
                        replyFile.moveTo(fsManager.resolveFile(actualTargetFileName));
                    }
                }
            } catch (FileSystemException e) {
                handleException("Error resolving reply file : " +
                        vfsOutInfo.getOutFileURI(), e);
            } finally {
                if (replyFile != null) {
                    try {
                        if (fsManager!= null &&
                            replyFile.getName() != null && replyFile.getName().getScheme() != null &&
                            replyFile.getName().getScheme().startsWith("file") &&
                            replyFile.getParent() != null && replyFile.getParent().getFileSystem() != null) {
                            fsManager.closeFileSystem(replyFile.getParent().getFileSystem());
                        }
                        replyFile.close();
                    } catch (FileSystemException ignore) {}
                }
            }
        } else {
            handleException("Unable to determine out transport information to send message");
        }
    }

    private void populateResponseFile(FileObject responseFile, MessageContext msgContext,
                                      boolean append, boolean lockingEnabled) throws AxisFault {
        
        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
        OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
        
        try {
            CountingOutputStream os = new CountingOutputStream(
                    responseFile.getContent().getOutputStream(append));
            try {
                messageFormatter.writeTo(msgContext, format, os, false);
            } finally {
                os.close();
            }
            
            // update metrics
            metrics.incrementMessagesSent(msgContext);
            metrics.incrementBytesSent(msgContext, os.getByteCount());
            
        } catch (FileSystemException e) {
            if (lockingEnabled) {
                VFSUtils.releaseLock(fsManager, responseFile);
            }
            metrics.incrementFaultsSending();
            handleException("IO Error while creating response file : " +
                            VFSUtils.maskURLPassword(responseFile.getName().getURI()), e);
        } catch (IOException e) {
            if (lockingEnabled) {
                VFSUtils.releaseLock(fsManager, responseFile);
            }
            metrics.incrementFaultsSending();
            handleException("IO Error while creating response file : " +
                            VFSUtils.maskURLPassword(responseFile.getName().getURI()), e);
        }
    }

    private void acquireLockForSending(FileObject responseFile, VFSOutTransportInfo vfsOutInfo)
            throws AxisFault {
        
        int tryNum = 0;
        // wait till we get the lock
        while (!VFSUtils.acquireLock(fsManager, responseFile)) {
            if (vfsOutInfo.getMaxRetryCount() == tryNum++) {
                handleException("Couldn't send the message to file : " +
                                VFSUtils.maskURLPassword(responseFile.getName().getURI()) +
                                ", unable to acquire the lock even after " + tryNum + " retries");
            } else {
                
                log.warn("Couldn't get the lock for the file : "
                        + VFSUtils.maskURLPassword(responseFile.getName().getURI()) + ", retry : " + tryNum
                        + " scheduled after : " + vfsOutInfo.getReconnectTimeout());
                try {
                    Thread.sleep(vfsOutInfo.getReconnectTimeout());
                } catch (InterruptedException ignore) {}
            }
        }
    }

    private FileObject getTempFileObject(FileSystemManager fsManager,
                                         FileObject originalFileObj,
                                         String fileName) throws FileSystemException {
        return fsManager.resolveFile(originalFileObj.getParent(), fileName);
    }
}
