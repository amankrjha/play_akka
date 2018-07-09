package utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Employee;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import play.libs.Json;

import static org.elasticsearch.index.query.QueryBuilders.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ESUtility {

    TransportClient client;
    //ObjectMapper mapper;

    @Inject
    public ESUtility(ESFactory factory){
        this.client = factory.getESClient();
        //mapper = new ObjectMapper();
    }

    public List<Employee> getAllEmployee(){
        SearchResponse response = client.prepareSearch("hr").setTypes("employee").get();
        return createListFromResponseString(response);
    }

    public List<Employee> getEmployeeByName(String name){
        BoolQueryBuilder query = boolQuery().must(termQuery("name", name));
        SearchResponse response = client.prepareSearch("hr").setTypes("employee").setQuery(query).get();
        return createListFromResponseString(response);
    }

    public String addEmployee(Employee emp){
        IndexResponse response = null;
        try {
            response = client.prepareIndex("hr", "employee", emp.getId())
                    .setSource(XContentFactory.jsonBuilder().startObject().field("name", emp.getName())
                            .field("id", emp.getId())
                            .field("salary", emp.getSalary()).endObject())
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.getResult().toString();
    }

    public String deleteEmployee(String id){
        DeleteResponse response = client.prepareDelete("hr", "employee", id).get();
        return response.status().toString();
    }

    private List<Employee> createListFromResponseString(SearchResponse response){
        return Arrays.stream(response.getHits().getHits()).
                map(hit -> Json.fromJson(Json.parse(hit.getSourceAsString()), Employee.class)).collect(Collectors.toList());
    }


}
