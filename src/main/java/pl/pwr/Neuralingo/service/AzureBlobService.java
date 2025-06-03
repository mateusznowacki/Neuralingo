package pl.pwr.Neuralingo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.NeuralingoApplication;

import java.io.*;
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
        if (!this.containerClient.exists()) {
            this.containerClient.create();
        }
    }

    // Upload z nazwą blobu (np. documentId)
    public String uploadFile(MultipartFile file, String blobName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            return blobClient.getBlobUrl(); // lub zwróć samo blobName
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się przesłać pliku do Azure Blob", e);
        }
    }

    public String uploadFile(File file, String blobName) {
        try (InputStream inputStream = new FileInputStream(file)) {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(inputStream, file.length(), true);
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się przesłać pliku do Azure Blob", e);
        }
    }

    // Pobranie pliku jako bajty (np. do odpowiedzi API)
    public byte[] downloadFile(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new RuntimeException("Plik nie istnieje w blob storage: " + blobName);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas pobierania pliku: " + blobName, e);
        }
    }

    public String downloadLocal(String blobName) {
        try {
            // Ścieżka do katalogu, gdzie znajduje się uruchomiony plik JAR
            String jarDir = new File(NeuralingoApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getParent();

            String relativePath = "resources/temp";
            File directory = new File(jarDir, relativePath);

            if (!directory.exists() && !directory.mkdirs()) {
                throw new RuntimeException("Nie można utworzyć katalogu: " + directory.getAbsolutePath());
            }

            Path localPath = Path.of(directory.getAbsolutePath(), blobName);
            File localFile = localPath.toFile();

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (!blobClient.exists()) {
                throw new RuntimeException("Plik nie istnieje: " + blobName);
            }

            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                blobClient.downloadStream(fos);
            }

            return localFile.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas pobierania pliku", e);
        }
    }


    // Usunięcie pliku z blob storage po nazwie
    public void deleteFile(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (blobClient.exists()) {
            blobClient.delete();
        }
    }
}
