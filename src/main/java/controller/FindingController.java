package controller;

import dao.FindingDAO;
import dto.FindingDTO;
import entities.Finding;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mapper.FindingMapper;

import java.util.List;

public class FindingController {
    private final FindingMapper findingMapper;
    private final FindingDAO findingDAO;

    public FindingController(FindingMapper findingMapper, FindingDAO findingDAO) {
        this.findingMapper = findingMapper;
        this.findingDAO = findingDAO;
    }

    public void sortStatusByAscending(Context ctx){
        long id = getLongId(ctx);

        List<Finding> findingList = findingDAO.sortByStatusAscending(id);
        List<FindingDTO> findingDTOS = findingMapper.toDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);

    }

    public void sortStatusByDescending(Context ctx){
        long id = getLongId(ctx);

        List<Finding> findingList = findingDAO.sortByStatusDescending(id);
        List<FindingDTO> findingDTOS = findingMapper.toDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    public void updateStatus(Context ctx){
        //få info
        long id = getLongId(ctx);
        FindingDTO findingDTO = ctx.bodyAsClass(FindingDTO.class);
        Finding finding = findingDAO.read(id);

        finding.setStatus(findingDTO.status());

        Finding updatedFinding = findingDAO.update(finding);

        FindingDTO findingDTOUpdated = findingMapper.toDTO(updatedFinding);
        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOUpdated);

    }

    public void sortCvssByAscending(Context ctx){
        long id = getLongId(ctx);

        List<Finding> findingList = findingDAO.sortCvssAscending(id);
        List<FindingDTO> findingDTOS = findingMapper.toDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }

    public void sortCvssByDescending(Context ctx){
        long id = getLongId(ctx);

        List<Finding> findingList = findingDAO.sortCvssDescending(id);
        List<FindingDTO> findingDTOS = findingMapper.toDTOList(findingList);

        ctx.status(HttpStatus.OK);
        ctx.json(findingDTOS);
    }


    private long getLongId(Context ctx) {
        return ctx.pathParamAsClass("id", Long.class).get();
    }
}