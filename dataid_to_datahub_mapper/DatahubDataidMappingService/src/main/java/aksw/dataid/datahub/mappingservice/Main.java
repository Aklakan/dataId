
package aksw.dataid.datahub.mappingservice;

import aksw.dataid.datahub.cli.DataIdProcesser;
import aksw.dataid.datahub.jsonutils.StaticJsonHelper;
import aksw.dataid.datahub.propertymapping.PropertyMapper;
import aksw.dataid.datahub.restclient.CkanRestClient;

import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;

import org.codehaus.jackson.JsonNode;
import org.glassfish.grizzly.http.server.HttpServer;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;


public class Main {
	private static String mainPath = System.getProperty("user.dir");
	private static String mainConfigPath = mainPath + "\\target\\classes\\MainConfig.Json";
	private static String mappingConfigPath;
	private static JsonNode mainConfigFile;
	private static CkanRestClient client;
	
	private static CkanRestClient createCkanRestClient() {
		String dataHubUrl = mainConfigFile.get("datahubActionUri").asText();
		String apiKey = mainConfigFile.get("datahubApiKey").asText();
		int timeout = Integer.parseInt(mainConfigFile.get("ckanTimeOut").asText());
		Map<String, String> actions = new HashMap<String, String>();
		Iterator<Entry<String, JsonNode>> i = mainConfigFile.get("ckanActionMap").getFields();
		for(Entry<String, JsonNode> key; i.hasNext();)
		{
			key = i.next();
			actions.put(key.getKey(), key.getValue().asText());
		}
		
		CkanRestClient client = new CkanRestClient(dataHubUrl, apiKey, actions, timeout);
		return client;
	}
	
    private static int getPort(int defaultPort) {
        //grab port from environment, otherwise fall back to default port 9998
        String httpPort = System.getProperty("jersey.test.port");
        if (null != httpPort) {
            try {
                return Integer.parseInt(httpPort);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9997)).build();
    }

    public static final URI BASE_URI = getBaseURI();
    
    protected static HttpServer startServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", "aksw.dataid.datahub.mappingservice");

        System.out.println("Starting grizzly2...");
        return GrizzlyWebContainerFactory.create(BASE_URI, initParams);
    }
    
    public static void main(String[] args) throws IOException {
        // Grizzly 2 initialization
		mainConfigFile = StaticJsonHelper.getJsonContent(mainConfigPath);
		String zw = mainConfigFile.get("mappingConfigPath").asText();
		mappingConfigPath = zw.startsWith("\\") ? (mainPath + zw) : zw;
	    client = createCkanRestClient();
	    
        HttpServer httpServer = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...",
                BASE_URI));
        System.in.read();
        httpServer.stop();
    }

	public static CkanRestClient getClient() {
		return client;
	}

	public static String getMappingConfigPath() {
		return mappingConfigPath;
	}    
}
