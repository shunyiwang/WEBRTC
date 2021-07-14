package com.example.honlive;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;

public class ConnectServerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);


        Button bt = findViewById(R.id.sure);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_username = findViewById(R.id.et_username);
                EditText et_password = findViewById(R.id.et_password);
                if(TextUtils.isEmpty(et_username.getText())){
                    Toast.makeText(getApplicationContext(), "地址输入为空！", Toast.LENGTH_SHORT).show();
                }else if(!isIpString(et_username.getText().toString())){
                    Toast.makeText(getApplicationContext(), "IP不合法！", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(et_password.getText())){
                    Toast.makeText(getApplicationContext(), "端口输入为空！", Toast.LENGTH_SHORT).show();
                }else if(!isPortString(et_password.getText().toString())){
                    Toast.makeText(getApplicationContext(), "端口输入不合法！", Toast.LENGTH_SHORT).show();
                }else{
//                    String datajson =
//                              "{" +
//                              "\"ip\"" +":"  +"\""+ et_username.getText().toString()+"\""+","+
//                              "\"port\""+":" + et_password.getText().toString() +
//                               "}";

                    String fileData =
                            "http://" + et_username.getText().toString() + ":" + et_password.getText().toString();
                    try {
                        writeToFile(fileData);
                        Toast.makeText(getApplicationContext(), "连接成功！", Toast.LENGTH_SHORT).show();
                        Intent intent =new Intent(ConnectServerActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }
    public boolean isIpString(String arg0){
        String[] strArr = arg0.split("\\.");
        if (strArr.length < 4||strArr.length > 4) {
            return false;
        }
        for (String strnum : strArr) {
            for(Character ch:strnum.toCharArray()){
                if(!isDigit(ch)){
                    return false;
                }
            }
            int ipnum = Integer.parseInt(strnum);
            if (ipnum < 0 || ipnum > 255) {
                return false;
            }
        }
        return true;
    }
    public boolean isPortString(String arg0){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(arg0).matches();
    }
    public void writeToFile(String result) throws IOException {
        BufferedWriter writer = null;
        File file = new File( this.getFilesDir().getPath().toString()+"/address.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("文件写入成功！");
    }

}
