package openapi.controller;

import openapi.service.OpenApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/convert")
public class OpenApiController {

    @Autowired
    private OpenApiService openApiService;

    @PostMapping(value = "/yaml", produces = "text/yaml")
    public ResponseEntity<String> convertToYaml(
            @RequestParam String endpoint,
            @RequestBody(required = true) Map<String, Object> body) {

        String yaml = openApiService.generateOpenApiYaml(endpoint, body);
        return ResponseEntity.ok()
                .header("Content-Type", "text/yaml")
                .body(yaml);
    }
}
