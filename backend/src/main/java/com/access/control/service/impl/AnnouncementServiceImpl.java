package com.access.control.service.impl;

import com.access.control.entity.Announcement;
import com.access.control.entity.AnnouncementRead;
import com.access.control.entity.User;
import com.access.control.mapper.AnnouncementMapper;
import com.access.control.mapper.AnnouncementReadMapper;
import com.access.control.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private AnnouncementReadMapper announcementReadMapper;

    @Override
    public boolean create(Announcement announcement) {
        if (announcement == null) return false;
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) return false;
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) return false;
        if (announcement.getPublisherId() == null) return false;
        return announcementMapper.insert(announcement) > 0;
    }

    @Override
    public boolean update(Announcement announcement) {
        if (announcement == null || announcement.getId() == null) return false;
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) return false;
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) return false;
        return announcementMapper.update(announcement) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        return announcementMapper.deleteById(id) > 0;
    }

    @Override
    public List<Announcement> listAdmin() {
        List<Announcement> list = announcementMapper.listAdmin();
        if (list != null) {
            for (Announcement a : list) {
                long total = a.getTotalStudents() == null ? 0 : a.getTotalStudents();
                long read = a.getReadCount() == null ? 0 : a.getReadCount();
                a.setUnreadCount(Math.max(0, total - read));
            }
        }
        return list;
    }

    @Override
    public Announcement detailAdmin(Long id) {
        Announcement a = announcementMapper.getAdminDetail(id);
        if (a == null) return null;
        long total = a.getTotalStudents() == null ? 0 : a.getTotalStudents();
        long read = a.getReadCount() == null ? 0 : a.getReadCount();
        a.setUnreadCount(Math.max(0, total - read));
        return a;
    }

    @Override
    public List<AnnouncementRead> listReaders(Long announcementId) {
        return announcementReadMapper.listReaders(announcementId);
    }

    @Override
    public List<User> listUnreadUsers(Long announcementId) {
        return announcementReadMapper.listUnreadUsers(announcementId);
    }

    @Override
    public List<Announcement> listForUser(Long userId) {
        return announcementMapper.listForUser(userId);
    }

    @Override
    public long countUnreadForUser(Long userId) {
        return announcementMapper.countUnreadForUser(userId);
    }

    @Override
    public boolean markRead(Long announcementId, Long userId) {
        if (announcementId == null || userId == null) return false;
        // insert: 1 row; duplicate update: 2 rows (MySQL). 都视为成功
        return announcementReadMapper.insertOrIgnore(announcementId, userId) >= 1;
    }
}

