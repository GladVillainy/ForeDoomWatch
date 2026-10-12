package controller;

import dao.FindingDAO;
import dk.bugelhartmann.UserDTO;
import dto.FindingDetailDTO;
import dto.FindingDTO;
import entities.Finding;
import exceptions.ValidationException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mapper.FindingMapper;
import security.dao.UserDAO;
import security.entities.User;

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
        User user = getLoggedInUser(ctx);

        List<Finding> findingList = findingDAO.sortByStatusAscending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);

    }

    public void sortStatusByDescending(Context ctx){
        User user = getLoggedInUser(ctx);

        List<Finding> findingList = findingDAO.sortByStatusDescending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    public void updateStatus(Context ctx){
        //få info
        User user = getLoggedInUser(ctx);
        FindingDTO findingDTO = ctx.bodyAsClass(FindingDTO.class);
        Finding finding = findingDAO.read(user.getID());

        ValidationException.validateOwnership(finding.getSoftware().getHost(), getLoggedInUser(ctx));

        finding.setStatus(findingDTO.status());

        Finding updatedFinding = findingDAO.update(finding);

        FindingDTO findingDTOUpdated = findingMapper.toDTO(updatedFinding);
        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOUpdated);

    }

    public void sortCvssByAscending(Context ctx){
        User user = getLoggedInUser(ctx);

        List<Finding> findingList = findingDAO.sortCvssAscending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    public void sortCvssByDescending(Context ctx){
        User user = getLoggedInUser(ctx);

        List<Finding> findingList = findingDAO.sortCvssDescending(user.getID());
        List<FindingDetailDTO> findingDTOS = findingMapper.toDetailDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    private User getLoggedInUser(Context ctx) {
        //I tokenet ligger emailen i username-feltet
        //Grundet at det ikke er min kode, og fra kursusmateriale tør jeg ikke at røre for meget ved den
        //Så længe den virker :)
        UserDTO loggedUser = ctx.attribute("user");
        User user = userDAO.findByEmail(loggedUser.getUsername());
        return user;
    }
}