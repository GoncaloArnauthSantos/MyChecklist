package pt.isel.ls;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isel.ls.Commands.*;
import pt.isel.ls.Logic.Arguments;
import pt.isel.ls.Model.CheckList;
import pt.isel.ls.Model.FullTemplate;
import pt.isel.ls.Model.Tag;
import pt.isel.ls.Model.Template;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.util.Objects;

public class CommandTests {
    private static final SQLServerDataSource src = new SQLServerDataSource();
    private static final Map<String, String> env = System.getenv();

    @BeforeClass
    public static  void initialize() throws SQLException {
        String amb = env.get("LS_DBCONN_TEST_SQLSRV");
        HashMap<String,String> map=new HashMap<>();

        String div [] = amb.split(";");

        for(String str: div){
            String nameAndValue[]= str.split("=");
            if(nameAndValue.length==2)
                map.put(nameAndValue[0],nameAndValue[1]);
        }

        src.setServerName(map.get("server"));
        src.setDatabaseName(map.get("database"));
        src.setUser(map.get("user"));
        src.setPassword(map.get("password"));

    }

    @Test
    public void test_CheckListById() throws SQLException {

        Connection  con = src.getConnection();

        GetCheckListByID teste = new GetCheckListByID();
        Arguments arg = new Arguments();

        arg.addVariableParameter("{cid}", "1");

        CheckList result = teste.execute(arg,con);
        Assert.assertTrue(result.id == 1);

        con.close();
    }

    @Test
    public void test_PostTemplate() throws SQLException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        PostTemplate teste = new PostTemplate();
        Arguments arg = new Arguments();
        arg.addArgument("name","Pedro");
        arg.addArgument("description","E o maior");

        int result = teste.execute(arg,con);
        PreparedStatement stm = con.prepareStatement("select * from Template " +
                "where tid = ?" );
        stm.setInt(1, result);
        ResultSet res = stm.executeQuery();
        res.next() ;
        Assert.assertEquals(res.getString(2), "Pedro");

        con.rollback();
        con.close();
    }

    @Test
    public void test_PostCheckLists() throws SQLException, ParseException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        PostCheckLists teste = new PostCheckLists();
        Arguments arg = new Arguments();

        arg.addArgument("name","Edu");
        arg.addArgument("description","E o quase maior");
        arg.addArgument("dueDate", null);

        int result = teste.execute(arg,con);
        PreparedStatement stm = con.prepareStatement("select * from Checklist " +
                "where cid = ?" );
        stm.setInt(1, result);
        ResultSet res = stm.executeQuery();
        res.next();
        Assert.assertEquals(res.getString(2), "Edu");

        con.rollback();
        con.close();
    }

    @Test
    public void test_GetAllCheckLists() throws SQLException {

        Connection con = src.getConnection();

        GetAllCheckLists teste = new GetAllCheckLists();
        Arguments arg = new Arguments();

        List<CheckList> result = teste.execute(arg, con);
        Assert.assertEquals(result.size(), 4);
        for (int i = 0; i < 4 ; i++)
            Assert.assertEquals(result.get(i).id, i+1);
        con.close();

    }

    @Test
    public void test_ChangeTaskIsClose() throws SQLException {

        Connection con = src.getConnection();
        con.setAutoCommit(false);

        ChangeTaskIsClose teste = new ChangeTaskIsClose();
        Arguments arg = new Arguments();

        Integer input = 1;
        arg.addVariableParameter("{cid}", input.toString());
        arg.addVariableParameter("{lid}", "2");
        arg.addArgument("isClosed", "true");
        String result = teste.execute(arg,con);

        PreparedStatement stm = con.prepareStatement("select * from Task " +
                "where lid = ?");
        stm.setInt(1, input);
        ResultSet rs = stm.executeQuery();
        rs.next();
        Assert.assertEquals(result, rs.getString(6));

        con.setAutoCommit(false);
        con.close();

    }

    @Test
    public void test_PostTemplateTask() throws SQLException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        PostTemplateTask teste = new PostTemplateTask();
        Arguments arg = new Arguments();
        arg.addArgument("name", "Benfica");
        arg.addArgument("description", "E o maior do mundo");
        arg.addVariableParameter("{tid}", "1");

        int result = teste.execute(arg,con);
        PreparedStatement stm = con.prepareStatement("select * from TemplateTask " +
                "where TempTskId = ?" );
        stm.setInt(1, result);
        ResultSet res = stm.executeQuery();
        res.next() ;
        Assert.assertEquals(res.getString(3), "Benfica");

        con.rollback();
        con.close();
    }

    @Test
    public void test_GetTemplates() throws SQLException {

        Connection con = src.getConnection();

        GetTemplates teste = new GetTemplates();
        Arguments arg = new Arguments();

        List<Template> result = teste.execute(arg, con);
        Assert.assertEquals(result.get(0).id, (Integer)1);
        Assert.assertEquals(result.get(1).id, (Integer)2);
        con.close();

    }

    @Test
    public void test_GetTemplateInfoByID() throws SQLException {

        Connection con = src.getConnection();

        GetTemplateInfoByID teste = new GetTemplateInfoByID();
        Arguments arg = new Arguments();

        arg.addVariableParameter("{tid}", "1");
        FullTemplate result = teste.execute(arg, con);

        Assert.assertEquals(result.temp.id, (Integer)1);

        con.close();

    }

    @Test
    public void test_GetCheckListsClosed() throws SQLException {

        Connection con = src.getConnection();

        GetCheckListsClosed teste = new GetCheckListsClosed();
        Arguments arg = new Arguments();

        List<CheckList> result = teste.execute(arg, con);

        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).id, 4);

        con.close();

    }

    @Test
    public void test_GetCheckListsOpenSortedByDueDate() throws SQLException {

        Connection con = src.getConnection();

        GetCheckListsOpenSortedByDueDate teste = new GetCheckListsOpenSortedByDueDate();
        Arguments arg = new Arguments();

        List<CheckList> result = teste.execute(arg, con);

        Assert.assertEquals(result.size(), 2);

        Assert.assertEquals(result.get(0).dueDate, "2010-10-16");
        Assert.assertEquals(result.get(1).dueDate, "2010-11-16");

        con.close();

    }

    @Test
    public void test_GetAllUncompletedChecklistsOrderedByOpenTasks() throws SQLException {
        Connection  con = src.getConnection();
        GetAllUncompletedChecklistsOrderedByOpenTasks teste = new GetAllUncompletedChecklistsOrderedByOpenTasks();
        Arguments arg = new Arguments();

        List<CheckList> result = teste.execute(arg,con);

        Assert.assertEquals(3, result.size());

        Assert.assertEquals(2, result.get(0).id);
        Assert.assertEquals(1, result.get(1).id);
        Assert.assertEquals(3, result.get(2).id);

        con.close();
    }

    @Test
    public void test_GetAllTags() throws SQLException,ParseException {
        Connection  con = src.getConnection();
        GetAllTags teste = new GetAllTags();
        Arguments arg = new Arguments();

        List<Tag> result = teste.execute(arg,con);

        Assert.assertEquals(result.size(), 2);

        Assert.assertEquals(result.get(0).gid, 1);
        Assert.assertEquals(result.get(1).gid, 2);

        con.close();
    }

    @Test
    public void test_PostTags() throws SQLException,ParseException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        PostTags teste = new PostTags();
        Arguments arg = new Arguments();

        arg.addArgument("name","Bebidas");
        arg.addArgument("color","Preto");

        Integer result = teste.execute(arg,con);

        GetAllTags testes = new GetAllTags();
        arg = new Arguments();

        List<Tag> results = testes.execute(arg,con);

        Assert.assertEquals(results.size(), 3);

        con.rollback();
        con.close();

    }

    @Test
         public void test_DeleteTagsByID() throws SQLException,ParseException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        DeleteTagsByID teste = new DeleteTagsByID();
        Arguments arg = new Arguments();

        arg.addVariableParameter("{gid}", "1");
        Integer result = teste.execute(arg,con);

        GetAllTags testes = new GetAllTags();
        arg = new Arguments();

        List<Tag> results = testes.execute(arg,con);

        Assert.assertEquals(results.size(), 1);
        con.rollback();
        con.close();

    }

    @Test
    public void test_DeleteCheckListWithCidBygID() throws SQLException,ParseException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        DeleteCheckListWithCidBygID teste = new DeleteCheckListWithCidBygID();
        Arguments arg = new Arguments();

        arg.addVariableParameter("{cid}", "1");
        arg.addVariableParameter("{gid}", "1");


        Integer result = teste.execute(arg,con);

        Assert.assertEquals(result.intValue(), 1);

        con.rollback();
        con.close();

    }

    @Test
    public void test_PostTagInCheckListByID() throws SQLException, ParseException {
        Connection  con = src.getConnection();
        con.setAutoCommit(false);

        PostTagInCheckListByID teste = new PostTagInCheckListByID();
        Arguments arg = new Arguments();
        arg.addArgument("gid", "2");
        arg.addVariableParameter("{cid}", "1");

        int result = teste.execute(arg,con);

        PreparedStatement stm = con.prepareStatement("select * from TagCheckList\n" +
                "where TagCheckList.cid = 1 and gid=2" );


        ResultSet res = stm.executeQuery();
        res.next();

        Assert.assertEquals(res.getInt(1) , 2 );
        Assert.assertEquals(res.getInt(2) , 1 );

        con.rollback();
        con.close();
    }
}
