package utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.typesafe.config.Config;
import model.Employee;

import play.libs.Json;

import javax.inject.Inject;
import java.io.*;
import java.util.Base64;
import java.util.List;

public class RedisCacheUtility {

    private ESUtility esUtility;
    private RedisClient client;
    private RedisURI  uri;
    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    public RedisCacheUtility(ESUtility utility, Config config){
        this.esUtility = utility;
        uri = RedisURI.Builder.redis(config.getString("redis_server"), config.getInt("redis_port")).build();
        client = RedisClient.create(uri);
    }

    public List<Employee> getAllEmployee(){
        List<Employee> employees = (List<Employee>)deSerializeJson(client.connect().sync().get("all_employees"));
        if(employees == null){
            employees = esUtility.getAllEmployee();
            String val = client.connect().sync().set("all_employees", serializeJson(employees));
            System.out.println(val);
        }
        return employees;
    }

    public List<Employee> getEmployeeByName(String name){
        List<Employee> employees = (List<Employee>)deSerializeJson(client.connect().sync().get("employee_"+name));
        if(employees == null){
            employees = esUtility.getEmployeeByName(name);
            String val = client.connect().sync().set("employee_"+name, serializeJson(employees));
            System.out.println(val);
        }
        return employees;
    }

    private String serializeJson(List<Employee> emp){
        return Json.toJson(emp).toString();
    }

    private List<Employee> deSerializeJson(String value){
        if(value == null){
            return null;
        }
        JsonNode node = null;
        try {
            node = mapper.readTree(value);
            return Json.mapper().readValue(node.traverse(), new TypeReference<List<Employee>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
    /*
    private String serialize(Object obj){
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream ous = null;
        try {
            ous = new ObjectOutputStream(bao);
            ous.writeObject(obj);
            ous.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(bao.toByteArray());
    }

    private Object deSerialize(String s){
        if(s == null){
            return null;
        }
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = null;
        Object o = null;
        try {
            ois = new ObjectInputStream(
                    new ByteArrayInputStream(  data ) );
            o  = ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return o;
    }
    */
}
