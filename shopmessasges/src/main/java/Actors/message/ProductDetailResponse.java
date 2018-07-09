package Actors.message;

import model.Product;

/**
 * Created by amajha on 6/14/2018.
 */
public class ProductDetailResponse extends Response{
    private final Product product;

    public ProductDetailResponse(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
