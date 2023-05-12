import java.util.logging.Formatter
import java.util.logging.LogRecord

/**
 * Formatter class for java logger
 */
class LogConsoleFormatter : Formatter() {
    override fun format(record: LogRecord?): String {
        return "[${record?.level}: ${record?.loggerName}] ${record?.message}\n"
    }
}