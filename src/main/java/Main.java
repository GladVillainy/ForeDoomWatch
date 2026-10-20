import config.ApplicationConfig;
import config.HibernateConfig;
import controller.FindingController;
import controller.HostController;
import controller.SoftwareController;
import controller.VulnerabilityController;
import dao.FindingDAO;
import dao.HostDAO;
import dao.SoftwareDAO;
import dao.VulnerabilityDAO;
import entities.Software;
import jakarta.persistence.EntityManagerFactory;
import mapper.FindingMapper;
import mapper.HostMapper;
import mapper.NVDMapper;
import mapper.ReferenceMapper;
import mapper.SoftwareMapper;
import mapper.VulnerabilityMapper;
import routes.FindingRoutes;
import routes.HostRoutes;
import routes.Routes;
import routes.SoftwareRoutes;
import routes.VulnerabilityRoutes;
import security.dao.UserDAO;
import security.routes.SecurityRoutes;
import utils.APIUtils;
import utils.ScanScheduler;
import utils.Service;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        //DAO
        HostDAO hostDAO = new HostDAO(emf);
        SoftwareDAO softwareDAO = new SoftwareDAO(emf, Software.class);
        VulnerabilityDAO vulnerabilityDAO = new VulnerabilityDAO(emf);
        FindingDAO findingDAO = new FindingDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        //Mappers
        HostMapper hostMapper = new HostMapper();
        SoftwareMapper softwareMapper = new SoftwareMapper();
        FindingMapper findingMapper = new FindingMapper();
        ReferenceMapper referenceMapper = new ReferenceMapper();
        VulnerabilityMapper vulnerabilityMapper = new VulnerabilityMapper(referenceMapper);
        NVDMapper nvdMapper = new NVDMapper();

        //NVD api
        String apiKey = System.getenv("apiKey");

        //Service
        APIUtils apiUtils = new APIUtils();
        Service service = new Service(apiUtils, nvdMapper, softwareDAO, vulnerabilityDAO, findingDAO, apiKey);
        ScanScheduler scanScheduler = new ScanScheduler(softwareDAO, service);
        scanScheduler.start();
        Runtime.getRuntime().addShutdownHook(new Thread(scanScheduler::stop));

        //Controllere
        HostController hostController = new HostController(hostDAO, hostMapper,userDAO);
        SoftwareController softwareController =
                new SoftwareController(softwareMapper,softwareDAO, userDAO, hostDAO, service, findingMapper);
        FindingController findingController = new FindingController(findingMapper, findingDAO, userDAO);
        VulnerabilityController vulnerabilityController =
                new VulnerabilityController(vulnerabilityMapper, vulnerabilityDAO);

        //Routes
        SecurityRoutes securityRoutes = new SecurityRoutes();
        HostRoutes hostRoutes = new HostRoutes(hostController);
        SoftwareRoutes softwareRoutes = new SoftwareRoutes(softwareController);
        FindingRoutes findingRoutes = new FindingRoutes(findingController);
        VulnerabilityRoutes vulnerabilityRoutes = new VulnerabilityRoutes(vulnerabilityController);
        Routes routes = new Routes(hostRoutes, softwareRoutes, findingRoutes, vulnerabilityRoutes, securityRoutes);

        //Start serveren
        new ApplicationConfig(routes).startServer(7070);


    }
}