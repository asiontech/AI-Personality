package com.shure.surdes.survey.util;

import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.utils.uuid.UUID;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class FileUtil {

    //上传操作
    public static AjaxResult uploadImage(MultipartFile file, String folder) {
        if (file == null) {
            return AjaxResult.error("请选择要上传的图片");
        }
        if (file.getSize() > 1024 * 1024 * 10) {
            return AjaxResult.error("文件大小不能大于10M");
        }
        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1,
                file.getOriginalFilename().length());
        if (!"jpg,jpeg,gif,png".toUpperCase().contains(suffix.toUpperCase())) {
            return AjaxResult.error("请选择jpg,jpeg,gif,png格式的图片");
        }
        String savePath = folder;
        File savePathFile = new File(savePath);
        if (!savePathFile.exists()) {
            //若不存在该目录，则创建目录
            savePathFile.mkdir();
        }
        //通过UUID生成唯一文件名
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
        try {
            //将文件保存指定目录
            //file.transferTo(new File(savePath + filename));
            //File file1 = new File(file.getOriginalFilename());
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(savePath + filename));
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("保存文件异常");
        }
        //返回文件名称
        return AjaxResult.success(filename);
    }

}
