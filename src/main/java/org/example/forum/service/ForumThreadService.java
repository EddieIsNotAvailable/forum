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

        int offset = getOffsetBitSize(thread.getContent());
        int[] periods = getPeriods(thread.getSubject());
        byte[] encodedFileData = TranscodeMessage.encodeMessage(thread.getFileData(), thread.getPassword(), offset, periods);
        thread.setFileData(encodedFileData);

        forumThreadRepository.save(thread);
    }

    public boolean validatePassword(Long threadId, String password) {
        Optional<ForumThread> t = forumThreadRepository.findById(threadId);
        if(t.isEmpty()) return false;
        ForumThread thread = t.get();
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

    public byte[] getThreadFileById(Long threadId) {
        ForumThread thread = forumThreadRepository.findById(threadId).orElse(null);
        if(thread == null) throw new IllegalArgumentException("Thread not found");
        return thread.getFileData();
    }
}
