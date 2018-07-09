package com.shop.Actor;

import Actors.message.*;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Product;
import model.SKU;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

public class ProductESDao extends AbstractActor {

    Cluster cluster = Cluster.get(context().system());

    public static Props props(){
        return Props.create(ProductESDao.class, () -> new ProductESDao());
    }

    private TransportClient client;
    private final String index = "catalog";
    private final String types = "products";
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductESDao(){
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch")
                .put("client.transport.ping_timeout", "3000ms")
                .build();
        client = new PreBuiltTransportClient(settings);
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
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
                .match(AllProductRequest.class, this::getAllProduct)
                .match(ProductDetailRequest.class, this::getProductDetail)
                .match(ClusterEvent.CurrentClusterState.class, this::watchClusterState)
                .match(ClusterEvent.MemberUp.class, this::register)
                .match(String.class, System.out::println)
                .build();
    }

    private void register(ClusterEvent.MemberUp up){
        Member member = up.member();
        if(member.status().equals(MemberStatus.up()) && member.hasRole("redis")){
            System.out.println("register Member address is "+member.address());
            context().actorSelection(member.address()+"/user/redis").tell("ELASTIC_REGISTRATION", self());
        }
    }

    private void watchClusterState(ClusterEvent.CurrentClusterState state){
        for(Member member : state.getMembers()){
            System.out.println("Member  "+member);
            if(member.status().equals(MemberStatus.up()) && member.hasRole("redis")){
                System.out.println("Member address is "+member.address());
                context().actorSelection(member.address()+"/user/redis").tell("ELASTIC_REGISTRATION", self());
            }
        }
    }
    private void getProductDetail(ProductDetailRequest req) {
        System.out.println("I am in get product details of ES");
        BoolQueryBuilder query = boolQuery().must(termQuery("id", req.getProductId()));
        SearchResponse response = client.prepareSearch(index).setTypes(types).setQuery(query).get();
        Product product = createObjectFromResponseString(response);
        ProductDetailResponse productDetailResponse = new ProductDetailResponse(product);
        sendResponse(productDetailResponse, req);
    }

    private void getAllProduct(AllProductRequest req) {
        System.out.println("I am in get All products of ES");
        SearchResponse response = client.prepareSearch(index).setTypes(types).get();
        List<Product> products =  createListFromResponseString(response);
        AllProductResponse allProductResponse = new AllProductResponse(products);
        sendResponse(allProductResponse, req);
        System.out.println("I have sent response back to sender");
    }

    private void getAllProductDummy(AllProductRequest req) {
        System.out.println("I am in get All products of ES");
        List<Product> products = new ArrayList<>();
        for(int i=0; i < 5; i++){
            Product product = new Product("name :"+i, "Desc :"+i, Integer.toString(i), null);
            products.add(product);
        }
        AllProductResponse allProductResponse = new AllProductResponse(products);
        sendResponse(allProductResponse, req);
        System.out.println("I have sent response back to sender");
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

    private List<Product> createListFromResponseString(SearchResponse response){
        return Arrays.stream(response.getHits().getHits()).
                map(hit -> {
                    try {
                        return mapper.readValue(hit.getSourceAsString(), Product.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList());
    }

    private Product createObjectFromResponseString(SearchResponse response){
        try {
            return mapper.readValue(response.getHits().getHits()[0].getSourceAsString(), Product.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
