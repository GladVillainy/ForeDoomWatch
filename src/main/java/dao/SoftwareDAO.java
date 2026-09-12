package dao;

import entities.Software;

public class SoftwareDAO extends GenericDAO<Software, Integer>  {
    public SoftwareDAO(Class<Software> entityClass) {
        super(entityClass);
    }
}