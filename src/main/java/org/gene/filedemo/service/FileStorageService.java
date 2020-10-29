package org.gene.filedemo.service;

import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.parser.AutoDetectParser;
import org.gene.filedemo.controller.FileController;
import org.gene.filedemo.exception.FileStorageException;
import org.gene.filedemo.exception.MyFileNotFoundException;
import org.gene.filedemo.payload.LicenseFile;
import org.gene.filedemo.property.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

//import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);


    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        logger.debug("FileStorageService - fileStorageLocation is " + fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }


    public List<LicenseFile> listFilesInStorage() {
        String fileType;
        List<LicenseFile> results = new ArrayList<LicenseFile>();
        LicenseFile licenseFile = null;
        File[] files = new File(String.valueOf(this.fileStorageLocation)).listFiles();
//If this pathname does not denote a directory, then listFiles() returns null.

logger.trace("listFilesInStorage - There are " + files.length + " files");
        for (File file : files) {
            if (file.isFile()) {
                Path source = Paths.get(file.getAbsolutePath());
                // Apache Tika is used to determine the file types.
                Tika tika = new Tika();
                AutoDetectParser parser = new AutoDetectParser();
                try {
                    fileType = tika.detect(file);
                    logger.trace("listFilesInStorage - file name is " + file.getName());
                    logger.trace("listFilesInStorage - tika file type: " + tika.detect(file));
                    if (fileType.contains("text")) {
                        licenseFile = new LicenseFile();
                        licenseFile.setFileName(file.getName());
                        licenseFile.setFileSize(file.length());
                        licenseFile.setFileType(fileType);
                        results.add(licenseFile);
                       // licenseFile = new LicenseFile(file.getName(), file.length(), tika.detect(file));
                        logger.trace("listFilesInStorage - " + licenseFile.toString());
                    }
                } catch (IOException ex) {
                    throw new FileStorageException("listFilesInStorage - ", ex.getCause());
                }

            }
        }
        logger.trace("listFilesInStorage - results size is " + results.size());
        return results;
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry!  Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            logger.trace("storeFile - targetLocation: " + targetLocation.getFileName());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName);
        }
    }
}
