package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description 分页查询结果模型类
 * @author Mr.M
 * @date 2022/9/6 14:15
 * @version 1.0
 */
@Data
@ToString
public class PageResult<T> implements Serializable {

    // 数据列表
    @ApiModelProperty("数据列表")
    private List<T> items;

    //总记录数
    @ApiModelProperty("总记录数")
    private long counts;


    //当前页码
    @ApiModelProperty("当前页码")
    private long page;

    //每页记录数
    @ApiModelProperty("每页记录数")
    private long pageSize;

    public PageResult(List<T> items, long counts, long page, long pageSize) {
        this.items = items;
        this.counts = counts;
        this.page = page;
        this.pageSize = pageSize;
    }



}