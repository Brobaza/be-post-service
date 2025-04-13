package post.service.be_post_service.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigYamlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            String profile = environment.getProperty("spring.profiles.active", "dev");
            String yamlFile = switch (profile) {
                case "production" -> "config.production.yaml";
                case "dev" -> "config.dev.yaml";
                default -> "config.yaml";
            };

            Yaml yaml = new Yaml();
            InputStream input = getClass().getClassLoader().getResourceAsStream(yamlFile);
            if (input != null) {
                Map<String, Object> yamlMap = yaml.load(input);
                if (yamlMap.containsKey("port")) {
                    environment.getPropertySources().addFirst(
                            new MapPropertySource("custom-config", Map.of("server.port", yamlMap.get("port"))));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load port from YAML", e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
