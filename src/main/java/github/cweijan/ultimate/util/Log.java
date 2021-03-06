package github.cweijan.ultimate.util;

import kotlin.jvm.JvmStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

    private static Logger loggerChecker = LoggerFactory.getLogger(Log.class);

    public static Logger getLogger() {

        return getLoggerInner();
    }

    private static Logger getLoggerInner() {
        StackTraceElement stackTrace = (new Throwable()).getStackTrace()[2];
        return LoggerFactory.getLogger(stackTrace.getClassName());
    }

    @JvmStatic
    public static void error(Object content) {
        if (loggerChecker.isErrorEnabled()) {
            if (content instanceof String) {
                getLoggerInner().error(content+"");
            } else {
                getLoggerInner().error(content != null ? Json.toJson(content) : null, "");
            }
        }

    }

    @JvmStatic
    public static void error(Object content, Throwable throwable) {
        if (loggerChecker.isErrorEnabled()) {
            if (content instanceof String) {
                getLoggerInner().error(content+"",throwable);
            } else {
                getLoggerInner().error(content != null ? Json.toJson(content) : null, "",throwable);
            }
        }

    }

    @JvmStatic
    public static void warn(Object content) {
        if (loggerChecker.isWarnEnabled()) {
            if (content instanceof String) {
                getLoggerInner().warn(content+"");
            } else {
                getLoggerInner().warn(content != null ? Json.toJson(content) : null, "");
            }
        }

    }

    @JvmStatic
    public static void debug(Object content) {
        if (loggerChecker.isDebugEnabled()) {
            if (content instanceof String) {
                getLoggerInner().debug(content+"");
            } else {
                getLoggerInner().debug(content != null ? Json.toJson(content) : null, "");
            }
        }

    }

    @JvmStatic
    public static void info(Object content) {
        if (loggerChecker.isInfoEnabled()) {
            if (content instanceof String) {
                getLoggerInner().info(content+"");
            } else {
                getLoggerInner().info(content != null ? Json.toJson(content) : null, "");
            }
        }

    }

    private Log() {
    }
}
