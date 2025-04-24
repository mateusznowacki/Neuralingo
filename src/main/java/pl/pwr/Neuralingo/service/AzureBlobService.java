package pl.pwr.Neuralingo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class AzureBlobService {

    private final BlobContainerClient containerClient;

    public AzureBlobService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public String uploadFile(MultipartFile file, String blobName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            return blobClient.getBlobUrl(); // lub tylko blobName, jeśli wolisz
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się przesłać pliku do Azure Blob", e);
        }
    }


    public byte[] downloadFile(String filename) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobClient blobClient = containerClient.getBlobClient(filename);
        blobClient.download(outputStream);
        return outputStream.toByteArray();
    }
}
