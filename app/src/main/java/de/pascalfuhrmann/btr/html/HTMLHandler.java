/* Copyright 2018 Pascal Fuhrmann, Jonas Lauschke

    This file is part of substitution_schedule.

    substitution_schedule is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    substitution_schedule is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with substitution_schedule.  If not, see <http://www.gnu.org/licenses/>.
  */
package de.pascalfuhrmann.btr.html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class HTMLHandler {
    private final String username;
    private final String password;
    private String htmlContent;

    public HTMLHandler(String username, String password) {
        this.username = username;
        this.password = password;
        this.htmlContent = "unknown";
    }

    /**
     * Sends a post request containing the userdata to Arktur (Server)
     * as well as saving the returned HTML data for later parsing purposes.
     * If the login didn't succeed it will either throw an error or returns
     * "invalid_login" which is checked in doInBackground()
     * @param user
     * @param pwd
     */
    public String login(String user, String pwd) {
        String postRequestMsg = "";
        try {
            /*
             * Encoding the username and password in UTF-8
             * then calling the postRequest to login and receive the HTML Data
             * from the server which will later be parsed.
             */
            user = URLEncoder.encode(user, "UTF-8");
            pwd = URLEncoder.encode(pwd, "UTF-8");
            String msg = "login=" + user + "&passwd=" + pwd + "&submit=Anmelden";
            postRequestMsg = postRequest("https://btr-rs.de/service-vertretungsplan.php", msg);

            /*
             * If the login wasn't successful the website returns an error
             * which contains "fehlgeschlagen". We are using it to check if the
             * login was successful or not.
             */
            if (postRequestMsg.contains("fehlgeschlagen")) {
                return "invalid_login";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postRequestMsg;
    }

    /**
     * Logs out of all devices which are logged into the account at the moment.
     * @param username
     * @return
     */
    public boolean logout(String username) {
        try {
            /*
             * Encoding the username properly with UTF-8
             * then calling the postRequest to logout and make sure that
             * the user doesn't have to worry about it anymore.
             */
            username = URLEncoder.encode(username, "UTF-8");
            postRequest("https://btr-rs.de/service-logout.php", "login=" + username + "&submit=Abmelden");
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a post request to any URL with an specified message and
     * imitates to be an Firefox/Windows client.
     * @param u url as string to which the client should send the r
     * @param msg post message body as string
     * @throws IOException
     */
    private String postRequest(String u, String msg) throws IOException {
        /*
         * Opening an HTTPS connection to the server while pretending to be a
         * Firefox + Windows User Agent
         */
        URL url = new URL(u);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setReadTimeout(15000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
        con.setDoInput(true);
        con.setDoOutput(true);

        /*
         * Setting up BufferedWriter to get the POST request out to the server
         */
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        /*
         * Sending POST Req.
         * Closing output stream after everything is done.
         */
        try {
            out.write(msg);
        } finally {
            out.flush();
            out.close();
            os.close();
        }

        /*
         * Iterates through the InputStream which gets data of type integer
         * it will then be converted to char and appended to a string.
         */
        StringBuilder postAnswer = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            for (int c = in.read(); c != -1; c = in.read()) {
                //System.out.print((char)c);
                postAnswer.append((char) c);
            }
            return postAnswer.toString();
        }
    }

    public Boolean processLogin() {
        logout(username);
        String htmlContent = login(username, password);
        logout(username);
        if(htmlContent != null) {
            this.htmlContent = htmlContent;
            return !htmlContent.contains("invalid_login");
        } else return false;
    }

    public String getHtmlContent() {
        return htmlContent;
    }
}
