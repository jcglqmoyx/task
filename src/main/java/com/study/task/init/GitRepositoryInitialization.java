package com.study.task.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GitRepositoryInitialization implements CommandLineRunner {
    @Value("${git.repository}")
    private String gitRepository;

    @Override
    public void run(String... args) throws IOException {
        String userHome = System.getProperty("user.home");
        String[] pullRepositoryCommand = { "sh", "-c", "cd " + userHome + " && git clone " + gitRepository };
        Runtime.getRuntime().exec(pullRepositoryCommand);
    }
}
