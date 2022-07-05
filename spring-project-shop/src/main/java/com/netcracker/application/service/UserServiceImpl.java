package com.netcracker.application.service;

import com.netcracker.application.controller.form.ProfileEditForm;
import com.netcracker.application.controller.form.UserDisplayForm;
import com.netcracker.application.controller.form.UserRegistrationForm;
import com.netcracker.application.security.UserService;
import com.netcracker.application.service.model.entity.Role;
import com.netcracker.application.service.model.entity.User;
import com.netcracker.application.service.repository.RoleRepository;
import com.netcracker.application.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service("userDetailsService")
public class UserServiceImpl implements UserService {
    private static final BigInteger CUSTOMER_ROLE_ID = BigInteger.valueOf(2);
    private static final BigInteger ADMIN_ROLE_ID = BigInteger.valueOf(1);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private final Map<BigInteger, User> users = new HashMap<>();

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    private void fill() {
        if (users.isEmpty()) {
            for (User user : userRepository.findAll()) {
                users.put(user.getId(), user);
            }
        }
    }

    public List<User> getAll() {
        fill();
        return new ArrayList<>(users.values());
    }

    public User getById(BigInteger id) {
        fill();
        return users.get(id);
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User currentUser = null;
        if (userDetails instanceof User) {
            currentUser = (User) userDetails;
        }
        return currentUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("Can't find user with username " + username);
        }
        return user;
    }

    @Override
    public boolean hasRole(String role) {
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)
                SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        boolean hasRole = false;
        for (GrantedAuthority authority : authorities) {
            hasRole = authority.getAuthority().equals(role);
            if (hasRole) {
                break;
            }
        }
        return hasRole;
    }

    @Override
    public User signupUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findById(CUSTOMER_ROLE_ID).orElseThrow(IllegalStateException::new);
        user.setRole(userRole);
        user.setRoleId(CUSTOMER_ROLE_ID);
        users.clear();
        return userRepository.save(user);
    }

    public ProfileEditForm getProfileEditForm(User user) {
        ProfileEditForm profileEditForm = new ProfileEditForm();

        profileEditForm.setAddress(user.getAddress());
        profileEditForm.setFirstName(user.getFirstName());
        profileEditForm.setLastName(user.getLastName());
        profileEditForm.setPhoneNumber(user.getPhoneNumber());

        return profileEditForm;
    }

    public String checkRegistrationValidity(UserRegistrationForm userRegistrationForm) {
        List<User> usersList = getAll();
        if (!userRegistrationForm.getPassword().equals(userRegistrationForm.getConfirmPassword())) {
            return "Password and Confirm password do not match";
        }
        if (usersList.stream().anyMatch(u -> u.getUsername().equals(userRegistrationForm.getUsername()))) {
            return "This username is already taken";
        }
        if (usersList.stream().anyMatch(u -> u.getEmail().equals(userRegistrationForm.getEmail()))) {
            return "Account with this email already exists";
        }
        return "";
    }

    public UserDisplayForm getUserDisplayForm(User user) {
        UserDisplayForm form = new UserDisplayForm();
        form.setUserId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setRoleName(user.getRole().getName());
        return form;
    }

    public List<UserDisplayForm> getUserDisplayForms(List<User> users) {
        return users.stream().map(this::getUserDisplayForm).collect(Collectors.toList());
    }

    public void updateUser(User user) {
        userRepository.save(user);
        users.clear();
    }

    public void deleteUser(User user) {
        users.remove(user.getId());
        userRepository.delete(user);
    }
}
