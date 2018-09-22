package file.watcher

class FileWatcherService {

    /**
     * Use prototype scope to ensure instance of LogController gets its own instance of this class
     * http://docs.grails.org/latest/guide/services.html#scopedServices
     */
    static scope = "prototype"

    static final int MAX_INITIAL_LINES = 10

    String getFileLocation() {
        logFile.absolutePath
    }

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
        lines.size() <= MAX_INITIAL_LINES ? lines : lines[-MAX_INITIAL_LINES..-1]
    }
}
