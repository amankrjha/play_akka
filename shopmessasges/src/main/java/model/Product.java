package model;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String name;
    private String desc;
    private String id;
    private List<SKU> skus;

    public Product(String name, String desc, String id, List<SKU> skus) {
        this.name = name;
        this.desc = desc;
        this.id = id;
        this.skus = skus;
    }

    public Product(){

    }

    public List<SKU> getSkus() {
        return skus;
    }

    public void setSkus(List<SKU> skus) {
        this.skus = skus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
