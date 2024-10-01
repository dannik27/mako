package com.dannik.mako.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {

  private String username;
  private boolean online;
  private boolean bot;

}
