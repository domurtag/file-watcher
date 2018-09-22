package file.watcher


import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo

class LogController {

    @MessageMapping("/lines")
    @SendTo("/topic/lines")
    protected String lines(String world) {
        return "hello, ${world}!"
    }
}