<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">

<head>
<meta charset="UTF-8">
<title>Login</title>
<link rel="stylesheet" href="css/style.css">
<script>
	function alertFunction(message) {
		if (message.length > 0)
			alert(message);
	}
</script>
</head>
<body onload="alertFunction('${messageToClient}')">
	<%
		request.removeAttribute("messageToClient");
	%>
	<%
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		if (session.getAttribute("name") != null) {
			response.sendRedirect("homePage.jsp");
	%>
	<%
		} else {
	%>
	<div class="login-page">
		<div class="form">
			<form class="login-form" action="LoginServlet" method="post">
				<input type="text" placeholder="username" name="username" autofocus
					required /> <input type="password" placeholder="password"
					name="password" required />
				<button>login</button>
				<p class="message" style="color: black;">
					Not registered? <a href="register.jsp">Create an account</a>
				</p>
			</form>
		</div>
	</div>
	<script
		src='http://cdnjs.cloudflare.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>

	<%
		}
	%>

</body>

</html>
