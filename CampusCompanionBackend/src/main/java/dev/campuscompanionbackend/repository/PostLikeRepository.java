package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.Post;
import dev.campuscompanionbackend.entity.PostLike;
import dev.campuscompanionbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 动态/评论点赞表 Repository
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 根据动态/评论查询所有点赞记录
     *
     * @param post the post
     * @return the list
     */
    List<PostLike> findByPost(Post post);

    /**
     * 检查用户是否已给某动态/评论点赞
     *
     * @param post the post
     * @param user the user
     * @return the boolean
     */
    boolean existsByPostAndUser(Post post, User user);

    /**
     * 根据用户查询所有点赞的动态/评论
     *
     * @param user the user
     * @return the list
     */
    List<PostLike> findByUser(User user);

    /**
     * 根据动态/评论和用户查询点赞记录
     *
     * @param post the post
     * @param user the user
     * @return the optional
     */
    Optional<PostLike> findByPostAndUser(Post post, User user);
}
