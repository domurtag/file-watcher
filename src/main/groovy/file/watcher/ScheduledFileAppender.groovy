package file.watcher

import groovy.util.logging.Slf4j

import java.time.LocalTime
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * A thread-safe class that appends lines to a file at regular intervals
 */
@Slf4j
class ScheduledFileAppender {

    private final File file

    private Timer fileWritingSchedule

    private final Lock lock = new ReentrantLock()

    private int lineCount = 0

    /**
     * Create an instance. Appending will not begin until start() is called
     * @param file the file to be appended to
     * @throws FileNotFoundException if file does not exist
     */
    ScheduledFileAppender(File file) throws FileNotFoundException {
        this.file = file

        if (!file.exists()) {
            throw new FileNotFoundException("File not found at path: ${file.absolutePath}")
        }
    }

    /**
     * Start writing lines to the file. Calling this method when the appender is already running will have no effect.
     * @param frequencyMs specifies the interval in milliseconds between each additional line being appended
     */
    void start(int frequencyMs) {
        lock.lock()

        try {
            if (!fileWritingSchedule) {
                fileWritingSchedule = new Timer('fileWritingSchedule', true)
                TimerTask fileAppender = [run: { -> appendLine() }] as TimerTask
                fileWritingSchedule.schedule(fileAppender, 0, frequencyMs)
            }
        } finally {
            lock.unlock()
        }
    }

    private appendLine() {
        int number = Random.newInstance().nextInt()
        file.append "Random number ${number}. This line was written at: ${LocalTime.now()}${System.lineSeparator()}"
        log.info "${++lineCount} line(s) added by the appender"
    }

    boolean isRunning() {
        fileWritingSchedule != null
    }

    /**
     * Stop writing lines to the file. Calling this method when the appender is not running will have no effect.
     */
    void stop() {
        lock.lock()

        try {
            if (fileWritingSchedule) {
                fileWritingSchedule.cancel()
                fileWritingSchedule = null
            }
        } finally {
            lock.unlock()
        }
    }
}
