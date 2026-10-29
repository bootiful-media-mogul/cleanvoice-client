# Cleanvoice v2 client for Spring (RestClient + HTTP service groups)

Spring Boot 4.1 / Spring Framework 7 / Java 17+. Add the module as a dependency and set:

```properties
cleanvoice.api-key=${CLEANVOICE_API_KEY}
# optional
cleanvoice.base-url=https://api.cleanvoice.ai
cleanvoice.polling.initial-delay=30s
cleanvoice.polling.interval=10s
cleanvoice.polling.max-backoff=30s
```

I'd also recomment you set `spring.threads.virtual.enabled=true` if you're running on Java 21+ or later and you intend to use this client in a web server or multithreaded environment like Spring MVC where virtual threads are available.

## What's in it

| Endpoint | Interface method |
|---|---|
| `GET /v1/account` | `CleanvoiceAccountApi.account()` |
| `POST /v2/upload?filename=` | `CleanvoiceUploadsApi.requestUploadUrl(filename)` |
| `PUT <signedUrl>` | `SignedUrlStorageApi.put(uri, contentType, resource)` |
| `POST /v2/edits` | `CleanvoiceEditsApi.create(request)` |
| `GET /v2/edits/{id}` | `CleanvoiceEditsApi.retrieve(id)` |
| `DELETE /v2/edits/{id}` | `CleanvoiceEditsApi.delete(id)` |

`CleanvoiceAutoConfiguration` registers two HTTP service groups with `@ImportHttpServices`:

- **`cleanvoice`** — the three API interfaces. A `RestClientHttpServiceGroupConfigurer` sets the base URL
  and adds `ApiKeyInterceptor` (`X-API-Key`) to this group only.
- **`cleanvoice-storage`** — `SignedUrlStorageApi`, for the PUT to the pre-signed URL. Deliberately
  no interceptor: the key must not leak to the storage host, and interceptors buffer the request body
  in memory, which you don't want for an hour of video.

`CleanvoiceClient` wraps the interfaces with the upload flow and polling.

## Usage

```java
@Bean
ApplicationRunner demo(CleanvoiceClient cleanvoice) {
    return args -> {
        System.out.println("credit: " + cleanvoice.account().credit().total());

        // local file: upload → submit → poll
        Edit edit = cleanvoice.process(Path.of("episode.mp3"),
                EditConfig.builder()
                        .removeNoise(true).normalize(true)
                        .fillers(true).longSilences(true)
                        .breath(EditConfig.Breath.NATURAL)
                        .exportFormat(EditConfig.ExportFormat.MP3)
                        .summarize(true)
                        .build(),
                Duration.ofMinutes(20));

        System.out.println(edit.result().downloadUrl());   // time-limited — download promptly
        System.out.println(edit.result().summarization().title());

        cleanvoice.deleteEdit(edit.taskId());               // optional; files expire after 7 days anyway
    };
}
```

Already-hosted file: `cleanvoice.createEdit("https://example.com/episode.mp3", EditConfig.enhance())`.
Multi-track: `cleanvoice.createMultitrackEdit(List.of(hostMic, guestMic), config)`.
Video: set `.video(true)` or you'll only get audio back.

## Notes

- `breath`, `remove_noise` and `studio_sound` accept boolean-or-string in the API, so they're enums
  whose `@JsonValue` emits `true`/`false` or the string.
- `social_content` comes back as `[]` when off and as an object when on; `Edit.Result.socialContent()`
  gives a typed `Optional`.
- Unknown statuses map to `EditStatus.UNKNOWN`; `POSTPROCESSING` is not terminal.
- Rate limits: 30/min for create and upload (100 enterprise), 300/min for polling.
