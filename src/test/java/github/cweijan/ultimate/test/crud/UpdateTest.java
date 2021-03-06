package github.cweijan.ultimate.test.crud;

import github.cweijan.ultimate.core.Query;
import github.cweijan.ultimate.core.result.ResultInfo;
import github.cweijan.ultimate.test.base.BaseTest;
import github.cweijan.ultimate.test.bean.Admin;
import github.cweijan.ultimate.test.bean.Lib;
import org.junit.Test;

import java.time.LocalDateTime;

public class UpdateTest extends BaseTest{

    @Test
    public void testUpdateBySql(){
        ResultInfo resultInfo = Query.db.executeSql("update lib set msd='test' where id =1");
        System.out.println(resultInfo);
    }

    @Test
    public void testUpdateByOperation(){

        Query<Admin> query = Query.of(Admin.class);
        query.update("test", "test2").executeUpdate();
        Query.db.update(query);
    }

    @Test
    public void testUpdateByQuery(){

        Query.of(Lib.class).update("test", "lib").eq("id", 1).executeUpdate();

    }

    @Test
    public void testUpdate(){

        Admin admin = new Admin();
        admin.setId(1);
        admin.setDelete(false);
        admin.setDate(LocalDateTime.now());
//        admin.setMsd("cweijain");
        Query.db.update(admin);

    }

}
