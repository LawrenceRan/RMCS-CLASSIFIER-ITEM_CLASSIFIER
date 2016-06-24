package contentclassification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    private static final String HOST = System.getProperty("spring.redis.host");
    private static final Integer DATABASE = Integer.parseInt(System.getProperty("spring.redis.database", "0"));
    private static final String PASSWORD = System.getProperty("spring.redis.password");
    private static final Integer PORT = Integer.parseInt(System.getProperty("spring.redis.port", "6379"));

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setDatabase(DATABASE);
        jedisConnectionFactory.setHostName("localhost");
        jedisConnectionFactory.setPassword(PASSWORD);
        jedisConnectionFactory.setPort(PORT);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(){
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
