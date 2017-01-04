##Wiremock FreeMarker Transformer
Wiremock FreeMarker Transformer is a [Wiremock](http://wiremock.org/) extension.

###Features
* Full [FreeMarker](http://freemarker.org/) templating in reponse definitions
* Request body in JSON (both JSON objects and JSON arrays) or XML available as FreeMarker variable named `request`

###Use within custom WireMock Java implementation

####Maven dependency
```
<dependency>
		<groupId>com.github.kwoding</groupId>
		<artifactId>wiremock-freemarker-transformer</artifactId>
		<version>1.0.1</version>
</dependency>
```

###Run it with [Wiremock standalone process](http://wiremock.org/docs/running-standalone/):

####Start WireMock server with FreeMarker Transformer extension

For Unix:
```
java -cp "wiremock-standalone-2.4.1.jar:wiremock-freemarker-transformer-1.0.1.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --extensions com.github.kwoding.wiremock.extension.FreeMarkerTransformer
```

For Windows:
```
java -cp "wiremock-standalone-2.4.1.jar;wiremock-freemarker-transformer-1.0.1.jar" com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --extensions com.github.kwoding.wiremock.extension.FreeMarkerTransformer
```

###How to use
* Add the `freemarker-transformer` to the response definition via the "body-transformer" name
* It is then possible to use FreeMarker variables in the response definition
* The body of the request (if present) is available via the FreeMarker variable named `request`
* It uses standard FreeMarker functionality to access the specific nodes

Example stub definition:
```
{
    "request": {
        "method": "POST",
        "url": "/test"
    },
    "response": {
        "status": 200,
        "body": "{<#list ['a', 'b', 'c'] as i> \"${i?counter}\": \"${i}\", </#list> \"message\": \"Value of message\", \"test\": \"${request.batters.batter[1].id} ${request.batters.batter[1].type}\", \"test2\": \"${request.topping[2].id} ${request.topping[2].type}\"}",
        "transformers": ["freemarker-transformer"]
    }
}
```

Corresponding example request

```
{
  "id": "0001",
  "type": "donut",
  "name": "Cake",
  "ppu": 0.55,
  "batters":
  {
    "batter":
    [
      { "id": "1001", "type": "Regular" },
      { "id": "1002", "type": "Chocolate" },
      { "id": "1003", "type": "Blueberry" },
      { "id": "1004", "type": "Devil's Food" }
    ]
  },
  "topping":
  [
    { "id": "5001", "type": "None" },
    { "id": "5002", "type": "Glazed" },
    { "id": "5005", "type": "Sugar" },
    { "id": "5007", "type": "Powdered Sugar" },
    { "id": "5006", "type": "Chocolate with Sprinkles" },
    { "id": "5003", "type": "Chocolate" },
    { "id": "5004", "type": "Maple" }
  ]
}
```

Response for this example:
```
{ "1": "a",  "2": "b",  "3": "c",  "message": "Value of message", "test": "1002 Chocolate", "test2": "5005 Sugar"}
```