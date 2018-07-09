package Actors.gauva;

import Actors.Redis.ProductRedisDao;
import Actors.elastic.ProductESDao;
import Actors.message.*;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import model.Product;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import static akka.pattern.PatternsCS.ask;
import static org.reflections.util.ConfigurationBuilder.build;

public class ProductActorGauva extends AbstractActor {

    Cluster cluster = Cluster.get(context().system());
    public static Props props(){
        return Props.create(ProductActorGauva.class, () -> new ProductActorGauva());
    }

    private ActorRef redis;
    private Cache<Object, Object> cache;

    public ProductActorGauva(){
        //this.redis = redis;
        cache = CacheBuilder.newBuilder().recordStats().build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AllProductRequest.class, this::getAllProducts)
                .match(AllProductResponse.class, this::setAllProducts)
                .match(ProductDetailRequest.class, this::getProductDetail)
                .match(ProductDetailResponse.class, this::setProductDetail)
                .match(String.class, (msg) ->{
                    if("REDIS_REGISTRATION".equals(msg)){
                        redis = context().sender();
                        System.out.println("Redis has been registered");
                    }
                })
                .build();
    }

    private void setProductDetail(ProductDetailResponse pdr) {
        System.out.println("Gauva : setting product details for "+ pdr.getProduct().getId());
        cache.put("product_"+pdr.getProduct().getId(), pdr.getProduct());
    }

    private void getProductDetail(ProductDetailRequest pdr) {
        System.out.println("Gauva : products details");
        Product product = (Product)cache.getIfPresent("product_"+pdr.getProductId());
        if(product == null){
            System.out.println("Gauva : has no product detail for "+pdr.getProductId());
            if(pdr instanceof GauvaCacheble){
                pdr.push(self());
            }
            redis.tell(pdr, context().sender());
        }else{
            System.out.println("Gauva : has product detail for "+pdr.getProductId());
            ProductDetailResponse response = new ProductDetailResponse(product);
            context().sender().tell(response, self());
        }
    }

    private void setAllProducts(AllProductResponse response) {
        System.out.println("Gauva : setting all products");
        cache.put("all_products", response.getProducts());
    }

    private void getAllProducts(AllProductRequest req) {

        System.out.println("Gauva : all products");
        List<Product> products = null;
        products = (List<Product>)cache.getIfPresent("all_products");
        if(products == null){
            System.out.println("Gauva : no all products");
            if(req instanceof GauvaCacheble){
                req.push(self());
            }
            redis.tell(req, context().sender());
        }else{
            System.out.println("Gauva : has all products");
            AllProductResponse response = new AllProductResponse(products);
            context().sender().tell(response, self());
        }
    }
     /*
    private void getProductDetails(ProductDetailRequest pd) {
        context().sender().tell("Here is details of products "+pd.getProductId(), self());
    }


        try {


            products = (List<Product>)cache.get("all_employees", () -> {
                System.out.println("Gauva : all products is not in gauva");
                CompletionStage<Object> res = ask(redis, new ProductRedisDao.AllEmployee(), 9000);
                System.out.println("Gauva : have called Redis for all products");
                return res.toCompletableFuture().get();
            });
            context().sender().tell(products, self());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/

}
