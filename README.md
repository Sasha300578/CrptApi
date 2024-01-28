#  CrptApi Java Client
Репозиторий содержит реализацию на Java (версия 17) класса для взаимодействия с API Честного Знака. Этот класс, CrptApi, предназначен для интеграции с системой Честного Знака и обеспечивает безопасное и эффективное использование API в многопоточных приложениях.

Особенности:
 - Thread-safe: Класс CrptApi разработан с учетом потокобезопасности, что позволяет использовать его в многопоточных приложениях без риска возникновения состояния гонки или других проблем синхронизации.
 - Ограничение на количество запросов: В конструкторе класса можно указать ограничение на количество запросов к API в заданный временной интервал, что помогает избежать превышения лимитов API и обеспечивает более стабильную работу приложения. Пример конструктора: public CrptApi(TimeUnit timeUnit, int requestLimit), где timeUnit определяет временной интервал (секунды, минуты и т.д.), а requestLimit - максимальное количество запросов в этом интервале.
   
Для использования CrptApi необходимо клонировать репозиторий и подключить следующие зависимости в ваш проект:

Apache HttpClient для отправки HTTP-запросов:

  - org.apache.httpcomponents:httpclient:4.5.13

Jackson Databind для работы с JSON:

  - com.fasterxml.jackson.core:jackson-databind:2.13.0

Код программы находится в папке src в файле CrptApi.java
