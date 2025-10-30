import gestion_Bulletin.dao.BulletinDao;
import gestion_Bulletin.dao.DataSourceProvider;
import gestion_Bulletin.service.BulletinServiceImpl;
import gestion_Bulletin.vue.BulletinView;

import javax.sql.DataSource;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DataSource ds = DataSourceProvider.getDataSource();

        BulletinDao bulletinDao = new BulletinDao(ds);
        BulletinServiceImpl bulletinService = new BulletinServiceImpl(ds, bulletinDao);

        Scanner scanner = new Scanner(System.in);
        BulletinView bulletinView = new BulletinView(bulletinService, scanner);
        bulletinView.menu();
    }
}