package pl.piechnat.skycashticket; 

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by Mateusz on 2017-05-19.
 */

class SkyCashClient {

    public static final int LOGIN_SUCCESSFUL = 0, ALREADY_LOGGED_IN = 1;

    private final int CONNECTION_TIMEOUT = 5000;
    private SkyCashData data;
    private JSONObject loginReq, initPayReq, confirmPayReq, getInfoReq, response;

    SkyCashClient(SkyCashData skyCashData) {
        data = skyCashData;
        try {
            // loginReq create
            loginReq = new JSONObject();
            JSONObject tO = new JSONObject();
            tO.put("branding", "SKYCASH");
            tO.put("deviceId", data.deviceId);
            tO.put("deviceModel", "SONY XPERIA E3");
            tO.put("displayHeight", 782);
            tO.put("displayWidth", 480);
            tO.put("generation", "GEN_3_0");
            tO.put("language", "pl");
            tO.put("networkOperatorName", "Virgin mobile");
            tO.put("osVersion", "4.4.4");
            tO.put("platform", "ANDROID");
            tO.put("version", "3.6.57");
            tO.put("isRooted", false);
            tO.put("CSCSalesCode", "");
            tO.put("RILSerialNumber", "");
            loginReq.put("deviceInfo", tO);
            loginReq.put("photoHooks", new JSONObject());
            loginReq.put("procedureName", "loginUser");
            loginReq.put("skyCashId", "phone:" + data.userId);
            loginReq.put("credentials", data.userPass);
            loginReq.put("applicationType", "ANDROID");
            loginReq.put("appType", "SKYCASH");
            loginReq.put("appGeneration", "GEN_3_0");
            loginReq.put("externalPartnerId", JSONObject.NULL);
            loginReq.put("externalClientId", JSONObject.NULL);
            tO = new JSONObject();
            tO.put("imei", "123456789012345");
            tO.put("factoryModel", "Sony Xperia E3");
            loginReq.put("installData", tO);
            loginReq.put("deviceId", data.deviceId);
            loginReq.put("applicationVersion", "3.6.57");
            loginReq.put("displaySize", "480x782");
            JSONArray tA = new JSONArray();
            tO = new JSONObject();
            tO.put("versionNum", 4322637);
            tO.put("storedDisplayables", new JSONArray());
            tO.put("procedureName", "getLayoutsSet");
            tA.put(tO);
            tO = new JSONObject();
            tO.put("ids", new JSONArray());
            tO.put("procedureName", "getOfflineItems");
            tA.put(tO);
            loginReq.put("additionalRequests", tA);
            // initPayReq create
            initPayReq = new JSONObject(loginReq, new String[] {"deviceInfo", "photoHooks"});
            initPayReq.put("procedureName", "initPayWithSource");
            initPayReq.put("fromSkyCashId", "phone:" + data.userId);
            initPayReq.put("toSkyCashId", data.supplierId);
            initPayReq.put("cMessage", data.ticketId + " DEV" + data.deviceId + " IDAPPidApp");
            tO = new JSONObject();
            tO.put("productCode", data.ticketId);
            tO.put("DEV", data.deviceId);
            tO.put("IDAPP", "idApp");
            initPayReq.put("cMessageNew", tO);
            initPayReq.put("amountInTicks", 0);
            initPayReq.put("currency", "0");
            // confirmPayReq create
            confirmPayReq = new JSONObject(loginReq, new String[] {"deviceInfo", "photoHooks"});
            confirmPayReq.put("procedureName", "confirmPay");
            confirmPayReq.put("paymentReference", "");
            confirmPayReq.put("credentials", data.userPin);
            confirmPayReq.put("notify", false);
            confirmPayReq.put("cmessage", data.ticketId + " DEV" + data.deviceId + " IDAPPidApp");
            tO = new JSONObject();
            tO.put("productCode", data.ticketId);
            tO.put("DEV", data.deviceId);
            tO.put("IDAPP", "idApp");
            confirmPayReq.put("cmessageNew", tO);
            getInfoReq = new JSONObject(loginReq, new String[] {"skyCashId", "photoHooks"});
            getInfoReq.put("procedureName", "getInfo");
        } catch (Exception e) {}
    }

    private String getResultError(String defaultMsg) {
        try {
            String errorMsg = response.getString("errorMessage");
            if (errorMsg.contentEquals("INVALID_SESSION_ID_MSG")) data.setSessionId(null);
            defaultMsg = defaultMsg + ": " + errorMsg;
            defaultMsg = defaultMsg + ". " + response.getString("additionalMessage");
        } catch (Exception e) {} finally {
            return defaultMsg.trim();
        }
    }

    private JSONObject sendData(JSONObject jsonData) throws SkyCashException {
        String sessionId = data.getSessionId();
        HttpURLConnection conn = null;
        try {
            jsonData.put("sessionId", sessionId);
            conn = (HttpURLConnection) (new URL("https://mars.skycash.com/tip/request/")).openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(CONNECTION_TIMEOUT);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            // headers
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.4.4; Xperia E3 Build/KTU84Q)");
            byte[] rawData = jsonData.toString().getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", Integer.toString(rawData.length));
            // request
            BufferedOutputStream outStrm = new BufferedOutputStream(conn.getOutputStream());
            outStrm.write(rawData);
            outStrm.close();
            // response
            BufferedInputStream inStrm = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            int length;
            byte[] buffer = new byte[1024];
            while ((length = inStrm.read(buffer)) != -1) response.write(buffer, 0, length);
            inStrm.close();
            this.response = new JSONObject(response.toString(StandardCharsets.UTF_8.name()));
            data.setSessionId(sessionId);
            return this.response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SkyCashException("Błąd połączenia: " +
                    e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public void updateBalance() throws Exception {
        data.lastBalance = sendData(getInfoReq).getJSONObject("accountInfo").getInt("availableBalanceInTicks");
    }

    public int login() throws SkyCashException {
        if (data.getSessionId() != null) return ALREADY_LOGGED_IN;
        try {
            data.setSessionId(sendData(loginReq).getString("sessionId"));
            data.lastBalance = response.getJSONObject("accountInfo").getInt("availableBalanceInTicks");
            return LOGIN_SUCCESSFUL;
        } catch(SkyCashException e) { throw e; } catch (Exception e) {
            throw new SkyCashException(getResultError("Błąd logowania"));
        }
    }

    public void buyTicket() throws SkyCashException {
        try {
            confirmPayReq.put("paymentReference", sendData(initPayReq).getString("payReference"));
            data.setTicket(sendData(confirmPayReq));
            updateBalance();
            data.setLastError(null);
            data.setSessionId(null);
        } catch(SkyCashException e) { throw e; } catch (Exception e) {
            throw new SkyCashException(getResultError("Błąd transakcji"));
        }
    }
}
