package controllers;

import Actors.Redis.ProductRedisDao;
import Actors.elastic.ProductESDao;
import Actors.gauva.ProductActorGauva;
import Actors.SayHello;
import Actors.message.AllProductRequest;
import Actors.message.AllProductResponse;
import Actors.message.ProductDetailRequest;
import Actors.message.Request;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ProviderSelection;
import model.Product;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import static akka.pattern.PatternsCS.ask;

public class ProductController extends Controller{

    ActorSystem actorSystem;
    //ActorRef productES;
    //ActorRef productRedis;
    ActorRef productGV;

    @Inject
    public ProductController(ActorSystem actorSystem){

        this.actorSystem = actorSystem;

        //this.productES = actorSystem.actorOf(ProductESDao.props(), "product_es");
        //this.productES = actorSystem.actorFor("akka://shopsystem/user/product_es");
        //this.productRedis = actorSystem.actorOf(ProductRedisDao.props(null), "product_rd");
        this.productGV = actorSystem.actorOf(ProductActorGauva.props(), "products_gv");


    }

    public CompletionStage<Result> AskActorToSayHello(){
        ActorRef ref = actorSystem.actorOf(SayHello.props("Hello "));
        System.out.println("I am in Product Controller");
        CompletionStage<Object> res = ask(ref, new Request("Aman"), 50000);
        System.out.println("I have returned Product Controller");
        return res.thenApplyAsync(res1 -> {
            System.out.println("I am in completion stage");
            if(res1 instanceof Request)
                return ok(((Request)res1).getName());
            else
                return internalServerError();
        });
    }

    public CompletionStage<Result> sayHello(){
        ActorRef ref = actorSystem.actorOf(SayHello.props("Hello "));
        System.out.println("I am in Product Controller");
        CompletionStage<Object> res = ask(ref, new Request("Aman"), 50000);
        System.out.println("I have returned Product Controller");
        return res.thenApplyAsync(res1 -> {
            System.out.println("I am in completion stage");
            if(res1 instanceof Request)
                return ok(((Request)res1).getName());
            else
                return internalServerError();
        });
    }

    public CompletionStage<Result> getAllProducts(){
        CompletionStage<Object> res = ask(productGV, new AllProductRequest(), 100000);
        return res.thenApplyAsync(res1 -> {
            AllProductResponse products = (AllProductResponse) res1;
            products.getProducts().forEach(p -> p.setSkus(null));
            return ok(Json.toJson(res1));
        });
    }

    public CompletionStage<Result> getProductDetails(String id){
        CompletionStage<Object> res = ask(productGV, new ProductDetailRequest(id), 100000);
        return res.thenApplyAsync(res1 -> {
            return ok(Json.toJson(res1));
        });
    }
}
