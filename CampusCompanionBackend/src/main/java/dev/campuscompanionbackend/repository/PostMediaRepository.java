package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.Post;
import dev.campuscompanionbackend.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动态/评论媒体资源 Repository
 */
@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

    /**
     * 根据所属动态/评论查询所有媒体资源
     *
     * @param post 关联的动态/评论
     * @return 媒体资源列表
     */
    List<PostMedia> findByPost(Post post);

    /**
     * 根据媒体文件ID查询媒体资源
     *
     * @param pmid 媒体文件ID
     * @return 媒体资源
     */
    PostMedia findByPmid(Long pmid);
}
