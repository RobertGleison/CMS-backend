package com.backend.Netflix.controllers;

import com.backend.Netflix.model.User;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.UserRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final FirebaseAuth firebaseAuth;


    public UserController() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/google-services.json");
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @GetMapping
    public List<User> getUsers() throws FirebaseAuthException {
        List<User> users = new ArrayList<>();
        ListUsersPage page = firebaseAuth.listUsers(null);

        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                users.add(new User(user.getUid(), user.getEmail()));
            }
            page = page.getNextPage();
        }
        return users;
    }
}