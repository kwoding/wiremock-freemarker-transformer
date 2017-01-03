package com.github.kwoding.wiremock.extension;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

public class FreeMarkerTransformerIntegrationTest {


    /**
     * WireMock server
     */
    @Rule
    public WireMockRule wiremockServer = new WireMockRule(wireMockConfig()
            .port(9980)
            .extensions(new FreeMarkerTransformer()));

    /**
     * Test valid JSON object request
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testJSONObjectRequest() throws IOException, URISyntaxException {

        String endpoint = "/test-json-object-request";
        String expectedResponseAsFile = FileUtils.readFileToString(new File("src/test/resources/expects/expect-valid-json-object.json"), "UTF-8");
        String expectedResponse = expectedResponseAsFile.toString();

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-json-object.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File(getClass().getClassLoader().getResource("requests/request-valid-json-object.json").toURI()))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(200)
            .body(Matchers.equalToIgnoringWhiteSpace(expectedResponse));
    }

    /**
     * Test valid JSON object request with body content (no body file)
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testJSONObjectRequestWithBodyContent() throws IOException, URISyntaxException {

        String endpoint = "/test-json-object-request-body";
        String templateBodyFile = FileUtils.readFileToString(new File("src/test/resources/__files/template-freemarker-json-object.json"), "UTF-8");
        String templateBody = templateBodyFile.toString();
        String expectedResponseAsFile = FileUtils.readFileToString(new File("src/test/resources/expects/expect-valid-json-object.json"), "UTF-8");
        String expectedResponse = expectedResponseAsFile.toString();

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBody(templateBody)
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File(getClass().getClassLoader().getResource("requests/request-valid-json-object.json").toURI()))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(200)
            .body(Matchers.equalToIgnoringWhiteSpace(expectedResponse));
    }

    /**
     * Test valid JSON array request
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testJSONArrayRequest() throws IOException, URISyntaxException {

        String endpoint = "/test-json-array-request";
        String expectedResponseAsFile = FileUtils.readFileToString(new File("src/test/resources/expects/expect-valid-json-array.json"), "UTF-8");
        String expectedResponse = expectedResponseAsFile.toString();

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-json-array.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-valid-json-array.json"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(200)
            .body(Matchers.equalToIgnoringWhiteSpace(expectedResponse));
    }

    /**
     * Test valid XML request
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testXMLRequest() throws IOException, URISyntaxException {

        String endpoint = "/test-xml-request";
        String expectedResponseAsFile = FileUtils.readFileToString(new File("src/test/resources/expects/expect-valid-xml.json"), "UTF-8");
        String expectedResponse = expectedResponseAsFile.toString();

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-xml.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-valid-xml.xml"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(200)
            .body(Matchers.equalToIgnoringWhiteSpace(expectedResponse));
    }


    /**
     * Test invalid JSON request
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testInvalidJSONObjectRequest() throws IOException, URISyntaxException {

        String endpoint = "/test-invalid-json-object-request";

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-json-object.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-invalid-json-object.json"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(500)
            .statusLine(Matchers.containsString("HTTP/1.1 500 Unable to parse request"));
    }

    /**
     * Test invalid XML request
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testInvalidXMLRequest() throws IOException, URISyntaxException {

        String endpoint = "/test-invalid-xml-request";

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-xml.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-invalid-xml.xml"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(500)
            .statusLine(Matchers.containsString("HTTP/1.1 500 Unable to parse request"));
    }

    /**
     * Test no request body
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testNoRequestBody() throws IOException, URISyntaxException {

        String endpoint = "/test-no-request-body";

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-json-object.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(500)
            .statusLine(Matchers.containsString("HTTP/1.1 500 Error processing template based on request"));
    }

    /**
     * Test incorrect JSON path in template
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testIncorrectJSONPathInTemplate() throws IOException, URISyntaxException {

        String endpoint = "/test-incorrect-json-path-template";

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-incorrect-json-path-reference.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-valid-json-object.json"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(500)
            .statusLine(Matchers.containsString("HTTP/1.1 500 Invalid reference in template"));

    }

    /**
     * Test incorrect FreeMarker variable in template
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testIncorrectFreeMarkerVariable() throws IOException, URISyntaxException {

        String endpoint = "/test-incorrect-freemarker-variable";

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-freemarker-incorrect-variable.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-valid-json-object.json"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(500)
            .statusLine(Matchers.containsString("HTTP/1.1 500 Error processing template based on request"));
    }

    /**
     * Test template without body file and body content
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testTemplateNoBodyFileAndContent() throws IOException, URISyntaxException {

        String endpoint = "/test-template-no-body-file-content";

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withTransformers("freemarker-transformer")));

            given()
                .port(9980)
                .body(new File("src/test/resources/requests/request-valid-json-object.json"))
            .when()
                .post(endpoint)
            .then()
                .assertThat()
                .statusCode(200);
    }

    /**
     * Test template without FreeMarker
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testTemplateNoFreeMarker() throws IOException, URISyntaxException {

        String endpoint = "/test-template-no-freemarker";
        String expectedResponseAsFile = FileUtils.readFileToString(new File("src/test/resources/expects/expect-no-freemarker.json"), "UTF-8");
        String expectedResponse = expectedResponseAsFile.toString();

        wiremockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withBodyFile("template-no-freemarker.json")
                        .withTransformers("freemarker-transformer")));

        given()
            .port(9980)
            .body(new File("src/test/resources/requests/request-valid-json-object.json"))
        .when()
            .post(endpoint)
        .then()
            .assertThat()
            .statusCode(200)
            .body(Matchers.equalToIgnoringWhiteSpace(expectedResponse));
    }
}
