package com.mypan.controller;

import com.mypan.component.RedisComponent;
import com.mypan.entity.config.AppConfig;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.DownloadFileDto;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import com.mypan.entity.vo.FileInfoVO;
import com.mypan.entity.vo.ResponseVO;
import com.mypan.enums.FileCategoryEnums;
import com.mypan.enums.FileFolderTypeEnums;
import com.mypan.enums.FileTypeEnums;
import com.mypan.enums.ResponseCodeEnum;
import com.mypan.exception.BusinessException;
import com.mypan.service.FileInfoService;
import com.mypan.utils.CopyTools;
import com.mypan.utils.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class CommonFileController extends ABaseController{

    @Resource
    private AppConfig appConfig;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;


    protected void getImage(HttpServletResponse response,String imageFolder,String imageName){
        if(StringUtils.isEmpty(imageFolder)||StringUtils.isEmpty(imageName)||!StringUtils.pathIsOk(imageFolder)||!StringUtils.pathIsOk(imageName)){
            return;
        }
        String imageSuffix=StringUtils.getFileSuffix(imageName);
        String filePath=appConfig.getProjectFolder()+ Constants.file_folder_file+imageFolder+"/"+imageName;
        imageSuffix=imageSuffix.replace(".","");
        String contentType="image/"+imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control","max-age=2592000");
        readFile(response,filePath);
    }

    protected void getFile(HttpServletResponse response,String fileId,String userId){
        String filePath=null;
        if(fileId.endsWith(".ts")){
            String[] tsArray=fileId.split("_");
            String realFileId=tsArray[0];
            FileInfo fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(realFileId,userId);
            if(null==fileInfo){
                return;
            }
            String fileName=fileInfo.getFilePath();
            fileName=StringUtils.getFileNameNoSuffix(fileName)+"/"+fileId;
            filePath=appConfig.getProjectFolder()+Constants.file_folder_file+fileName;
        }else {
            FileInfo fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(fileId,userId);
            if(null==fileInfo){
                return;
            }
            if(FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategary())) {
                String fileNameNoSuffix = StringUtils.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath = appConfig.getProjectFolder() + Constants.file_folder_file + fileNameNoSuffix + "/" + Constants.m3u8_name;
            }else {
                filePath=appConfig.getProjectFolder()+Constants.file_folder_file+fileInfo.getFilePath();
            }
            File file=new File(filePath);
            if(!file.exists()){
                return;
            }
        }
        readFile(response,filePath);
    }

    protected ResponseVO getFolderInfo(String path,String usrId){
        String[] pathArray=path.split("/");
        FileInfoQuery fileInfoQuery=new FileInfoQuery();
        fileInfoQuery.setUserId(usrId);
        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfoQuery.setFileIdArray(pathArray);
        String oderBy="field(file_id,\""+ org.apache.commons.lang3.StringUtils.join(pathArray,"\",\"") +"\")";
        fileInfoQuery.setOrderBy(oderBy);
        List<FileInfo> fileInfoList=fileInfoService.findListByParam(fileInfoQuery);
        return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FileInfoVO.class));
    }

    protected ResponseVO createDownloadUrl(String fileId,String userId) throws BusinessException {
        FileInfo fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(fileId,userId);
        if(fileInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code=StringUtils.getRandomString(Constants.length_50);
        DownloadFileDto downloadFileDto=new DownloadFileDto();
        downloadFileDto.setDownloadCode(code);
        downloadFileDto.setFilePath(fileInfo.getFilePath());
        downloadFileDto.setFileName(fileInfo.getFileName());
        redisComponent.saveDownloadCode(code,downloadFileDto);
        return getSuccessResponseVO(code);
    }

    protected void download(HttpServletRequest request,HttpServletResponse response,String code) throws Exception{
        DownloadFileDto downloadFileDto=redisComponent.getDownloadCode(code);
        if(null==downloadFileDto){
            return;
        }
        String filePath=appConfig.getProjectFolder()+Constants.file_folder_file+downloadFileDto.getFilePath();
        String fileName=downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        if(request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0){//ie浏览器
            fileName= URLEncoder.encode(fileName,"UTF-8");
        }else {
            fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
        }
        response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
        readFile(response,filePath);
    }


}

