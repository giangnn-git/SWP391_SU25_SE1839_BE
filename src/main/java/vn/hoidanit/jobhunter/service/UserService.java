package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;

@Service
public interface UserService {

    User handleCheckAccount(String input);

    User handleCheckPassword(String password, User employee);
}