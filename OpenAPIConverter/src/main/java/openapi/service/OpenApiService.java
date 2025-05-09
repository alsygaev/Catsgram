package openapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenApiService {

    private static final String AGGREGATE_BASE_URL = "http://localhost:8080/api";

    public String generateOpenApiYaml(String endpoint, Map<String, Object> requestBody) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = AGGREGATE_BASE_URL + "/" + endpoint;

            if (requestBody == null) {
                requestBody = new HashMap<>();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Object> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class
            );

            Object response = responseEntity.getBody();
            if (response == null) {
                return "Ответ пустой или некорректный";
            }

            Map<String, Object> openApiSpec = new LinkedHashMap<>();
            openApiSpec.put("openapi", "3.0.0");
            openApiSpec.put("servers", List.of(
                    Map.of("url", "http://localhost:8080")
            ));


            Map<String, String> info = new LinkedHashMap<>();
            info.put("title", "Техническая документация по /" + endpoint);
            info.put("version", "1.0.0");
            openApiSpec.put("info", info);

            String domainDesc = extractDescriptionFromResponse(response);
            info.put("description", domainDesc);


            Map<String, Object> paths = extractPaths(endpoint, response);
            openApiSpec.put("paths", paths);

            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.writeValueAsString(openApiSpec);

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при генерации YAML: " + e.getClass().getSimpleName() +
                    (e.getMessage() != null ? " — " + e.getMessage() : " — (нет описания)");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractDescriptionFromResponse(Object response) {
        if (response instanceof Map<?, ?> map) {
            // Ищем первую строку-описание
            for (Object value : map.values()) {
                if (value instanceof String desc) {
                    return desc;
                }
                if (value instanceof Map<?, ?> innerMap) {
                    for (Object innerValue : innerMap.values()) {
                        if (innerValue instanceof String innerDesc) {
                            return innerDesc;
                        }
                    }
                }
            }
        } else if (response instanceof List<?> list && !list.isEmpty()) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> domainMap) {
                    Object descObj = domainMap.get("description");
                    if (descObj instanceof String desc) {
                        return desc;
                    }
                    // Ищем в subdomains
                    Object subdomains = domainMap.get("subdomains");
                    if (subdomains instanceof List<?> subList) {
                        for (Object subItem : subList) {
                            if (subItem instanceof Map<?, ?> subMap) {
                                Object subDesc = subMap.get("description");
                                if (subDesc instanceof String) {
                                    return (String) subDesc;
                                }
                            }
                        }
                    }
                }
            }
        }
        return "Описание недоступно"; // Фолбэк
    }


    private Map<String, Object> extractPaths(String endpoint, Object response) {
        Map<String, Object> paths = new LinkedHashMap<>();

        if (response instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();

                // если вложенная Map (например, "domains" -> { "tankFarm": "...", "azs": "..." })
                if (value instanceof Map<?, ?> innerMap && innerMap.values().stream().allMatch(v -> v instanceof String)) {
                    for (Map.Entry<?, ?> innerEntry : innerMap.entrySet()) {
                        String innerKey = String.valueOf(innerEntry.getKey());
                        String description = String.valueOf(innerEntry.getValue());
                        paths.put("/" + innerKey, Map.of(
                                "post", Map.of(
                                        "summary", "Контекст для " + innerKey,
                                        "responses", Map.of("200", Map.of("description", description))
                                )
                        ));
                    }
                }
                // если сразу строки (например, "tankFarm": "Контекст нефтебазы")
                else if (value instanceof String description) {
                    paths.put("/" + key, Map.of(
                            "post", Map.of(
                                    "summary", "Контекст для " + key,
                                    "responses", Map.of("200", Map.of("description", description))
                            )
                    ));
                }
                // fallback: добавляем корневой путь
                else {
                    paths.put("/" + endpoint, Map.of(
                            "post", Map.of(
                                    "summary", "Нет описания",
                                    "responses", Map.of("200", Map.of("description", "Успешный ответ"))
                            )
                    ));
                }
            }

        } else if (response instanceof List<?> list && !list.isEmpty()) {
            for (Object obj : list) {
                if (obj instanceof Map<?, ?> rawDomainMap) {
                    Map<String, Object> domainMap = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : rawDomainMap.entrySet()) {
                        domainMap.put(String.valueOf(entry.getKey()), entry.getValue());
                    }

                    String domain = String.valueOf(domainMap.getOrDefault("domain", endpoint));
                    String domainDesc = String.valueOf(domainMap.getOrDefault("description", "Контекст " + domain));

                    List<?> subdomainsRaw = (List<?>) domainMap.get("subdomains");
                    if (subdomainsRaw != null) {
                        for (Object subRaw : subdomainsRaw) {
                            if (!(subRaw instanceof Map<?, ?>)) continue;

                            Map<String, Object> sub = new LinkedHashMap<>();
                            for (Map.Entry<?, ?> entry : ((Map<?, ?>) subRaw).entrySet()) {
                                sub.put(String.valueOf(entry.getKey()), entry.getValue());
                            }

                            String subdomain = String.valueOf(sub.get("name"));

                            List<?> servicesRaw = (List<?>) sub.get("services");
                            if (servicesRaw != null) {
                                for (Object serviceRaw : servicesRaw) {
                                    if (!(serviceRaw instanceof Map<?, ?>)) continue;

                                    Map<String, Object> service = new LinkedHashMap<>();
                                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) serviceRaw).entrySet()) {
                                        service.put(String.valueOf(entry.getKey()), entry.getValue());
                                    }

                                    String methodName = String.valueOf(service.get("method"));
                                    String methodDesc = String.valueOf(service.getOrDefault("description", "Метод " + methodName));

                                    String path = "/" + domain + "/" + subdomain + "/" + methodName;
                                    paths.put(path, Map.of(
                                            "post", Map.of(
                                                    "summary", methodDesc,
                                                    "responses", Map.of("200", Map.of("description", "Успешный ответ"))
                                            )
                                    ));
                                }
                            }
                        }
                    }

                    String rootPath = "/" + domain;
                    paths.putIfAbsent(rootPath, Map.of(
                            "post", Map.of(
                                    "summary", "Контекст для " + domain,
                                    "responses", Map.of("200", Map.of("description", domainDesc))
                            )
                    ));
                }
            }
        } else {
            paths.put("/" + endpoint, Map.of(
                    "post", Map.of(
                            "summary", "Сырой ответ",
                            "responses", Map.of("200", Map.of("description", response.toString()))
                    )
            ));
        }

        return paths;
    }


}
