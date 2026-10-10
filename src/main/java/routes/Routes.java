package routes;

import io.javalin.apibuilder.EndpointGroup;
import security.routes.SecurityRoutes;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private final HostRoutes hostRoutes;
    private final SoftwareRoutes softwareRoutes;
    private final FindingRoutes findingRoutes;
    private final VulnerabilityRoutes vulnerabilityRoutes;
    private final SecurityRoutes securityRoutes;

    public Routes(HostRoutes hostRoutes, SoftwareRoutes softwareRoutes, FindingRoutes findingRoutes,
                  VulnerabilityRoutes vulnerabilityRoutes,
                  SecurityRoutes securityRoutes) {
        this.hostRoutes = hostRoutes;
        this.softwareRoutes = softwareRoutes;
        this.findingRoutes = findingRoutes;
        this.vulnerabilityRoutes = vulnerabilityRoutes;
        this.securityRoutes = securityRoutes;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            //Host routes og software under en host
            path("/hosts", () -> {
                hostRoutes.getRoutes().addEndpoints();
                path("/{hostId}/software", softwareRoutes.getHostScopedRoutes());
            });

            path("/software", softwareRoutes.getRoutes());
            path("/findings", findingRoutes.getRoutes());
            path("/vulnerabilities", vulnerabilityRoutes.getRoutes());
            path("/auth", securityRoutes.getRoutes());
        };
    }
}