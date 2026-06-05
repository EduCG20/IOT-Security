package controller;

import dao.UserDAO;
import entities.User;

public class AuthController {
    private UserDAO userDAO;
    private User currentUser;
    
    public AuthController() {
        this.userDAO = new UserDAO();
    }
    
    public boolean login(String username, String password) {
        User user = userDAO.validateUser(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return isAuthenticated() && currentUser.isAdmin();
    }
}