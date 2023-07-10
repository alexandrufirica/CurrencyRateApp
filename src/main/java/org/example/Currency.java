package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;

public class Currency extends  CurrencyService{

    ResultSet resultSet;
    public Currency() throws IOException {
        if (checkTodayCurrency()) {
            sendLiveRequest();
            writeInDatabase();
            httpClient.close();
        } else {
            System.out.println("Today Currency allready in data base!");
        }
    }

    static CloseableHttpClient httpClient = HttpClients.createDefault();

    public static JSONObject exchangeRates;

    public static void sendLiveRequest(){

        // The following line initializes the HttpGet Object with the URL in order to send a request
        HttpGet get = new HttpGet("http://apilayer.net/api/live?access_key=" + getMyApiKey() + "&currencies=EUR,GBP,CAD,RON&source=USD&format=1");

        try {
            CloseableHttpResponse response =  httpClient.execute(get);
            HttpEntity entity = response.getEntity();

            // the following line converts the JSON Response to an equivalent Java Object
             exchangeRates = new JSONObject(EntityUtils.toString(entity));

            File file = new File("currencyrates.json");

                    try {
                        FileWriter myWriter = new FileWriter("currencyrates.json");
                        myWriter.write(exchangeRates.toString());
                        myWriter.close();

                        System.out.println("Successfully wrote to the file.");
                        System.out.println(file.getAbsolutePath());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }

            response.close();
        } catch (ClientProtocolException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (ParseException e) {

            e.printStackTrace();
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    //this method write currency rate in data base table
    public void writeInDatabase()  {
        Connect();

        String SQL = "INSERT INTO rates"
                + "(date, usdeur, usdgbp, usdcad, usdron)"
                + "VALUES (?, ?, ?, ?, ?)";

        DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");


        try (PreparedStatement pstmt = con.prepareStatement(SQL))  {
            Date timeStampDate = new Date((long)(exchangeRates.getLong("timestamp")*1000));
            String formattedDate = dateFormat.format(timeStampDate);

            pstmt.setString(1, formattedDate.toString());
            pstmt.setDouble(2, exchangeRates.getJSONObject("quotes").getDouble("USDEUR"));
            pstmt.setDouble(3, exchangeRates.getJSONObject("quotes").getDouble("USDGBP"));
            pstmt.setDouble(4, exchangeRates.getJSONObject("quotes").getDouble("USDCAD"));
            pstmt.setDouble(5, exchangeRates.getJSONObject("quotes").getDouble("USDRON"));

            pstmt.executeUpdate();
            System.out.println("Succsessfuly wrote to database.");

        }catch ( SQLException ex){
            System.out.println(ex.getMessage());
        }catch (JSONException ex){
            System.out.println(ex.getMessage());
        }
    }

    // the following method give the API Key from a file apikey.txt
    public static final String getMyApiKey(){
        String apikey ="";
        try {
            File myfile = new File("apikey.txt");
            Scanner myReader = new Scanner(myfile);
            while (myReader.hasNextLine()) {
                apikey = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return apikey;
    }

    public boolean checkTodayCurrency(){
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MM yyyy");
        String todayDate = LocalDateTime.now().format(dateFormat).toString();

        try {
            Connect();

            String SQL = "select * from rates where date = ?";

            pst = con.prepareStatement(SQL);
            pst.setString(1,todayDate);

            resultSet = pst.executeQuery();
            if(resultSet.next()) {
                return false;
            }

        }catch ( SQLException ex){
            System.out.println(ex.getMessage());
        }

        return true;

    }

}
