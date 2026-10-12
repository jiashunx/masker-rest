package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.exception.MRestFileUploadException;
import io.github.jiashunx.masker.rest.framework.model.MRestFileUpload;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestFileUploadRequest extends MRestRequest {

    private List<Attribute> attributeList = new ArrayList<>();
    private List<MRestFileUpload> fileUploadList = new ArrayList<>();

    public MRestFileUploadRequest(MRestRequest restRequest) {
        super(restRequest);
    }

    @Override
    public void release() {
        super.release();
        for (Attribute attribute: attributeList) {
            attribute.release();
        }
        for (MRestFileUpload fileUpload: fileUploadList) {
            fileUpload.release();
        }
    }

    public List<Attribute> getAttributeList() {
        return this.attributeList;
    }

    public List<MRestFileUpload> getFileUploadList() {
        return this.fileUploadList;
    }

    public MRestFileUpload getFileUploadOnlyOne() {
        if (this.fileUploadList.size() != 1) {
            throw new IllegalArgumentException("there is more than one or no fileupload object.");
        }
        return this.fileUploadList.get(0);
    }

    /**
     * 添加上传请求中的普通表单属性.
     * 注: 此处会对属性名/值做快照, 避免multipart解码器destroy后访问已释放的缓冲区(use-after-free).
     * @param attribute 解码得到的属性
     */
    public void addAttribute(Attribute attribute) {
        Objects.requireNonNull(attribute);
        try {
            attributeList.add(new MemoryAttribute(attribute.getName(), attribute.getValue()));
        } catch (IOException e) {
            throw new MRestFileUploadException("snapshot multipart attribute failed", e);
        }
    }

    public void addFileUploadObj(FileUpload fileUpload) {
        fileUploadList.add(new MRestFileUpload(fileUpload));
    }

}
