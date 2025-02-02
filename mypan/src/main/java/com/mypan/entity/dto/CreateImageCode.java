package com.mypan.entity.dto;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Random;

public class CreateImageCode {
    //宽度
    private int width=160;
    //高度
    private int height=40;
    //验证码的字符个数
    private int codeCount=4;
    //验证码干扰线数
    private int lineCount=20;
    //验证码
    private String code=null;
    //验证码图片buffer
    private BufferedImage bufferedImage=null;
    Random random=new Random();

    public CreateImageCode() {
        createImage();
    }

    public CreateImageCode(int width, int height) {
        this.width = width;
        this.height = height;
        createImage();
    }

    public CreateImageCode(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        createImage();
    }

    public CreateImageCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        createImage();
    }
    private void createImage(){
        int fontWidth=width/codeCount;//字体的宽度
        int fontHeight=height-5;
        int codeY=height-8;

        //图像buffer
        bufferedImage=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics g=bufferedImage.getGraphics();
        //设置背景色
        g.setColor(getRandColor(200,250));
        g.fillRect(0,0,width,height);
        //设置字体
        Font font=new Font("Fixedsys",Font.BOLD,fontHeight);
        g.setFont(font);

        //设置干扰线
        for(int i=0;i<lineCount;i++){
            int xs=random.nextInt(width);
            int ys=random.nextInt(height);
            int xe=xs+random.nextInt(width);
            int ye=ys+random.nextInt(height);
            g.setColor(getRandColor(1,255));
            g.drawLine(xs,ys,xe,ye);
        }
        //添加噪点
        float yawpRate=0.01f;
        int area=(int)(yawpRate*width*height);
        for(int i=0;i<area;i++){
            int x=random.nextInt(width);
            int y=random.nextInt(height);
            bufferedImage.setRGB(x,y,random.nextInt(255));
        }

        String str1=randomStr(codeCount);//得到随机字符
        this.code=str1;
        for(int i=0;i<codeCount;i++){
            String strRand=str1.substring(i,i+1);
            g.setColor(getRandColor(1,255));
            //g.drawString(a,x,y)
            //a为要画出来的东西，x和y表示要画的东西最左侧字符的基线位于此图形上下文坐标系的（x,y）处
            g.drawString(strRand,i*fontWidth+3,codeY);
        }
    }
    //得到随机字符
    private String randomStr(int n){
        String str1="ABCDEFGHIJKLMNOPORSTUVWXYZabcdefghijklmnopgrstuvwxyz1234567890";
        String str2="";
        int len=str1.length()-1;
        double r;
        for(int i=0;i<n;i++){
            r=(Math.random())*len;
            str2=str2+str1.charAt((int)r);
        }
        return str2;
    }
    //得到随机颜色
    private Color getRandColor(int fc,int bc){//给定范围获得随机颜色
        if(fc>255) fc=255;
        if(bc>255) bc=255;
        int r=fc+random.nextInt(bc-fc);
        int g=fc+random.nextInt(bc-fc);
        int b=fc+random.nextInt(bc-fc);
        return new Color(r,g,b);
    }
    public void write(OutputStream sos) throws IOException{
        ImageIO.write(bufferedImage,"png",sos);
        sos.close();
    }
    public BufferedImage getBufferedImage(){
        return bufferedImage;
    }
    public String getCode(){
        return code;
    }
}
