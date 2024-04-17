package org.example.forum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity @Getter @Setter @ToString
@NoArgsConstructor
public class ForumThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max= 100)
    private String subject;
    @Size(max= 2000)
    private String content;

    @NotNull
    private String fileContentType;

    @Lob @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    @NotNull
    private byte[] fileData;
    @NotNull
    private String password;
    @NotNull
    private String dateTime;


}
