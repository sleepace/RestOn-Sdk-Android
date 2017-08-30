package com.restonsdk.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil {
	
	/*
	String oriPath = "C:\\Users\\Administrator\\Desktop\\d_t\\0000010000710_1499306399_0.bat";
	  byte[] bb = FileUtil.readFileByByte(oriPath);
	  for (int i = 0; i < bb.length; i++) {
	   System.out.println((bb[i]&0xff));
	  }
	  */
	  
	/**
	  * 读文件
	  * @param path  文件路径
	  * @return
	  */
	 public static ByteBuffer readFileByBuffer(String path){
	  File file = new File(path);
	  
	  if(file != null){
	   FileInputStream fis = null;
	   FileChannel fc = null;
	   try {
	    if(!file.exists())
	     file.createNewFile();
	    fis = new FileInputStream(file);
	    // 获取管道
	    fc = fis.getChannel();
	    ByteBuffer buffer = ByteBuffer.allocate((int)fc.size());
	    if(fc.read(buffer) > 0){
	     // 写入数据
	     fc.read(buffer);
	    }
	    // 返回数据
	    return buffer;
	   } catch (Exception e) {
	    e.printStackTrace();
	   } finally {
	    try {
	     if (fc != null)
	      fc.close();
	     if (fis != null)
	      fis.close();
	    } catch (IOException e) {
	     e.printStackTrace();
	    }
	   }
	  }
	  
	  return null;
	 }
	public static byte[] readFileByByte(String path){
	  ByteBuffer byteBuffer = readFileByBuffer(path);
	  
	  if(byteBuffer != null){
	   return byteBuffer.array();
	  }
	  
	  return null;
	 }
	
	public static void main(String[] args){
		String oriPath = "F://173_1500825600";
		  byte[] bb = FileUtil.readFileByByte(oriPath);
		  /*breathRateAry[i-1] = j;
		   fread(&j, sizeof(unsigned char), 1, fp);
		   heartRateAry[i - 1] = j;
		   fread(&j, sizeof(unsigned char), 1, fp);
		   statusAry[i - 1] = j;
		   fread(&j, sizeof(unsigned char), 1, fp);
		   statusValueAry[i - 1] = j;
		   */
		  byte[] breathRateAry = new byte[bb.length/4];
		  short[] heartRateAry = new short[bb.length/4];
		  byte[] statusAry = new byte[bb.length/4];
		  byte[] statusValueAry = new byte[bb.length/4];
		  int k = 0;
		  for (int i = 0; i < bb.length; i++) {
			  if(i!=0&&i%4==0){
				  k++;
			  }
			  if(i%4==0)
				  breathRateAry[k] = bb[i]; 
			  if(i%4==1)
				  heartRateAry[k] = (short)(bb[i]&0xff); 
			  if(i%4==2)
				  statusAry[k] = bb[i]; 
			  if(i%4==3)
				  statusValueAry[k] = bb[i]; 
		  }
		  System.out.println();
	}
}
