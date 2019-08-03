<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="css/select.css">
<style>
::-webkit-datetime-edit-text {
	color: black;
	padding: 0 0.3em;
}

::-webkit-datetime-edit-month-field {
	color: black;
}

::-webkit-datetime-edit-day-field {
	color: black;
}

::-webkit-datetime-edit-year-field {
	color: black;
}
</style>
<script>
	function alertFunction(message) {
		if (message.length > 0 && message != "Available")
			alert(message);
		else if (message == "Available") {
			var x = confirm("Slot available. Do you want to book it?");
			if (x == true) {
				var xhttp = new XMLHttpRequest();
				xhttp.onreadystatechange = function() {
					if (this.readyState == 4 && this.status == 200) {
						alert("Slot Booked");
					}
				};
				xhttp.open("GET", "/book", true);
				xhttp.send();
			} else {
				alert("Not booked");
			}
			//document.getElementById("requestToBook").innerHTML = "Slot Available. Click <a href = 'book'>here</a> to book it.<br>";
		}
		var today = new Date();
		var dd = String(today.getDate()).padStart(2, '0');
		var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
		var yyyy = today.getFullYear();
		today = yyyy + "-" + mm + "-" + dd;
		var maxDate = new Date();
		maxDate.setDate(maxDate.getDate() + 14);
		dd = String(maxDate.getDate()).padStart(2, '0');
		mm = String(maxDate.getMonth() + 1).padStart(2, '0');
		yyyy = maxDate.getFullYear();
		maxDate = yyyy + "-" + mm + "-" + dd;
		document.getElementById("datebook").min = today;
		document.getElementById("datebook").max = maxDate;
	}
</script>

<title>Home Page</title>
</head>
<body onload="alertFunction('${messageToClient}')">
	<center>
		<p id="requestToBook"></p>
	</center>
	<%
		request.removeAttribute("messageToClient");
	%>
	<%
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		if (session.getAttribute("name") == null) {
			response.sendRedirect("login.jsp");
	%>
	<%
		} else {
	%>
	<div class="login-page">
		<center>
			<h3>Choose the lab and the timings from bellow</h3>
		</center>
		<center>
			<p id="requestToBook"></p>
		</center>
		<div class="form">
			<form class="login-form" action="CheckAvailability" method="post">
				<div class="custom-select" style="width: 270px;">
					<select name="Labs" id="Labs">
						<option value="defaultLab">Select Lab:</option>
						<option value="Lab 1">Lab 1</option>
						<option value="Lab 2">Lab 2</option>
						<option value="Lab 3">Lab 3</option>
					</select>
				</div>
				<br> Select Date <input type="date" name="dateofbooking"
					id="datebook" required> Enter the time(local)<input type="time"
					name="time" min="10:00" max="21:00" required> Enter the
				duration(in hrs)<input type="number" name="duration" min="1" max="5"
					required>
				<button>Check Availability</button>

			</form>
			<br> <a href="Logout"
				style="text-decoration: none; color: white;"><button>Logout</button></a>
		</div>
	</div>
	<%
		}
	%>
	<script src="js/select_javascript.js"></script>
</body>
</html>