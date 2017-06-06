package org.vaadin.training.bugrap;

import org.vaadin.bugrap.domain.BugrapRepository;

public class Test {

    public static void main(String[] args) {
        // final String db = "default";
        // final String db = "jdbc:hsqldb:file:/tmp/bugrap;create=true";
        final String db = "/tmp/bugrap";
        BugrapRepository repo = new BugrapRepository(db);
        boolean result = repo.populateWithTestData();
        System.out.println("Result: " + result);
        repo.findProjects().stream().forEach(System.out::println);
    }

}
