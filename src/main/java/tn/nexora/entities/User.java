package tn.nexora.entities;

import java.sql.Timestamp;

public class User {

    private long user_id;
    private String full_name;
    private String email;
    private String password_hash;
    private String role;
    private String verification_status;
    private String account_status;
    private String face_id_data;
    private Timestamp created_at;

    // Constructor without ID
    public User(String full_name, String email, String password_hash,
                String role, String verification_status,
                String account_status, String face_id_data) {

        this.full_name = full_name;
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.verification_status = verification_status;
        this.account_status = account_status;
        this.face_id_data = face_id_data;
    }

    // Full constructor
    public User(long user_id, String full_name, String email, String password_hash,
                String role, String verification_status,
                String account_status, String face_id_data, Timestamp created_at) {

        this.user_id = user_id;
        this.full_name = full_name;
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.verification_status = verification_status;
        this.account_status = account_status;
        this.face_id_data = face_id_data;
        this.created_at = created_at;
    }

    // Getters & Setters

    public long getUser_id() { return user_id; }
    public void setUser_id(long user_id) { this.user_id = user_id; }

    public String getFull_name() { return full_name; }
    public void setFull_name(String full_name) { this.full_name = full_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getVerification_status() { return verification_status; }
    public void setVerification_status(String verification_status) { this.verification_status = verification_status; }

    public String getAccount_status() { return account_status; }
    public void setAccount_status(String account_status) { this.account_status = account_status; }

    public String getFace_id_data() { return face_id_data; }
    public void setFace_id_data(String face_id_data) { this.face_id_data = face_id_data; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + user_id +
                ", name='" + full_name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
