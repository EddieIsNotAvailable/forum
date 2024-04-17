package org.example.forum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForumThreadDTO {

    private Long id;
    private String subject;
    private String content;
    private String fileContentType;
    private byte[] fileData;
    private String dateTime;
}
