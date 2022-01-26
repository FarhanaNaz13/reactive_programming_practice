package com.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MonoTest {
    @BeforeAll
    public static void setUp() {
        BlockHound.install();
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }


    @Test
    public void test(){
        log.info("1st MonoTest done");
    }

    @Test
    public void monoSubscriber() {
        String name = "Farhana Naz";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("monoSubscriber test done");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Farhana Naz";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));
        log.info("------------monoSubscriberConsumer--------------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Farhana Naz";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);

        log.info("------------monoSubscriberConsumerError--------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }
    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Farhana Naz";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("finished!"));

        log.info("------------monoSubscriberConsumerComplete--------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Farhana Naz";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("finished!")
                , Subscription::cancel);

        log.info("------------monoSubscriberConsumerSubscription--------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }
    @Test
    public void monoDoOnMethods() {
        String name = "Farhana Naz";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}",s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}",s)) //will not be executed
                .doOnSuccess(s -> log.info("doOnSuccess executed {}", s));

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("finished!"));

        log.info("----------monoDoOnMethods----------------");
    }
    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing this doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoOnErrorResume() {
        String name = "Farhana Naz";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoOnErrorReturn() {
        String name = "Farhana Naz";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
