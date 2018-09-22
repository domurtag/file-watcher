package file.watcher


import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo

class LogController {

    FileWatcherService fileWatcherService
    private Timer fileWritingSchedule

    def index() {
        [
                logFile: fileWatcherService.fileLocation,
                lines: fileWatcherService.mostRecentLines,
                isAppenderRunning: fileWritingSchedule != null
        ]
    }

    @MessageMapping("/lines")
    @SendTo("/topic/lines")
    protected String lines(String world) {
        return "hello, ${world}!"
    }

    def startWriting() {
        if (!fileWritingSchedule) {
            fileWritingSchedule = new Timer('fileWritingSchedule', true)
            TimerTask fileAppender = [run: { -> fileWatcherService.appendLine() }] as TimerTask
            long frequencyMs = 200
            fileWritingSchedule.schedule(fileAppender, 0, frequencyMs)
        }
        redirect(action: "index")
    }

    def stopWriting() {
        if (fileWritingSchedule) {
            fileWritingSchedule.cancel()
            fileWritingSchedule = null
        }
        redirect(action: "index")
    }
}