package contentclassification.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.lang.reflect.Method;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    private static Logger logger = LoggerFactory.getLogger(RedisConfig.class);

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


    @Bean
    public CacheManager cacheManager() {
        logger.info("About to load cache manager bean.");
        RedisCacheManager redisCacheManager = new RedisCacheManager(getRedisTemplate());
        redisCacheManager.setDefaultExpiration(300);
        logger.info("Done loading cache manager bean. RedisCacheManager : "+ redisCacheManager.toString());
        return redisCacheManager;
    }

    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

    @Bean
    public KeyGenerator keyGenerator(){
        return new KeyGenerator() {
            @Override
            public Object generate(Object o, Method method, Object... objects) {
                StringBuilder sb = new StringBuilder();
                sb.append(o.getClass().getName());
                sb.append(method.getName());
                for (Object obj : objects) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }


    @Bean
    public StringRedisTemplate getRedisTemplate(){
        final StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory());

        @SuppressWarnings("unchecked")
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer(Object.class);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }
}
