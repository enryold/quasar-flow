package it.enryold.quasarflow.interfaces;


import co.paralleluniverse.fibers.Suspendable;
import it.enryold.quasarflow.models.utils.QRoutingKey;

import java.util.Optional;

@FunctionalInterface
public interface IRoutingKeyExtractor<Object> {

    @Suspendable
    QRoutingKey extactRoutingKeyFromObject(Object o);
}
