package com.cafemanager.cafe.controller;

import com.cafemanager.cafe.entity.Student;
import com.cafemanager.cafe.entity.User;
import com.cafemanager.cafe.model.AddFundsRequest;
import com.cafemanager.cafe.model.ChargeRequest;
import com.cafemanager.cafe.service.StripeService;
import com.cafemanager.cafe.service.StudentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    @Value("${stripe.public.key}")
    private String stripePublicKey;

    private final StripeService paymentsService;
    private final StudentService studentService;

    public PaymentController(StripeService paymentsService, StudentService studentService) {
        this.paymentsService = paymentsService;
        this.studentService = studentService;
    }

    @PostMapping("/charge")
    public String charge(ChargeRequest chargeRequest, Model model)
            throws StripeException {
        chargeRequest.setDescription("Funds added on date: " + java.time.LocalDate.now()+" by user: "+chargeRequest.getStudentId());
        chargeRequest.setCurrency(ChargeRequest.Currency.USD);
        Charge charge = paymentsService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());

        studentService.addFunds(chargeRequest,charge);
        return "result";
    }
    @GetMapping("/addfunds")
    public String loadAddFundsPage(Model model, @RequestParam("studentId") Integer studentId){
        Student student = studentService.findById(studentId);
        model.addAttribute("student", student);
        return "addfunds";
    }
    @PostMapping("/add-funds")
    public  String loadCheckOutPage(Model model, @ModelAttribute AddFundsRequest addFundsRequest){
        model.addAttribute("studentId", addFundsRequest.userId());
        model.addAttribute("amount", (addFundsRequest.amount() * 100)); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.USD);
        return "checkout";
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}
