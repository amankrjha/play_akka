package Actors.Redis;

import Actors.elastic.ProductESDao;
import Actors.message.*;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import model.Employee;
import model.Product;
import play.libs.Json;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.pattern.PatternsCS.ask;

public class ProductRedisDao extends AbstractActor {

    public static Props props(ActorRef esdao){
        return Props.create(ProductRedisDao.class, () -> new ProductRedisDao(esdao));
    }

    private RedisClient client;
    private RedisURI uri;
    private ObjectMapper mapper = new ObjectMapper();
    private ActorRef esdao;


    public ProductRedisDao(ActorRef esdao){
        uri = RedisURI.Builder.redis("localhost", 6379).build();
        client = RedisClient.create(uri);
        this.esdao = esdao;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AllProductRequest.class, this::getAllEmployee)
                .match(AllProductResponse.class, this::setAllEmployees)
                .match(ProductDetailRequest.class, this::getProductDetail)
                .build();
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

    private void setAllEmployees(AllProductResponse res) {
        System.out.println("Redis : setting all products");
        client.connect().sync().set("all_products", serializeJson(res.getProducts()));
    }

    private void getAllEmployee(AllProductRequest req) {
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

    /*
            System.out.println("Redis : all products not Redis");
            CompletionStage<Object> result = ask(esdao,new ProductESDao.ProductList(), 8000);
            System.out.println("Redis : called ES for all products");
            result.thenAccept(response -> {
                client.connect().sync().set("all_employees", serializeJson((List<Employee>)response));
                context().sender().tell(response, self());
            });
            */
    /*
    public List<Employee> getEmployeeByName(String name){
        List<Employee> employees = (List<Employee>)deSerializeJson(client.connect().sync().get("employee_"+name));
        if(employees == null){
            employees = esUtility.getEmployeeByName(name);
            String val = client.connect().sync().set("employee_"+name, serializeJson(employees));
            System.out.println(val);
        }
        return employees;
    }*/

    private String serializeJson(List<Product> emp){
        return Json.toJson(emp).toString();
    }

    private List<Product> deSerializeJson(String value){
        if(value == null){
            return null;
        }
        JsonNode node = null;
        try {
            node = mapper.readTree(value);
            return Json.mapper().readValue(node.traverse(), new TypeReference<List<Product>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
