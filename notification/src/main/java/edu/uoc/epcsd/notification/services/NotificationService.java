package edu.uoc.epcsd.notification.services;

import edu.uoc.epcsd.notification.kafka.ProductMessage;
import edu.uoc.epcsd.notification.rest.dtos.GetUserResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Log4j2
@Component
public class NotificationService {

    @Value("${userService.getUsersToAlert.url}")
    private String userServiceUrl;

    @Autowired
    private RestTemplateBuilder clientBuilder;

    public void notifyProductAvailable(ProductMessage productMessage) {

        GetUserResponse[] users = clientBuilder
                .build()
                .getForObject(
                        userServiceUrl,
                        GetUserResponse[].class,
                        productMessage.getProductId(),
                        LocalDate.now());

        if (users == null)
            return;

        Arrays
                .stream(users)
                .forEach(this::sendEmail);

    }

    private void sendEmail(GetUserResponse user) {
        log.info("Sending an email to user: " + user.getFullName());
    }
}
