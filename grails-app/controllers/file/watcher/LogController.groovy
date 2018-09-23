package file.watcher

class LogController {

    FileWatcherService fileWatcherService

    private Timer fileWritingSchedule

    private final ScheduledFileAppender logFileAppender = new ScheduledFileAppender()

    /**
     * Render the home page
     * @return
     */
    def index() {
        [
                logFile: fileWatcherService.fileLocation,
                lines: fileWatcherService.mostRecentLines,
                isAppenderRunning: fileWritingSchedule != null
        ]
    }

    /**
     * Start automatically appending lines to the logfile at regular intervals
     * @return
     */
    def startWriting() {
        logFileAppender.start(400)
        redirect(action: "index")
    }

    /**
     * Stop appending lines to the logfile
     * @return
     */
    def stopWriting() {
        logFileAppender.stop()
        redirect(action: "index")
    }
}