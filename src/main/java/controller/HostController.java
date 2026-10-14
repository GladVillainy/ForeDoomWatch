package controller;

import dao.HostDAO;
import dk.bugelhartmann.UserDTO;
import dto.HostDTO;
import entities.Host;
import exceptions.ValidationException;
import io.javalin.http.HttpStatus;
import mapper.HostMapper;
import io.javalin.http.Context;
import security.dao.UserDAO;
import security.entities.User;
import utils.AuthUtil;

import java.util.List;
import java.util.Map;

public class HostController implements IController {

    private HostDAO hostDAO;
    private HostMapper hostMapper;
    private UserDAO userDAO;

    public HostController(HostDAO hostDAO, HostMapper hostMapper, UserDAO userDAO) {
        this.hostDAO = hostDAO;
        this.hostMapper = hostMapper;
        this.userDAO = userDAO;
    }

    @Override
    public void getAll(Context ctx) {
        //hent user
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        List<Host> hostList = hostDAO.findByUserId(user.getID());

        List<HostDTO> hostDTOList = hostMapper.toDTOList(hostList);

        ctx.status(HttpStatus.OK);
        ctx.json(hostDTOList);
    }

    @Override
    public void getById(Context ctx) {
        //getid og find host
        long id = getLongId(ctx);
        Host host = hostDAO.read(id);

        ValidationException.validateOwnership(host, AuthUtil.getLoggedInUser(ctx, userDAO));
        //Lav om til dto
        HostDTO hostDTO = hostMapper.toDTO(host);
        ctx.status(HttpStatus.OK);
        ctx.json(hostDTO);
    }

    public void create(Context ctx) {
        //hent user
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        //Få host
        HostDTO hostDTO = ctx.bodyAsClass(HostDTO.class);
        ValidationException.requireValue(hostDTO.hostname(), "name", "Host");
        //Lav til entity
        Host host = hostMapper.toEntity(hostDTO);
        //Sæt user
        host.setUser(user);
        //Gem i database
        Host createdHost = hostDAO.create(host);
        //Return dto
        HostDTO createdHostDTO = hostMapper.toDTO(createdHost);
        ctx.status(HttpStatus.CREATED);
        ctx.json(createdHostDTO);
    }

    @Override
    public void update(Context ctx) {
        //get id
        long id = getLongId(ctx);
        //find hosten og lav til entity
        HostDTO hostDTO = ctx.bodyAsClass(HostDTO.class);

        Host host = hostDAO.read(id);

        //validate ownership
        ValidationException.validateOwnership(host,AuthUtil.getLoggedInUser(ctx, userDAO));

        if(hostDTO.hostDescription() != null){
            host.setHostDescription(hostDTO.hostDescription());
        }
        if(hostDTO.hostname() != null){
            host.setHostName(hostDTO.hostname());
        }


        //Update hosten og return it
        Host updatedHost = hostDAO.update(host);
        HostDTO updatedHostDTO = hostMapper.toDTO(updatedHost);

        ctx.status(HttpStatus.OK);
        ctx.json(updatedHostDTO);
    }

    @Override
    public void delete(Context ctx) {
        long id = getLongId(ctx);
        Host host = hostDAO.read(id);
        ValidationException.validateOwnership(host, AuthUtil.getLoggedInUser(ctx, userDAO));
        hostDAO.delete(id);

        ctx.status(HttpStatus.OK);
        ctx.json(Map.of("message", "Host deleted",
                "id", id));
    }


}