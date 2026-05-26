package com.access.control.service.impl;

import com.access.control.entity.EmergencyContact;
import com.access.control.mapper.EmergencyContactMapper;
import com.access.control.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private static final int MAX_CONTACTS = 3;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Autowired
    private EmergencyContactMapper emergencyContactMapper;

    @Override
    public List<EmergencyContact> listMine(Long userId) {
        return emergencyContactMapper.listByUserId(userId);
    }

    @Override
    @Transactional
    public String save(Long userId, EmergencyContact contact) {
        if (userId == null || contact == null) {
            return "参数错误";
        }
        String name = trim(contact.getName());
        String phone = trim(contact.getPhone());
        String relation = trim(contact.getRelation());

        if (name.isEmpty()) {
            return "请输入联系人姓名";
        }
        if (name.length() > 50) {
            return "姓名过长";
        }
        if (phone.isEmpty()) {
            return "请输入联系电话";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "请输入正确的11位手机号";
        }
        if (relation.length() > 20) {
            return "关系说明过长";
        }
        if (relation.isEmpty()) {
            relation = "其他";
        }

        Long id = contact.getId();
        if (id == null) {
            int count = emergencyContactMapper.countByUserId(userId);
            if (count >= MAX_CONTACTS) {
                return "最多添加 " + MAX_CONTACTS + " 位紧急联系人";
            }
            EmergencyContact row = new EmergencyContact();
            row.setUserId(userId);
            row.setName(name);
            row.setPhone(phone);
            row.setRelation(relation);
            return emergencyContactMapper.insert(row) > 0 ? null : "保存失败";
        }

        EmergencyContact existing = emergencyContactMapper.getByIdAndUserId(id, userId);
        if (existing == null) {
            return "联系人不存在";
        }
        int rows = emergencyContactMapper.update(id, userId, name, phone, relation);
        return rows > 0 ? null : "保存失败";
    }

    @Override
    @Transactional
    public boolean delete(Long userId, Long id) {
        if (userId == null || id == null) {
            return false;
        }
        return emergencyContactMapper.deleteByIdAndUserId(id, userId) > 0;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
