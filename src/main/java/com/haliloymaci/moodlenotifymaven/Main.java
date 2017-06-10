/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.haliloymaci.moodlenotifymaven;

import static com.haliloymaci.moodlenotifymaven.DiffFiles.readFile;
import static com.haliloymaci.moodlenotifymaven.SendMail.generateAndSendEmail;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author halil
 */
public class Main {

    public static final String GTU_MOODLE_URL = "https://bilmuh.gtu.edu.tr/moodle/login/index.php";
    public static final String MOODLE_COURSES_BASE_URL = "https://bilmuh.gtu.edu.tr/moodle/course/view.php?id=";
    public static final int BITIRME_PROJESI_GROUP_URL = 125;
    private static String MOODLE_USERNAME = null;
    private static String MOODLE_PASSWORD = null;
    public static String CONFIG_FILE_NAME = "config.txt";
    private static String courseIDs = null;
    private static boolean printCheckingTime = true;
    private static boolean sendEmail = true;
    private static int COURSE_ID_LINE_NUMBER = 49;
    public static String SOURCE_EMAIL_ID = null;
    public static String SOURCE_EMAIL_PASSWORD = null;
    public static boolean saveMode = false;
    private static final String COURSE_DIRECTROY = "Courses";
    private static final String MAIL_FILE_TAG = "_mailList.txt";
    private static boolean isDisplayParameters = true;
    private static String LogFileName = "CheckingTimes.log";
    private static boolean isNotifyUnreadPost = true;

    public static void main(String[] args) {
        checkArguments(args);

        // create new config file if it does not exist in current directory
        File configFile = new File(CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            createConfigFile();
            System.err.println("Config file is not found. Created automatically. Please write necessary information to \"" + configFile.getAbsolutePath() + "\".");
            System.exit(-1);
        } else {
            String errorMessage = setParametersInConfigFile();
            if (errorMessage != null) {
                System.err.println(errorMessage + ". Correct it in " + CONFIG_FILE_NAME + ". If you lose"
                        + " parameter id, delete config file. Program creates new config file with parameters.");
                System.exit(-1);
            }
        }

        if (isDisplayParameters) {
            System.out.println("Welcome to Moodle Notification Program!");
            displayParameters();
        }
        List<String> urlList = new ArrayList<String>();
        String[] urlStr = courseIDs.split(",");
        for (String urlStr1 : urlStr) {
            urlList.add(MOODLE_COURSES_BASE_URL + urlStr1);
        }

        if (saveMode) {
            try {
                saveCourseData(urlList);
            } catch (KeyManagementException | NoSuchAlgorithmException | IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (!new File(COURSE_DIRECTROY).exists()) {
                System.err.println("Courses file not found. Save mode enabled and automatically creating...");
                try {
                    saveCourseData(urlList);
                } catch (KeyManagementException | NoSuchAlgorithmException | IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            for (String urlStr1 : urlStr) {
                File f = new File(COURSE_DIRECTROY + File.separator + urlStr1 + ".html");
                if (!f.exists()) {
                    System.err.println("File:" + f.getAbsolutePath() + " is not found. Save mode  enable and automatically creating that file...");
                    try {
                        saveCourseData(urlList);
                    } catch (KeyManagementException | NoSuchAlgorithmException | IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }

            try {
                if (printCheckingTime) {
                    System.out.println("Checking courses...");
                }
                listenUrlList(urlList);
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException | MessagingException | InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static void checkArguments(String[] args) {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-t")) {
                sendEmail = false;
                System.out.println("-t : test mode active. Notifications will not sent via mail.");
            } else if (arg.equals("-d")) {
                printCheckingTime = false;
                System.out.println("-d : do not display checking time is active.");
            } else if (arg.equals("-c")) {
                if ((i + 1) < args.length) {
                    CONFIG_FILE_NAME = args[++i];
                } else {
                    System.out.println(programUsage);
                }
            } else if (arg.equals("-h")) {
                System.out.println(programUsage);
                System.exit(0);
            } else if (arg.equals("-s")) {
                System.out.println("save mode.");
                saveMode = true;
            } else if (arg.equals("-p")) {
                isDisplayParameters = false;
            } else {
                System.err.println("Invalid parameter.");
                System.out.println(programUsage);
                System.exit(-1);
            }

        }
    }

    private static void createConfigFile() {
        try (PrintWriter writer = new PrintWriter(CONFIG_FILE_NAME)) {
            writer.print(configFileContent);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String setParametersInConfigFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE_NAME))) {
            String line = br.readLine();

            while (line != null) {

                // remove white space and comment
                if (line.contains("#")) {
                    if (line.indexOf("#") != 0) {
                        line = line.substring(0, line.indexOf("#") - 1);
                    } else {
                        line = br.readLine();
                        continue;
                    }
                }
                line = line.replaceAll(" ", "");
                if (line.length() < 3 || !line.contains(":")) {
                    line = br.readLine();
                    continue;
                }

                // check parameters are exits
                String[] parameters = line.split(":");
//                for (String parameter : parameters) {
//                    System.out.println(parameter);
//                }
                if (parameters[0].equals("moodle_username")) {
                    MOODLE_USERNAME = parameters[1];
                } else if (parameters[0].equals("moodle_password")) {
                    MOODLE_PASSWORD = parameters[1];
                } else if (parameters[0].equals("moodle_course_link_ids")) {
                    courseIDs = parameters[1];
                } else if (parameters[0].equals("source_gmail_id")) {
                    SOURCE_EMAIL_ID = parameters[1];
                } else if (parameters[0].equals("source_gmail_password")) {
                    SOURCE_EMAIL_PASSWORD = parameters[1];
                }
                line = br.readLine();
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(-2);
        }

        // check validation of parameters
        if (MOODLE_USERNAME == null || MOODLE_USERNAME.isEmpty()) {
            return "Error! Moodle username is empty!";
        }
        if (MOODLE_PASSWORD == null || MOODLE_PASSWORD.isEmpty()) {
            return "Error! Moodle password is empty!";
        }

        if (courseIDs == null || courseIDs.isEmpty()) {
            return "Error! Course IDs is empty!";
        }

        String[] courses = courseIDs.split(",");
        for (String course : courses) {
            if (!course.matches("[0-9]+")) {
                return "Error! Course IDs:\"" + course + "\" must only have digits. Example: 74";
            }
        }
        return null;
    }

    private static void displayParameters() {
        System.out.println("----Parameters----");
        System.out.println("username:" + MOODLE_USERNAME);
        System.out.println("password: " + MOODLE_PASSWORD);
        System.out.println("courseIDs:" + courseIDs);
    }

    public static void saveCourseData(List<String> urlList) throws KeyManagementException, NoSuchAlgorithmException, IOException {

        String loginUrl = GTU_MOODLE_URL;
        LoginWebSite.setNoCertificateReady();
        CookieHandler.setDefault(new CookieManager());
        LoginWebSite loginWebSite = new LoginWebSite();
        String page;
        page = loginWebSite.getPageContent(loginUrl);
        String postParams = loginWebSite.getFormParams(page, MOODLE_USERNAME, MOODLE_PASSWORD);
        loginWebSite.sendPost(loginUrl, postParams);

        // create source files
        new File(COURSE_DIRECTROY).mkdir();
        String[] courseIDStrings = courseIDs.split(",");
        createMailFiles(courseIDStrings);
        List<String> sourceFileList = new ArrayList<String>();
        for (int i = 0; i < urlList.size(); i++) {
            String sourceFileName = COURSE_DIRECTROY + File.separator + courseIDStrings[i] + ".html";
            String[] courseInformation = connectUrl(loginWebSite, urlList.get(i), null, sourceFileName);
            sourceFileList.add(courseInformation[0]);
            String courseName = courseInformation[1];
            System.out.println("Saved course:" + courseName + ", url:" + urlList.get(i));
        }
    }

    public static void createMailFiles(String[] courseIds) {
        for (String courseId : courseIds) {
            File f = new File(COURSE_DIRECTROY + File.separator + courseId + MAIL_FILE_TAG);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void listenUrlList(List<String> urlList) throws KeyManagementException, NoSuchAlgorithmException, IOException, MessagingException, InterruptedException {

        // login moodle
        String loginUrl = GTU_MOODLE_URL;
        LoginWebSite.setNoCertificateReady();
        CookieHandler.setDefault(new CookieManager());
        LoginWebSite loginWebSite = new LoginWebSite();
        String page = loginWebSite.getPageContent(loginUrl);
        String postParams = loginWebSite.getFormParams(page, MOODLE_USERNAME, MOODLE_PASSWORD);
        loginWebSite.sendPost(loginUrl, postParams);

        String tempFileName = File.createTempFile("temp", ".html").getAbsolutePath();

        // login moodle
        LoginWebSite.setNoCertificateReady();
        CookieHandler.setDefault(new CookieManager());
        loginWebSite = new LoginWebSite();
        page = loginWebSite.getPageContent(loginUrl);
        postParams = loginWebSite.getFormParams(page, MOODLE_USERNAME, MOODLE_PASSWORD);
        loginWebSite.sendPost(loginUrl, postParams);

        String[] courseIDStrings = courseIDs.split(",");

        // create log file if it is not exists, otherwise append it
        File logFile = new File(LogFileName);
        logFile.createNewFile();

        for (int i = 0; i < urlList.size(); i++) {
            String[] courseInformations = connectUrl(loginWebSite, urlList.get(i), null, tempFileName);
            String baseFileName = COURSE_DIRECTROY + File.separator + courseIDStrings[i];
            String sourceFileName = baseFileName + ".html";
            String mailListFileName = baseFileName + MAIL_FILE_TAG;
            List<String> differences = DiffFiles.diffTwoFileThatIsMultiline(sourceFileName, tempFileName);

//            String courseName = findCourseIdAndName(sourceFileName);
            String courseName = courseInformations[1];

            System.out.println("Checking " + courseName);
            if (!differences.isEmpty()) {
                differences = removeInvalidDifferences(differences);
                if (!differences.isEmpty()) {
                    String differencesAsHtml = "";
                    String content = "New activity is occured in " + courseName + ":\n";
                    for (int j = 0; j < differences.size(); j++) {
                        content += "\t" + differences.get(j) + "\n";
                        differencesAsHtml += differences.get(j) + "<br>";
                    }
                    System.out.print(content);
                    Files.write(Paths.get(logFile.getAbsolutePath()), content.getBytes(), StandardOpenOption.APPEND);

                    if (sendEmail) {
                        List<String> mailList = readMailFile(mailListFileName);
                        if (mailList == null) {
                            System.err.println("Could not read file: " + mailListFileName);
                        } else if (mailList.isEmpty()) {
                            System.out.println("Could not send mail because mail list file is empty.");
                        } else {
                            for (int j = 0; j < mailList.size(); j++) {
                                generateAndSendEmail(mailList.get(j), "Activity in " + courseName, urlList.get(i), courseName, differencesAsHtml, SOURCE_EMAIL_ID, SOURCE_EMAIL_PASSWORD);
                                String mailMessage = "Mail is sent to " + mailList.get(j) + System.lineSeparator();
                                System.out.println();
                                Files.write(Paths.get(logFile.getAbsolutePath()), mailMessage.getBytes(), StandardOpenOption.APPEND);
                            }

                        }
                    }
                }
                writeToFile(readFile(tempFileName), null, sourceFileName);
            }
        }
        if (printCheckingTime) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            String checkingTimeString = "Checked courses at " + dateFormat.format(date) + System.lineSeparator();
            System.out.println(checkingTimeString); // exampel date: 2016/11/16 12:08:43
            Files.write(Paths.get(logFile.getAbsolutePath()), checkingTimeString.getBytes(), StandardOpenOption.APPEND);
        }
    }

    private static String findCourseIdAndName(String sourceFileName) {
        String nameAndId = DiffFiles.readFile(sourceFileName, COURSE_ID_LINE_NUMBER);
        String courseNamePart = nameAndId;
        int iteration = 1;
        while (nameAndId != null) {
            courseNamePart = DiffFiles.readFile(sourceFileName, COURSE_ID_LINE_NUMBER + iteration);
            if (courseNamePart.equals("Page")) {
                break;
            }
            nameAndId += " " + courseNamePart;
            ++iteration;
            if (iteration > 10) {
                break;
            }
        }

        if (nameAndId != null && nameAndId.length() > 100) {
            nameAndId = "unknown";
        }
        return nameAndId;
    }

    private static List<String> readMailFile(String mailFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(mailFileName))) {

            List<String> mailList = new ArrayList<>();

            String line = br.readLine();

            while (line != null) {
                if (!line.contains("@") || !line.contains(".") || line.length() < 10) {
                    System.err.println("Wrong email:" + line + " is ignored!");
                } else {
                    // do not add duplicates
                    if (!mailList.contains(line)) {
                        mailList.add(line);
                    }
                }
                line = br.readLine();
            }

            return mailList;
        } catch (IOException ex) {
            Logger.getLogger(LoginWebSite.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static List<String> removeInvalidDifferences(List<String> differences) {
        List<String> newDifferences = new LinkedList<>();
        for (int i = 0; i < differences.size(); i++) {
            String diff = differences.get(i);
            if (diff.contains("DeleteDelta")) {
                continue;
            }
            if (isChangeWeek(diff)) {
                continue;
            }

            diff = unreadPost(diff);
            if (diff == null) {
                continue;
            }

            if (onlyTimeChanged(diff)) {
                continue;
            }

            // difference is legal
            newDifferences.add(diff);
        }
        return newDifferences;
    }

    public static String unreadPost(String difference) {
        String pattern = "\\[ChangeDelta, position\\: [0-9]+, lines: \\[([0-9]+)\\] to \\[([0-9]+)\\]\\]";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(difference);
        if (m.find()) {
            try {
                int newPost = Integer.parseInt(m.group(2));
                int oldPost = Integer.parseInt(m.group(1));

                int diffNum = newPost - oldPost;
                if (diffNum > 0 && isNotifyUnreadPost) {
                    return diffNum + " new unread post on 'Sorular Formu'.";
                } else {
                    return null;
                }
            } catch (IndexOutOfBoundsException ex) {
                return difference;
            }
        }

        String pattern2 = "\\[ChangeDelta, position\\: [0-9]+, lines: \\[post\\] to \\[posts\\]\\]";
        r = Pattern.compile(pattern2);
        m = r.matcher(difference);
        if (m.find()) {
            return null;
        }
        String pattern3 = "\\[ChangeDelta, position\\: [0-9]+, lines: \\[posts\\] to \\[post\\]\\]";
        r = Pattern.compile(pattern3);
        m = r.matcher(difference);
        if (m.find()) {
            return null;
        }

        return difference;
    }

    /**
     * connect web-page
     *
     * @param loginWebSite
     * @param url
     * @param showType show web-page given program
     * @param fileName file name that content was written, if it is null, create temporar file and
     * @return 2d string, first string is temp file name for visible text, second string is course
     * name
     * @throws Exception
     */
    private static String[] connectUrl(LoginWebSite loginWebSite, String url, String showType, String fileName) throws IOException {
        // read web-page as html
        String result = loginWebSite.getPageContent(url);

        // write 
        String file = writeToFile(result, null, null);
        // read web-page only visible text
        Document doc = Jsoup.parse(new File(file), "UTF-8");
        String text = doc.body().text();
        String courseName = doc.title();

        // convert multiple line
        text = text.replaceAll(" ", System.lineSeparator());
        // write content to temporary file
        String tempFileName = writeToFile(text, null, fileName);
        String[] returnString = new String[2];
        returnString[0] = tempFileName;
        returnString[1] = courseName;

        return returnString;
    }

    /**
     *
     * @param content written and shown content
     * @param showType show file given program
     * @param fileName file name that content was written, if it is null, create temporar file and
     * return it
     * @return file name that content was written
     */
    public static String writeToFile(String content, String showType, String fileName) {
        try {
            File file;
            if (fileName == null || fileName.isEmpty()) {
                file = File.createTempFile("temp", ".html");
            } else {
                file = new File(fileName);
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(content);
            }
            if (showType != null) {
                if (showType.equals("browser")) {
                    Desktop.getDesktop().browse(file.toURI());
                } else {
                    String cmd = "notepad++ " + file.getCanonicalPath();
                    Runtime.getRuntime().exec(cmd);
                }
            }
            return file.getAbsolutePath();

        } catch (IOException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Check whether moodle course week is change
     *
     * @param delta deleted or inserted delta
     * @return true if week is changed, otherwise false
     */
    public static boolean isChangeWeek(String delta) {
        String[] parts = delta.split(":");
        if (parts.length > 2) {
            if (parts[2].equals(" [This, week]]")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if only time changed in course example situation exist in microprocessor course
     *
     * @param difference difference strng in diffFiles class output
     * @return
     */
    public static boolean onlyTimeChanged(String difference) {
        String pattern = "\\[ChangeDelta, position: [0-9]+, lines: \\[[0-2]?[0-9]:[0-5][0-9]\\] to \\[[0-2]?[0-9]:[0-5][0-9]\\]\\]";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(difference);
        return m.find();
    }

    private static String configFileContent = "### GTU MOODLE NOTIFICATION SYSTEM CONFIG FILE ###" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "moodle_username:121044019 " + System.lineSeparator()
            + "" + System.lineSeparator()
            + "moodle_password:yourMoodlePassword" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "moodle_course_link_ids:100,125,108 # it means that (CSE464, CSE495/496, BIL436)" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "source_gmail_id:yourSourceMailThatSendNotificationMailToUser@gmail.com # it must be a gmail account" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "source_gmail_password:yourmailpassword" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "# You can learn moodle course link IDs by looking course URL on browser." + System.lineSeparator()
            + "# Example: CSE102 - C programming course URL is \"http://193.140.134.13/moodle/course/view.php?id=73\", so your link ID is 73." + System.lineSeparator()
            + "# use comma seperate values for multiple course link IDs, example: 73,74,114." + System.lineSeparator()
            + "# some course link IDs:" + System.lineSeparator()
            + "# CSE102 - C Programming, linkID: 73" + System.lineSeparator()
            + "# CSE108 - C Programming Laboratory, linkID: 74" + System.lineSeparator()
            + "# CSE244 - Systems Programming, linkID: 119" + System.lineSeparator()
            + "# CSE336 - Microprocessors Laboratory, linkID: 114" + System.lineSeparator()
            + "# CSE422 - Theory of Computation, linkID: 110" + System.lineSeparator()
            + "# CSE425 - Introduction to Operations Research, linkID: 109" + System.lineSeparator()
            + "# BIL436 - Introduction to Digital Integrated Circuits, linkID: 108" + System.lineSeparator()
            + "# CSE444 - Software Engineering II, linkID: 106" + System.lineSeparator()
            + "# CSE445 - Compiler Design, linkID: 105" + System.lineSeparator()
            + "# CSE458 - Introduction to Big Data Analytics, linkID: 102" + System.lineSeparator()
            + "# CSE461 - Computer Graphics, linkID: 101" + System.lineSeparator()
            + "# CSE464 - Digital Image Processing, linkID: 100" + System.lineSeparator()
            + "# CSE495/496 - Bitirme Projesi, linkID: 125" + System.lineSeparator()
            + "# BSB501/MBG524 - Biocomputing, linkID: 127" + System.lineSeparator()
            + "# CSE436/536 - Digital Integrated Circuits, linkID: 92" + System.lineSeparator()
            + "# CSE611 - Big Data Analytics, linkID 82:" + System.lineSeparator()
            + "# all link together: 73,74,119,114,110,109,108,106,105,102,100,101,125,127,92,82"
            + "all courses in moodle at 2017 spring are: 73,74,124,75,122,121,120,119,117,116,128,115,114,112,111,110,109,108,106,105,104,103,102,101,100,99,98,125,127,96,95,94,93,92,89,88,87,86,126,85,84,83,82,81,80,79,78,77,76" + System.lineSeparator()
            + "" + System.lineSeparator()
            + "# If any error ocurrecd, remove this file completely on same directory that jar file exists. It will be created automatically with default parameters." + System.lineSeparator()
            + "" + System.lineSeparator()
            + "# java -jar moodleNotify.jar [-t] [-d] [-c configFilePath] [-h]" + System.lineSeparator()
            + "# -t : test mode, do not send mail, only print changes to stdout." + System.lineSeparator()
            + "# -d : do not display checking time." + System.lineSeparator()
            + "# -c [configFilePath] : set parameters in \"configFile\" rathen \"config.txt\"." + System.lineSeparator()
            + "# -h : Display this help message.";

    private static String programUsage = "Program Usage:" + System.lineSeparator()
            + "java -jar moodleNotify.jar [-t] [-d] [-c configFilePath] [-h]" + System.lineSeparator()
            + "-t : test mode, do not send mail, only print changes to stdout." + System.lineSeparator()
            + "-d : do not display checking time." + System.lineSeparator()
            + "-c [configFilePath] : set parameters in \"configFile\" rathen \"" + CONFIG_FILE_NAME + "\"." + System.lineSeparator()
            + "-h : Display this help message."
            + "-s : save Mode. Create course directory and courses files."
            + "-p : do not display config parameters";
}
