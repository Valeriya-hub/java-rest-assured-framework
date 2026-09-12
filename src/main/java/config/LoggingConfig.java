package config;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintStream;

public class LoggingConfig {
    private static final Logger log = LogManager.getLogger("RestAssured");

    public static PrintStream loggerPrintStream() {
        OutputStream out = new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    log.info(buffer.toString());
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }
        };

        return new PrintStream(out, true);
    }

    public static RequestLoggingFilter requestFilter() {
        return new RequestLoggingFilter(loggerPrintStream());
    }

    public static ResponseLoggingFilter responseFilter() {
        return new ResponseLoggingFilter(loggerPrintStream());
    }
}


