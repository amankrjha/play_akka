package Actors.message;

import model.Product;

import java.util.List;

public class AllProductResponse extends Response{
    private final List<Product> products;

    public AllProductResponse(List<Product> products){
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }
}
