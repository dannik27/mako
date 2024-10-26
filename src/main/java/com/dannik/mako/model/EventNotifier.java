package com.dannik.mako.model;

@FunctionalInterface
public interface EventNotifier {
    void notify(String message);
}
