package file.watcher

class LogController {

    FileWatcherService fileWatcherService

    private Timer fileWritingSchedule

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
        if (!fileWritingSchedule) {
            fileWritingSchedule = new Timer('fileWritingSchedule', true)
            TimerTask fileAppender = [run: { -> fileWatcherService.appendLine() }] as TimerTask
            long frequencyMs = 400
            fileWritingSchedule.schedule(fileAppender, 0, frequencyMs)
        }
        redirect(action: "index")
    }

    /**
     * Stop appending lines to the logfile
     * @return
     */
    def stopWriting() {
        if (fileWritingSchedule) {
            fileWritingSchedule.cancel()
            fileWritingSchedule = null
        }
        redirect(action: "index")
    }
}