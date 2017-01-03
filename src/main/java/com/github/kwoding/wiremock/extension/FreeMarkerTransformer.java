package com.github.kwoding.wiremock.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import freemarker.core.InvalidReferenceException;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FreeMarkerTransformer extends ResponseDefinitionTransformer {

    private static final String NAME = "freemarker-transformer";
    private Configuration configuration;

    public FreeMarkerTransformer() {

        //Freemarker configuration object
        configuration = new Configuration(Configuration.VERSION_2_3_23);
    }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {

        // Return the configured response when there is no body file and body content specified
        if ((!responseDefinition.specifiesBodyFile() && !responseDefinition.specifiesBodyContent())) {
            return responseDefinition;
        }

        String transformedDefinitionBody;

        try {
            // Set response definition body based on body file or body content
            String responseDefinitionBody = getResponseDefinitionBody(responseDefinition, files);

            // Return the configured response when there is no freemarker template declared in the body file or body content
            if (!freeMarkerTemplateDeclared(responseDefinitionBody)) {
                return responseDefinition;
            }

            // Set template for FreeMarker
            Template template = new Template("template", new StringReader(responseDefinitionBody), configuration);
            // Set request body as String
            String requestBody = request.getBodyAsString();
            // Build FreeMarker data model
            Map<String, Object> data = new HashMap<String, Object>();

            // Set FreeMarker variable based on request body
            if (requestBody != null) {
                setFreeMarkerRequestBodyVariable(data, requestBody);
            }
            // Process template and output to String
            StringWriter stringWriter = new StringWriter();
            template.process(data, stringWriter);
            transformedDefinitionBody = stringWriter.toString();

        } catch (IOException ioe) {
            return ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                    .withStatusMessage("Error processing template based on request: " + ioe.getMessage())
                    .build();
        } catch (InvalidReferenceException ire) {
            return ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                    .withStatusMessage("Invalid reference in template: " + ire.getMessage())
                    .build();
        } catch (TemplateException te) {
            return ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                    .withStatusMessage("Incorrect template definition: " + te.getMessage())
                    .build();
        } catch (ParserConfigurationException pce) {
            return ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                    .withStatusMessage("Incorrect configuration: " + pce.getMessage())
                    .build();
        } catch (SAXException saxe) {
            return ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                    .withStatusMessage("Unable to parse request: " + saxe.getMessage())
                    .build();
        }

        // Return new transformed response definition
        return new ResponseDefinitionBuilder()
                .withStatus(200)
                .withBody(transformedDefinitionBody)
                .build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    /**
     * Set the response definition body based on body file or body content
     *
     * @param responseDefinition ResponseDefinition of WireMock
     * @param files              FileSource of WireMock
     * @throws IOException
     */
    private String getResponseDefinitionBody(ResponseDefinition responseDefinition, FileSource files) throws IOException {

        String responseDefinitionBody = null;

        if (responseDefinition.specifiesBodyFile()) {

            String responseDefinitionBodyFilePath = files.getPath().concat("/" + responseDefinition.getBodyFileName());
            responseDefinitionBody = FileUtils.readFileToString(new File(responseDefinitionBodyFilePath), "UTF-8");

        } else if (responseDefinition.specifiesBodyContent()) {

            responseDefinitionBody = responseDefinition.getBody();

        }
        return responseDefinitionBody;
    }

    /**
     * @param data        FreeMarker data model
     * @param requestBody request body to use for the FreeMarker variable
     * @throws TemplateModelException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void setFreeMarkerRequestBodyVariable(Map data, String requestBody) throws TemplateModelException, IOException, ParserConfigurationException, SAXException {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert JSON string to Map (in case of JSON object)
            Map<String, Object> requestMap = objectMapper.readValue(requestBody, new TypeReference<HashMap<String, Object>>() {
            });
            data.put("request", requestMap);

        } catch (JsonMappingException jme) {

            // Convert JSON string to List (in case of JSON array)
            List<Object> requestList = objectMapper.readValue(requestBody, new TypeReference<ArrayList<Object>>() {
            });
            data.put("request", requestList);

        } catch (IOException ioe) {

            // Use FreeMarker's node model to parse the XML
            NodeModel requestNodeModel = NodeModel.parse(new InputSource(new StringReader(requestBody)));
            data.put("request", requestNodeModel);
        }
    }

    /**
     * Check if a FreeMarker template is declared based on a regex pattern
     *
     * @param template String to check on regex pattern for FreeMarker variables
     * @return true or false for determining whether it is a FreeMarker template
     */
    private boolean freeMarkerTemplateDeclared(String template) {

        Pattern patternFreeMarkerVariable = Pattern.compile("\\$\\{(.+)\\}");
        Pattern patternFreeMarkerBlock = Pattern.compile("\\<\\#(.+)\\>");
        Matcher matcherFreeMarkerVariable = patternFreeMarkerVariable.matcher(template);
        Matcher matcherFreeMarkerBlock = patternFreeMarkerBlock.matcher(template);

        if (matcherFreeMarkerVariable.find()) {
            return true;
        } else if (matcherFreeMarkerBlock.find()) {
            return true;
        } else {
            return false;
        }
    }

}