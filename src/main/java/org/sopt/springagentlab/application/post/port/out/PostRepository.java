package org.sopt.springagentlab.application.post.port.out;

import org.sopt.springagentlab.domain.Post;

public interface PostRepository {

    /** Saves a validated post and returns its generated ID. */
    Long save(Post post);
}
