package com.mathan.sankar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Controller
public class ContClass {
	@RequestMapping("/")
	public ModelAndView defaultPage() {
		return new ModelAndView("login.jsp");
	}

	@RequestMapping("/login")
	public ModelAndView login(HttpServletResponse responses, HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		responses.setContentType("text/html");
		responses.setCharacterEncoding("UTF-8");
		modelAndView.setViewName("login.jsp");
		return modelAndView;
	}

	@RequestMapping("/RegisterServlet")
	public ModelAndView registerAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		ModelAndView modelAndView = new ModelAndView("login.jsp");
		Entity userEntity = null;
		int id = 1;
		Query query = new Query("User");
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		for (@SuppressWarnings("unused")
		Entity e : preparedQuery.asIterable()) {
			id++;
		}
		userEntity = new Entity("User", id);
		userEntity.setProperty("Name", request.getParameter("name"));
		userEntity.setProperty("Username", request.getParameter("username"));
		userEntity.setProperty("Password", request.getParameter("password"));
		userEntity.setProperty("Email", request.getParameter("email"));
		datastoreService.put(userEntity);
		request.setAttribute("messageToClient", "Successfuly Registered. Now Login.");
		return modelAndView;
	}

	@RequestMapping(value = "/LoginServlet", method = RequestMethod.POST)
	public ModelAndView loginAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView modelAndView = new ModelAndView();
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		@SuppressWarnings("deprecation")
		Query query = new Query("User").addFilter("Username", FilterOperator.EQUAL, request.getParameter("username"));
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		int flag = 0;
		for (Entity entity : preparedQuery.asIterable()) {
			flag = 1;
			String password = entity.getProperty("Password").toString();
			String email = entity.getProperty("Email").toString();
			if (password.equals(request.getParameter("password"))) {
				HttpSession session = request.getSession();
				session.setAttribute("name", request.getParameter("username"));
				session.setAttribute("mail", email);
				modelAndView.setViewName("homePage.jsp");
			} else {
				request.setAttribute("messageToClient", "Password Wrong");
				modelAndView.setViewName("login.jsp");
			}
		}
		if (flag == 0) {
			request.setAttribute("messageToClient", "No such user");
			modelAndView.setViewName("login.jsp");
		}
		return modelAndView;
	}

	@RequestMapping("/Logout")
	public ModelAndView logoutAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView modelAndView = new ModelAndView("login.jsp");
		response.setContentType("text/html");
		if (request.getSession().getAttribute("name") != null) {
			request.getSession().removeAttribute("name");
			request.getSession().invalidate();
			request.setAttribute("messageToClient", "Logout Successful.");
		} else {
			response.sendRedirect("login.jsp");
		}

		return modelAndView;
	}

	@RequestMapping("/CheckAvailability")
	public ModelAndView CheckAvailabilityNew(HttpServletRequest request, HttpServletResponse response)
			throws ParseException, IOException {
		ModelAndView modelAndView = new ModelAndView("homePage.jsp");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		String labName = request.getParameter("Labs");
		if (labName.equals("defaultLab")) {
			request.setAttribute("messageToClient", "Please select a lab!!");
			return modelAndView;
		}

		String date = request.getParameter("dateofbooking");
		 //System.out.println("Date from the form = "+date);

		String time = request.getParameter("time") + ":00";
		//System.out.println("Time from the form = "+time);
		request.getSession().setAttribute("time", time);
		
		String newDateAndTime = "";
		DateFormat formatterLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateLocal = formatterLocal.parse(date+" "+time);
		DateFormat formatterUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatterUTC.setTimeZone(TimeZone.getTimeZone("UTC")); // UTC timezone
		newDateAndTime = formatterUTC.format(dateLocal).toString();
		String[] splitDateAndTime = new String[2];
		splitDateAndTime = newDateAndTime.split(" ");	
		date = splitDateAndTime[0];
		time = splitDateAndTime[1];
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		String duration = request.getParameter("duration");
		// System.out.println(duration);
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		@SuppressWarnings("deprecation")
		Query query = new Query("LabDetails").addFilter("LabName", FilterOperator.EQUAL, labName).addFilter("Date",
				FilterOperator.EQUAL, date);
		int flag = 0;
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		List<String> BookedSlots = new ArrayList<>();
		List<String> BookedTimes = new ArrayList<>();
		int bookedSlotsCount = 0;

		for (Entity entity : preparedQuery.asIterable()) {
			flag = 1;
			String bookedSlots = "";
			// System.out.println("Inside the for loop");
			String booked = entity.getProperty("BookedSlots").toString();
			BookedSlots = Arrays.asList(booked.split(", "));
			for (String str : BookedSlots) {
				String[] strarr = str.split(" to ");
				BookedTimes.add(strarr[0]);
				BookedTimes.add(strarr[1]);
			}
			for (int i = 0; i < BookedTimes.size() - 1; i += 2) {
				String start = BookedTimes.get(i);
				String end = BookedTimes.get(i + 1);
				Calendar bookedSlotsStartTime = Calendar.getInstance();
				bookedSlotsStartTime.setTime(formatTime(start));
				bookedSlotsStartTime.add(Calendar.DATE, 1);
				// System.out.println("Start time : " + bookedSlotsStartTime.getTime());

				Calendar bookedSlotsEndTime = Calendar.getInstance();
				bookedSlotsEndTime.setTime(formatTime(end));
				bookedSlotsEndTime.add(Calendar.DATE, 1);
				// System.out.println("End time : " + bookedSlotsEndTime.getTime());

				Calendar requestedSlotStartTime = Calendar.getInstance();
				requestedSlotStartTime.setTime(formatTime(time));
				requestedSlotStartTime.add(Calendar.DATE, 1);
				// System.out.println("Test time 1 : " + requestedSlotStartTime.getTime());

				Date testDateUno = requestedSlotStartTime.getTime();

				bookedSlots = "" + booked + ", " + dateFormat.format(requestedSlotStartTime.getTime());
				if (testDateUno.after(bookedSlotsStartTime.getTime())
						&& testDateUno.before(bookedSlotsEndTime.getTime())
						|| (testDateUno.compareTo(bookedSlotsStartTime.getTime()) == 0)) {
					bookedSlotsCount++;
					break;
				}
				Calendar requestedSlotEndTime = Calendar.getInstance();
				requestedSlotEndTime.setTime(formatTime(time));
				requestedSlotEndTime.add(Calendar.DATE, 1);
				requestedSlotEndTime.add(Calendar.SECOND, Integer.parseInt(duration) * 60 * 60);

				Date testDateDos = requestedSlotEndTime.getTime();

				Calendar cal5 = Calendar.getInstance();
				cal5.setTime(formatTime("21:00:00"));
				cal5.add(Calendar.DATE, 1);
				if (testDateDos.compareTo(cal5.getTime()) > 0) {
					request.setAttribute("messageToClient", "End Time exceeds the lab closing time");
					return modelAndView;
				}
				bookedSlots = bookedSlots + " to " + dateFormat.format(requestedSlotEndTime.getTime());
				// System.out.println("Test time after increment : " +
				// requestedSlotEndTime.getTime());
				if (testDateDos.after(bookedSlotsStartTime.getTime())
						&& testDateDos.before(bookedSlotsEndTime.getTime())
						|| testDateDos.compareTo(bookedSlotsEndTime.getTime()) == 0) {
					bookedSlotsCount++;
					break;
				}

				testDateUno = bookedSlotsStartTime.getTime();
				if (testDateUno.after(requestedSlotStartTime.getTime())
						&& testDateUno.before(requestedSlotEndTime.getTime())
						|| (testDateUno.compareTo(requestedSlotStartTime.getTime()) == 0)) {
					bookedSlotsCount++;
					break;
				}

				testDateDos = bookedSlotsEndTime.getTime();
				if (testDateDos.after(requestedSlotStartTime.getTime())
						&& testDateDos.before(requestedSlotEndTime.getTime())
						|| testDateDos.compareTo(requestedSlotEndTime.getTime()) == 0) {
					bookedSlotsCount++;
					break;
				}
			}
			if (bookedSlotsCount == 0) {
				// System.out.println(bookedSlots);
				List<String> sortedBookedList = Arrays.asList(bookedSlots.split(", "));
				Collections.sort(sortedBookedList);
				String newBookedList = "";
				for (String str : sortedBookedList) {
					// System.out.println(str);
					newBookedList = newBookedList + str + ", ";
				}
				// System.out.println(newBookedList);
				newBookedList = newBookedList.trim();
				newBookedList = newBookedList.substring(0, newBookedList.length() - 1);
				// System.out.println(newBookedList);
				request.getSession().setAttribute("LabName", labName);
				request.getSession().setAttribute("Date", date);
				request.getSession().setAttribute("newBookedList", newBookedList);
				request.setAttribute("messageToClient", "Available");
				String availableSlots = calculateAvailableSlots(newBookedList);
			} else {
				request.setAttribute("messageToClient", "Slot is not available. Please select another slot.");
				return modelAndView;
			}
		}
		// No bookings have been done for that date
		if (flag == 0) {
			Calendar labEndTime = Calendar.getInstance();
			labEndTime.setTime(formatTime("21:00:00"));
			labEndTime.add(Calendar.DATE, 1);
			String bookedSlots = "";
			Calendar slotTime = Calendar.getInstance();
			slotTime.setTime(formatTime(time));
			// System.out.println("Time after adding to the calendar is " +
			// dateFormat.format(slotTime.getTime()));
			bookedSlots = dateFormat.format(slotTime.getTime()) + " to ";
			int dur = Integer.parseInt(duration) * 60 * 60;
			slotTime.add(Calendar.SECOND, dur);
			slotTime.add(Calendar.DATE, 1);
			Date y = slotTime.getTime();
			// System.out.println(labEndTime.getTime());
			if (y.compareTo(labEndTime.getTime()) > 0) {
				request.setAttribute("messageToClient",
						"End Time exceeds the lab closing time. Please choose another time.");
				return modelAndView;
			}
			bookedSlots = bookedSlots + dateFormat.format(slotTime.getTime());
			System.out.println("Time after incrementing the calendar is " + dateFormat.format(slotTime.getTime()));
			System.out.println(bookedSlots);
			request.getSession().setAttribute("LabName", labName);
			request.getSession().setAttribute("Date", date);
			request.getSession().setAttribute("bookedSlots", bookedSlots);
			request.setAttribute("messageToClient", "Available");
		}
		return modelAndView;
	}

	@RequestMapping("/book")
	public ModelAndView BookTheLab(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
			String labName = request.getSession().getAttribute("LabName").toString();
			String date = request.getSession().getAttribute("Date").toString();
			@SuppressWarnings("deprecation")
			Query query = new Query("LabDetails").addFilter("LabName", FilterOperator.EQUAL, labName).addFilter("Date",
					FilterOperator.EQUAL, date);
			PreparedQuery preparedQuery = datastoreService.prepare(query);
			int flag = 0;
			for (Entity entity : preparedQuery.asIterable()) {
				String newBookedList = request.getSession().getAttribute("newBookedList").toString();
				flag = 1;
				entity.setProperty("BookedSlots", newBookedList);
				datastoreService.put(entity);
			}
			if (flag == 0) {
				String bookedSlots = request.getSession().getAttribute("bookedSlots").toString();
				Entity labDetails = new Entity("LabDetails");
				labDetails.setProperty("LabName", labName);
				labDetails.setProperty("Date", date);
				labDetails.setProperty("BookedSlots", bookedSlots);
				datastoreService.put(labDetails);
			}

			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("mathug123@gmail.com", "Lab Booking Project"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(request.getSession().getAttribute("mail").toString(), "Mr. "+request.getSession().getAttribute("name")));
			msg.setSubject("Booking Confirmation");
			msg.setText("Your lab has been booked for the date " + date+".\n The time is " +request.getSession().getAttribute("time").toString());
			Transport.send(msg);

		} catch (Exception exception) {
			response.sendRedirect("homePage.jsp");
		}

		// Send the mail in this part.

		request.getSession().removeAttribute("newBookedList");
		request.getSession().removeAttribute("LabName");
		request.getSession().removeAttribute("Date");

		ModelAndView modelAndView = new ModelAndView("homePage.jsp");
		return modelAndView;
	}

	private String calculateAvailableSlots(String bookedSlots) {
		// TODO Auto-generated method stub
		// Starting time = 10:00
		// End time = 21:00

		return null;
	}

	public Date formatTime(String time) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = dateFormat.parse(time);
		return date;
	}

	public Date formatDate(String date) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd");
		Date formatDate = dateFormat.parse(date);
		return formatDate;
	}
}
