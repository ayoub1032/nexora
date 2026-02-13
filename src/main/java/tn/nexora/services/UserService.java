package tn.nexora.services;

import tn.nexora.entities.User;
import tn.nexora.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection connection;

    public UserService() {
        connection = MyConnection.getInstance().getConnection();
    }

    // CREATE
    public void addUser(User user) {

        String sql = "INSERT INTO users (full_name, email, password_hash, role, verification_status, account_status, face_id_data) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, user.getFull_name());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword_hash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getVerification_status());
            ps.setString(6, user.getAccount_status());
            ps.setString(7, user.getFace_id_data());

            ps.executeUpdate();
            System.out.println("✅ User added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User u = new User(
                        rs.getLong("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("verification_status"),
                        rs.getString("account_status"),
                        rs.getString("face_id_data"),
                        rs.getTimestamp("created_at")
                );

                users.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    // DELETE
    public void deleteUser(long id) {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
            System.out.println("🗑 User deleted!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UPDATE
    public void updateUser(User user) {

        String sql = "UPDATE users SET full_name=?, email=?, role=?, account_status=? WHERE user_id=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, user.getFull_name());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getAccount_status());
            ps.setLong(5, user.getUser_id());

            ps.executeUpdate();
            System.out.println("✏ User updated!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
