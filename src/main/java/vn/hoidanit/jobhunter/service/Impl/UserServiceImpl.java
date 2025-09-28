package vn.hoidanit.jobhunter.service.Impl;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User handleCheckAccount(String input) {
        return this.userRepository.findByEmailOrPhoneNumber(input, input)
                .orElseThrow(() -> new NoSuchElementException("Email or Phone isn't correct"));
    }

    public User handleCheckPassword(String password, User employee) {
        System.out.println(password);
        System.out.println(employee.getPassword());
        if (!this.passwordEncoder.matches(password, employee.getPassword())) {
            throw new NoSuchElementException("Password isn't correct");
        }
        return employee;
    }
}
