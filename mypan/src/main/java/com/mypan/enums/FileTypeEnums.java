package com.mypan.enums;

import org.apache.commons.lang3.ArrayUtils;

public enum FileTypeEnums {
    VIDEO(FileCategoryEnums.VIDEO,1,new String[]{".mp4",".avi",".rmvb",".mkv",".mov"},"视频"),
    MUSIC(FileCategoryEnums.MUSIC,2,new String[]{".mp3",".wav",".wma",".mp2",".flac",".midi",".ra",".ape",".aac",".cda"},"音乐"),
    IMAGE(FileCategoryEnums.IMAGE,3,new String[]{".jpeg",".jpg",".png",".gif",".bmp",".dds",".psd",".pdt",".webp",".xmp",".svg",".tiff"},"图片"),
    PDF(FileCategoryEnums.DOC,4,new String[]{".pdf"},"pdf"),
    WORD(FileCategoryEnums.DOC,5,new String[]{".doc",".docx"},"word"),
    EXCEL(FileCategoryEnums.DOC,6,new String[]{".xlsx"},"excel"),
    TXT(FileCategoryEnums.DOC,7,new String[]{".txt"},"txt"),
    PROGRAME(FileCategoryEnums.Others,8,new String[]{".h",".c",".hpp",".hxx",".cpp",".cc",".c++",".cxx",".m",
    ".o",".s",".dll",".cs",".java",".class",".js",".ts",".css",".scss",".vue",".jsx",".sql",".md",".json", ".html",
    ".xml"},"code"),
    ZIP(FileCategoryEnums.Others,9,new String[]{".zip",".tar",".rar",".7z",".cab",".gz",".ace",".uue",
    ".bz",".jar",".iso",".mpq"},"压缩包"),
    OTHERS(FileCategoryEnums.Others,10,new String[]{},"其他");


    private FileCategoryEnums category;
    private Integer type;
    private String[] suffixes;
    private String desc;

    FileTypeEnums(FileCategoryEnums category, Integer type, String[] suffixes, String desc) {
        this.category = category;
        this.type = type;
        this.suffixes = suffixes;
        this.desc = desc;
    }


    public static FileTypeEnums getFileTypeBySuffix(String suffix){
        for(FileTypeEnums item:FileTypeEnums.values()){
            if(ArrayUtils.contains(item.getSuffixes(),suffix)){
                return item;
            }
        }
        return FileTypeEnums.OTHERS;
    }
    public static FileTypeEnums getByType(Integer type){
        for(FileTypeEnums item:FileTypeEnums.values()){
            if(item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }

    public FileCategoryEnums getCategory() {
        return category;
    }

    public Integer getType() {
        return type;
    }

    public String[] getSuffixes() {
        return suffixes;
    }

    public String getDesc() {
        return desc;
    }
}
