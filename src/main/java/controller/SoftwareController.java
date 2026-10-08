package controller;

import dao.HostDAO;
import dao.SoftwareDAO;
import dto.FindingDTO;
import dto.FindingDetailDTO;
import dto.SoftwareDTO;
import entities.Finding;
import entities.Host;
import entities.Software;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mapper.FindingMapper;
import mapper.SoftwareMapper;
import utils.Service;

import java.util.List;
import java.util.Map;

public class SoftwareController implements IController {
    private final SoftwareMapper softwareMapper;
    private final SoftwareDAO softwareDAO;
    private final HostDAO hostDAO;
    private final Service service;
    private final FindingMapper findingMapper;

    public SoftwareController(SoftwareMapper softwareMapper, SoftwareDAO softwareDAO, HostDAO hostDAO,
                              Service service, FindingMapper findingMapper) {
        this.softwareMapper = softwareMapper;
        this.softwareDAO = softwareDAO;
        this.hostDAO = hostDAO;
        this.service = service;
        this.findingMapper = findingMapper;
    }

    @Override
    public void getAll(Context ctx) {
        List<Software> softwareList = softwareDAO.readAll();
        List<SoftwareDTO> softwareDTOList = softwareMapper.toDTOList(softwareList);

        ctx.status(HttpStatus.OK);
        ctx.json(softwareDTOList);
    }

    @Override
    public void getById(Context ctx) {
        long id = getLongId(ctx);
        Software software = softwareDAO.read(id);

        SoftwareDTO softwareDTO = softwareMapper.toDTO(software);
        ctx.status(HttpStatus.OK);
        ctx.json(softwareDTO);
    }

    @Override
    public void create(Context ctx) {
        long hostId = ctx.pathParamAsClass("hostId", Long.class).get();
        Host host = hostDAO.read(hostId);

        SoftwareDTO softwareDTO = ctx.bodyAsClass(SoftwareDTO.class);
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

        SoftwareDTO softwareDTO = ctx.bodyAsClass(SoftwareDTO.class);
        Software software = softwareDAO.read(id);

        software.setSoftwareName(softwareDTO.softwareName());
        software.setVersion(softwareDTO.version());
        software.setVendor(softwareDTO.vendor());

        Software updatedSoftware = softwareDAO.update(software);
        SoftwareDTO updatedSoftwareDTO = softwareMapper.toDTO(updatedSoftware);

        ctx.status(HttpStatus.OK);
        ctx.json(updatedSoftwareDTO);
    }

    @Override
    public void delete(Context ctx) {
        long id = getLongId(ctx);

        softwareDAO.delete(id);
        ctx.status(HttpStatus.OK);
        ctx.json(Map.of("message", "Software was deleted",
                "id", id));
    }

    public void scan(Context ctx) {
        // Hent id fra url og kald find findVulnerabilities
        long id = getLongId(ctx);
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

        // Tjek ellers kaster read en 404
        hostDAO.read(hostId);

        List<Software> softwares = softwareDAO.readByHost(hostId);
        List<SoftwareDTO> softwareDTOList = softwareMapper.toDTOList(softwares);

        ctx.status(HttpStatus.OK);
        ctx.json(softwareDTOList);
    }
}
