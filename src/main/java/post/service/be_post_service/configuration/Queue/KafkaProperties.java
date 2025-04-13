package post.service.be_post_service.configuration.Queue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private List<String> brokers;
    private String groupId;
    private List<Topic> topics;

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public static class Topic {
        private String name;
        private int partitions;
        private int replicationFactor;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPartitions() {
            return partitions;
        }

        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }

        public int getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(int replicationFactor) {
            this.replicationFactor = replicationFactor;
        }
    }
}
