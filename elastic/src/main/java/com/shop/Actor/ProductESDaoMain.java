package com.shop.Actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClientReceptionist;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ProductESDaoMain {

    public static void main(String args[]){
        ActorSystem system = ActorSystem.create("shopsystem");
        ActorRef productES = system.actorOf(ProductESDao.props(), "product_es");

        //System.out.println(productES);
        ClusterClientReceptionist.get(system).registerService(productES);
    }
}
