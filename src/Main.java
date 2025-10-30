import dao.BulletinDao;
import dao.DataSourceProvider;
import service.BulletinServiceImpl;
import vue.BulletinView;

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