package cms.model.service;

import cms.model.dao.AdminDao;
import cms.model.dto.Admin;
import cms.util.Validator;

public class AuthService {

    private final AdminDao adminDao;

    public AuthService(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    public Admin login(String username, String password) {
        String user = Validator.requireText(username, "Username");
        String pass = Validator.requireText(password, "Password");
        Admin admin = adminDao.findByUsername(user);
        if (admin == null || !admin.getPassword().equals(pass)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return admin;
    }
}
