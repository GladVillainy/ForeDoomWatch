package config;

import entities.*;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        //syntax: configuration.addAnnotatedClass(Study.class);
        // Add more entities here...
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Finding.class);
        configuration.addAnnotatedClass(Host.class);
        configuration.addAnnotatedClass(Software.class);
        configuration.addAnnotatedClass(Vulnerability.class);
        configuration.addAnnotatedClass(Finding.class);
        configuration.addAnnotatedClass(Metrics.class);
        configuration.addAnnotatedClass(Reference.class);
    }
}