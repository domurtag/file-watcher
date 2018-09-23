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
                    var lengthBefore = $("#file-lines li").length;
                    $('#file-lines ul').append(message.body);
                    var linesAdded = $("#file-lines li").length - lengthBefore;

                    if (linesAdded) {
                        toastr.clear();
                        toastr.info(linesAdded + ' new log lines added');
                    }
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
        <g:remoteLink action="startWriting" class="btn btn-success">
            Start appender
        </g:remoteLink>
        <g:remoteLink action="stopWriting" class="btn btn-danger">
            Stop appender
        </g:remoteLink>
    </div>

    <div id="file-lines">
        <h2>Log file content</h2>
        <ul class="list-group">
            <g:render template="lines"/>
        </ul>
    </div>
</div>
</body>
</html>
