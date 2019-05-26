package it.enryold.quasarflow.interfaces;


import java.util.Optional;

@FunctionalInterface
public interface IRoutingKeyExtractor<Object> {

    Optional<String> extactRoutingKeyFromObject(Object o);
}
