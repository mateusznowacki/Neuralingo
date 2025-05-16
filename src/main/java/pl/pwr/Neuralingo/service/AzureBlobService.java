package pl.pwr.Neuralingo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

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

    public byte[] downloadFromBlob(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new RuntimeException("Plik nie istnieje w blob storage: " + blobName);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);
        return outputStream.toByteArray();
    }

public String downloadLocal(String blobName) {
    try {
        // Pobierz katalog `resources/temp` z katalogu roboczego
        String relativePath = "resources/temp";
        File directory = new File(relativePath);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Nie można utworzyć katalogu: " + relativePath);
            }
        }

        // Tworzymy ścieżkę pliku lokalnego
        Path localPath = Path.of(relativePath, blobName);
        File localFile = localPath.toFile();

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new RuntimeException("Plik nie istnieje w blob storage: " + blobName);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(localFile)) {
            blobClient.download(fileOutputStream);
        }

        return localFile.getAbsolutePath();

    } catch (IOException e) {
        throw new RuntimeException("Błąd podczas pobierania i zapisu pliku: " + blobName, e);
    }
}

    public byte[] downloadFile(String filename) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobClient blobClient = containerClient.getBlobClient(filename);
        blobClient.download(outputStream);
        return outputStream.toByteArray();
    }
}
