package com.tjpu.pk.common.utils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * 
 * @author: lip
 * @date: 2018/10/11 0011 下午 6:30
 * @Description: 图片处理工具类
 * @updateUser: 
 * @updateDate: 
 * @updateDescription: 
 * @param: 
 * @return: 
*/
public abstract class ImageUtil {
	private static char mapTable[] = {
			'0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '0', '1',
			'2', '3', '4', '5', '6', '7',
			'8', '9'};

	public static 	String checkKey = "checkKey2018";
	/**
	 * 
	 * @author: lip
	 * @date: 2018/10/11 0011 下午 6:37
	 * @Description: 生成随机验证码
	 * @updateUser: 
	 * @updateDate: 
	 * @updateDescription: 
	 * @param: 
	 * @return: 
	*/
	public static Map<String, Object> getImageCode(int width, int height) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if (width <= 60) width = 60;
		if (height <= 40) height = 40;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		//生成随机类
		Random random = new Random();
		// 设定背景色
		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, width, height);
		//设定字体
		g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		// 随机产生168条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(getRandColor(160, 200));
		for (int i = 0; i < 168; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}
		//取随机产生的码
		String randomCode = "";
		//4代表4位验证码,如果要生成更多位的认证码,则加大数值
		int num = (width-4*2)/5;
		for (int i = 0; i < 4; ++i) {
			randomCode += mapTable[(int) (mapTable.length * Math.random())];
			// 将认证码显示到图象中
			g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
			//直接生成
			Font font = new Font("宋体",Font.PLAIN,height/2);
			g.setFont(font);
			String str = randomCode.substring(i, i + 1);
			g.drawString(str, num * i + num, height/2+height/5);
		}
		// 释放图形上下文
		g.dispose();
		returnMap.put("image", image);
		returnMap.put("randomCode", randomCode);
		return returnMap;
	}

	//给定范围获得随机颜色
	static Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255) fc = 255;
		if (bc > 255) bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
}
