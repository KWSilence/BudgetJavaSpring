package server.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import server.entities.Role;
import server.entities.User;
import server.repos.UserRepo;

import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {
    private final UserRepo userRepo;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public RegistrationController(UserRepo userRepo, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepo = userRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Map<String, Object> model) {
        User userDB = userRepo.findByUsername(user.getUsername());

        if (userDB != null) {
            model.put("error", "user with this username already exist");
            return "registration";
        }

        user.setActive(true);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(Role.USER));
        userRepo.save(user);

        return "redirect:/login";
    }
}
