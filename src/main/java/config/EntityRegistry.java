package config;

import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        //syntax: configuration.addAnnotatedClass(Study.class);
        // TODO: Add more entities here...
    }
}