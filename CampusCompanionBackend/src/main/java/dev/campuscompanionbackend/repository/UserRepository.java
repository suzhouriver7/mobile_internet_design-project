package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户表 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据邮箱查询用户
     *
     * @param email the email
     * @return the optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户类型查询
     *
     * @param userType the user type
     * @return the iterable
     */
    Iterable<User> findByUserType(UserType userType);

    /**
     * 根据SQL Like匹配规则模糊查询
     *
     * @param rule the rule
     * @return the iterable
     */
    Iterable<User> findByNicknameContaining(String rule);

    /**
     * 检查邮箱是否存在
     *
     * @param email the email
     * @return the boolean
     */
    boolean existsByEmail(String email);
}
