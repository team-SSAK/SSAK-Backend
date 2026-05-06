package com.ssak.ssak.domain.community;

import com.ssak.ssak.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.restaurant.restaurantId = :restId")
    List<Post> findAllByRestaurant_RestaurantId(Long restId);

    List<Post> findByUser(User user);
}
