package controller;

import dao.HostDAO;
import dao.SoftwareDAO;
import dk.bugelhartmann.UserDTO;
import dto.FindingDTO;
import dto.FindingDetailDTO;
import dto.SoftwareDTO;
import entities.Finding;
import entities.Host;
import entities.Software;
import exceptions.ValidationException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mapper.FindingMapper;
import mapper.SoftwareMapper;
import security.dao.UserDAO;
import security.entities.User;
import utils.AuthUtil;
import utils.Service;

import java.util.List;
import java.util.Map;

public class SoftwareController implements IController {
    private SoftwareMapper softwareMapper;
    private SoftwareDAO softwareDAO;
    private UserDAO userDAO;
    private HostDAO hostDAO;
    private Service service;
    private FindingMapper findingMapper;

    public SoftwareController(SoftwareMapper softwareMapper, SoftwareDAO softwareDAO, UserDAO userDAO,
                              HostDAO hostDAO, Service service, FindingMapper findingMapper) {
        this.softwareMapper = softwareMapper;
        this.softwareDAO = softwareDAO;
        this.userDAO = userDAO;
        this.hostDAO = hostDAO;
        this.service = service;
        this.findingMapper = findingMapper;
    }

    @Override
    public void getAll(Context ctx) {
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        List<Software> softwareList = softwareDAO.readByUser(user.getID());
        List<SoftwareDTO> softwareDTOList = softwareMapper.toDTOList(softwareList);

        ctx.status(HttpStatus.OK);
        ctx.json(softwareDTOList);
    }

    @Override
    public void getById(Context ctx) {
        long id = getLongId(ctx);
        Software software = softwareDAO.read(id);
        ValidationException.validateOwnership(software, AuthUtil.getLoggedInUser(ctx, userDAO));

        SoftwareDTO softwareDTO = softwareMapper.toDTO(software);
        ctx.status(HttpStatus.OK);
        ctx.json(softwareDTO);
    }

    @Override
    public void create(Context ctx) {
        //validere og hent host
        long hostId = ctx.pathParamAsClass("hostId", Long.class).get();
        Host host = hostDAO.read(hostId);
        ValidationException.validateOwnership(host, AuthUtil.getLoggedInUser(ctx, userDAO));

        //byggelse af software
        SoftwareDTO softwareDTO = ctx.bodyAsClass(SoftwareDTO.class);
        ValidationException.requireValue(softwareDTO.softwareName(), "name", "software");
        ValidationException.requireValue(softwareDTO.vendor(), "vendor", "software");
        ValidationException.requireValue(softwareDTO.version(), "version", "software");

        Software software = softwareMapper.toEntity(softwareDTO);
        software.setHost(host);

        Software createdSoftware = softwareDAO.create(software);
        SoftwareDTO createdSoftwareDTO = softwareMapper.toDTO(createdSoftware);

        ctx.status(HttpStatus.CREATED);
        ctx.json(createdSoftwareDTO);
    }

    @Override
    public void update(Context ctx) {
        long id = getLongId(ctx);

        Software software = softwareDAO.read(id);
        ValidationException.validateOwnership(software, AuthUtil.getLoggedInUser(ctx, userDAO));

        SoftwareDTO softwareDTO = ctx.bodyAsClass(SoftwareDTO.class);

        if (softwareDTO.softwareName() != null) {
            software.setSoftwareName(softwareDTO.softwareName());
        }
        if (softwareDTO.version() != null) {
            software.setVersion(softwareDTO.version());
        }
        if (softwareDTO.vendor() != null) {
            software.setVendor(softwareDTO.vendor());
        }

        Software updatedSoftware = softwareDAO.update(software);
        SoftwareDTO updatedSoftwareDTO = softwareMapper.toDTO(updatedSoftware);

        ctx.status(HttpStatus.OK);
        ctx.json(updatedSoftwareDTO);
    }

    @Override
    public void delete(Context ctx) {
        long id = getLongId(ctx);
        Software software = softwareDAO.read(id);
        ValidationException.validateOwnership(software, AuthUtil.getLoggedInUser(ctx, userDAO));

        softwareDAO.delete(id);
        ctx.status(HttpStatus.OK);
        ctx.json(Map.of("message", "Software was deleted",
                "id", id));
    }

    public void scan(Context ctx) {
        // Hent id fra url og kald find findVulnerabilities
        long id = getLongId(ctx);
        Software software = softwareDAO.read(id);
        ValidationException.validateOwnership(software, AuthUtil.getLoggedInUser(ctx, userDAO));

        List<Finding> findings = service.findVulnerabilities(id);

        // Lav listen af Finding om til en liste af FindingDetailDTO
        List<FindingDetailDTO> findingDetailDTOList = findingMapper.toDetailDTOList(findings);

        // return json
        ctx.status(HttpStatus.OK);
        ctx.json(findingDetailDTOList);
    }

    public void getByHost(Context ctx) {
        // Hent hostId fra url
        long hostId = ctx.pathParamAsClass("hostId", Long.class).get();
        Host host = hostDAO.read(hostId);
        ValidationException.validateOwnership(host, AuthUtil.getLoggedInUser(ctx, userDAO));

        List<Software> softwares = softwareDAO.readByHost(hostId);
        List<SoftwareDTO> softwareDTOList = softwareMapper.toDTOList(softwares);

        ctx.status(HttpStatus.OK);
        ctx.json(softwareDTOList);
    }
}
