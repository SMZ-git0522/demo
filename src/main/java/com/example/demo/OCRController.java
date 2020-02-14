package com.example.demo;

import com.baidu.aip.ocr.AipOcr;
import com.example.demo.JsonChange;
import freemarker.template.Template;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * @Author: wangzhenze
 * @Description:
 * @Date: Created in 2020/2/5 4:48 下午
 */
/**
 * 文字识别OCR模块控制器
 */
@RestController
public class OCRController {
    @Autowired
    JsonChange jsonChange;
    @Autowired
    WordUtil wordUtil;
    //接口申请免费，请自行申请使用，如果学习使用可以用下
    public static final String APP_ID = "18441142";
    public static final String API_KEY = "oXEEIko1Wz1klzlwNxLX0Vip";
    public static final String SECRET_KEY = "o3brr7eWrgy53sn9G1N2USWVnZH4rfej";
    @RequestMapping(value = "/ocr", method = RequestMethod.POST)
    public Map ocr(MultipartFile file) throws Exception {

        //接收图像二进制数据
        byte[] buf = file.getBytes();
        //初始化百度接口
        AipOcr client=new AipOcr(APP_ID,API_KEY,SECRET_KEY);
        //首选参数
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("language_type", "CHN_ENG");    //中英语言
        JSONObject res = client.basicGeneral(buf, options);
        Map map = jsonChange.json2map(res.toString());
        //System.out.println(res.toString());

//        System.out.println(res.toString());
        return map;
    }
    @RequestMapping("/export")
    public Map export(MultipartFile file) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        //模拟表格数据
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
//

                byte[] buf = file.getBytes();
        //初始化百度接口
        AipOcr client=new AipOcr(APP_ID,API_KEY,SECRET_KEY);
        //首选参数
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("language_type", "CHN_ENG");    //中英语言
        JSONObject res = client.basicGeneral(buf, options);
        Map map1 = jsonChange.json2map(res.toString());
        List<HashMap<String,String>> wordsResult = (List<HashMap<String,String>>)map1.get("words_result");
        map.put("personlist",wordsResult);
        //生成word
        wordUtil.exportWord("/Users/wangzhenze/Downloads/export.docx", "/Users/wangzhenze/Downloads/", "生成文件.docx", map);
        Map<String,String> returnMap = new HashMap<>();
        returnMap.put("res","生成成功");
        return returnMap;
    }
    //导出word
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download() throws Exception {
        //word
        HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        //设置文件名
        String fileName = new String("生成文件.docx".getBytes("UTF-8"), "iso-8859-1");
        headers.setContentDispositionFormData("attachment", "生成文件.docx");
        String file = "/Users/wangzhenze/Downloads/生成文件.docx";

        //把文件转成字节数组
        File byteFile = new File(file);
        int size = (int) byteFile.length();
        FileInputStream inputStream = new FileInputStream(byteFile);
        byte[] bytes = new byte[size];

        int offset=0;
        int readed;
        while(offset<size && (readed = inputStream.read(bytes, offset,inputStream.available() )) != -1){
            offset+=readed;
        }
        inputStream.close();

        //返回
        return new ResponseEntity<byte[]>(bytes,headers, HttpStatus.OK);
    }
    //002.身份证识别
    @RequestMapping(value = "/idCard",method = RequestMethod.POST)
    public Map idCard(@RequestParam("file_idcard") MultipartFile[] fileArr) throws Exception {
        String key = "pk";
        //EncryptUtil des = new EncryptUtil(key, "utf-8");
        MultipartFile mf1 = fileArr[0];
        MultipartFile mf2 = fileArr[1];
        byte[] by1 = mf1.getBytes();
        byte[] by2 = mf2.getBytes();
        AipOcr client1 = new AipOcr(APP_ID,API_KEY,SECRET_KEY);
        JSONObject jo_fr = client1.idcard(by1, "front", new HashMap<String, String>());    //正面
        System.out.println("正面：" + jo_fr);
        AipOcr client2 = new AipOcr(APP_ID,API_KEY,SECRET_KEY);
        JSONObject jo_ba = client2.idcard(by2, "back", new HashMap<String, String>());     //背面
        System.out.println("背面：" + jo_ba);
        Map map = new HashMap<String, String>();
        map.put("front", jsonChange.json2map(jo_fr.toString()));
        map.put("back", jsonChange.json2map(jo_ba.toString()));
        System.out.println(map.toString());
        return map;
    }
    //有道翻译
    /**
     * 有道第三方翻译
     * @param
     * @return
     */
    @RequestMapping("/questWord")
    @ResponseBody
    public Map addWord(MultipartFile file,String from ,String to) throws Exception {
        HashMap<String,String> returnMap = new HashMap<>();
        //接收图像二进制数据
        byte[] buf = file.getBytes();
        //初始化百度接口
        AipOcr client=new AipOcr(APP_ID,API_KEY,SECRET_KEY);
        //首选参数
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("language_type", "CHN_ENG");    //中英语言
        JSONObject res = client.basicGeneral(buf, options);
        Map map = jsonChange.json2map(res.toString());
        List<HashMap<String,String>> wordsResult = (List<HashMap<String,String>>)map.get("words_result");
        String word = "";
        for(int i =0;i<wordsResult.size();i++){
            word+=wordsResult.get(i).get("words");
        }
        //String word = "早上好";
        YouDaoUtil youDaoUtil = new YouDaoUtil();
        //获得有道翻译过来的json字符串
        String result = youDaoUtil.main(word,from,to);
        try {
            // json 转jsonobject
            JSONObject jsonObject = new JSONObject(result);
            // 提取jsonobject中的使用信息    web下为"网络解释"
            String a = jsonObject.get("translation").toString();
            returnMap.put("translation",a);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        return AjaxResult.success("翻译失败");
        return returnMap;
    }
}