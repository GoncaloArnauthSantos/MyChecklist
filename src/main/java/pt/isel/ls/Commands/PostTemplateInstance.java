package pt.isel.ls.Commands;

import pt.isel.ls.Logic.Arguments;

import java.sql.*;

public class PostTemplateInstance implements Command {

    private static final int Tid = 1;
    private static final int TempName = 2;
    private static final int TempDescript = 3;
    private static final int TempTaskName = 6;
    private static final int TempTaskDescript = 7;

    public PostTemplateInstance() {
    }

    @Override
    public Integer execute(Arguments args, Connection con) throws SQLException {
        try {
            boolean hasTasks=true;

            con.setAutoCommit(false);

            PreparedStatement selectStm = con.prepareStatement("select * from Template inner join TemplateTask\n" +
                    "on(Template.tid=TemplateTask.tid)\n" +
                    "where Template.tid=?\n" +
                    "\n",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            Integer tid = Integer.parseInt(args.variableParameters.get("{tid}"));
            selectStm.setInt(1, tid);
            ResultSet selectRs = selectStm.executeQuery();
            String name = args.arguments.get("name");
            String desc = args.arguments.get("description");
            String dueDate = args.arguments.get("dueDate");

           if( !selectRs.next()) {
               PreparedStatement selectTemplate= con.prepareStatement("select * from Template \n" +
                       "where tid = ?",
                       ResultSet.TYPE_SCROLL_INSENSITIVE,
                       ResultSet.CONCUR_READ_ONLY);
               selectTemplate.setInt(1, tid);
               selectRs = selectTemplate.executeQuery();
               hasTasks=false;
           }

            if(selectRs.next()) {
                if (name == null || name == "")
                    name = selectRs.getString(TempName);
                if (desc == null || desc == "")
                    desc = selectRs.getString(TempDescript);
            }
            selectRs.beforeFirst();

            PreparedStatement stm2 = con.prepareStatement("insert into Checklist (Name, Descrip, DueDate, tid)" +
                            " values (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stm2.setString(1, name);
            stm2.setString(2, desc);
            if(dueDate!=null && dueDate!="")
                stm2.setString(3, dueDate);
            else
                stm2.setString(3, null);
            stm2.setInt(4, tid);

            stm2.executeUpdate();
            ResultSet rs1= stm2.getGeneratedKeys();
            rs1.next();

            if(!hasTasks) {
                con.commit();
                return rs1.getInt(1);
            }

            PreparedStatement stm3 = con.prepareStatement("insert into Task (Name, Descrip, cid)" +
                    " values (?, ?, ?)");

            while (selectRs.next()) {
                stm3.setString(1,selectRs.getString(TempTaskName));
                stm3.setString(2,selectRs.getString(TempTaskDescript));
                stm3.setInt(3, rs1.getInt(Tid));
                stm3.executeUpdate();
            }
            con.commit();
            return rs1.getInt(1);
        }catch (SQLException e){
            con.rollback();
            throw e;
        }
    }

    @Override
    public String toString() {
        return "POST /templates/{tid}/create - creates a new checklist with the tasks of the template tid, given the following parameters\n" +
                "name - an optional checklist name. If absent, the checklist will have the template's name.\n" +
                "description - an optional checklist description. If absent, the checklist will have the template's description.\n" +
                "dueDate - an optional due date for the checklist completion.\n";
    }
}
