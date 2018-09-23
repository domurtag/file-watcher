package file.watcher

import grails.gsp.PageRenderer
import org.springframework.messaging.simp.SimpMessageSendingOperations

import java.nio.file.*

class FileWatcherService {

    SimpMessageSendingOperations brokerMessagingTemplate

    PageRenderer groovyPageRenderer

    /**
     * Use prototype scope to ensure instance of LogController gets its own instance of this class
     * http://docs.grails.org/latest/guide/services.html#scopedServices
     */
    static scope = "prototype"

    static final int MAX_INITIAL_LINES = 10

    /**
     * Get the logfile
     * @return
     */
    File getLogFile() {
        logFile
    }

    private int lastLineIndex = 0;

    /**
     * Create the log file if it doesn't exist
     */
    private static File logFile = {
        def tempDir = System.getProperty('java.io.tmpdir')
        def file = new File(tempDir, 'log.txt')

        if (!file.exists()) {
            assert file.createNewFile(), "Failed to create log file at ${file.absolutePath}"
        }
        file
    }()

    /**
     * Get the most recent lines in the file up to a maximum of 10
     * @return
     */
    List<String> getMostRecentLines() {
        def lines = logFile.readLines()
        lastLineIndex = lines.size()
        def logFileLines = lines.size() <= MAX_INITIAL_LINES ? lines : lines[-MAX_INITIAL_LINES..-1]

        // start listening for changes to the log file
        Thread.startDaemon('logFileWatcher') { -> registerListener() }
        logFileLines
    }

    /**
     * Listen for changes to the log file. Any new lines will be sent to the client via the WebSocket
     * @return
     */
    private registerListener() {

        FileSystem fileSystem = FileSystems.default
        Path logFileParentDir = fileSystem.getPath(logFile.parentFile.absolutePath)

        fileSystem.newWatchService().withCloseable { WatchService watchService ->
            logFileParentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

            while (true) {
                WatchKey watchKey = watchService.take()
                watchKey.pollEvents().each { event ->
                    // we're only listening to ENTRY_MODIFY events so the context is always a Path
                    Path changed = (Path) event.context()

                    // Individual files can't be watched, only directories, so check that the event target is the logfile
                    if (changed.endsWith(logFile.name)) {
                        def allLines = logFile.readLines()
                        def newLines = allLines[lastLineIndex..-1]

                        log.info "Total line count: ${allLines.size()}. Found new lines beginning at index $lastLineIndex"
                        lastLineIndex = allLines.size()
                        def newLinesMarkup = groovyPageRenderer.render(template: '/log/lines', model: [lines: newLines])
                        brokerMessagingTemplate.convertAndSend "/topic/lines", newLinesMarkup
                    }
                }

                // if the key is invalid (e.g. because the dir was deleted), stop watching
                if (!watchKey.reset()) {
                    watchKey.cancel()
                    break
                }
            }
        }
    }
}
