package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	

	

	Random random=new Random(1000);   //4210

	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	

	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	
	
	
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		System.out.println("Email=>>"+email);
	
		//generating OTP of 4 digit

		int otp = random.nextInt(999999);  //exclusive number random Otp ranger
		System.out.println("OTP=>>"+otp);
		
		
		//write code for send otp to email
		String subject="OTP Form Smart Contact Manager {SCM}";
		String meassge=""
				+"<div style='border:1px solid #e2e2e2;padding:20px;'>"
				+"<h1>"
				+"OTP is:"
				+"<b>"+otp
				+"</b>"
				+"</h1>"
				+"</div>";
		
		String to=email;
		boolean flag=this.emailService.sendEmail(subject, meassge, to);
		if(flag) {
			session.setAttribute("myotp",otp);
			session.setAttribute("email",email);
			return "varify_otp";
		}
		else {
		session.setAttribute("message", "Check your email id..!!");
		return "forgot_email_form";
		}
	}
	
	
	
	
	//verify otp
	@PostMapping("/varify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {


		int myOtp=(int) session.getAttribute("myotp");
		String email=(String) session.getAttribute("email");
		
	 System.out.println("User OTP"+otp);
	 System.out.println("User OTP"+myOtp);
	 
	 
	 if(myOtp==otp) {
		 //password change form
		 
		 User user = this.userRepository.getUserByUserName(email);
		 if(user==null) {
			 
			 //send error message
			 session.setAttribute("message","user does not exits with this email........!!!");
			 return "forgot_email_form";
		 }
		 else {
			 //send change password form
		 }
		 
		 
		 return "password_change_form";
	 }
	 else {
		 session.setAttribute("message","you have entered worng otp.....!!");
		 return "varify_otp";
	 }
		
	}
	
	
	
	
	
	
	
	///change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		String email=(String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?changer=password changed successfully......";
		
		
	}
}
