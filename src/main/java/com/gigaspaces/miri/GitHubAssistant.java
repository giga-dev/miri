package com.gigaspaces.miri;

import com.intellij.ide.util.PropertiesComponent;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GitHubAssistant {
    private static final String key = "miri.github.token";
    private static GitHubAssistant instance;
    private final GitHub gitHub;
    private final Map<String, Boolean> repos;

    private GitHubAssistant(GitHub gitHub) {
        this.gitHub = gitHub;
        this.repos = new LinkedHashMap<>(MiriUtils.getRepositories());
    }

    public static String getOAuthToken() {
        return PropertiesComponent.getInstance().getValue(key, "");
    }

    public static void setOAuthToken(String token) {
        if (token.isEmpty())
            PropertiesComponent.getInstance().unsetValue(key);
        else
            PropertiesComponent.getInstance().setValue(key, token);
    }

    public static synchronized GitHubAssistant instance() {
        if (instance == null) {
            String token = getOAuthToken();
            if (!token.isEmpty()) {
                try {
                    instance = new GitHubAssistant(GitHub.connectUsingOAuth(token));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to init github", e);
                }
            }
        }
        return instance;
    }

    public GitHub getGitHub() {
        return gitHub;
    }

    public Map<String, Boolean> getRepos() {
        return repos;
    }

    public void deleteBranch(String repository, String name) throws IOException {
        deleteBranch(gitHub.getRepository(repository), name);
    }

    public void deleteBranch(GHRepository repository, String name) throws IOException {
        repository.getRef("heads/" +name).delete();
    }

    public GHRef createBranch(String repository, String name, String baseBranch) throws IOException {
        return createBranch(gitHub.getRepository(repository), name, baseBranch);

    }

    public GHRef createBranch(GHRepository repository, String name, String baseBranch) throws IOException {
        return repository.createRef("refs/heads/" +name, repository.getBranch(baseBranch).getSHA1());
    }
}
