package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.model.MRestFileUpload;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;

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

    public void addAttribute(Attribute attribute) {
        attributeList.add(Objects.requireNonNull(attribute));
    }

    public void addFileUploadObj(FileUpload fileUpload) {
        fileUploadList.add(new MRestFileUpload(fileUpload));
    }

}
