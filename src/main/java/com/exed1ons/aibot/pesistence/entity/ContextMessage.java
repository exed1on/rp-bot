package com.exed1ons.aibot.pesistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Table(name = "context_messages")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContextMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "text", columnDefinition = "TEXT")
    @NotBlank
    @NotEmpty
    @Size(min = 1, max = 1024)
    String text;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    Role role;

    @Column(name = "author_id")
    String authorId;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    MessageType messageType;

    @Column(name = "requires_tools")
    Boolean requiresTools;

    @Column(name = "model_used")
    String modelUsed;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}