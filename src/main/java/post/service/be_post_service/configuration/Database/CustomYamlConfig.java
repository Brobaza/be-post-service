package post.service.be_post_service.configuration.Database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "postgres")
public class CustomYamlConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;

    public String getJdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }
}
