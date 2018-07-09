package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import model.Employee;
import play.libs.Json;
import play.mvc.*;

import utility.ESFactory;
import utility.ESUtility;
import utility.GauvaCache;
import utility.RedisCacheUtility;
import views.html.*;

import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    @Inject
    ESUtility esUtility;

    @Inject
    RedisCacheUtility redisUtility;

    @javax.inject.Inject
    GauvaCache gauvaCache;

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result getAllEmployee(){
        List<Employee> employees = gauvaCache.getAllEmployee();
        return ok(Json.toJson(employees));
    }

    public Result getEmployeeByName(String name){
        List<Employee> employees = gauvaCache.getEmployeeByName(name);
        return ok(Json.toJson(employees));
    }


    public Result addEmployee(){
        JsonNode json = request().body().asJson();
        if(json != null){
            Employee emp = Json.fromJson(json, Employee.class);
            String result = esUtility.addEmployee(emp);
            return created(result);
        }else{
            return badRequest("Check karr...");
        }
    }

    public Result deleteEmployee(String id){
        return ok(esUtility.deleteEmployee(id));
    }

    public Result getGauvaStats(){
        return ok(gauvaCache.getStats());
    }


}
