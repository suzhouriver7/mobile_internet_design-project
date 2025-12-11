package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.Order;
import dev.campuscompanionbackend.entity.Post;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.ContentStatus;
import dev.campuscompanionbackend.enums.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动态/评论主表 Repository
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据发布者查询动态/评论
     *
     * @param user   the user
     * @param type   the type
     * @param status the status
     * @return the list
     */
    List<Post> findByUserAndTypeAndStatus(User user, PostType type, ContentStatus status);

    /**
     * 根据父级动态/评论查询所有评论，按创建时间降序
     *
     * @param parentPost the parent post
     * @param type       the type
     * @param status     the status
     * @return the list
     */
    List<Post> findByParentPostAndTypeAndStatusOrderByCreatedAtDesc(Post parentPost, PostType type, ContentStatus status);

    /**
     * 根据关联订单查询动态
     *
     * @param order  the order
     * @param status the status
     * @return the list
     */
    List<Post> findByOrderAndStatus(Order order, ContentStatus status);

    /**
     * F查询所有正常状态的动态，按创建时间降序
     *
     * @param type   the type
     * @param status the status
     * @return the list
     */
    List<Post> findByTypeAndStatusOrderByCreatedAtDesc(PostType type, ContentStatus status);
}
