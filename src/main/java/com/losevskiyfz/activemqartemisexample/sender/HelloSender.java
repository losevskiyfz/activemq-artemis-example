package com.losevskiyfz.activemqartemisexample.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.losevskiyfz.activemqartemisexample.config.JmsConfig;
import com.losevskiyfz.activemqartemisexample.model.HelloWorldMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;

    public HelloSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    private MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
    private ObjectMapper objectMapper = jacksonMessageConverter.getObjectMapper();

    @Scheduled(fixedRate = 2000)
    public void sendMessage(){

        HelloWorldMessage message = HelloWorldMessage
                .builder()
                .id(UUID.randomUUID())
                .message("Hello World!")
                .build();

        jmsTemplate.convertAndSend(JmsConfig.MY_QUEUE, message);

    }

    @Scheduled(fixedRate = 2000)
    public void sendandReceiveMessage() throws JMSException {

        HelloWorldMessage message = HelloWorldMessage
                .builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        Message receviedMsg = jmsTemplate.sendAndReceive(JmsConfig.MY_SEND_RCV_QUEUE, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message helloMessage = null;

                try {
                    helloMessage = session.createTextMessage(objectMapper.writeValueAsString(message));
                    helloMessage.setStringProperty("_type", "com.losevskiyfz.activemqartemisexample.model.HelloWorldMessage");

                    System.out.println("Sending Hello");

                    return helloMessage;

                } catch (JsonProcessingException e) {
                    throw new JMSException("boom");
                }
            }
        });

        System.out.println(receviedMsg.getBody(String.class));

    }

}