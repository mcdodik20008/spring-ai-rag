package mcdodik.springai.api.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration
class ChatConfiguration {
    @Bean
    @Scope("prototype")
    fun chatMemory(): ChatMemory =
        MessageWindowChatMemory
            .builder()
            .maxMessages(20)
            .build()

    @Bean
    fun springSessionDefaultRedisSerializer(objectMapper: ObjectMapper): RedisSerializer<Any> {
        // 1. Создаем копию стандартного ObjectMapper'а, чтобы не влиять на другие части приложения.
        val configuredObjectMapper = objectMapper.copy()

        // 2. Говорим Jackson'у смотреть на все поля (даже приватные), а не на геттеры.
        configuredObjectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        configuredObjectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        // 3. Отключаем ошибку, если у объекта нет видимых свойств (наша проблема).
        configuredObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

        // 4. Включаем запись информации о типах в JSON. Это нужно для правильного
        // восстановления объектов из Redis, особенно для коллекций и интерфейсов.
        configuredObjectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY,
        )

        return GenericJackson2JsonRedisSerializer(configuredObjectMapper)
    }
}
