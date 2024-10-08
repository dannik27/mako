package com.dannik.mako.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Greeting {

  private String message;
  private GameDto activeGame;
}
