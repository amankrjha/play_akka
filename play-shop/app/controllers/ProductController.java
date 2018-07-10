package controllers;

import Actors.gauva.ProductActorGauva;
import Actors.message.AllProductRequest;
import Actors.message.AllProductResponse;
import Actors.message.ProductDetailRequest;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import static akka.pattern.PatternsCS.ask;

public class ProductController extends Controller{

    ActorSystem actorSystem;
    ActorRef productGV;



    @Inject
    public ProductController(ActorSystem actorSystem){

        this.actorSystem = actorSystem;
        this.productGV = actorSystem.actorOf(ProductActorGauva.props(), "products_gv");
    }

    public Result index(){
        return ok("Good to go..");
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
