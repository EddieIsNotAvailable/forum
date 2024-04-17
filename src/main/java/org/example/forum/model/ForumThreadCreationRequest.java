package org.example.forum.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter @NoArgsConstructor
public class ForumThreadCreationRequest {

    @Size(max=100)
    private String subject;

    @Size(max=500)
    private String content;

    @NotNull
    private MultipartFile fileData;

    @NotNull
    private String password;
}
