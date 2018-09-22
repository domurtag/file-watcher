<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>

    <asset:javascript src="application"/>
    <asset:javascript src="spring-websocket"/>

    <script type="text/javascript">
        $(function () {
            var socket = new SockJS("${createLink(uri: '/stomp')}");
            var client = Stomp.over(socket);

            client.connect({}, function () {
                client.subscribe("/topic/lines", function (message) {
                    console.log("Received: " + message.body);
                    $('#file-lines ul').append(message.body);
                });
            });
        });
    </script>
</head>

<body>

<div class="container">
    <div class="page-header">
        <h1>Log file location: ${logFile}</h1>
    </div>

    <div class="btn-group" role="group">
        <g:link action="startWriting" class="btn btn-success ${isAppenderRunning ? 'disabled' : ''}">
            Start appender
        </g:link>
        <g:link action="stopWriting" class="btn btn-danger ${isAppenderRunning ? '' : 'disabled'}">
            Stop appender
        </g:link>
    </div>

    <div id="file-lines">
        <h2>Log file content</h2>
        <ul class="list-group">
            <g:render template="newLines" model="line: lines"/>
        </ul>
    </div>
</div>
</body>
</html>
