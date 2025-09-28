package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.ApiResponse;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
public class EmployeeController {

    private final UserService employeeService;

    public EmployeeController(UserService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@Valid @RequestParam String user, @RequestParam String password) {
        User foundEmployee = this.employeeService.handleCheckAccount(user);
        User correctEmployee = this.employeeService.handleCheckPassword(password, foundEmployee);

        var result = new ApiResponse<>(HttpStatus.OK, "Login successfully", correctEmployee, null);
        return ResponseEntity.ok(result);
    }
}
