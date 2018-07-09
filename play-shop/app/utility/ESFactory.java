package utility;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ESFactory {

    private Config config;
    private TransportClient client;

    @Inject
    public ESFactory(Config config){
        this.config = config;
        createESClient();
    }

    public TransportClient getESClient(){
        if(client == null){
            createESClient();
        }
        return client;
    }

    private void createESClient(){
        Settings settings = Settings.builder().put("cluster.name", config.getString("cluster.name"))
                .put("client.transport.ping_timeout", config.getString("client.transport.ping_timeout"))
                .build();
        client = new PreBuiltTransportClient(settings);
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(config.getString("es_server")), config.getInt("es_port")));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
