/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */
package android.pattern.util;

import android.util.Log;

import java.util.Hashtable;

public class Logger {
    public static boolean logFlag = true;
    private final static int logLevel = Log.VERBOSE;
    private static Hashtable<String, Logger> sLoggerTable = new Hashtable<String, Logger>();

    private String mUserName;

    private Logger(String name) {
        mUserName = name;
    }

    /**
     * @param
     * @return
     */
    @SuppressWarnings("unused")
    public static Logger getLogger(String userName) {
        Logger classLogger = (Logger) sLoggerTable.get(userName);
        if (classLogger == null) {
            classLogger = new Logger(userName);
            sLoggerTable.put(userName, classLogger);
        }
        return classLogger;
    }

    /**
     * Get The Current Function Name
     *
     * @return
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return mUserName + "[ " + Thread.currentThread().getName() + ": " + st.getFileName()
                    + ":" + st.getLineNumber() + " " + st.getMethodName() + " ]";
        }
        return null;
    }

    /**
     * The Log Level:i
     *
     * @param str
     */
    public void i(String TAG, Object str) {
        if (logFlag) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                if (name != null) {
                    Log.i(TAG, name + " - " + str);
                } else {
                    Log.i(TAG, str.toString());
                }
            }
        }

    }

    /**
     * The Log Level:d
     *
     * @param str
     */
    public void d(String TAG, Object str) {
        if (logFlag) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                if (name != null) {
                    Log.d(TAG, name + " - " + str);
                } else {
                    Log.d(TAG, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:V
     *
     * @param str
     */
    public void v(String TAG, Object str) {
        if (logFlag) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                if (name != null) {
                    Log.v(TAG, name + " - " + str);
                } else {
                    Log.v(TAG, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:w
     *
     * @param str
     */
    public void w(String TAG, Object str) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                if (name != null) {
                    Log.w(TAG, name + " - " + str);
                } else {
                    Log.w(TAG, str.toString());
                }
            }
        }
    }

    public void w(String TAG, Object str, Exception ex) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                if (str.toString().equals("")) {
                    Log.w(TAG, ex.getMessage(), ex);
                } else {
                    Log.w(TAG, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param str
     */
    public void e(String TAG, Object str) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(TAG, name + " - " + str);
                } else {
                    Log.e(TAG, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param ex
     */
    public void e(String TAG, Object str, Exception ex) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                if (str.toString().equals("")) {
                    Log.e(TAG, ex.getMessage(), ex);
                } else {
                    Log.e(TAG, str.toString());
                }

            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param log
     * @param tr
     */
    public void e(String TAG, String log, Throwable tr) {
        if (logFlag) {
            String line = getFunctionName();
            Log.e(TAG, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mUserName + line
                    + ":] " + log + "\n", tr);
        }
    }
}