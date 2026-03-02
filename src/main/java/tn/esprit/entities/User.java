package tn.esprit.entities;

public class User {
    private long userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;  // ADMIN, USER
    private String verificationStatus;
    private String accountStatus;  // ACTIVE, INACTIVE
    private String faceIdData;
    private String createdAt;

    // Constructors
    public User() {}

    public User(String fullName, String email, String passwordHash, String role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.verificationStatus = "UNVERIFIED";
        this.accountStatus = "ACTIVE";
        this.faceIdData = null;
    }

    public User(long userId, String fullName, String email, String passwordHash,
                String role, String verificationStatus, String accountStatus,
                String faceIdData, String createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.verificationStatus = verificationStatus;
        this.accountStatus = accountStatus;
        this.faceIdData = faceIdData;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getFaceIdData() { return faceIdData; }
    public void setFaceIdData(String faceIdData) { this.faceIdData = faceIdData; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}
