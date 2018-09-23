package file.watcher

import grails.gsp.PageRenderer
import org.springframework.messaging.simp.SimpMessageSendingOperations

import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap

class FileWatcherService {

    SimpMessageSendingOperations brokerMessagingTemplate

    PageRenderer groovyPageRenderer

    static final int MAX_INITIAL_LINES = 10

    /**
     * Get the logfile
     * @return
     */
    File getLogFile() {
        logFile
    }

    private final Map<String, Integer> lastLineIndices = new ConcurrentHashMap<String, Integer>()

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
        String clientId = UUID.randomUUID().toString()
        lastLineIndices[clientId] = lines.size()
        def logFileLines = lines.size() <= MAX_INITIAL_LINES ? lines : lines[-MAX_INITIAL_LINES..-1]

        // start listening for changes to the log file
        Thread.startDaemon('logFileWatcher') { -> registerListener(clientId) }
        logFileLines
    }

    /**
     * Listen for changes to the log file. Any new lines will be sent to the client via the WebSocket
     * @return
     */
    private registerListener(String clientId) {

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
                        def lineCount = allLines.size()
                        Integer lastLineIndex = lastLineIndices[clientId]

                        if (lineCount > lastLineIndex) {
                            def newLines = allLines[lastLineIndex..-1]
                            log.info "Total line count: ${lineCount}. Found new lines beginning at index $lastLineIndex"
                            lastLineIndices[clientId] = lineCount
                            def newLinesMarkup = groovyPageRenderer.render(template: '/log/lines', model: [lines: newLines])
                            brokerMessagingTemplate.convertAndSend "/topic/lines", newLinesMarkup
                        }
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
