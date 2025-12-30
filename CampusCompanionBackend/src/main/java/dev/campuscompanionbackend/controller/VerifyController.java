package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.exception.UserExistException;
import dev.campuscompanionbackend.service.VerifyService;
import dev.campuscompanionbackend.exception.EmailInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verify")
public class VerifyController extends BaseController{

    private final VerifyService verifyService;

    @Autowired
    public VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @PostMapping("/email/{email}")
    public ApiResponse<Void> verifyEmail(@PathVariable String email) throws EmailInvalidException, UserExistException {
        verifyService.verifyEmail(email);
        return ApiResponse.success(String.format("已向 %s 发送验证码", email), null);
    }
}
