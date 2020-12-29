package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestFileDownloadException;
import io.github.jiashunx.masker.rest.framework.model.MRestHeader;
import io.github.jiashunx.masker.rest.framework.model.MRestHeaders;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class MResponseHelper {

    private static String statusPageTemplate = null;
    static {
        statusPageTemplate = IOUtils.loadContentFromClasspath("masker-rest/template/status.html", MResponseHelper.class.getClassLoader());
    }

    public static void redirect(ChannelHandlerContext ctx, String targetURL) {
        write(ctx, HttpResponseStatus.TEMPORARY_REDIRECT, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status) {
        write(ctx, status, new MRestHeaders());
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Object> headers) {
        write(ctx, status, null, headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, MRestHeaders headers) {
        write(ctx, status, null, headers);
    }

    public static void writeJSON(ChannelHandlerContext ctx, String string) {
        writeJSON(ctx, string, new HashMap<>());
    }

    public static void writeJSON(ChannelHandlerContext ctx, String string, Map<String, Object> headers) {
        writeJSON(ctx, string, new MRestHeaders(headers));
    }

    public static void writeJSON(ChannelHandlerContext ctx, String string, MRestHeaders headers) {
        write(ctx, string.getBytes(), headers);
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes) {
        writeJSON(ctx, bytes, new HashMap<>());
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        writeJSON(ctx, bytes, new MRestHeaders(headers));
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes, MRestHeaders headers) {
        write(ctx, bytes, new MRestHeaders(headers).add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes) {
        write(ctx, HttpResponseStatus.OK, bytes, new HashMap<>());
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, MRestHeaders headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void writeStatusPageAsHtml(ChannelHandlerContext ctx, HttpResponseStatus status) {
        write(ctx, status, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML));
    }

    public static void writeStatusPage(ChannelHandlerContext ctx, HttpResponseStatus status) {
        writeStatusPage(ctx, status, new HashMap<>());
    }

    public static void writeStatusPage(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Object> headers) {
        writeStatusPage(ctx, status, new MRestHeaders(headers));
    }

    public static void writeStatusPage(ChannelHandlerContext ctx, HttpResponseStatus status, MRestHeaders headers) {
        write(ctx, status, getStatusPageBytes(status), headers);
    }

    public static String getStatusPageContent(HttpResponseStatus status) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", status.code());
        params.put("reason", status.reasonPhrase());
        params.put("mrf.version", MRestUtils.getFrameworkVersion());
        return MRestUtils.format(statusPageTemplate, params);
    }

    public static byte[] getStatusPageBytes(HttpResponseStatus status) {
        return getStatusPageContent(status).getBytes(StandardCharsets.UTF_8);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Object object) {
        write(ctx, status, object, new HashMap<>());
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Object object, Map<String, Object> headers) {
        write(ctx, status, object, new MRestHeaders(headers));
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Object object, MRestHeaders headers) {
        write(ctx, status, MRestSerializer.jsonSerialize(object), headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes) {
        write(ctx, status, bytes, new HashMap<>());
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        write(ctx, status, bytes, new MRestHeaders(headers));
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
        byte[] _bytes = bytes == null ? new byte[0] : bytes;
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(_bytes));
        HttpHeaders httpHeaders = response.headers();
        if (headers != null) {
            List<MRestHeader> headerList = headers.getHeaders();
            headerList.forEach(header -> {
                httpHeaders.add(header.getKey(), header.getValue());
            });
        }
        httpHeaders.add(Constants.HTTP_HEADER_CONTENT_LENGTH, response.content().readableBytes());
        ctx.write(response);
        ctx.flush();
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile) {
        write(ctx, downloadedFile, new HashMap<>());
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, Consumer<File> callback) {
        write(ctx, downloadedFile, new HashMap<>(), callback);
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, Map<String, Object> headers) {
        write(ctx, downloadedFile, new MRestHeaders(headers));
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, Map<String, Object> headers, Consumer<File> callback) {
        write(ctx, downloadedFile, new MRestHeaders(headers), callback);
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, MRestHeaders headers) {
        write(ctx, downloadedFile, headers, null);
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, MRestHeaders headers, Consumer<File> callback) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(downloadedFile, "r");
            long fileLength = randomAccessFile.length();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders httpHeaders = response.headers();
            if (headers != null) {
                List<MRestHeader> headerList = headers.getHeaders();
                headerList.forEach(header -> {
                    httpHeaders.add(header.getKey(), header.getValue());
                });
            }
            httpHeaders.add(Constants.HTTP_HEADER_CONTENT_LENGTH, fileLength);
            httpHeaders.add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_OCTETSTREAM);
            httpHeaders.add(Constants.HTTP_HEADER_CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", downloadedFile.getName()));
            ctx.write(response);
            ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long l, long l1) throws Exception {

                }
                @Override
                public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                    randomAccessFile.close();
                    if (callback != null) {
                        callback.accept(downloadedFile);
                    }
                }
            });
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (Throwable throwable) {
            throw new MRestFileDownloadException(throwable);
        }
    }

}
