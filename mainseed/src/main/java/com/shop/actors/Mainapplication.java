package com.shop.actors;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Mainapplication {

    public static void main(String args[]){
        //Config config = ConfigFactory.load();
        //System.out.println(config.getString("akka.cluster.auto-down-unreachable-after").toString());
        ActorSystem system = ActorSystem.create("shopsystem");


        system.actorOf(Props.create(ClusterListner.class),
                "clusterListener");
    }

}
