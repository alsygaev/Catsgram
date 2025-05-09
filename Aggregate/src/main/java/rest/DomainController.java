package rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class DomainController {

    private static final String jsonDomainList = """
        {
           "tankFarm": "Контекст нефтебазы",
           "azs": "Контекст автозаправочной станции"
        }
        """;


    private static final String jsonTankFarm = """
        {
          "domain": "TankFarm",
          "description": "Контекст нефтебазы",
          "subdomains": [
            {
              "name": "RGS",
              "description": "Резервуар Горизонтальный Стальной",
              "services": [
                {
                  "method": "getId",
                  "description": "Метод для получения наименование резервуара по Id"
                },
                {
                  "method": "getVolume",
                  "description": "Метод для получения уровня топлива в РГС"
                }
              ]
            }
          ]
        }
        """;
    private static final String jsonAZS = """
        {
          "domain": "azs",
          "description": "Контекст АЗС",
          "subdomains": [
            {
              "name": "RVS",
              "description": "Резервуар Вертикальный Стальной",
              "services": [
                {
                  "method": "getAZSbyId",
                  "description": "Метод для получения наименование АЗС по id"
                }
              ]
            },
            {
              "name": "TRK",
              "description": "Топливо-раздаточная колонка",
              "services": [
                {
                  "method": "checkFuel",
                  "description": "Метод для получения данных по топливу в ТРК"
                }
              ]
            }
          ]
        }
        """;


    ObjectMapper mapper = new ObjectMapper();

    Map<String, Object> tankFarmMap = mapper.readValue(jsonTankFarm, Map.class);

    Map<String, Object> azsMap = mapper.readValue(jsonAZS, Map.class);


    public DomainController() throws JsonProcessingException {
    }

    /**
     * Получить список всех доменов или отдельного домена
     */
    @PostMapping("/domains")
    public Map<String, Object> getDomains(@RequestBody(required = false) Map<String, List<String>> request) throws JsonProcessingException {
        // Строка с JSON-описанием доменов
        final String jsonDomainList = """
        {
           "tankFarm": "Контекст нефтебазы",
           "azs": "Контекст автозаправочной станции"
        }
        """;

        // Преобразуем JSON в Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> allDomainDescriptions = mapper.readValue(jsonDomainList, Map.class);

        Map<String, String> filteredDescriptions;

        // Если запрос пустой — возвращаем все описания
        if (request == null || !request.containsKey("domains")) {
            filteredDescriptions = allDomainDescriptions;
        } else {
            List<String> requestedDomains = request.get("domains");

            // Оставляем только те, которые реально есть в JSON-описании
            filteredDescriptions = allDomainDescriptions.entrySet().stream()
                    .filter(entry -> requestedDomains.contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("domains", filteredDescriptions);
        return response;
    }


    /**
     * Получить все методы выбранного домена
     * {
     *   "domains": ["TankFarm"]
     * }
     */
    @PostMapping("/methods")
    public List<Map<String, Object>> getMethods(@RequestBody Map<String, Object> request) {
        List<String> domains = (List<String>) request.get("domains");
        String requestedSubdomain = (String) request.get("subdomain");

        if (domains == null || domains.isEmpty()) {
            throw new IllegalArgumentException("Атрибут 'domains' обязателен и должен содержать хотя бы один домен");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (String domain : domains) {
            Map<String, Object> domainMap;

            switch (domain) {
                case "TankFarm":
                    domainMap = tankFarmMap;
                    break;
                case "azs":
                    domainMap = azsMap;
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный домен: " + domain);
            }

            List<Map<String, Object>> subdomains = (List<Map<String, Object>>) domainMap.get("subdomains");
            List<Map<String, Object>> subdomainResults = new ArrayList<>();

            for (Map<String, Object> subdomain : subdomains) {
                String subName = (String) subdomain.get("name");

                if (requestedSubdomain == null || requestedSubdomain.equals(subName)) {
                    Map<String, Object> subdomainInfo = new LinkedHashMap<>();
                    subdomainInfo.put("name", subdomain.get("name"));
                    subdomainInfo.put("description", subdomain.get("description"));
                    subdomainInfo.put("services", subdomain.get("services"));

                    subdomainResults.add(subdomainInfo);
                }
            }

            if (subdomainResults.isEmpty()) {
                throw new IllegalArgumentException("Поддомен не найден: " + requestedSubdomain);
            }

            Map<String, Object> domainResult = new LinkedHashMap<>();
            domainResult.put("domain", domainMap.get("domain"));
            domainResult.put("description", domainMap.get("description"));
            domainResult.put("subdomains", subdomainResults);

            result.add(domainResult);
        }

        return result;
    }


    /*@PostMapping("/methods")
    public List<Map<String, Object>> getMethods(@RequestBody Map<String, List<String>> request) throws JsonProcessingException {
        List<String> requestedDomains = request.get("domains");

        if (requestedDomains == null || requestedDomains.isEmpty()) {
            throw new IllegalArgumentException("Атрибут 'domains' обязателен и должен содержать хотя бы один домен.");
        }

        List<Map<String, Object>> response = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (String domain : requestedDomains) {
            switch (domain) {
                case "TankFarm":
                    response.add(tankFarmMap);
                    break;

                // Пример для будущего домена
                // case "azs":
                //     response.add(azsMap);
                //     break;

                default:
                    throw new IllegalArgumentException("Неизвестный домен: " + domain);
            }
        }

        return response;
    }*/


    /*@PostMapping("/methods")
    public List<Map<String, Object>> getMethods(@RequestBody Map<String, String> request) {
        String requestedDomain = request.get("domain");
        String requestedSubdomain = request.get("subdomain");

        if (!tankFarmMap.get("domain").equals(requestedDomain)) {
            throw new IllegalArgumentException("Неизвестный домен: " + requestedDomain);
        }

        List<Map<String, Object>> methods = new ArrayList<>();

        // Получаем список поддоменов
        List<Map<String, Object>> subdomains = (List<Map<String, Object>>) tankFarmMap.get("subdomains");

        for (Map<String, Object> subdomain : subdomains) {
            String subdomainName = (String) subdomain.get("name");

            // Проверяем совпадение поддомена
            if (subdomainName.equals(requestedSubdomain)) {
                List<Map<String, Object>> services = (List<Map<String, Object>>) subdomain.get("services");

                if (services != null) {
                    methods.addAll(services);
                }

                break; // поддомен найден, выход из цикла
            }
        }

        if (methods.isEmpty()) {
            throw new IllegalArgumentException("Поддомен не найден или не содержит сервисов: " + requestedSubdomain);
        }

        return methods;
    }*/
}
