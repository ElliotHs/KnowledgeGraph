package com.radar.knowledgegraph.controller;

import com.radar.knowledgegraph.entity.LiteratureNode;
import com.radar.knowledgegraph.service.KeywordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@RestController
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);


    @Autowired
    private KeywordService keywordService;


    @RequestMapping(value = "/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "文件为空";
            }
            String fileName = file.getOriginalFilename();
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            log.info("上传的文件名为：" + fileName);
            log.info("文件的后缀名为：" + suffixName);
            String filePath = "/Users/xuchengchuan/Desktop/upload/";
            String path = filePath + fileName;
            File dest = new File(path);
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();// 新建文件夹
            }
            file.transferTo(dest);// 文件写入
            System.out.println(path);


            LiteratureNode literatureNode = new LiteratureNode(fileName,path);

            String[] keyword = new String[] {"雷达","船用","相控阵"};

            keywordService.setKeywordNode(literatureNode,keyword);

            return "upload success";

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "upload failure";
    }

    @PostMapping("/batch")
    public String handleFileUpload(HttpServletRequest request) {
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        MultipartFile file = null;
        BufferedOutputStream stream = null;
        String[][] keywordArray = new String[][] {{"雷达","舰载","相控阵"},{"激光","机载","雷达"},{"雷达","地面","生物识别"}};
        String[] keyword = new String[] {};
        for (int i = 0; i < files.size(); ++i) {
            file = files.get(i);
            String filePath = "/Users/xuchengchuan/Desktop/upload/";
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    String fileName = file.getOriginalFilename();
                    String path = filePath + fileName;
                    //设置文件路径及名字
                    stream = new BufferedOutputStream(new FileOutputStream(
                            new File(path)));

                    keyword = keywordArray[i];
                    LiteratureNode literatureNode = new LiteratureNode(fileName,path);
                    keywordService.setKeywordNode(literatureNode,keyword);


                    stream.write(bytes);// 写入
                    stream.close();
                } catch (Exception e) {
                    stream = null;
                    return "the " + i + " file upload failure";
                }
            } else {
                return "the " + i + " file is empty";
            }
        }
        return "upload Multifile success";
    }

    @GetMapping("/download")
    public String downloadFile(HttpServletRequest request, HttpServletResponse response) {
        String fileName = "/Users/xuchengchuan/Desktop/download/基于雷达和加速度传感器的动车测速系统_葛锁良.pdf";// 文件名
        if (fileName != null) {
            //设置文件路径
            File file = new File("/Users/xuchengchuan/Desktop/upload/基于雷达和加速度传感器的动车测速系统_葛锁良.pdf");
            if (file.exists()) {
                response.setContentType("application/force-download");
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    return "download success";
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "failure";
    }
}
