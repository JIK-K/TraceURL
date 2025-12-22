package com.traceurl.traceurl.core.shorturl.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.traceurl.traceurl.common.constant.CommonError;
import com.traceurl.traceurl.common.constant.FileError;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.core.shorturl.entity.QrCode;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    public void generateAndSave(ShortUrl shortUrl){
        String fullUrl = "http://localhost:8080/"+shortUrl.getShortCode();
        String fileName = shortUrl.getShortCode() + ".png";

        saveFile(fullUrl, fileName);

        QrCode qrCode = QrCode.builder()
                .shortUrl(shortUrl)
                .format("PNG")
                .payload(fullUrl)
                .filePath("/uploads/qr/" + fileName)
                .build();

        qrCodeRepository.save(qrCode);
    }

    public Resource downloadQrCode(String fileName){
        try{
            Path filePath = Paths.get(uploadPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists() || resource.isReadable()){
                return resource;
            }else{
                throw new BusinessException(FileError.FILE_NOT_FOUND);
            }
        }catch (MalformedURLException e){
            throw new BusinessException(CommonError.INTERNAL_SERVER_ERROR);
        }
    }

    private void saveFile(String content, String fileName){
        try{
            File directory = new File(uploadPath);
            if(!directory.exists()) directory.mkdirs();

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE,250,250);

            Path path = FileSystems.getDefault().getPath(uploadPath + fileName);;
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        }catch(Exception e){
            throw new BusinessException(CommonError.INTERNAL_SERVER_ERROR);
        }
    }
}
