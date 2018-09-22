<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>

    <asset:javascript src="application" />
    <asset:javascript src="spring-websocket" />

    <script type="text/javascript">
        $(function() {
            var socket = new SockJS("${createLink(uri: '/stomp')}");
            var client = Stomp.over(socket);

            client.connect({}, function() {
                client.subscribe("/topic/lines", function(message) {
                    console.log("Received: " + message.body);
                });
            });

            $("#helloButton").click(function() {
                client.send("/app/lines", {}, "world");
            });
        });
    </script>
</head>
<body>

<div class="container">
    <div class="page-header">
        <h1>Content of file at: ${logFile}</h1>
    </div>

    <ul id="file-lines">
        <g:each in="${lines}" var="line">
            <li>${line}</li>
        </g:each>
    </ul>

    <button id="helloButton">hello</button>
</div>
</body>
</html>