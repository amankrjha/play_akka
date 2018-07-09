package Actors.message;

import akka.actor.ActorRef;

import java.util.Stack;

public abstract class CachedRequest extends Request{
    protected final Stack<ActorRef> callChainActorRefs = new Stack<>();

    public CachedRequest(){
        super("Cached Request");
    }

    public ActorRef pop(){
        return callChainActorRefs.pop();
    }

    public void push(ActorRef ref){
        callChainActorRefs.push(ref);
    }

    public int toCache(){
        return callChainActorRefs.size();
    }
}
