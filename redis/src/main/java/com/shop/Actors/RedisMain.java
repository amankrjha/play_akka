package com.shop.Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClientReceptionist;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class RedisMain {

    public static void main(String[] args){
        Config config = ConfigFactory.load();
        System.out.println(config.getList("akka.cluster.roles"));
        ActorSystem system = ActorSystem.create("shopsystem");
        ActorRef productES = system.actorOf(ProductRedisDao.props(), "redis");
        //System.out.println(productES);
        ClusterClientReceptionist.get(system).registerService(productES);
    }
}
