package com.dannik.mako.repositories;


import com.dannik.mako.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserRepository {

  private final Map<String, User> users = new HashMap<>();

  public User getOrCreate(String username) {
    return users.computeIfAbsent(username, u -> {
      log.info("New user {} created", username);
      return new User(username);
    });
  }
}
