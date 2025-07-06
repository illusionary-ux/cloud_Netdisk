package edu.cuit.cloud_netdisk.service;

import edu.cuit.cloud_netdisk.pojo.vo.RecycleBinItemVO;

import java.util.List;

public interface RecycleBinService {
    /**
     * 获取回收站列表
     * @param userId 用户ID
     * @return 用户的回收站项目列表
     */
    List<RecycleBinItemVO> getRecycleBinItems(Long userId);

    /**
     * 恢复回收站项目
     * @param id 回收站项目ID
     * @param userId 用户ID
     */
    void restoreItem(Long id, Long userId);

    /**
     * 彻底删除回收站项目
     * @param id 回收站项目ID
     * @param userId 用户ID
     */
    String deleteItem(Long id, Long userId);

    /**
     * 清空回收站
     * @param userId 用户ID
     */
    void emptyRecycleBin(Long userId);
} 