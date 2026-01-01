package dev.campuscompanionbackend.service;


import dev.campuscompanionbackend.enums.VerifyCodeRecordType;
import dev.campuscompanionbackend.exception.EmailInvalidException;
import dev.campuscompanionbackend.exception.UserExistException;

public interface VerifyService {

    /**
     * 验证邮箱
     *
     * @param email the email
     * @throws EmailInvalidException the email verify exception
     */
    void verifyEmail(String email, VerifyCodeRecordType type) throws EmailInvalidException, UserExistException;
}
