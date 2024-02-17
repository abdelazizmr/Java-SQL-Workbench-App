import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class SQLTuto extends JFrame implements ActionListener{

    //
    private JLabel lblServer, lblDb, lblTables, lblRequest, lblResultat;
    private JTextField txtServer, txtUsername, txtPassword;
    private JButton btnTables, btnConnectServer,btnConnectDb, btnExecute;
    private JComboBox<String> cmbDB;
    private JList<String> lsTables;
    private DefaultListModel<String> dlm;
    private JTextArea txtRequest,txtResultat;
    private JScrollPane scrollRequest,scrollResult;
    private String request, result, database, table;
    ArrayList<String> allDb = new ArrayList<String>();
    private JPanel pTable,pRequest,pResult,pDB,pCentral,contentPane;

    private Connection cnx;
    private Statement st;
    private ResultSet rs;

    public SQLTuto() {
        super("SQL - Training");
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        lblServer = new JLabel("Server");
        lblDb = new JLabel("Databases");
        lblTables = new JLabel("Tables");
        lblResultat = new JLabel("Result");
        lblRequest = new JLabel("Request");

        txtServer = new JTextField("127.0.0.1");
        txtUsername = new JTextField("root");
        txtPassword = new JTextField("1234");

        btnConnectServer = new JButton("Connect");
        btnConnectDb = new JButton("Connect DB");
        btnTables = new JButton("Tables");
        btnExecute = new JButton("Execute");

        cmbDB = new JComboBox<>();
        lsTables = new JList<>();

        txtRequest = new JTextArea(5, 55);
        txtResultat = new JTextArea(10, 55);

        scrollRequest = new JScrollPane(txtRequest);
        scrollResult = new JScrollPane(txtResultat);

        pDB = new JPanel(new GridLayout(5, 2));
        pDB.add(lblServer);
        pDB.add(txtServer);
        pDB.add(txtUsername);
        pDB.add(txtPassword);
        pDB.add(btnConnectServer);
        pDB.add(new JLabel());
        pDB.add(lblDb);
        pDB.add(new JLabel());
        pDB.add(cmbDB);
        pDB.add(btnConnectDb);
        contentPane.add(pDB, BorderLayout.NORTH);

        pTable = new JPanel(new BorderLayout());
        pTable.add(lblTables, BorderLayout.NORTH);
        pTable.add(lsTables);
        contentPane.add(pTable, BorderLayout.WEST);

        pRequest = new JPanel(new BorderLayout());
        pRequest.add(lblRequest, BorderLayout.NORTH);
        pRequest.add(scrollRequest);

        pResult = new JPanel(new BorderLayout());
        pResult.add(lblResultat, BorderLayout.NORTH);
        pResult.add(scrollResult);

        pCentral = new JPanel();
        pCentral.add(pRequest);
        pCentral.add(btnExecute);
        pCentral.add(pResult);

        contentPane.add(pCentral, BorderLayout.CENTER);

        btnExecute.addActionListener( this);
        btnTables.addActionListener( this);
        btnConnectDb.addActionListener( this);
        btnConnectServer.addActionListener( this);

        cmbDB.addActionListener(this);

        cmbDB.addActionListener(e -> {});

        setContentPane(contentPane);


        lsTables.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                table = lsTables.getSelectedValue();
                if(table == null){
                    return;
                }

                String query = "SELECT * FROM `"+table+"`";
                try {
                    rs = st.executeQuery(query);
                    ResultSetMetaData rsmd =  rs.getMetaData();
                    int nbColonnes =  rsmd.getColumnCount();
                    String schema = "Table: "+table+"\n";
                    for (int i = 1; i < nbColonnes; i++) {
                        schema += "------  " + rsmd.getColumnName(i)+ "  " + rsmd.getColumnTypeName(i)+"  ("+rsmd.getPrecision (i)+")\n";
                    }
                    txtResultat.setText(schema);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

    }

    public void actionPerformed(ActionEvent e) {

        // connecting to the server and fetching all databases
        if( e.getSource() == btnConnectServer){
            this.connect();
        }

        // displaying the list of the tables in the selected database
        if( e.getSource() == btnConnectDb){
            txtResultat.setText("");
            String selected = (String) cmbDB.getSelectedItem();
            System.out.println(selected);
            try {
                st.execute("use`"+selected+"`");
                rs = st.executeQuery("show tables");
                dlm = new DefaultListModel<>();
                while(rs.next()){
                    dlm.addElement(rs.getString(1));
                }

                lsTables.setModel(dlm);

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        if( e.getSource() == btnExecute){
            String query = txtRequest.getText();
            txtResultat.setText("");
            try {
                boolean queryType = st.execute(query);
                if(queryType){
                    try {
                        rs = st.executeQuery(query);
                        ResultSetMetaData rsmd =  rs.getMetaData();
                        int nbColonnes =  rsmd.getColumnCount();
                        String schema = "";
                        for (int i = 1; i <= nbColonnes; i++) {
                            schema += rsmd.getColumnName(i) + "\t";
                        }
                        while (rs.next()){
                            schema += "\n";
                            for (int i = 1; i <= nbColonnes; i++) {
                                schema+= rs.getString(i) + "\t";
                            }
                        }
                        txtResultat.setText(schema);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }else{
                    int n = st.executeUpdate(query);
                    txtResultat.setText(n+" rows affected.");
                }
            } catch (SQLException ex) {
                txtResultat.setText("");
                txtResultat.setText(ex.getMessage());
                throw new RuntimeException(ex);
            }

        }

    }





    public void connect() {
        Connection connect = null;
        String uid = txtUsername.getText();
        String mdp = txtPassword.getText();
        String server = txtServer.getText();
        String Pilote = "com.mysql.cj.jdbc.Driver";
        String Base = "jdbc:mysql://"+server+"/";

        try {
            Class.forName(Pilote);
            cnx = DriverManager.getConnection(Base, uid, mdp);
            System.out.println("Connexion avec la base " + Base + " établie");
            st = cnx.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = st.executeQuery("SHOW DATABASES");



            while(rs.next()){
                cmbDB.addItem(rs.getString("Database"));
            }


        } catch (ClassNotFoundException e) {
            System.out.println("Erreur de Pilote : " + e);
        } catch (SQLException e) {
            System.out.println("Erreur de BD : " + e);
        }

    }

    public static void main(String[] args) {
        new SQLTuto();
    }
}
