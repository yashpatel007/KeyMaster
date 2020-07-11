/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.keymaster;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author yashpatel
 */
public class KeyMaster {
    
    // buffer strings
    String buffer; 
    String hotkey;
    Robot robot;
    
    // logic seperators
    Boolean isCapturing;
    
    // json key value store
    private JSONObject keyset;
    // constructor
    public KeyMaster() {
        
        buffer = new String();
        hotkey = new String();
 
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(KeyMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        isCapturing = false;
        
         getKeysData();
        loggeroff();
    }
    
    private void loggeroff(){
            // Get the logger for "org.jnativehook" and set the level to off.
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
    }
    
    public void startRecognition(){
      
          
        try{
            
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener(){
                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeEvent){}

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent){}

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent){   
                    String keyText = NativeKeyEvent.getKeyText(nativeEvent.getKeyCode());
                    
                    if(keyText == "⌫"){
                        buffer = removeLastChar(buffer);
                        if(!buffer.endsWith("///")){
                            isCapturing = false;
                        }
                    }else{
                        addToBuffer(keyText);
                    }
                   
                    if(keyText == "/" && buffer.endsWith("///")){
                           isCapturing = true;
                           System.out.println("iscap");
                    }
                    
                    if(isCapturing && keyText == "→"){
                          
                     
                        String[] temp = buffer.split("///");

                        // for debugging
                        for(String s : temp){
                            System.out.println("--"+s+"--");
                        }

                        hotkey = temp[temp.length-1];
                        hotkey = removeLastChar(hotkey);
                        
                        System.out.println("Hot key recognized: ->"+ hotkey);
                        
                        removehotkey(hotkey);
                        
                        if(keyset.containsKey(hotkey.toLowerCase())){
                            typeString(robot, (String)keyset.get(hotkey.toLowerCase()) );
                        }else{
                             typeString(robot, "Not a hot key");
                        }
                        
                        //typeString(robot, "hello world");
                        //System.out.println("Hello world");
                        resetBuffer();
                        isCapturing = false;
                    }
                     System.out.println("Buffer->"+buffer);
                }
            });
        }catch (NativeHookException e){
            e.printStackTrace();
        }
                      
    }
    
    private void addToBuffer(String key){
            this.buffer = this.buffer.concat(key);
    }
    
    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
    
    private void resetBuffer(){
        this.buffer = new String();
    }
    
    private void removehotkey(String hotkey){
        
        for (int i=0 ;i<hotkey.length()+3;i++) {
            int keyCode = KeyEvent.VK_BACK_SPACE;
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
        
    }
    
    private void typeString(Robot robot, String keys) {
        for (char c : keys.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                throw new RuntimeException(
                    "Key code not found for character '" + c + "'");
            }
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);

        }
    }
    
    public void insertTokeyset(String key, String value){
        keyset.put(key, value);
        
        //Write JSON file
        try (FileWriter file = new FileWriter("/Users/yashpatel/NetBeansProjects/KeyMaster/src/main/java/com/mycompany/keymaster/data/data.json")) {
 
            file.write(keyset.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        getKeysData();
        
    }
    
    public void removeFromkeyset(String key){
        keyset.remove(key);
         //Write JSON file
        try (FileWriter file = new FileWriter("/Users/yashpatel/NetBeansProjects/KeyMaster/src/main/java/com/mycompany/keymaster/data/data.json")) {
 
            file.write(keyset.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        getKeysData();
        
    
    }
    
    public JSONObject getKeysData(){
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("/Users/yashpatel/NetBeansProjects/KeyMaster/src/main/java/com/mycompany/keymaster/data/data.json"))
        {
            //Read JSON file
           Object obj = jsonParser.parse(reader);
           JSONObject jsonobj = (JSONObject) obj;
           keyset = jsonobj;
          return keyset;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
       
    }
 
} 

