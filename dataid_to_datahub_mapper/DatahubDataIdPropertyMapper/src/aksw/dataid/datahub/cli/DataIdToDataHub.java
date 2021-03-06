package aksw.dataid.datahub.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;

import aksw.dataid.datahub.jsonobjects.Dataset;
import aksw.dataid.datahub.jsonutils.JsonFileManager;
import aksw.dataid.datahub.jsonutils.StaticJsonHelper;
import aksw.dataid.datahub.propertymapping.*;
import aksw.dataid.datahub.restclient.CkanRestClient;

public class DataIdToDataHub {

	private static String mainPath = System.getProperty("user.dir");
	private static String mainConfigPath = mainPath + "\\bin\\MainConfig.Json";
	private static JsonNode mainConfigFile;
	private static String sourceUrl = null;
	private static PropertyMapper mapper;
	private static CkanRestClient client;
	private static CommandLine cli;

	public static void main(String[] args) 
	{
		mainConfigFile = StaticJsonHelper.getJsonContent(mainConfigPath);
		String zw = mainConfigFile.get("mappingConfigPath").asText();
		String mappingPath = zw.startsWith("\\") ? (mainPath + zw) : zw;
		CommandLine cli = getCli(args);
		String dataIdContent = getDataIdContent(cli);
		Dataset set = null;
		DataIdProcesser processor;
		try {
			processor = new DataIdProcesser(dataIdContent, mappingPath);
			set = processor.createDataset();
		} catch (DataHubMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		client = createCkanRestClient();
						
	    if(cli.getOptionValue("create") != null)
	    {
	    	client.CreateDataset(cli.getOptionValue("create"), set);
	    }
	    else if(cli.getOptionValue("update") != null)
	    {
	    	client.UpdateDataset(cli.getOptionValue("update"), set);
	    }
	}

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


	private static String getDataIdContent(CommandLine cli) {
		String content = null;
	    if(cli.getOptionValue("file") != null)
	    {
	    	sourceUrl = cli.getOptionValue("file");
	    	return StaticHelper.readFile(sourceUrl);
	    }
	    else
	    {
		    sourceUrl = cli.getOptionValue("url");
		    return StaticHelper.readUrl(sourceUrl);
	    }
	}

	private static CommandLine getCli(String[] args) {
		Options options = createInputOptions();
	    CommandLineParser parser = new BasicParser();
	    CommandLine cli = null;
	    try {
	        // parse the command line arguments
	        cli = parser.parse( options, args );
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "The entered arguments are not sound.  Reason: " + exp.getMessage() );
	        System.exit(0);
	    }
		return cli;
	}

	private static String getPrettyString(JsonNode dataHubObject) {
		try {
			return new ObjectMapper().defaultPrettyPrintingWriter().writeValueAsString(dataHubObject);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static Options createInputOptions()
	{
		Options options = new Options();
		
		options.addOption(OptionBuilder.withArgName( "api key" )
                .hasArg()
                .withDescription(  "override the datahub-api-key given in the MainConfig file" )
                .create( "datahubApiKey"));
		
		OptionGroup group1 = new OptionGroup();
		group1.addOption(OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "use given file as DataId-source (formats: ttl, n3, json)" )
                .create( "file"));
		group1.addOption(OptionBuilder.withArgName( "url" )
                .hasArg()
                .withDescription(  "use given url as DataId-source (formats: ttl, n3, json)" )
                .create( "url"));
		group1.isRequired();
		options.addOptionGroup(group1);

		OptionGroup group2 = new OptionGroup();
		group2.addOption(OptionBuilder.withArgName( "dataset name" )
                .hasArg()
                .withDescription(  "create a new dataset with a given name" )
                .create( "create"));
		group2.addOption(OptionBuilder.withArgName( "dataset name" )
                .hasArg()
                .withDescription(  "update a given dataset" )
                .create( "update"));
		group2.isRequired();
		options.addOptionGroup(group2);
		
		return options;
	}
}
