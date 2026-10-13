package controller;

import dao.FindingDAO;
import dk.bugelhartmann.UserDTO;
import dto.FindingDetailDTO;
import dto.FindingDTO;
import entities.Finding;
import entities.FindingStatus;
import exceptions.ValidationException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mapper.FindingMapper;
import security.dao.UserDAO;
import security.entities.User;
import utils.AuthUtil;

import java.time.LocalDateTime;
import java.util.List;

public class FindingController {
    private FindingMapper findingMapper;
    private FindingDAO findingDAO;
    private UserDAO userDAO;

    public FindingController(FindingMapper findingMapper, FindingDAO findingDAO, UserDAO userDAO) {
        this.findingMapper = findingMapper;
        this.findingDAO = findingDAO;
        this.userDAO = userDAO;
    }

    public void sortStatusByAscending(Context ctx){
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        List<Finding> findingList = findingDAO.sortByStatusAscending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);

    }

    public void sortStatusByDescending(Context ctx){
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        List<Finding> findingList = findingDAO.sortByStatusDescending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    public void updateStatus(Context ctx){
        //få info
        Long id = getLongId(ctx);
        Finding finding = findingDAO.read(id);

        ValidationException.validateOwnership(finding.getSoftware(), AuthUtil.getLoggedInUser(ctx, userDAO));
        FindingDTO findingDTO = ctx.bodyAsClass(FindingDTO.class);


        ValidationException.requireValue(findingDTO.status(), "status", "Finding");

        finding.setStatus(findingDTO.status());
        finding.setUpdatedAt(LocalDateTime.now());

        if (findingDTO.status() == FindingStatus.RESOLVED) {
            finding.setResolvedAt(LocalDateTime.now());
        } else {
            finding.setResolvedAt(null);
        }

        Finding updatedFinding = findingDAO.update(finding);
        FindingDTO findingDTOUpdated = findingMapper.toDTO(updatedFinding);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOUpdated);

    }

    public void sortCvssByAscending(Context ctx){
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        List<Finding> findingList = findingDAO.sortCvssAscending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    public void sortCvssByDescending(Context ctx){
        User user = AuthUtil.getLoggedInUser(ctx, userDAO);

        List<Finding> findingList = findingDAO.sortCvssDescending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }



    private Long getLongId(Context ctx) {
        return ctx.pathParamAsClass("id", Long.class).get();
    }
}