package org.sopt.springagentlab.infrastructure.persistence;

import org.sopt.springagentlab.application.post.port.out.PostRepository;
import org.sopt.springagentlab.domain.Post;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostPersistenceAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    public PostPersistenceAdapter(PostJpaRepository postJpaRepository) {
        this.postJpaRepository = postJpaRepository;
    }

    @Override
    @Transactional
    public Long save(Post post) {
        PostJpaEntity entity = new PostJpaEntity(post.getTitle(), post.getContent());
        return postJpaRepository.save(entity).getId();
    }
}
