package com.mipt.nikitabumagin.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachment {

    Long id;
    Long taskId;
    String fileName;
    String storedFileName;
    String contentType;
    Long size;
    LocalDateTime uploadedAt;
}
