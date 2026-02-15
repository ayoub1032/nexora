package tn.esprit.utils;

/**
 * Utility to generate BCrypt hash for admin password setup
 * 
 * Run this as a Java application to generate a BCrypt hash
 * Copy the output and paste into ADMIN_ACCOUNT_SETUP.sql
 */
public class HashGeneratorUtil {
    
    public static void main(String[] args) {
        // Default admin password - CHANGE THIS if you want a different password
        String plainPassword = "Admin@12345!";
        
        // Generate BCrypt hash
        String bcryptHash = PasswordHasher.hash(plainPassword);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("BCrypt Hash Generator");
        System.out.println("=".repeat(60));
        System.out.println("\nPlain Password: " + plainPassword);
        System.out.println("\nGenerated BCrypt Hash:");
        System.out.println(bcryptHash);
        System.out.println("\n" + "-".repeat(60));
        System.out.println("COPY THE HASH ABOVE AND PASTE IT INTO ADMIN_ACCOUNT_SETUP.sql");
        System.out.println("Replace: GENERATED_BCRYPT_HASH_HERE");
        System.out.println("-".repeat(60) + "\n");
    }
}
