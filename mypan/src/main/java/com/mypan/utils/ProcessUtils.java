package com.mypan.utils;

import com.mypan.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class ProcessUtils {
    private static final Logger logger= LoggerFactory.getLogger(ProcessUtils.class);

    public static String executeCommand(String cmd,Boolean outPrintLog) throws BusinessException{
        if(StringUtils.isEmpty(cmd)){
            logger.error("---命令执行失败，因为要执行的ffmpeg指令为空!---");
            return null;
        }
        Runtime runtime=Runtime.getRuntime();
        Process process=null;
        try {
            process=Runtime.getRuntime().exec(cmd);
            //执行ffmpeg指令
            //取出输出流h和错误流的信息
            //注意，必须要取出ffmpeg在执行命令过程中产生的输出信息，如果不取的话输出流信息填满jvm存储输出信息的缓存区时，线程就会堵塞
            PrintStream errorStream=new PrintStream(process.getErrorStream());
            PrintStream inputStream=new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            //等待执行完
            process.waitFor();
            String result=errorStream.stringBuffer.append(inputStream.stringBuffer+"\n").toString();
            if(outPrintLog){
                logger.info("执行命令：{}已执行完毕,执行结果:{}",cmd,result);
            }else {
                logger.info("执行命令：{}已执行完毕",cmd);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException("视频转换失败");
        }finally {
            if(null!=process){
                ProcessKiller ffmpegKiller=new ProcessKiller(process);
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }

    //程序退出结束前结束已有的ffmpeg进程
    private static class ProcessKiller extends Thread{
        private Process process;

        public ProcessKiller(Process process){
            this.process=process;
        }
        @Override
        public void run(){
            this.process.destroy();
        }
    }

    //用于取出ffmpeg线程执行过程中产生的各种输出信息
    static class PrintStream extends Thread{
        InputStream inputStream=null;
        BufferedReader bufferedReader=null;
        StringBuffer stringBuffer=new StringBuffer();

        public PrintStream(InputStream inputStream){
            this.inputStream=inputStream;
        }

        @Override
        public void run(){
            try {
                if(null==inputStream){
                    return;
                }
                bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                String line=null;
                while((line=bufferedReader.readLine())!=null){
                    stringBuffer.append(line);
                }
            }catch (Exception e){
                logger.error("读取输入流出错错误信息："+e.getMessage());
            }finally {
                try {
                    if(null!=bufferedReader){
                        bufferedReader.close();
                    }
                    if(null!=inputStream){
                        inputStream.close();
                    }
                }catch (Exception e){
                    logger.error("调用printStream读取输出流后，关闭出错");
                }
            }
        }
    }
}
