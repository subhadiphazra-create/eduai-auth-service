package com.eduai.mailservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for bulk email send operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkEmailResponseDto {

    /** Batch/job ID */
    private String batchId;

    /** Total recipients in the request */
    private int totalRecipients;

    /** Number of emails successfully queued */
    private int queued;

    /** Number of emails that failed validation */
    private int failed;

    /** Whether the batch was accepted */
    private boolean accepted;

    /** Status message */
    private String message;

    /** Per-recipient results (messageId, status) */
    private List<RecipientResult> results;

    /** Correlation ID */
    private String correlationId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecipientResult {
        private String messageId;
        private String email;
        private boolean queued;
        private String reason;
        private Map<String, String> metadata;
    }
}
