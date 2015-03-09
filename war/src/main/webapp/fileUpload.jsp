<%-- 
    Document   : fileUpload.jsp
    Created on : Jun 13, 2008, 7:37:54 AM
    Author     : dwbrown
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.fds.ar.fileupload.CookieUtil" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>Request Information</h2>
<hr>
Session ID: <%=session.getId() %>
<hr>
<% 
Cookie[] cookies = request.getCookies();
String BASE_DIRECTORY = CookieUtil.getCookieVal(cookies, "BASE_DIRECTORY", "/home/dwrown");
%>
The <CODE>BASE_DIRECTORY</CODE><%=BASE_DIRECTORY%>
<br>
JSP Request Method: <%=request.getMethod()%>
<br>
Context Path: <%=request.getContextPath() %>
<br>
Request URI: <%= request.getRequestURI()%>
<br>
Request Protocol: <%=request.getProtocol()%>
<br>
Servlet path: <%= request.getServletPath()%>
<br>
Path info: <%= request.getPathInfo()%>console/console.portal?_nfpb=true&_pageLabel=AppDeploymentsControlPage
<br>
Query string: <%= request.getQueryString()%>
<br>
Content length: <%= request.getContentLength() %>
<br>
Content type: <%= request.getContentType() %>
<br>
Server name: <%=request.getServerName() %>
<br>
Server port: <%= request.getServerPort() %>
<br>
Remote user: <%= request.getRemoteUser() %>
<br>
Remote address: <%= request.getRemoteAddr() %>
<br>
Remote host: <%= request.getRemoteHost() %>
<br>
Authorization scheme: <%= request.getAuthType() %> 
<br>
Locale: <%= request.getLocale() %>
<hr>
The browser you are using is
<%= request.getHeader("User-Agent") %>
    </body>
</html>
