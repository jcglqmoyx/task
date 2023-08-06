package com.study.task.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class GitRepositoryInitialization implements CommandLineRunner {
    @Value("${git.repository}")
    private String gitRepository;

    @Override
    public void run(String... args) throws IOException {
        String userHome = System.getProperty("user.home");
        String[] pullRepositoryCommand = {"sh", "-c", "cd " + userHome + " && git clone " + gitRepository};
        Runtime.getRuntime().exec(pullRepositoryCommand);
    }
}
