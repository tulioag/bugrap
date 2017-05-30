package org.vaadin.training.bugrap;

import org.vaadin.bugrap.domain.BugrapRepository;

public class Repository {

    private static BugrapRepository repo;

    public static BugrapRepository getInstance() {
        if (repo == null)
            throw new IllegalStateException("Repository not initialized!");
        return repo;
    }

    static BugrapRepository create(String databasePath) {
        if(repo != null)
            throw new IllegalStateException("Repository already initialized!");
        repo = new BugrapRepository(databasePath);
        return repo;
    }

}
