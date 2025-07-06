package edu.cuit.cloud_netdisk.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "分页查询结果")
public class PageResultVO<T> {
    @ApiModelProperty(value = "总记录数")
    private Long total;

    @ApiModelProperty(value = "当前页数据")
    private List<T> list;

    @ApiModelProperty(value = "当前页码")
    private Integer pageNum;

    @ApiModelProperty(value = "每页数量")
    private Integer pageSize;

    @ApiModelProperty(value = "总页数")
    private Integer pages;

    public PageResultVO(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }
} 