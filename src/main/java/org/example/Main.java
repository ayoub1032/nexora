package org.example;

import tn.nexora.entities.User;
import tn.nexora.services.UserService;

public class Main {

    public static void main(String[] args) {

        UserService us = new UserService();

        User u = new User(
                "Mariemt",
                "tester@nexora.com",
                "123456",
                "TRADER",
                "NON_VERIFIE",
                "ACTIVE",
                null
        );

        us.addUser(u);

        System.out.println(us.getAllUsers());
    }
}
