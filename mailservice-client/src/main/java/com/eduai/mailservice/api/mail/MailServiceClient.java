package com.eduai.mailservice.api.mail;

import com.eduai.mailservice.dto.request.BulkEmailRequestDto;
import com.eduai.mailservice.dto.request.EmailRequestDto;
import com.eduai.mailservice.dto.request.OtpRequestDto;
import com.eduai.mailservice.dto.request.ScheduleEmailRequestDto;
import com.eduai.mailservice.dto.response.BulkEmailResponseDto;
import com.eduai.mailservice.dto.response.EmailResponseDto;
import com.eduai.mailservice.dto.response.OtpResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OpenFeign client for consuming the EduAI Mail Service from other Spring Boot apps.
 *
 * <h2>Usage in consumer app:</h2>
 * <pre>
 * // 1. Add mailservice-client dependency to pom.xml
 * // 2. Add @EnableFeignClients to your main class
 * // 3. Configure the mail service URL:
 *
 * // application.yml:
 * mail-service:
 *   url: http://eduai-mail-service:8082
 *
 * // 4. Inject and use:
 * {@literal @}Autowired
 * private MailServiceClient mailServiceClient;
 *
 * mailServiceClient.sendOtp(OtpRequestDto.builder()
 *     .toEmail("user@example.com")
 *     .purpose("Login Verification")
 *     .appName("MyApp")
 *     .build());
 * </pre>
 *
 * <h2>application.yml config in consumer app:</h2>
 * <pre>
 * spring:
 *   cloud:
 *     openfeign:
 *       client:
 *         config:
 *           mail-service:
 *             url: ${MAIL_SERVICE_URL:http://localhost:8082}
 *             connectTimeout: 5000
 *             readTimeout: 15000
 *             loggerLevel: basic
 * </pre>
 */
@FeignClient(
        name  = "mail-service",
        url   = "${mail-service.url:http://localhost:8082}",
        path  = "/api/v1/mail"
)
public interface MailServiceClient extends MailApi {

    // All methods are inherited from MailApi — no additional code needed.
    // Override below if you need custom fallback / error decoder per method.
}
