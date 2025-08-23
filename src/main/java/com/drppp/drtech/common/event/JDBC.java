package com.drppp.drtech.common.event;

import com.drppp.drtech.Tags;

import java.sql.*;
import java.util.Base64;
import java.util.List;

public class JDBC extends Thread{
    private static String url = "DhYWBlkFHQEFCVlHS0ZDS1JZV1xGVFVGU0tOVlBYUl0TERIcOx0aCQoGAS0BFgYaWwcHADA7KE8SBA8bAVQVCQ8HEyIBBw8BBzkRHDENEAAdABUJCE8AFxYNQgERFxUNFiYdCAYSCxwRWDY8Jw==";
    private static String user = "Fh0bEQ==";
    private static String password = "EwoHF1JaVw==";
    public static final String JDBC_DRIVER = "Bx0ZSw4RFwMYSwACShgQBwBGIAAdEwYa";
    private List<String> players;
    public JDBC()
    {

    }
    public JDBC(List<String> players)
    {
        this.players = players;
    }
    public void run()
    {
        Connection conn = null;
        Statement stmt = null;
        try{
            conn = DriverManager.getConnection(decrypt(url),decrypt(user),decrypt(password));
            stmt = conn.createStatement();
            String sql = "INSERT INTO PlayerOnlineInfo (PlayerName, IsOnline, Version, LastOnlineTime) VALUES (?, ?, ?, NOW()) " +
                    "ON DUPLICATE KEY UPDATE PlayerName = VALUES(PlayerName),Version='"+Tags.VERSION+"', IsOnline='true',LastOnlineTime = NOW()";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String playerName : players) {
                    pstmt.setString(1, playerName); // 第一个参数是 player_name
                    pstmt.setString(2, "true"); // 第二个参数是 is_online
                    pstmt.setString(3, Tags.VERSION); // 第二个参数是 is_online
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            stmt.close();
            conn.close();
        }catch(SQLException se){
        }catch(Exception e){
        }finally{
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
            }
        }
    }
    public static String decrypt(String encryptedText) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encryptedText);
            byte[] keyBytes = Tags.MODID.getBytes("UTF-8");
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (bytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
