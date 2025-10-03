package de.tuotuo.cloudjvmdemo;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka 4.0 部署测试工具
 * 支持 KRaft 模式（无需 Zookeeper）
 */
public class KafkaDeploymentTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TEST_TOPIC = "test-topic";

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("Kafka 4.0 部署测试");
        System.out.println("==============================================");
        System.out.println("连接地址: " + BOOTSTRAP_SERVERS);
        System.out.println("Kafka 4.0 使用 KRaft 模式（无需 Zookeeper）");
        System.out.println("==============================================\n");

        try {
            // 0. 测试网络连通性
            testNetworkConnection();

            // 1. 测试连接到 Kafka 集群
            testConnection();

            // 2. 测试创建主题
            testCreateTopic();

            // 3. 测试生产消息
            testProducer();

            // 4. 测试消费消息
            testConsumer();

            System.out.println("\n==============================================");
            System.out.println("✓ Kafka 4.0 部署测试全部通过！");
            System.out.println("==============================================");
        } catch (Exception e) {
            System.err.println("\n==============================================");
            System.err.println("✗ Kafka 部署测试失败");
            System.err.println("==============================================");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("\n详细堆栈:");
            e.printStackTrace();
            System.err.println("\n请检查:");
            System.err.println("1. Kafka 4.0 是否已正确启动（使用 KRaft 模式）");
            System.err.println("2. 配置文件 kraft/server.properties 中的监听地址");
            System.err.println("3. 端口 9092 是否被占用或被防火墙阻止");
        }
    }

    /**
     * 测试网络连通性
     */
    private static void testNetworkConnection() {
        System.out.println("【步骤 0】测试网络连通性");
        System.out.println("-------------------------------------------");

        String host = BOOTSTRAP_SERVERS.split(":")[0];
        int port = Integer.parseInt(BOOTSTRAP_SERVERS.split(":")[1]);

        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 5000);
            System.out.println("✓ 端口连接成功: " + BOOTSTRAP_SERVERS);
            System.out.println("  TCP 连接正常，Kafka 端口可访问\n");
        } catch (Exception e) {
            System.err.println("✗ 无法连接到 " + BOOTSTRAP_SERVERS);
            System.err.println("  错误: " + e.getMessage());
            System.err.println("\n故障排查建议:");
            System.err.println("  1. 检查 Kafka 是否启动: ps aux | grep kafka");
            System.err.println("  2. 检查端口监听: netstat -tuln | grep " + port);
            System.err.println("  3. 检查配置文件: kraft/server.properties");
            System.err.println("     listeners=PLAINTEXT://:9092");
            System.err.println("     advertised.listeners=PLAINTEXT://localhost:9092");
            throw new RuntimeException("网络连接失败", e);
        }
    }

    /**
     * 测试连接到 Kafka 集群（使用 Admin 客户端）
     */
    private static void testConnection() throws Exception {
        System.out.println("【步骤 1】测试连接到 Kafka 4.0 集群");
        System.out.println("-------------------------------------------");

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 15000);

        try (Admin admin = Admin.create(props)) {
            // 获取集群信息
            var clusterInfo = admin.describeCluster();
            String clusterId = clusterInfo.clusterId().get();
            int nodeCount = clusterInfo.nodes().get().size();
            var controller = clusterInfo.controller().get();

            System.out.println("✓ 成功连接到 Kafka 4.0 集群");
            System.out.println("  集群 ID: " + clusterId);
            System.out.println("  节点数量: " + nodeCount);
            System.out.println("  Controller: " + controller.host() + ":" + controller.port());
            System.out.println("  模式: KRaft（无 Zookeeper）\n");
        }
    }

    /**
     * 测试创建主题
     */
    private static void testCreateTopic() throws Exception {
        System.out.println("【步骤 2】测试创建主题");
        System.out.println("-------------------------------------------");

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);

        try (Admin admin = Admin.create(props)) {
            // 先尝试删除已存在的测试主题
            try {
                admin.deleteTopics(Collections.singletonList(TEST_TOPIC)).all().get();
                System.out.println("  清理旧主题: " + TEST_TOPIC);
                Thread.sleep(2000); // 等待删除完成
            } catch (Exception e) {
                // 主题不存在，忽略
            }

            // 创建新主题（Kafka 4.0 支持更多配置选项）
            NewTopic newTopic = new NewTopic(TEST_TOPIC, 3, (short) 1); // 3个分区，1个副本
            admin.createTopics(Collections.singletonList(newTopic)).all().get();

            System.out.println("✓ 主题创建成功");
            System.out.println("  主题名称: " + TEST_TOPIC);
            System.out.println("  分区数: 3");
            System.out.println("  副本数: 1\n");
        }
    }

    /**
     * 测试生产消息
     */
    private static void testProducer() throws Exception {
        System.out.println("【步骤 3】测试生产消息");
        System.out.println("-------------------------------------------");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // 确保消息可靠性
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Kafka 4.0 默认启用幂等性

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            int messageCount = 10;
            System.out.println("发送 " + messageCount + " 条测试消息...\n");

            for (int i = 1; i <= messageCount; i++) {
                String key = "key-" + i;
                String value = "Kafka 4.0 测试消息 #" + i + " - 时间戳: " + System.currentTimeMillis();

                ProducerRecord<String, String> record =
                        new ProducerRecord<>(TEST_TOPIC, key, value);

                Future<RecordMetadata> future = producer.send(record);
                RecordMetadata metadata = future.get();

                System.out.println("  ✓ 消息 #" + i + " 发送成功");
                System.out.println("    分区: " + metadata.partition() +
                        " | 偏移量: " + metadata.offset() +
                        " | 键: " + key);
            }

            System.out.println("\n✓ 所有消息发送完成\n");
        }
    }

    /**
     * 测试消费消息
     */
    private static void testConsumer() throws Exception {
        System.out.println("【步骤 4】测试消费消息");
        System.out.println("-------------------------------------------");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + System.currentTimeMillis());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TEST_TOPIC));

            System.out.println("开始消费消息（最多等待 15 秒）...\n");

            int messageCount = 0;
            long startTime = System.currentTimeMillis();
            int expectedMessages = 10;

            while (messageCount < expectedMessages && (System.currentTimeMillis() - startTime) < 15000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    messageCount++;
                    System.out.println("  ✓ 消息 #" + messageCount + " 接收成功");
                    System.out.println("    键: " + record.key() +
                            " | 分区: " + record.partition() +
                            " | 偏移量: " + record.offset());
                    System.out.println("    值: " + record.value());
                }
            }

            System.out.println("\n✓ 消息消费完成");
            System.out.println("  总计接收: " + messageCount + " 条消息");

            if (messageCount == 0) {
                throw new Exception("未接收到任何消息，请检查 Kafka 配置");
            }

            if (messageCount < expectedMessages) {
                System.out.println("  ⚠ 警告: 预期 " + expectedMessages + " 条，实际接收 " + messageCount + " 条");
            }
        }
    }
}