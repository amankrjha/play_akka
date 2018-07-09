package com.shop.Actors;

import Actors.message.*;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import model.Product;

import java.io.IOException;
import java.util.List;

public class ProductRedisDao extends AbstractActor {

    Cluster cluster = Cluster.get(context().system());
    public static Props props(){
        return Props.create(ProductRedisDao.class, () -> new ProductRedisDao());
    }

    private RedisClient client;
    private RedisURI uri;
    private ObjectMapper mapper = new ObjectMapper();
    private ActorRef esdao;


    public ProductRedisDao(){
        uri = RedisURI.Builder.redis("localhost", 6379).build();
        client = RedisClient.create(uri);
        //this.esdao = esdao;
    }

    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterEvent.MemberUp.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AllProductRequest.class, this::getAllProducts)
                .match(AllProductResponse.class, this::setAllProducts)
                .match(ProductDetailRequest.class, this::getProductDetail)
                .match(ClusterEvent.MemberUp.class, this::register)
                .match(ClusterEvent.CurrentClusterState.class, this::watchClusterState)
                .match(String.class, msg -> {
                    if("ELASTIC_REGISTRATION".equals(msg)){
                        System.out.println("I have recieved registration");
                        esdao = context().sender();
                        System.out.println(esdao);
                        esdao.tell("woooooo", self());
                        System.out.println("Send happy msg");
                    }
                })
                .build();
    }


    private void register(ClusterEvent.MemberUp up){
        Member member = up.member();
        if(member.status().equals(MemberStatus.up()) && member.hasRole("gauva")){
            System.out.println("register Member address is "+member.address());
            context().actorSelection(member.address()+"/user/products_gv").tell("REDIS_REGISTRATION", self());
        }
    }
    private void watchClusterState(ClusterEvent.CurrentClusterState state){
        for(Member member : state.getMembers()){
            System.out.println("Member  "+member);
            if(member.status().equals(MemberStatus.up()) && member.hasRole("gauva")){
                System.out.println("Member address is "+member.address());
                context().actorSelection(member.address()+"/user/products_gv").tell("REDIS_REGISTRATION", self());
            }
        }
    }
    private void getProductDetail(ProductDetailRequest req) {
        System.out.println("Redis : product details for "+req.getProductId());
        Product product = (Product)deSerializeJson(client.connect().sync().get("products_"+req.getProductId()));
        if(product == null){
            System.out.println("Redis : no product details for "+req.getProductId());
            if(req instanceof RedisCacheble){
                req.push(self());
            }
            esdao.tell(req, context().sender());

        }else{
            System.out.println("Redis : has product details for "+req.getProductId());
            ProductDetailResponse response = new ProductDetailResponse(product);
            sendResponse(response, req);
        }

    }

    private void setAllProducts(AllProductResponse res) {
        System.out.println("Redis : setting all products");
        client.connect().sync().set("all_products", serializeJson(res.getProducts()));
    }

    private void getAllProducts(AllProductRequest req) {
        System.out.println("Redis : all products");
        List<Product> products = (List<Product>)deSerializeJson(client.connect().sync().get("all_products"));
        if(products == null){
            System.out.println("Redis : no all products");
            if(req instanceof RedisCacheble){
                req.push(self());
            }
            esdao.tell(req, context().sender());

        }else{
            System.out.println("Redis : has all products");
            AllProductResponse response = new AllProductResponse(products);
            sendResponse(response, req);
        }
    }

    private void sendResponse(Response response, Request request){
        if(request instanceof CachedRequest){
            CachedRequest req = (CachedRequest) request;
            while(req.toCache() > 0){
                ActorRef t = req.pop();
                t.tell(response, self());
            }
        }
        context().sender().tell(response, self());
    }

    private String serializeJson(List<Product> emp){
        return  "";//Json.toJson(emp).toString();
    }

    private List<Product> deSerializeJson(String value){
        if(value == null){
            return null;
        }
        JsonNode node = null;
        try {
            node = mapper.readTree(value);
            return null; //Json.mapper().readValue(node.traverse(), new TypeReference<List<Product>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
