# quasar-flow 
## Handle data flows using quasar fibers and reactive-streams.
&nbsp;[![Build Status](https://travis-ci.org/enryold/quasar-flow.svg?branch=master)](https://travis-ci.org/enryold/quasar-flow) &nbsp;[![](https://jitpack.io/v/enryold/quasar-flow.svg)](https://jitpack.io/#enryold/quasar-flow)


### Goal - Build a concurrency system that

- It's easy to use.
- It's simply to read.
- It's simply to code.
- Uses reactive-streams channels / processors / subscriber with quasar fibers under the hood.
- Uses FanIn/FanOut concurrency patterns.
- Have common methods for size/byte batching with flushing timeouts.


### Main entities:

- Emitter: Entity that emit flows of objects on a channel.  
  - An Emitter could be a broadcast emitter.
  - An Emitter could be a routed emitter, every processor should subscribe on a particular data object property.
  - An Emitter could have 1-N subscribers.


- Processor: Entity that receives an Emitter's data flow.
  - A Processor could process an Emitter data-flow with 1-N fiber/s.
  - A Processor could transform the emitter data-flow with a transformation function.
  - A Processor could process an Emitter data-flow with N fibers and return 1 result Emitter using FanIn pattern.
  - A Processor could process an Emitter data-flow with N fibers and return N result Emitter using FanOut pattern.
  - A Processor could process an Emitter data-flow batching results grouping them by size.
  - A Processor could process an Emitter data-flow batching results grouping them by a custom user-defined accumulator.
  
  
- Consumer: Entity that could receives both Emitter or Processors data-flow 
  - A Consumer could process an Emitter/Processor data-flow with 1-N fiber/s.
  - A Consumer could transform an Emitter/Processor data-flow with a transformation function.
  - A Consumer could process an Emitter/Processor data-flow with N fibers and apply an user-defined task to the result.
  - A Consumer could process an Emitter/Processor data-flow batching results grouping them by size and apply an user-defined task to the result.
  - A Consumer could process an Emitter/Processor data-flow batching results grouping them by a custom user-defined accumulator and apply an user-defined task to the result.
  
 

### Examples:


#### Emitter -> Processor -> Consumer
```java
// LINEAR LAYOUT
         QuasarFlow.newFlow()
                         .broadcastEmitter(stringEmitterTask) // BUILD A BROADCAST EMITTER FROM TASK
                         .addProcessor() // ADD A PROCESSOR
                         .process() // PROCESS 
                         .addConsumer() // ADD A CONSUMER
                         .consume(str -> System.out.println(str)) // CONSUME WITH CONSUMER TASK
                         .start();
         
         // NESTED LAYOUT
         QuasarFlow.newFlow()
                .broadcastEmitter(null) // BUILD A BROADCAST EMITTER FROM TASK
                .addProcessor(p -> { // ADD A PROCESSOR
                    p.process() // PROCESS 
                            .addConsumer(c -> // ADD A CONSUMER
                                    c.consume(str -> System.out.println(str))); // CONSUME WITH CONSUMER TASK
                })
                .start(); 



