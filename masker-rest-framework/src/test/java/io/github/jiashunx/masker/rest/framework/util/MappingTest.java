package io.github.jiashunx.masker.rest.framework.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * content-type对照表生成
 * @author jiashunx
 */
public class MappingTest {

    public static void main(String[] args) throws Exception {
        // 获取content-type对照表
//        String url = "https://tool.oschina.net/commons";
        HttpURLConnection connection = null;
        URL url = new URL("https://tool.oschina.net/commons");
        connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        if (connection.getResponseCode() == 200) {
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(inputStream.available());
            IOUtils.copy(inputStream, baos);
            String html = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            Document document = Jsoup.parse(html);
            Element body = document.body();
            Element tableElement = body.getElementsByTag("table").get(0);
            Element tbodyElement = tableElement.child(1);
            Elements trElements = tbodyElement.children();
            System.out.println("{");
            for (Element trElement : trElements) {
                Element td0 = trElement.child(0);
                Element td1 = trElement.child(1);
                Element td2 = trElement.child(2);
                Element td3 = trElement.child(3);
                System.out.println("    \"" + td0.text() + "\": \"" + td1.text() + "\",");
                System.out.println("    \"" + td2.text() + "\": \"" + td3.text() + "\",");
            }
            System.out.println("}");
        }
    }

}
