package Actors;

import Actors.message.Request;
import akka.actor.AbstractActor;
import akka.actor.Props;

public class SayHello extends AbstractActor {


    private final String message;

    public static Props props(String message){
        return Props.create(SayHello.class, () -> new SayHello(message));
    }

    public SayHello(String message){
        this.message = message;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, req -> {
                    System.out.println("I am in Say hello");
                    Thread.sleep(10000);
                    context().sender().tell(new Request(message + req.getName()), self());
                    System.out.println("Sayhello waiting is over and response is sent back");
                })
                .build();
    }
}
