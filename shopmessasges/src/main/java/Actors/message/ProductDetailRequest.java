package Actors.message;

/**
 * Created by amajha on 6/14/2018.
 */
public class ProductDetailRequest extends CachedRequest implements GauvaCacheble, RedisCacheble {
    private final String productId;

    public ProductDetailRequest(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
