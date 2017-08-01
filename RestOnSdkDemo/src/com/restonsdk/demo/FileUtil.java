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
}
