package gestion_Bulletin.test;

import gestion_Bulletin.dao.BulletinDao;
import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.service.BulletinServiceImpl;
import gestion_Bulletin.service.BulletinService;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BulletinServiceTest {
    private DataSource dataSource;
    private BulletinService bulletinService;

    public void setup() throws Exception{
        System.out.println("Initialisation de BulletinServiceTest...");

    }
}
