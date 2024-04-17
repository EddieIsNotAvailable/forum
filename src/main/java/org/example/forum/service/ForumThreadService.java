package org.example.forum.service;

import org.example.forum.model.ForumThread;
import org.example.forum.model.ForumThreadDTO;
import org.example.forum.repository.ForumThreadRepository;
import org.example.forum.utility.TranscodeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.example.forum.utility.TranscodeMessage.getOffsetBitSize;
import static org.example.forum.utility.TranscodeMessage.getPeriods;

@Service
public class ForumThreadService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ForumThreadRepository forumThreadRepository;

    @Autowired
    public ForumThreadService(ForumThreadRepository forumThreadRepository) {
        this.forumThreadRepository = forumThreadRepository;
    }

    private ForumThreadDTO ThreadToDTO(ForumThread thread) {
        return new ForumThreadDTO(
            thread.getId(),
            thread.getSubject(),
            thread.getContent(),
            thread.getFileContentType(),
//            Base64.getEncoder().encodeToString(thread.getFileData()),
            thread.getFileData(),
            thread.getDateTime()
        );
    }

    public Page<ForumThreadDTO> getAllThreadsPageable(Pageable pageable) {
        Page<ForumThread> threads = forumThreadRepository.findAllByOrderByIdDesc(pageable);
        return threads.map(this::ThreadToDTO);
    }

    public void createThread(ForumThread thread) {
        LocalDateTime now = LocalDateTime.now();

        String dateTime = now.format(DATE_TIME_FORMATTER);
        thread.setDateTime(dateTime);

//        LocalDateTime d = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
//        int sec = d.getSecond();
//        int offset = getOffsetBitSize(thread.getSubject(),0);
//        int period = getPeriodBitSize(thread.getSubject(), sec);

        //Split subject into words, remove empty strings
        System.out.println("Thread content: " + thread.getContent());
        System.out.println("Thread subject: " + thread.getSubject());
        System.out.println("file size (bytes): " + thread.getFileData().length);
        int offset = getOffsetBitSize(thread.getContent());
        int[] periods = getPeriods(thread.getSubject());
        System.out.println("In creating thread, offset: " + offset + " periods: " + Arrays.toString(periods));
        byte[] encodedFileData = TranscodeMessage.encodeMessage(thread.getFileData(), thread.getPassword(), offset, periods);

        thread.setFileData(encodedFileData);

        System.out.println("Saved thread (id: " + thread.getId() + " offset: " + offset + " periods: " + Arrays.toString(periods) + ")");
        forumThreadRepository.save(thread);
    }

    public boolean validatePassword(Long threadId, String password) {
        Optional<ForumThread> t = forumThreadRepository.findById(threadId);
        if(t.isEmpty()) return false;

        ForumThread thread = t.get();
//        int offset = getOffsetBitSize(thread.getContent());
//        int[] periods = getPeriods(thread.getSubject());
//        return TranscodeMessage.decodeMessage(thread.getFileData(), thread.getPassword(), offset, periods);
        return thread.getPassword().equals(password);
    }

    public void deleteThread(Long threadId) {
        forumThreadRepository.deleteById(threadId);
    }

    public ForumThreadDTO getThreadById(Long threadId) {
        ForumThread thread = forumThreadRepository.findById(threadId).orElse(null);
        if(thread == null) throw new IllegalArgumentException("Thread not found");
        return ThreadToDTO(thread);
    }
}
