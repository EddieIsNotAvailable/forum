package org.example.forum.controller;

import jakarta.validation.Valid;
import org.example.forum.model.ForumThread;
import org.example.forum.model.ForumThreadDTO;
import org.example.forum.model.ForumThreadCreationRequest;
import org.example.forum.service.ForumThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.BitSet;
import java.util.Optional;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final ForumThreadService forumThreadService;

    @Autowired
    public ThreadController(ForumThreadService forumThreadService) {
        this.forumThreadService = forumThreadService;
    }

    @GetMapping(path = "/all", produces = "application/json")
    public ResponseEntity<Page<ForumThreadDTO>> getAllThreads(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumThreadDTO> threads = forumThreadService.getAllThreadsPageable(pageable);
        return ResponseEntity.ok(threads);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createThread(@Valid @ModelAttribute ForumThreadCreationRequest thread, BindingResult result) {

        if(result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        MultipartFile file = thread.getFileData();

        byte[] fileData = null;
        try {
            fileData = file.getBytes();
        } catch(IOException e) {
            return ResponseEntity.badRequest().body("Failed to read file");
        }

        ForumThread newThread = new ForumThread();
        newThread.setSubject(thread.getSubject());
        newThread.setContent(thread.getContent());
        newThread.setFileContentType(file.getContentType());
        newThread.setFileData(fileData);
        newThread.setPassword(thread.getPassword());
        try {
            forumThreadService.createThread(newThread);
        } catch (Exception e) {
            System.out.println("Error creating thread: " + e);
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Failed to save thread");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(path="{threadId}/{password}")
    public ResponseEntity<String> getThread(@PathVariable Long threadId, @PathVariable String password) {
        boolean validPassword = forumThreadService.validatePassword(threadId, password);
        if(!validPassword) {
            return ResponseEntity.badRequest().body("Invalid password");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path="{threadId}/{password}")
    public ResponseEntity<?> deleteThread(@PathVariable Long threadId, @PathVariable String password) {
        boolean validPassword = forumThreadService.validatePassword(threadId, password);
        if(!validPassword) {
            return ResponseEntity.badRequest().body("Invalid password");
        }
        forumThreadService.deleteThread(threadId);
        return ResponseEntity.ok().build();
    }

    //Use to get single thread by id, requested externally
    @GetMapping(path="/{threadId}")
    public ResponseEntity<?> getThread(@PathVariable Long threadId) {
//        System.out.println("Getting thread by id: " + threadId);
        try {
            ForumThreadDTO thread = forumThreadService.getThreadById(threadId);
            String fdata = thread.getFileData();
            byte[] fbytes = Base64.getDecoder().decode(fdata);
            BitSet bits = BitSet.valueOf(fbytes);
            System.out.println("In Controller");
            for(int i=0; i<bits.size(); i++) {
                if(bits.get(i)) System.out.println("File [" + i + "] = 1");

            }

            //Print fbytes in hex
            for(int i=0; i< 50; i++) {
                System.out.printf("%d: %02X\t", i, fbytes[i]);

            }


            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
