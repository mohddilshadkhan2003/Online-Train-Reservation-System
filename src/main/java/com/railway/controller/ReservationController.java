package com.railway.controller;

import com.railway.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute("loginForm") LoginForm loginForm,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (reservationService.authenticate(loginForm.getUsername(), loginForm.getPassword())) {
            session.setAttribute("loggedInUser", loginForm.getUsername());
            return "redirect:/dashboard";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid username or password.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "keyword", required = false) String keyword,
                           HttpSession session,
                           Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", session.getAttribute("loggedInUser"));
        model.addAttribute("bookingForm", new BookingForm());
        model.addAttribute("reservations", reservationService.listReservations(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("trainList", reservationService.findAllTrains());
        return "dashboard";
    }

    @PostMapping("/dashboard/book")
    public String book(@ModelAttribute("bookingForm") BookingForm bookingForm,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        try {
            reservationService.createReservation(
                    bookingForm.getPassenger(),
                    bookingForm.getTrainNo(),
                    bookingForm.getTrainName(),
                    bookingForm.getTravelClass(),
                    bookingForm.getJourneyDate(),
                    bookingForm.getSource(),
                    bookingForm.getDestination()
            );
            redirectAttributes.addFlashAttribute("success", "Booking created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/cancel/{pnr}")
    public String cancel(@PathVariable String pnr,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        try {
            reservationService.cancelReservation(pnr);
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    public static class LoginForm {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class BookingForm {
        private String passenger;
        private String trainNo;
        private String trainName;
        private String travelClass = "Select class";
        private String journeyDate;
        private String source;
        private String destination;

        public String getPassenger() {
            return passenger;
        }

        public void setPassenger(String passenger) {
            this.passenger = passenger;
        }

        public String getTrainNo() {
            return trainNo;
        }

        public void setTrainNo(String trainNo) {
            this.trainNo = trainNo;
        }

        public String getTrainName() {
            return trainName;
        }

        public void setTrainName(String trainName) {
            this.trainName = trainName;
        }

        public String getTravelClass() {
            return travelClass;
        }

        public void setTravelClass(String travelClass) {
            this.travelClass = travelClass;
        }

        public String getJourneyDate() {
            return journeyDate;
        }

        public void setJourneyDate(String journeyDate) {
            this.journeyDate = journeyDate;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }
    }
}
