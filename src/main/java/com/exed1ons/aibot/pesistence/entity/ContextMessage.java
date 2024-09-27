package com.exed1ons.aibot.pesistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    Role role;

    @Column(name = "author_id")
    String authorId;
}