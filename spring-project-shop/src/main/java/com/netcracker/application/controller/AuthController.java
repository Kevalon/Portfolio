package com.netcracker.application.controller;

import com.netcracker.application.controller.form.ProfileEditForm;
import com.netcracker.application.controller.form.UserRegistrationForm;
import com.netcracker.application.service.UserServiceImpl;
import com.netcracker.application.service.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class AuthController {
    private final UserServiceImpl userService;

    @Autowired
    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        model.addAttribute("logout", true);
        SecurityContextHolder.getContext().setAuthentication(null);
        return "auth/login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new UserRegistrationForm());
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") UserRegistrationForm userRegistrationForm, Model model) {

        String errorMessage = userService.checkRegistrationValidity(userRegistrationForm);
        if (!errorMessage.equals("")) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", errorMessage);
            userRegistrationForm.setPassword("");
            userRegistrationForm.setUsername("");
            userRegistrationForm.setConfirmPassword("");
            model.addAttribute("user", userRegistrationForm);
            return "auth/registration";
        }

        User user = new User();
        user.setUsername(userRegistrationForm.getUsername());
        user.setPassword(userRegistrationForm.getPassword());
        user.setEmail(userRegistrationForm.getEmail());
        userService.signupUser(user);
        model.addAttribute("successfulRegistration", true);

        return "auth/login";
    }

    @GetMapping("/profile")
    public String profile() {
        return "auth/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        ProfileEditForm profileEditForm = userService.getProfileEditForm(userService.getCurrentUser());
        model.addAttribute("profileInfoForm", profileEditForm);
        return "auth/editProfile";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@ModelAttribute("profileInfoForm") ProfileEditForm profileEditForm) {
        User user = userService.getCurrentUser();
        user.setFirstName(profileEditForm.getFirstName());
        user.setLastName(profileEditForm.getLastName());
        user.setAddress(profileEditForm.getAddress());
        user.setPhoneNumber(profileEditForm.getPhoneNumber());

        userService.updateUser(user);
        return "redirect:/profile";
    }
}
