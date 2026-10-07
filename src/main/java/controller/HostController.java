package controller;

import dao.HostDAO;
import dto.HostDTO;
import entities.Host;
import io.javalin.http.HttpStatus;
import mapper.HostMapper;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class HostController implements IController {

    private HostDAO hostDAO;
    private HostMapper hostMapper;

    public HostController(HostDAO hostDAO, HostMapper hostMapper) {
        this.hostDAO = hostDAO;
        this.hostMapper = hostMapper;
    }


    @Override
    public void getAll(Context ctx) {
        List<Host> host = hostDAO.readAll();
        List<HostDTO> hostDTOList = hostMapper.toDTOList(host);

        ctx.status(HttpStatus.OK);
        ctx.json(hostDTOList);
    }

    @Override
    public void getById(Context ctx) {
        //get id og find host
        long id = getLongId(ctx);
        Host host = hostDAO.read(id);

        //Lav om til dto
        HostDTO hostDTO = hostMapper.toDTO(host);
        ctx.status(HttpStatus.OK);
        ctx.json(hostDTO);
    }

    @Override
    public void create(Context ctx) {
        //Få host
        HostDTO hostDTO = ctx.bodyAsClass(HostDTO.class);
        //Lav til entity
        Host host = hostMapper.toEntity(hostDTO);
        //Gem i database
        Host createdHost = hostDAO.create(host);
        //Return dto
        HostDTO createdHostDTO = hostMapper.toDTO(createdHost);
        ctx.status(HttpStatus.CREATED);
        ctx.json(createdHostDTO);
    }

    @Override
    public void update(Context ctx) {
        //hent id
        long id = getLongId(ctx);

        //Lav om til en entity
        HostDTO hostDTO = ctx.bodyAsClass(HostDTO.class);
        Host host = hostDAO.read(id);

        host.setHostDescription(hostDTO.hostDescription());
        host.setHostName(hostDTO.hostname());

        //Update hosten og return it
        Host updatedHost = hostDAO.update(host);
        HostDTO updatedHostDTO = hostMapper.toDTO(updatedHost);

        ctx.status(HttpStatus.OK);
        ctx.json(updatedHostDTO);
        }

    @Override
    public void delete(Context ctx) {
        long id = getLongId(ctx);

        hostDAO.delete(id);
        ctx.status(HttpStatus.OK);
        ctx.json(Map.of( "message", "Host deleted",
                "id", id));
    }


}