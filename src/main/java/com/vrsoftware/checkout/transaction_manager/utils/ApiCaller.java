package com.vrsoftware.checkout.transaction_manager.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiCaller {
    public static String baseUrl = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";
    private static HttpClient client = HttpClient.newHttpClient();

    //to Unit tests be possible
    public static void setHttpClient(HttpClient httpClient) {
        client = httpClient;
    }

    public static String checkExchangeRate(String countryCurrency, LocalDateTime localDateTime) throws IOException, InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        String url = String.format("%s?fields=country_currency_desc,exchange_rate,record_date&filter=country_currency_desc:eq:%s,record_date:gte:%s,record_date:lte:%s&sort=-record_date",
                baseUrl, countryCurrency, localDateTime.minusMonths(6).format(formatter), localDateTime.format(formatter));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray records = jsonResponse.getJSONArray("data");

            if (records.length() > 0) {
                JSONObject mostRecentRecord = records.getJSONObject(0);
                return mostRecentRecord.getString("exchange_rate");
            } else
                return "Exchange rate not found within the last 6 months.";
        } else
            return "Error calling the API. Status code: " + response.statusCode();
    }

    public static List<String> getCountryCurrencies() throws IOException, InterruptedException {
        String url = baseUrl + "?fields=country_currency_desc";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray records = jsonResponse.getJSONArray("data");

            List<String> currencies = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                currencies.add(record.getString("country_currency_desc"));
            }
            return currencies;
        } else
            return Arrays.asList("Error calling the API. Status code: " + response.statusCode());
    }
}
