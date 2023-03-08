package stream;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.internals.Topic;
import org.slf4j.LoggerFactory;

public class Util implements AutoCloseable {

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(Util.class);

    public void createTopics(final Properties allProperties, List<NewTopic> topicList) 
        throws ExecutionException,TimeoutException,InterruptedException {
            try (final AdminClient client = AdminClient.create(allProperties) ){
                logger.info("Creating Topics");
                
                client.createTopics(topicList).values().forEach((topic,future) -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        logger.info(e.toString());
                    }
                });

                Collection<String> topicNames = topicList
                    .stream()
                    .map(t -> t.name())
                    .collect(Collectors.toCollection(LinkedList::new));
                
                logger.info("Asking cluster for topic desc");

                client.describeTopics(topicNames)
                    .allTopicNames()
                    .get(10,TimeUnit.SECONDS)
                    .forEach((name, description) -> logger.info("Topic Desc: {}", description.toString()));
            }
    }

    public class Randomizer implements AutoCloseable,Runnable {
        private Properties props;
        private String topic;
        private Producer<String, Object> producer;
        private boolean closed;

        public Randomizer(Properties producerProps, String topic) {
            this.props = producerProps;
            this.props.setProperty("client.id", "fake");
            this.topic = topic;
            this.closed = false;
        }

        @Override
        public void run() {
            try(KafkaProducer<String, Object> producer = new KafkaProducer<String, Object>(props)) {
                Faker faker = new Faker();
                while (!closed) {
                    try {
                        Object result = producer.send(new ProducerRecord<String,Object>(this.topic, faker.address().buildingNumber())).get();
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.info("Interrupted");
                    }
                }
            } catch (Exception e) {
                logger.error(topic, e.toString());
            }
        }
        @Override
        public void close() {
            closed=true;
        }
        
    }

    @Override
    public void close() {
        if (executorService!=null){
            executorService.shutdown();
            executorService=null;
        }
    }

    
}