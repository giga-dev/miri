package com.gigaspaces.miri;

import com.intellij.ide.util.PropertiesComponent;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

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

    private void deleteBranch(GHRepository repository, String name) throws IOException {
        repository.getRef("heads/" +name).delete();
    }

    public GHBranch getBranchIfExists(GHRepository repository, String name) throws IOException {
        try {
            return repository.getBranch(name);
        } catch (GHFileNotFoundException e) {
            return null;
        }
    }

    public Collection<String> listBranches(String repository) throws IOException {
        return listBranches(gitHub.getRepository(repository));
    }

    public Collection<String> listBranches(GHRepository repository) throws IOException {
        return repository.getBranches().keySet();
    }


    public GHTagObject getTagIfExists(GHRepository repository, String name) throws IOException {
        try {
            GHRef ref = repository.getRef("tags/" + name);
            return repository.getTagObject(ref.getObject().getSha());
        } catch (GHFileNotFoundException e) {
            return null;
        }
    }

    public GHCommit getCommitIfExists(GHRepository repository, String sha) throws IOException {
        try {
            return repository.getCommit(sha);
        } catch (GHFileNotFoundException e) {
            return null;
        }
    }

    public GHRef createBranchFromSha(GHRepository repository, String name, String sha) throws IOException {
        return repository.createRef("refs/heads/" + name, sha);
    }
}
