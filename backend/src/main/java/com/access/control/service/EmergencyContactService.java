package com.access.control.service;

import com.access.control.entity.EmergencyContact;

import java.util.List;

public interface EmergencyContactService {

    List<EmergencyContact> listMine(Long userId);

    String save(Long userId, EmergencyContact contact);

    boolean delete(Long userId, Long id);
}
