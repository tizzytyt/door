package com.access.control.service;

import com.access.control.entity.Announcement;
import com.access.control.entity.AnnouncementRead;
import com.access.control.entity.User;

import java.util.List;

public interface AnnouncementService {

    // --- admin ---
    boolean create(Announcement announcement);

    boolean update(Announcement announcement);

    boolean delete(Long id);

    List<Announcement> listAdmin();

    Announcement detailAdmin(Long id);

    List<AnnouncementRead> listReaders(Long announcementId);

    List<User> listUnreadUsers(Long announcementId);

    // --- student ---
    List<Announcement> listForUser(Long userId);

    long countUnreadForUser(Long userId);

    boolean markRead(Long announcementId, Long userId);
}

