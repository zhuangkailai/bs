package com.tjpu.sp.common.utils;

import com.tjpu.pk.common.utils.DataFormatUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WebDriverUtil {


    private static String driverPath;
    private static String screenShotPath;


    static {
        driverPath = DataFormatUtil.parseProperties("chrome.driver.path");
        screenShotPath = DataFormatUtil.parseProperties("screen.shot.path");
    }

    /**
     * 打开浏览器，不能向下滑动
     *
     * @param url
     * @return
     */
    public static String getDocument(String url, String windowX, String windowY) {
        Document doc = null;
        String fileName = "";
        String pathFile = DataFormatUtil.parseProperties("screen.shot.path");
        createFile(pathFile);
        System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
        //创建chrome参数对象
        ChromeOptions options = new ChromeOptions();
        //浏览器后台运行
        options.addArguments("--headless");
        options.addArguments("--window-size=" + windowX + "," + windowY);
        options.addArguments("--no-sandbox");
        options.addArguments("--user-data-dir="+pathFile);
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--hide-scrollbars");
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);
            //设置超时10秒等待
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.findElement(By.id("selectClass")).click();
            WebElement element = driver.findElement(By.cssSelector(".tarClass"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border = \"5px solid white\"", element);
            ((JavascriptExecutor) driver).executeScript("scrollTo(0," + windowY + ")");
            //max size the browser
            driver.manage().window().maximize();
            Date nowDay = new Date();
            fileName = screenShotPath + DataFormatUtil.getDateYMD(nowDay);
            createFile(fileName);
            fileName = fileName + "/" + DataFormatUtil.getDateYMDHMS1(nowDay) + ".png";
            snapshot((TakesScreenshot) driver, fileName);
            //关闭浏览器
            driver.close();
            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return fileName;
    }

    /**
     * @author: lip
     * @date: 2021/1/28 0028 上午 9:03
     * @Description: 创建文件夹目录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    
    private static void createFile(String fileName) {
        File dir = new File(fileName);
        if (!dir.exists()) {// 判断目录是否存在
            dir.mkdirs();  //多层目录需要调用mkdirs
        }
    }



    /**
     * @Description:
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/3/16 16:47
     */
    public static void snapshot(TakesScreenshot drivername, String filename) {
        // this method will take screen shot ,require two parameters ,one is driver name, another is file name
        File scrFile = drivername.getScreenshotAs(OutputType.FILE);
        // Now you can do whatever you need to do with it, for example copy somewhere
        try {
            //System.out.println("save snapshot path is:" + filename);
            FileUtils.copyFile(scrFile, new File(filename));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //System.out.println("Can't save screenshot");
            e.printStackTrace();
        } finally {
            //System.out.println("screen shot finished");
        }
    }


    /**
     * 解析传过来的doc
     *
     * @param doc
     */
    public static void parse(Document doc) {
        if (doc == null) {
            System.out.println("doc is null, unable to continue! ");
            return;
        }
        Elements content = doc.select("div.content");

        //System.out.println(select);
        for (Element element : content) {
            //获取文章标题
            String title = element.select("a.title").text();

            //获取获取帖子网址
            String url = element.select("a.title").attr("href");
            url = "https://www.jianshu.com" + url;

            //获取文章的摘要
            String digest = element.select("p.abstract").text();

            //获取文章作者名称
            String author = element.select("a.nickname").text();

            //获取作者网址
            String authorUrl = element.select("a.nickname").attr("href");
            authorUrl = "https://www.jianshu.com" + authorUrl;

            System.out.println("title: " + title);
            System.out.println("url: " + url);
            System.out.println("digest:  " + digest);
            System.out.println("author: " + author);
            System.out.println("authorUrl: " + authorUrl);
            System.out.println("--------------\n");


        }
    }


}
