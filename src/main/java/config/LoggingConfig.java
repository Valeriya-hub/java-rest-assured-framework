package config;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class LoggingConfig {
    private static final Logger log = LogManager.getLogger("RestAssured");

    public static PrintStream loggerPrintStream() {
        OutputStream out = new OutputStream() {

// One buffer per thread, so parallel tests can never mix their characters.
// Bytes are collected first and decoded as UTF-8 once a whole line is complete.
            private final ThreadLocal<ByteArrayOutputStream> buffer =
                    ThreadLocal.withInitial(ByteArrayOutputStream::new);

            @Override
            public void write(int b) {
                ByteArrayOutputStream current = buffer.get();
                if (b == '\n') {
                    log.info(current.toString(StandardCharsets.UTF_8));
                    current.reset();
                } else {
                    current.write(b);
                }
            }
        };

        return new PrintStream(out, true, StandardCharsets.UTF_8);
    }

    public static RequestLoggingFilter requestFilter() {
        return new RequestLoggingFilter(loggerPrintStream());
    }

    public static ResponseLoggingFilter responseFilter() {
        return new ResponseLoggingFilter(loggerPrintStream());
    }
}


