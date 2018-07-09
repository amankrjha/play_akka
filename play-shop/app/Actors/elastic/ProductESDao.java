package Actors.elastic;

import Actors.message.*;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import model.Employee;
import model.Product;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import play.libs.Json;
import static org.elasticsearch.index.query.QueryBuilders.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductESDao extends AbstractActor {

    public static Props props(){
        return Props.create(ProductESDao.class, () -> new ProductESDao());
    }

    private TransportClient client;
    private final String index = "catalog";
    private final String types = "products";

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
    public Receive createReceive() {
        return receiveBuilder()
                .match(AllProductRequest.class, this::getAllProduct)
                .match(ProductDetailRequest.class, this::getProductDetail)
                .build();
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
                map(hit -> Json.fromJson(Json.parse(hit.getSourceAsString()), Product.class)).collect(Collectors.toList());
    }

    private Product createObjectFromResponseString(SearchResponse response){
        return Json.fromJson(Json.parse(response.getHits().getHits()[0].getSourceAsString()), Product.class);
    }
}
