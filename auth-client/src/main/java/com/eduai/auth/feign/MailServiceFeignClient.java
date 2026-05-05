package com.eduai.auth.feign;

import com.eduai.mailservice.dto.request.OtpRequestDto;
import com.eduai.mailservice.dto.response.OtpResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mail-service", url = "${mail-service.url:http://localhost:8084}")
public interface MailServiceFeignClient {
    @PostMapping("/api/v1/mail/send/otp")
    ResponseEntity<OtpResponseDto> sendOtp(@RequestBody OtpRequestDto request);
}