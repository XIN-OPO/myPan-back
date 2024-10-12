package com.mypan.enums;

public enum ShareValidTypeEnum {
    DAY_1(0,1,"一天"),
    DAT_7(1,7,"7天"),
    DAY_30(2,30,"30天"),
    FOREVER(3,-1,"永久有效");

    private Integer type;
    private Integer days;
    private String desc;

    ShareValidTypeEnum(Integer type, Integer days, String desc) {
        this.type = type;
        this.days = days;
        this.desc = desc;
    }

    public static ShareValidTypeEnum getByType(Integer type){
        for (ShareValidTypeEnum typeEnum:ShareValidTypeEnum.values()){
            if(typeEnum.getType().equals(type)){
                return typeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public Integer getDays() {
        return days;
    }

    public String getDesc() {
        return desc;
    }
}
