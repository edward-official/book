<%--
  Created by IntelliJ IDEA.
  User: antinori
  Date: 09/06/2025
  Time: 22:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <h1>[number 1]: ${param.data1}</h1>
    <h1>[number 2]: ${param.data2}</h1>
    <h1>[sum]: ${Integer.parseInt(param.data1) + Integer.parseInt(param.data2)}</h1>
</body>
</html>
