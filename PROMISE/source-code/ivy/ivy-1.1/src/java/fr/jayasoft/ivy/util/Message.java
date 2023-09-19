/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author x.hanin
 *
 */
public class Message {
    // messages level copied from ant project, to avoid dependency on ant
    /** Message priority of "error". */
    public static final int MSG_ERR = 0;
    /** Message priority of "warning". */
    public static final int MSG_WARN = 1;
    /** Message priority of "information". */
    public static final int MSG_INFO = 2;
    /** Message priority of "verbose". */
    public static final int MSG_VERBOSE = 3;
    /** Message priority of "debug". */
    public static final int MSG_DEBUG = 4;

    private static MessageImpl _impl = new DefaultMessageImpl(2);

    private static StringBuffer _problems = new StringBuffer();
    
    private static boolean _showProgress = true;
    
    public static void init(MessageImpl impl) {
        _impl = impl;
        showInfo();
    }

    private static void showInfo() {
        Properties props = new Properties();
        InputStream module = Message.class.getResourceAsStream("/module.properties");
        if (module != null) {
            try {
                props.load(module);
                info(":: Ivy "+props.getProperty("version")+" - "+props.getProperty("date")+" :: http://ivy.jayasoft.org/ ::");
            } catch (IOException e) {
                info(":: Ivy non official version :: http://ivy.jayasoft.org/ ::");
            }
        } else {
            info(":: Ivy non official version :: http://ivy.jayasoft.org/ ::");
        }
    }

    public static void debug(String msg) {
        if (_impl != null) {
            _impl.log(msg, MSG_DEBUG);
        } else {
            System.err.println(msg);
        }
    }
    public static void verbose(String msg) {
        if (_impl != null) {
            _impl.log(msg, MSG_VERBOSE);
        } else {
            System.err.println(msg);
        }
    }
    public static void info(String msg) {
        if (_impl != null) {
            _impl.log(msg, MSG_INFO);
        } else {
            System.err.println(msg);
        }
    }
    public static void warn(String msg) {
        if (_impl != null) {
            _impl.log("WARN: "+msg, MSG_WARN);
        } else {
            System.err.println(msg);
        }
        _problems.append("\tWARN:  "+msg).append("\n");
    }
    public static void error(String msg) {
        if (_impl != null) {
            _impl.log("ERROR: "+msg, MSG_VERBOSE);
        } else {
            System.err.println(msg);
        }
        _problems.append("\tERROR: "+msg).append("\n");
    }
    public static void sumupProblems() {
        if (_problems.length() > 0) {
            info("\n:: problems summary ::");
            info(_problems.toString());
            info("\t--- USE VERBOSE OR DEBUG MESSAGE LEVEL FOR MORE DETAILS ---");
            _problems = new StringBuffer();
        }
    }

    public static void progress() {
        if (_showProgress) {
            _impl.progress();
        }
    }

    public static void endProgress() {
        endProgress("");
    }

    public static void endProgress(String msg) {
        if (_showProgress) {
            _impl.endProgress(msg);
        }
    }

    public static boolean isShowProgress() {
        return _showProgress;
    }
    public static void setShowProgress(boolean progress) {
        _showProgress = progress;
    }
}
