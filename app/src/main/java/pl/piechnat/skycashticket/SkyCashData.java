package pl.piechnat.skycashticket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class SkyCashData {

    private static SkyCashData instance = null;
    private final String HTML_HEAD;

    private SharedPreferences memory;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private JSONArray lastTicket;
    private long realPayment, validityPeriod, fakePayment, sessionExpiration;
    private String sessionId, lastError;

    boolean onlineMode;
    long fakeInterval, lastBalance;
    String deviceId, userId, userPass, userPin, ticketId, supplierId,
        ticketName, ticketCity;

    static SkyCashData getInstance(Context context) {
        if (instance == null) instance = new SkyCashData(context);
        return instance;
    }

    private SkyCashData(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        HTML_HEAD = "<html><head><meta charset=\"UTF-8\"/>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1\" /><style>" +
            "  body, html { background: #efefef; font-size: " +
                Long.toString(14) + "px; font-family: sans-serif; color: #444; }" +
            "  p { margin: 0 0 0.8em 0; }" +
            "  h2 { font-size: 1.285em; font-weight: 300; margin: 0 0 0.4em 0; color: #888; }" +
            "  img { width: " + Integer.toString(Math.round(454 / metrics.density)) + "px; margin: 0.22em 0; }" +
            "</style></head><body>";
        memory = context.getSharedPreferences("skycashTicket-dataPreferencesFile", 0);
        try {
            String DEFAULT_TICKET = "[{\"label\":\"Fotokod:\",\"type\":\"image/png\",\"value\":\"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\"},{\"label\":\"\",\"type\":\"text/plain\",\"value\":\"Bilet normalny 20-minutowy\"},{\"label\":\"Ważny do:\",\"type\":\"text/plain\",\"value\":\"06-05-2017 22:18:57\"},{\"label\":\"Liczba sztuk:\",\"type\":\"text/plain\",\"value\":\"1\"},{\"label\":\"Cena za 1 bilet:\",\"type\":\"text/plain\",\"value\":\"2,80 PLN\"},{\"label\":\"Numer kontrolny:\",\"type\":\"text/plain\",\"value\":\"18623431\"},{\"label\":\"Numer biletu:\",\"type\":\"text/plain\",\"value\":\"LDZ 00536363\"},{\"label\":\"Bilet zakupiony:\",\"type\":\"text/plain\",\"value\":\"06-05-2017 21:58:57\"},{\"label\":\"Kwota transakcji:\",\"type\":\"text/plain\",\"value\":\"2,80 PLN\"},{\"label\":\"Numer transakcji:\",\"type\":\"text/plain\",\"value\":\"1000110000133664831\"}]";
            lastTicket = new JSONArray(memory.getString("lastTicket", DEFAULT_TICKET));
        } catch (Exception e) {}
        realPayment = memory.getLong("realPayment", 0);
        validityPeriod = memory.getLong("validityPeriod", 20 * 60 * 1000);
        fakePayment = memory.getLong("fakePayment", 0);
        sessionId = memory.getString("sessionId", null);
        sessionExpiration = memory.getLong("sessionExpiration", 0);
        lastBalance = memory.getLong("lastBalance", -1);
        ticketName = memory.getString("ticketName", "Bilet normalny 20-minutowy");
        ticketCity = memory.getString("ticketCity", "Łódź");
        // settings
        onlineMode = memory.getBoolean("onlineMode", false);
        deviceId = memory.getString("deviceId", "2600197305500925446916"); //must come from the original application
        userId = memory.getString("userId", "48123456789");
        userPass = memory.getString("userPass", "haslo1234");
        userPin = memory.getString("userPin", "1234");
        ticketId = memory.getString("ticketId", "N20M");
        supplierId = memory.getString("supplierId", "phone:112212");
        fakeInterval = memory.getLong("fakeInterval", 3 * 60 * 1000);
        lastError = memory.getString("lastError", null);
    }

    void saveData() {
        SharedPreferences.Editor editor = memory.edit();
        editor.putLong("fakePayment", fakePayment);
        editor.putString("sessionId", sessionId);
        editor.putLong("sessionExpiration", sessionExpiration);
        editor.putString("lastError", lastError);
        editor.putLong("lastBalance", lastBalance);
        editor.commit();
    }

    void saveSettings() {
        SharedPreferences.Editor editor = memory.edit();
        editor.putBoolean("onlineMode", onlineMode);
        editor.putLong("fakeInterval", fakeInterval);
        editor.putString("deviceId", deviceId);
        editor.putString("userId", userId);
        editor.putString("userPass", userPass);
        editor.putString("userPin", userPin);
        editor.putString("ticketId", ticketId);
        editor.putString("supplierId", supplierId);
        editor.apply();
    }

    String getSessionId() {
        return System.currentTimeMillis() < sessionExpiration ? sessionId : null;
    }

    void setSessionId(String id) {
        sessionExpiration = (sessionId = id) != null ? System.currentTimeMillis() + (10 * 60 * 1000) : 0;
    }

    String getLastError() {
        return lastError;
    }

    void setLastError(String msg) {
        lastError = msg != null ? "(" + dateFormat.format(new Date()) + ") " + msg : null;
    }

    boolean isValidTicket() {
        return System.currentTimeMillis() <= (realPayment + validityPeriod);
    }

    void setTicket(JSONObject response) throws SkyCashException {
        try {
            JSONArray arr = response.getJSONObject("transactionInfo").getJSONArray("displayItems");
            long tmp1 = dateFormat.parse(arr.getJSONObject(7).getString("value")).getTime();
            long tmp2 = dateFormat.parse(arr.getJSONObject(2).getString("value")).getTime();
            realPayment = tmp1;
            validityPeriod = tmp2 - tmp1;
            lastTicket = arr;
            fakePayment = 0;
            arr = response.getJSONObject("offlineItem").getJSONObject("listRepresentation").getJSONArray("listItemViewData");
            ticketName = arr.getJSONObject(0).getString("text");
            ticketCity = arr.getJSONObject(2).getString("text");
            SharedPreferences.Editor editor = memory.edit();
            editor.putString("lastTicket", lastTicket.toString());
            editor.putLong("realPayment", realPayment);
            editor.putLong("validityPeriod", validityPeriod);
            editor.putString("ticketName", ticketName);
            editor.putString("ticketCity", ticketCity);
            editor.commit();
        } catch (Exception e) {
            throw new SkyCashException("Błąd biletu: " + e.getLocalizedMessage());
        }
    }

    String getRealExpiration() {
        return dateFormat.format(new Date(realPayment + validityPeriod));
    }

    String getTicket(boolean forceUpdate) {
        long now = System.currentTimeMillis();
        if (now > (fakePayment + validityPeriod) || forceUpdate) {
            if (isValidTicket()) {
                fakePayment = forceUpdate ? realPayment : realPayment - fakeInterval;
            } else {
                fakePayment = now - fakeInterval;
            }
        }
        StringBuilder res = new StringBuilder(HTML_HEAD);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(fakePayment);
            lastTicket.getJSONObject(7).put("value", dateFormat.format(cal.getTime()));
            try {
                String hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
                String nbr = lastTicket.getJSONObject(5).getString("value");
                nbr = nbr.substring(nbr.length() - 4, nbr.length() - 1);
                int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
                lastTicket.getJSONObject(5).put("value", String.format("%02d%d%c%s%c",
                        cal.get(Calendar.WEEK_OF_YEAR), day > 0 ? day : 7, hour.charAt(0), nbr, hour.charAt(1)));
            } catch (Exception e) {}
            lastTicket.getJSONObject(2).put("value", dateFormat.format(new Date(fakePayment + validityPeriod)));
            for (int i = 0; i < lastTicket.length(); i++) {
                JSONObject item = lastTicket.getJSONObject(i);
                res.append("<h2>").append(item.getString("label")).append("</h2>");
                if (item.getString("type").contentEquals("text/plain")) {
                    res.append("<p>").append(item.getString("value")).append("</p>");
                } else {
                    res.append("<p style=\"text-align: center\"><img src=\"data:").append(item.getString("type"))
                            .append(";base64,").append(item.getString("value")).append("\" /></p>");
                }
            }
        } catch (Exception e) {}
        return res.append("</body></html>").toString();
    }

    String getTicket() { return getTicket(false); }
}

class SkyCashException extends Exception {
    public SkyCashException(String message) {
        super(message);
    }
}


