package github.cweijan.ultimate.test.base;

import github.cweijan.ultimate.core.DbUltimate;
import github.cweijan.ultimate.db.config.DbConfig;
import github.cweijan.ultimate.db.init.DBInitialer;
import github.cweijan.ultimate.test.code.AdminTypeEnum;
import github.cweijan.ultimate.util.Log;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseTest {

    protected static DbConfig dbConfig;
    protected static DbUltimate dbUltimate;
    protected static DBInitialer dbInitialer;

    @BeforeClass
    public static void initConfig() {

        dbConfig = new DbConfig();
        dbConfig.setUrl("jdbc:mysql://localhost:3306/ultimate");
        dbConfig.setUsername("root");
        dbConfig.setPassword("123456");
        dbConfig.setDriver("com.mysql.jdbc.Driver");
        dbConfig.setCreateNonexistsTable(true);
        dbConfig.setScanPackage("github.cweijan.ultimate");
        dbUltimate = new DbUltimate(dbConfig);
        dbInitialer = new DBInitialer(dbConfig);
    }

    @Test
    public void testInit(){
        Log.info("init successful");
    }

}
