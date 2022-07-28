package com.kuromaru.progs;

//Importy pre debug hook pre chrome...
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

//Importy pre manipulaciu zo subormi
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//Gui importy
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Exception importy
import java.security.GeneralSecurityException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

//Class sluziaci len ako ulozisko (objekt)
class Web
{
    //String site;
    String username;
    String password;
}

public class PasswordManger //extends javax.swing.JFrame
{
    public static RSA rsa;
    public static String MasterPass;
    public static String aad;
    public static String fileContent = "";
    public static boolean passCorrect = false;

    private static File databaseFile;
    private static javax.swing.JButton okBtn;
    private static javax.swing.JButton cancelBtn;
    private static javax.swing.JLabel loginPageText;
    private static javax.swing.JTextField passField;

    private static JFrame loginPageWindow;
    private static JFrame passConfirm;
    private static JFrame loginConfirmationWindow;
    private static JFrame exitWrongPasswordWindow;
    private static JFrame editorWindow;
    private static JTable dataTable = new JTable();

    private static Map<String, Web> webFields = new HashMap<String, Web>();
    private static javax.swing.JComboBox<String> comboSites;
    private static javax.swing.JButton addSiteButton;
    private static javax.swing.JButton removeSiteButton;

    private static javax.swing.JLabel siteLabel;
    private static javax.swing.JLabel loginLabel;
    private static javax.swing.JLabel passwordLabel;

    private static javax.swing.JTextField loginBox;
    private static javax.swing.JTextField passwordBox;

    private static HashMap db = new HashMap<String, Web>();

    public static FileEncryptor fileencryptor = new FileEncryptor();

    //Tu startuje program(hlavna metoda alebo funkcia...)
    public static void main(String[] args) throws InterruptedException, GeneralSecurityException, IOException, ClassNotFoundException
    {
        //Vytvori subor databazi na disku ak neexistuje ak existuje tak vypise do konzoly
        createFile();

        createLoginPageFrame();
    }

    static void createFile()
    {
        Path path = Paths.get("db.dat");

        if (Files.exists(path))
        {
            System.out.println("File already exists.");
        }

        if (Files.notExists(path))
        {
            try
            {
                databaseFile = new File("db.dat");
                if (databaseFile.createNewFile())
                {
                    System.out.println("File created: " + databaseFile.getName());
                }
                else
                {
                    System.out.println("File already exists... reading db");
                }
            }
            catch (IOException e)
            {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    static void writeDataToFile(String data) throws IOException
    {
        String fileContent = "";
        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get("db.dat"));
            fileContent = new String(bytes);
        }
        catch (IOException e)
        {
            System.out.println("Chyba pri zapisovani do db.dat...");
            e.printStackTrace();
        }

        //Zapise novy riadok do db.dat data ziska z parametru funkccie data...
        FileWriter myWriter = new FileWriter("db.dat");
        myWriter.write(fileContent + data + '\n');
        myWriter.close();
    }
    static void confirmPassFrame() throws Exception
    {
        loginConfirmationWindow = new JFrame();
        JLabel InfoText = new JLabel();

        String valid = "Login succesfull...";
        String invalid = "INVALID PASSWORD...";

        loginConfirmationWindow.add(InfoText);
        InfoText.setText(" ");
        InfoText.setFont(new java.awt.Font("Segoe UI", 0, 36)); //

        GroupLayout layout = new GroupLayout(loginConfirmationWindow.getContentPane());
        loginConfirmationWindow.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(22, 22, 22).addComponent(InfoText).addContainerGap(65, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(105, 105, 105).addComponent(InfoText).addContainerGap(121, Short.MAX_VALUE)));

        if(passCorrect)
        {
            InfoText.setText(valid);
            InfoText.setForeground(new java.awt.Color(0, 255, 0));
            readDB();
            createEditor();
            loginPageWindow.dispose();
            /*loginPageWindow.setVisible(false);/*/
        }
        else
        {
            InfoText.setText(invalid);
            InfoText.setForeground(new java.awt.Color(255, 0, 0));
        }
        //vsetko stlacit dokopy a nastavit poziciu na stred obrazovky(setLocationRelativeTo(null))....
        loginConfirmationWindow.pack();
        loginConfirmationWindow.setVisible(true);
        loginConfirmationWindow.setLocationRelativeTo(null);
    }

    static void createLoginPageFrame()
    {
        loginPageWindow = new JFrame("Login page");

        okBtn = new JButton();
        cancelBtn = new JButton();
        loginPageText = new JLabel();
        passField = new JTextField();


        loginPageText.setFont(new java.awt.Font("Segoe UI", 0, 36)); //
        loginPageText.setText("PASS WALLET LOGIN TABLE");
        okBtn.setText("Ok");
        cancelBtn.setText("Cancel");

        loginPageWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        okBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                MasterPass = passField.getText();
                passCorrect = true;
                try
                {
                    BufferedReader br = new BufferedReader(new FileReader("db.dat"));
                    if (br.readLine() != null)
                    {
                        fileencryptor.decryptFile("Keys.dat",MasterPass);
                        confirmPassFrame();
                    }
                }
                catch(IllegalBlockSizeException e)
                {
                    loginPageWindow.dispose();
                    try
                    {
                        Thread.sleep(1500);
                        exitBadPassword();
                    }
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                catch(BadPaddingException  e)
                {
                    loginPageWindow.dispose();
                    try
                    {
                        exitBadPassword();
                        Thread.sleep(1500);
                    }
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        GroupLayout layout = new GroupLayout(loginPageWindow.getContentPane());
        loginPageWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPageWindow.setLayout(layout);

        //Horizontalne a vertikalne nastavenie co ma byt kde (komponenty okna) //Nastavenie layoutu(rozlozenia okna)
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(26, Short.MAX_VALUE).addComponent(loginPageText).addGap(24, 24, 24)).addGroup(layout.createSequentialGroup().addGap(42, 42, 42).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(layout.createSequentialGroup().addComponent(okBtn).addGap(266, 266, 266).addComponent(cancelBtn)).addComponent(passField, GroupLayout.PREFERRED_SIZE, 410, GroupLayout.PREFERRED_SIZE)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(loginPageText).addGap(43, 43, 43).addComponent(passField, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(okBtn).addComponent(cancelBtn)).addContainerGap(88, Short.MAX_VALUE)));

        //vsetko stlacit dokopy a nastavit poziciu na stred obrazovky(setLocationRelativeTo(null))....
        loginPageWindow.pack();
        loginPageWindow.setVisible(true);
        loginPageWindow.setLocationRelativeTo(null);
    }
    public static void exitBadPassword()
    {
        exitWrongPasswordWindow = new JFrame("");
        JLabel badPassText = new javax.swing.JLabel();

        exitWrongPasswordWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        badPassText.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        badPassText.setText("Bad password.... exiting");

        //Nastavenie layoutu(rozlozenia okna)
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(exitWrongPasswordWindow.getContentPane());
        exitWrongPasswordWindow.getContentPane().setLayout(layout);layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(94, Short.MAX_VALUE).addComponent(badPassText).addGap(87, 87, 87)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(109, 109, 109).addComponent(badPassText).addContainerGap(145, Short.MAX_VALUE)));

        //vsetko stlacit dokopy a nastavit poziciu na stred obrazovky(setLocationRelativeTo(null))....
        exitWrongPasswordWindow.pack();
        exitWrongPasswordWindow.setLocationRelativeTo(null);
        exitWrongPasswordWindow.setVisible(true);
    }

    static void readDB() throws Exception
    {
        rsa = new RSA();
        rsa.readKeypair();

        byte[] bytes = Files.readAllBytes(Paths.get("db.dat"));
        fileContent = new String(bytes);

        long lines = Files.lines(Paths.get("db.dat")).count();

        //debug line...
        System.out.println(fileContent);

        for (int i = 0; i < lines; i++)
        {
            String site;
            String login;
            String pass;

            //////Precitanie db riadok po riadku plus decryptovanie pomocou RSA klucoveho suboru...////////////////
            //fileContent = new String(fileContent.substring(fileContent.indexOf("site:"),fileContent.indexOf(" ")));
            //site = new String(fileContent.substring(fileContent.indexOf("site:")+5, fileContent.indexOf((" login:"))));
            site = new String(fileContent.substring(fileContent.indexOf("site:")+5, fileContent.indexOf(" ####login:")));
            System.out.println("site:" + site);
            //login = new String(fileContent.substring(fileContent.indexOf(" login:")+7, fileContent.indexOf(" password")));
            login = new String(fileContent.substring(fileContent.indexOf(" ####login:")+11, fileContent.indexOf(" $$$$password:")));
            System.out.println("login:" + login);
            System.out.println("decrypted login: " +rsa.decrypt(login));
            login = rsa.decrypt(login);
            //pass = new String(fileContent.substring(fileContent.indexOf(" password:")+10, fileContent.indexOf('\n')));
            pass = new String(fileContent.substring(fileContent.indexOf(" $$$$password:")+14, fileContent.indexOf(" *****")));
            System.out.println("password:" + pass);
            System.out.println("decypted password: " + rsa.decrypt(pass));
            pass = rsa.decrypt(pass);
            fileContent = new String(fileContent.substring(fileContent.indexOf(" *****") + 1, fileContent.length()));

            Web web = new Web();
            web.username = login;
            web.password = pass;

            db.put(site, web);

            DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
            model.addRow(new Object[]{ site, login, pass});
        }
    }

    public static void createEditor() throws IOException, GeneralSecurityException
    {
        editorWindow = new JFrame("Editor");
        siteLabel = new javax.swing.JLabel();
        loginLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();

        comboSites = new javax.swing.JComboBox<>();
        loginBox = new javax.swing.JTextField();
        passwordBox = new javax.swing.JTextField();

        addSiteButton = new javax.swing.JButton();
        removeSiteButton = new javax.swing.JButton();

        JScrollPane dataPane = new javax.swing.JScrollPane();
        dataTable = new JTable();

        JPanel oknoEditorInside = new JPanel();
        oknoEditorInside = new javax.swing.JPanel();
        oknoEditorInside.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JButton startButton = new javax.swing.JButton();


        siteLabel.setText("Site");
        loginLabel.setText("Login");
        passwordLabel.setText("Password");
        addSiteButton.setText("Add");
        startButton.setText("START");
        removeSiteButton.setText("Remove selected line");


        comboSites.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Instagram", "Twitter", "Facebook", "Google", "Twitch", "Centrum","Svetelektro","Azet", "Mojevideo" }));
        dataTable.setModel(new javax.swing.table.DefaultTableModel(new Object [][] {}, new String [] {"Website", "Login", "Password"})
        {
           /* boolean[] canEdit = new boolean []
            {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
            */
        });

        //Listenery okna
        startButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try
                {
                    editorWindow.dispose();
                    fillData();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (GeneralSecurityException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        editorWindow.addWindowListener(new WindowAdapter()
        {
            //Zakryptovanie a ulozenie RSA klucov v subore Keys.dat po zatvoreni editora(Hlavne okno)....
            //Poznamka: neskor prerobit tak aby sa RSA kluce rozbalovali po zadani prvotneho heslaa iba do pamete a nie na disk. Vid riadok 248...
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Closed");
                try
                {
                    fileencryptor.encryptFile("Keys.dat",MasterPass);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                catch (GeneralSecurityException ex)
                {
                    ex.printStackTrace();
                }
                //zatvorit okno po zavreti(???)
                //musi to tam byt...
                e.getWindow().dispose();
            }
        });

        addSiteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
                model.addRow(new Object[]{comboSites.getSelectedItem().toString(), loginBox.getText(), passwordBox.getText()});
                try
                {
                    writeDataToFile(new String("site:" + comboSites.getSelectedItem().toString()+ " ####login:"+ rsa.encrypt(loginBox.getText()) + " $$$$password:" + rsa.encrypt(passwordBox.getText()))+" *****");
                }
                catch (Exception e)
                {
                    System.out.println("chyba zapisovnia do db.dat...");
                    e.printStackTrace();
                }
            }
        });

        //Zatial nie plne funkcne... dorobit vymazavanie aj zo suboru ako aj z tabulky....
        removeSiteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
                model.removeRow(dataTable.getSelectedRow());

            }
        });

        //Nastavenie rozlozenia okna Editora....
        dataPane.setViewportView(dataTable);
        javax.swing.GroupLayout dataTableLayout = new javax.swing.GroupLayout(oknoEditorInside);
        oknoEditorInside.setLayout(dataTableLayout);
        dataTableLayout.setHorizontalGroup(dataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dataTableLayout.createSequentialGroup().addContainerGap(21, Short.MAX_VALUE).addGroup(dataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE).addGroup(dataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(removeSiteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(dataPane, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE))).addGap(19, 19, 19)));
        dataTableLayout.setVerticalGroup(dataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(dataTableLayout.createSequentialGroup().addGap(14, 14, 14).addComponent(dataPane, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(removeSiteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(34, 34, 34).addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(21, Short.MAX_VALUE)));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(editorWindow.getContentPane());
        editorWindow.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(48, 48, 48).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(siteLabel).addComponent(comboSites, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(33, 33, 33).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(loginLabel).addComponent(loginBox, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(40, 40, 40).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(passwordBox, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(addSiteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(passwordLabel))).addComponent(oknoEditorInside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(57, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(47, 47, 47).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(siteLabel).addComponent(loginLabel).addComponent(passwordLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(comboSites, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(loginBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(passwordBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(addSiteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(oknoEditorInside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));

        loginConfirmationWindow.dispose();
        //loginConfirmationWindow.setVisible(false);
        // vsetko stlacit dokopy a nastavit poziciu na stred obrazovky(setLocationRelativeTo(null))....
        editorWindow.pack();
        editorWindow.setVisible(true);
        editorWindow.setLocationRelativeTo(null);
    }

    public static void fillData() throws InterruptedException, GeneralSecurityException, IOException
    {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        WebDriver driver = new ChromeDriver(options);

        //driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        Web page = new Web();
        Iterator dbIterator = db.entrySet().iterator();

        Set<String> windows = driver.getWindowHandles();
        ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
        driver.switchTo().defaultContent();

        while (dbIterator.hasNext())
        {
            Map.Entry mapElement = (Map.Entry)dbIterator.next();

            Web entry = (Web)mapElement.getValue();
            String website = (String) mapElement.getKey();

            //String title = driver.getTitle();
            int pocet = tabs.size();

            for (int i=1;i<pocet;i++)
            {
                System.out.println("Title: " + driver.getTitle());
                System.out.println("URL: " + driver.getCurrentUrl() + '\n');

                //==================INSTAGRAM=================================
                if(driver.getCurrentUrl().contains("instagram.com")&&website.equals("Instagram"))
                {
                    page.username = "username";
                    page.password = "password";
                    webFields.put("Instagram",page);

                    WebElement username = driver.findElement(By.name(webFields.get("Instagram").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.name(webFields.get("Instagram").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Twitter=================================
                if(driver.getCurrentUrl().contains("twitter.com")&&website.equals("Twitter"))
                {
                    page = new Web();
                    page.password = "password";
                    page.username = "text";
                    webFields.put("Twitter",page);

                    WebElement username = driver.findElement(By.name(webFields.get("Twitter").username));
                    username.sendKeys(entry.username);
                    username.sendKeys(Keys.RETURN);
                    Thread.sleep(800);
                    WebElement password = driver.findElement(By.name(webFields.get("Twitter").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Facebook=================================
                if(driver.getCurrentUrl().contains("facebook.com")&&website.equals("Facebook"))
                {
                    page = new Web();
                    page.password = "pass";
                    page.username = "email";
                    webFields.put("Facebook",page);

                    WebElement username = driver.findElement(By.name(webFields.get("Facebook").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.name(webFields.get("Facebook").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Google=================================
                if(driver.getCurrentUrl().contains("accounts.google.com") ||driver.getCurrentUrl().contains("accounts.google.sk")  &&website.equals("Google"))
                {
                    page = new Web();
                    page.password = "password";
                    page.username = "identifier";
                    webFields.put("Google",page);

                    WebElement username = driver.findElement(By.name(webFields.get("Google").username));
                    username.sendKeys(entry.username);
                    username.sendKeys(Keys.RETURN);
                    Thread.sleep(2000);
                    WebElement password = driver.findElement(By.name(webFields.get("Google").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Twitch=================================
                if(driver.getCurrentUrl().contains("twitch.com")&&website.equals("Twitch"))
                {
                    page = new Web();
                    page.password = "password-input";
                    page.username = "login-username";
                    webFields.put("Twitch",page);

                    WebElement username = driver.findElement(By.id(webFields.get("Twitch").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.id(webFields.get("Twitch").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Centrum=================================
                if(driver.getCurrentUrl().contains("centrum.sk")&&website.equals("Centrum"))
                {
                    page = new Web();
                    page.password = "password";
                    page.username = "username";
                    webFields.put("Centrum",page);

                    WebElement username = driver.findElement(By.id(webFields.get("Centrum").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.id(webFields.get("Centrum").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Svet Elektro=================================
                if(driver.getCurrentUrl().contains("svetelektro.com")&&website.equals("Svetelektro"))
                {
                    page = new Web();
                    page.password = "user_pass";
                    page.username = "login-username";
                    webFields.put("Svetelektro",page);

                    WebElement username = driver.findElement(By.id(webFields.get("Svetelektro").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.id(webFields.get("Svetelektro").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Mojevideo=================================
                if(driver.getCurrentUrl().contains("www.mojevideo.sk")&&website.equals("Mojevideo"))
                {
                    page = new Web();
                    page.password = "l_password";
                    page.username = "l_username";
                    webFields.put("Mojevideo",page);

                    WebElement username = driver.findElement(By.name(webFields.get("Mojevideo").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.name(webFields.get("Mojevideoo").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }

                //==================Azet=================================
                if(driver.getCurrentUrl().contains("www.azet.sk")&&website.equals("Azet"))
                {
                    page = new Web();
                    page.password = "form[password]";
                    page.username = "form[username]";
                    webFields.put("Azet",page);

                    WebElement username = driver.findElement(By.name(webFields.get("Azet").username));
                    username.sendKeys(entry.username);
                    WebElement password = driver.findElement(By.name(webFields.get("Azet").password));
                    password.sendKeys(entry.password);
                    password.sendKeys(Keys.RETURN);
                }
                //prejst kazde okno a skusit kazdy web...
                driver.switchTo().window(tabs.get(i-1));
            }
        }
    }
}