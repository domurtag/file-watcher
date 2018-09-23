package file.watcher

import javax.annotation.PostConstruct

class LogController {

    FileWatcherService fileWatcherService

    private ScheduledFileAppender logFileAppender

    @PostConstruct
    private initFileAppender() {
        logFileAppender = new ScheduledFileAppender(fileWatcherService.logFile)
    }

    /**
     * Render the home page
     * @return
     */
    def index() {
        [
                logFile: fileWatcherService.logFile.absolutePath,
                lines: fileWatcherService.mostRecentLines,
                isAppenderRunning: logFileAppender.running
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