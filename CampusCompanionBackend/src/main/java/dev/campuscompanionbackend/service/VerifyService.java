package dev.campuscompanionbackend.service;


import dev.campuscompanionbackend.service.exception.EmailVerifyException;

public interface VerifyService {

    /**
     * 验证邮箱
     *
     * @param email the email
     * @throws EmailVerifyException the email verify exception
     */
    void verifyEmail(String email) throws EmailVerifyException;
}
