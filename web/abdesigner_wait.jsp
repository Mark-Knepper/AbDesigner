<%--
    Document   : abdesigner_wait
    Created on : Apr 2, 2026
    Author     : Pisitkun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/includes/header.html"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>AbDesigner - Processing</title>
<style type="text/css">
    @keyframes dots {
        0%, 20% { content: ''; }
        40% { content: '.'; }
        60% { content: '..'; }
        80%, 100% { content: '...'; }
    }
    #status-msg::after {
        content: '';
        animation: dots 1.5s steps(1, end) infinite;
    }
</style>
<%@include file="/includes/title.html"%>

<%
    String proteinName = (String) request.getAttribute("proteinName");
    if (proteinName == null) proteinName = "your protein";
    int sequenceLength = 0;
    Object seqLenAttr = request.getAttribute("sequenceLength");
    if (seqLenAttr != null) sequenceLength = (Integer) seqLenAttr;
%>

<h1>Processing: <%= proteinName %></h1>
<% if (sequenceLength > 1000) { %>
<p>This is a large protein (<%= String.format("%,d", sequenceLength) %> amino acids) &mdash; this may take a few minutes.</p>
<% } %>
<p id="status-msg" style="font-size:1.1em;">Computing</p>

<script type="text/javascript">
    var pollInterval;
    var MAX_WAIT_MS = 10 * 60 * 1000;

    function checkStatus() {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "AbDesignerServlet?action=status", true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                try {
                    var data = JSON.parse(xhr.responseText);
                    if (data.ready) {
                        clearInterval(pollInterval);
                        var msg = document.getElementById("status-msg");
                        msg.style.animation = "none";
                        msg.className = "";
                        msg.textContent = "Done! Loading results...";
                        window.location.href = "AbDesignerServlet?action=result";
                    } else if (data.error) {
                        clearInterval(pollInterval);
                        var msg = document.getElementById("status-msg");
                        msg.style.color = "red";
                        msg.style.animation = "none";
                        msg.className = "";
                        msg.textContent = "Error: " + data.error;
                    }
                } catch (e) {
                    // JSON parse error, keep polling
                }
            }
        };
        xhr.send();
    }

    pollInterval = setInterval(checkStatus, 3000);

    setTimeout(function() {
        clearInterval(pollInterval);
        var msg = document.getElementById("status-msg");
        msg.style.color = "red";
        msg.style.animation = "none";
        msg.className = "";
        msg.textContent = "The computation is taking longer than expected. Please try again.";
    }, MAX_WAIT_MS);
</script>

<%@include file="/includes/footer.html"%>
