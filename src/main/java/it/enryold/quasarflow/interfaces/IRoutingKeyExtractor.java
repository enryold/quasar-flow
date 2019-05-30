package it.enryold.quasarflow.interfaces;


import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.util.Optional;

@FunctionalInterface
public interface IRoutingKeyExtractor<Object> {

    QRoutingKey extactRoutingKeyFromObject(Object o);
}
