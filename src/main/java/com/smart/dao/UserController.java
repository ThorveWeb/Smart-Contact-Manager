package com.smart.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;



import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {

		String username = principal.getName();
		System.out.println("USERNAME====="+username);
		
		//get the user using username={email}
		User  user= userRepository.getUserByUserName(username);
		System.out.println("User==="+user);		
		model.addAttribute("user", user);
	
	}
	
	
	// Dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
	
		return "normal/user_dashboard";
	}
	
	
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add contct");
		model.addAttribute("contact", new Contact());
		return "normal/add_contanct_form";
	}
	
	
	//processing add contact form  --54
	@PostMapping("/process-contact")
	public String processContent(@ModelAttribute Contact contact,
			Principal principal,
			@RequestParam("profileImage") MultipartFile file,
			HttpSession session
			) {
		
	try {
		
	
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		//processing and uploading.. file.
		if(file.isEmpty()) {
			//if the file is empty then try our message
			System.out.println("File is Empty");
			contact.setImage("default.png");
		}
		else {
			//file the file to folder and update the name to contant
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image upload......");
		}
		
		user.getContacts().add(contact);		
		
		//user id display and contact.java to string comment
		contact.setUser(user);
		this.userRepository.save(user);
		
		System.out.println("Data=="+contact);
		System.out.println("Added to data base");
		
		
		
		//message alert
		session.setAttribute("message", new Message("Your contact is added...!!Add More.....","success"));
		
	}
	catch(Exception e) {
		System.out.println("Error.."+e.getMessage());
		//message alert
				session.setAttribute("message", new Message("Some went wrong...!!Try again.....","danger"));
			
	}
		return "normal/add_contanct_form";
	}
	
	
	
	
	
	
	
	//show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(Model m,Principal principal,@PathVariable("page") Integer page)
	{
		m.addAttribute("title", "Show User contacts");
		//contact ki list ko bhejni hai

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		//current page 
		//contact per page-5
		PageRequest pageable=PageRequest.of(page, 4);
		
	
		
	//	List<Contact> contacts=user.getContacts();
		Page<Contact> contacts=this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	
	
	
	
	//showing partical contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
      
		//only same user information display bug
		String userName=principal.getName();
	    User user = this.userRepository.getUserByUserName(userName);
	    if(user.getId()==contact.getUser().getId()) {

			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
	    }
	    
		
		return "normal/contact_detail";
	}
	
	
	//delete contact handler one data

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session) {
	
		System.out.println("CID"+cId);
		
		Contact contact = this.contactRepository.findById(cId).get();
		//check..Assigment....
		System.out.println("Contact"+contact.getcId());
		
		contact.setUser(null);
		
		this.contactRepository.delete(contact);
		session.setAttribute("message", new Message("Contact deleted succesfully.....","success"));
		return "redirect:/user/show-contacts/0";
	}

	
	
	//upload form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		
     return "normal/update_form";
	}
	
	
	
	
	
	
	//update contact handler
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandle(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			HttpSession session,
			Model m,
			Principal principal) {
		
	try {
		//old contact details
		Contact oldcontactdetail = this.contactRepository.findById(contact.getcId()).get();
		
		//image..
		if(!file.isEmpty()) {
			//file work...
			//rewirte...			
			
			//upadte new image
           File saveFile = new ClassPathResource("static/img").getFile();			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
			
			contact.setImage(file.getOriginalFilename());
			
		}else {
			contact.setImage(oldcontactdetail.getImage());
		
		}
		//current user...
		User user=this.userRepository.getUserByUserName(principal.getName());
		contact.setUser(user);
		this.contactRepository.save(contact);
		session.setAttribute("message", new Message("Your contact is update....","success"));
	}
	catch(Exception e) {
		e.printStackTrace();
	}
		System.out.println("Contact name"+contact.getName());
		System.out.println("Contact name"+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	//your profile handler
		@GetMapping("/settings")
		public String changepassword(Model model) {
			
			model.addAttribute("title", "Setting Page");
			return "normal/settings";
		}
		
		
		
		
		///creating order for payment
		@PostMapping("/create_order")
        @ResponseBody		
		public String crateOrder(@RequestBody Map<String, Object> data) {
			System.out.println(data);
			
			int amt=Integer.parseInt(data.get("amount").toString());
			
			return "";
		}
		
}
