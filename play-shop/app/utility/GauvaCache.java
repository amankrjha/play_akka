package utility;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import model.Employee;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GauvaCache {


    RedisCacheUtility redisCacheUtility;
    Cache<Object, Object> cache;

    @Inject
    public GauvaCache(RedisCacheUtility redisCacheUtility){
        this.redisCacheUtility = redisCacheUtility;
        cache = CacheBuilder.newBuilder().recordStats().build();
    }

    public List<Employee> getAllEmployee() {
        try {
            return (List<Employee>)cache.get("all_employees", () -> redisCacheUtility.getAllEmployee());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getEmployeeByName(String name){
        try {
            return (List<Employee>)cache.get("employee_"+name, () -> redisCacheUtility.getEmployeeByName(name));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStats(){
        StringBuilder builder = new StringBuilder();
        builder.append("Hits count "+cache.stats().hitCount());
        builder.append("\n");
        builder.append("Miss count "+cache.stats().missCount());
        return builder.toString();
    }
}
