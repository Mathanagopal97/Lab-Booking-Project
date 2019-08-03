<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
	<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Register Page</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<c:if test="${not empty messageToClient}">  
        ${messageToClient}<br />
</c:if></center>
<%request.removeAttribute("messageToClient"); %>
<div class="login-page">
		<div class="form">
			<form class="register-form" action="RegisterServlet" method="post">
				<p class="errormsg" id="errormsg"></p>
			</form>
			<form class="login-form" action="RegisterServlet" method="post">
				<input type="text" placeholder="name" name="name" autofocus required /> <input
					type="text" placeholder="username" name="username" required /> <input
					type="password" placeholder="password" name="password" required />
				<input type="text" placeholder="email" name="email" required />
				<button>create</button>
				<p class="message" style="color: black;">
					Already registered? <a href="login.jsp">Sign In</a>
				</p>
			</form>
		</div>
	</div>
</body>
</html>