package com.cafemanager.cafe.repository;

import com.cafemanager.cafe.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
  User findByEmail(String email);

}
