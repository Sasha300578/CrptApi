import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CrptApi {

    public static class Document {
        // Аннотации для работы с JSON
        @JsonProperty("description")
        private Description description;

        @JsonProperty("doc_id")
        private String docId;

        @JsonProperty("doc_status")
        private String docStatus;

        @JsonProperty("doc_type")
        private String docType;

        @JsonProperty("importRequest")
        private boolean importRequest;

        @JsonProperty("owner_inn")
        private String ownerInn;

        @JsonProperty("participant_inn")
        private String participantInn;

        @JsonProperty("producer_inn")
        private String producerInn;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("production_date")
        private Date productionDate;

        @JsonProperty("production_type")
        private String productionType;

        @JsonProperty("products")
        private List<Product> products;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("reg_date")
        private Date regDate;

        @JsonProperty("reg_number")
        private String regNumber;


        // Вложенный класс для описания продукта
        public static class Description {
            @JsonProperty("participantInn")
            private String participantInn;
        }


        // Вложенный класс для описания продукции
        public static class Product {
            @JsonProperty("certificate_document")
            private String certificateDocument;

            @JsonFormat(pattern = "yyyy-MM-dd")
            @JsonProperty("certificate_document_date")
            private Date certificateDocumentDate;

            @JsonProperty("certificate_document_number")
            private String certificateDocumentNumber;

            @JsonProperty("owner_inn")
            private String ownerInn;

            @JsonProperty("producer_inn")
            private String producerInn;

            @JsonFormat(pattern = "yyyy-MM-dd")
            @JsonProperty("production_date")
            private Date productionDate;

            @JsonProperty("tnved_code")
            private String tnvedCode;

            @JsonProperty("uit_code")
            private String uitCode;

            @JsonProperty("uitu_code")
            private String uituCode;
        }
    }


    // Семафор для ограничения количества одновременных запросов
    private final Semaphore requestSemaphore;
    private final String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final long requestInterval; // Интервал между запросами

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestSemaphore = new Semaphore(requestLimit);
        this.httpClient = createHttpClient();
        this.requestInterval = timeUnit.toMillis(1); // Перевод единиц времени в миллисекунды
    }

    public void createDocument(Document document, String signature) {
        try {
            requestSemaphore.acquire(); // Получение разрешения от семафора
            String requestBody = prepareRequestBody(document, signature);
            sendPostRequest(apiUrl, requestBody);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            requestSemaphore.release(); // Освобождение разрешения семафора
        }
    }

    private String prepareRequestBody(Document document, String signature) throws IOException {
        // Создание объекта Map для хранения всех данных
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("document", document); // Добавление документа
        requestBodyMap.put("signature", signature); // Добавление подписи

        // Преобразование Map в JSON-строку
        return objectMapper.writeValueAsString(requestBodyMap);
    }

    private void sendPostRequest(String url, String requestBody) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpPost);
    }

    private HttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public void createDocumentFromJsonFile(String jsonFilePath, String signature) {
        try {
            String jsonContent = readJsonFromFile(jsonFilePath);
            Document document = objectMapper.readValue(jsonContent, Document.class);
            createDocument(document, signature);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readJsonFromFile(String filePath) throws IOException {
        byte[] encoded = Files.readAllBytes(new File(filePath).toPath());
        return new String(encoded, StandardCharsets.UTF_8);
    }


    public static void main(String[] args) {
        String jsonFilePath = "C:\\Users\\User\\Documents\\test.JSON";

        TimeUnit timeUnit = TimeUnit.MINUTES;
        CrptApi crptApi = new CrptApi(timeUnit, 1); // Ограничение в 1 запрос в минуту

        crptApi.createDocumentFromJsonFile(jsonFilePath, "example_signature");
    }
}
