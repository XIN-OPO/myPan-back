package com.mypan.controller;

import com.mypan.entity.config.AppConfig;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import com.mypan.entity.vo.FileInfoVO;
import com.mypan.entity.vo.ResponseVO;
import com.mypan.enums.FileFolderTypeEnums;
import com.mypan.enums.FileTypeEnums;
import com.mypan.service.FileInfoService;
import com.mypan.utils.CopyTools;
import com.mypan.utils.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class CommonFileController extends ABaseController{

    @Resource
    private AppConfig appConfig;

    @Resource
    private FileInfoService fileInfoService;


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
            String fileName=fileInfo.getFilePath();
            fileName=StringUtils.getFileNameNoSuffix(fileName)+"/"+fileId;
            filePath=appConfig.getProjectFolder()+Constants.file_folder_file+fileName;
        }else {
            FileInfo fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(fileId,userId);
            if(null==fileInfo){
                return;
            }
            if(FileTypeEnums.VIDEO.getCategory().equals(fileInfo.getFileCategary())) {
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

    public ResponseVO getFolderInfo(String path,String usrId){
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
}
