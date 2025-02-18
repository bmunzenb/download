# Download Core

This is the core module containing all classes that implement the downloading logic
for the [command line application](../app).

You can use this in your own application via the
[package published on GitHub](https://github.com/bmunzenb/download/packages/2366629).

## User guide

Basic use involves three simple steps:
1. Define a queue of URLs to download
2. Define a target factory to specify where to write downloaded content
3. Execute the download

### Step 1: Define a queue of URLs to download

This can be done one of three ways:
1. Use a base `URLQueue` where you can specify a list of URLs to download.  Use this method
   when you know the URLs ahead of time, for example from a playlist.
2. Use a `TemplateURLQueue` where you define a URL template with placeholders for data
   supplied at runtime from a `ParamIterator`.  Use this method when there is a
   predictable pattern to the URLs, and/or when the number of URLs to download is unknown
   ahead of time.
3. Implement your own `URLQueue` from the interface.

#### Using a `URLQueue`

Create a queue using one of the companion functions that takes a list of URL `String`s,
for example:

```kotlin
val queue = URLQueue.of(
    "http://www.example.com/document.txt",
    "http://www.example.com/video.mp4",
    "http://www.example.com/image.png"
)
```

#### Using a `TemplateURLQueue`

Create a `TemplateURLQueue` using a Kotlin string formatted template and an implementation
of `ParamIterator`, for example:

```kotlin
val params = ListParamIterator(
    listOf("documents", "report.txt"),
    listOf("images", "profile.png"),
    listOf("videos", "intro.mp4")
)

// With the above params, this will download three URLs:
// 1. http://www.example.com/documents/report.txt
// 2. http://www.example.com/images/profile.png
// 3. http://www.example.com/videos/intro.mp4
val queue = TemplateURLQueue("http://www.example.com/%s/%s", params)
```

There are supplied implementations of `ParamIterator` for simple incrementing values:

```kotlin
// Download the first 100 images
val params = IntRangeParamIterator(range = 1..100)
val queue = TemplateURLQueue("http://www.example.com/images/%d.png", params)
```

```kotlin
// Keep downloading images until an error occurs
val params = IncrementUntilErrorParamIterator(start = 1)
val queue = TemplateURLQueue("http://www.example.com/images/%d.png", params)
```

#### Implementing your own `URLQueue`

You can implement custom logic for a `URLQueue` by implementing its interface consisting
of the function:

```kotlin
fun next(result: Result<ResultData>) : URL?
```

This function takes the Kotlin `Result` of the previous download (or `ResultFirst` if it's the first call), and should
return a `URL` for the next URL in the queue, or `null` if there are no further URLs.

#### Composing queues

You can compose multiple `URLQueue`s together with the plus operator:

```kotlin
// queue1, queue2, and queue3 all implement URLQueue
val composedQueue = queue1 + queue2 + queue3
```

*Note that each queue in a composed queue will receive a `ResultFirst` when the first URL is requested, and not the
result of the last download when the previous queue in the chain returns `null`.*

### Step 2: Define a target factory

Create a `TargetFactory` that defines where the contents of a URL should be written to
when downloaded.  There are three options available:
1. `URLPathFileTargetFactory` will write the contents of all URLs to a directory structure
   derived from the URL paths.
2. `FileTargetFactory` will write the contents of all URLs to the specified file.  This
   supports appending to the file for each URL so that the contents of all URLs are
   concatenated together.  This is useful if the server has chunked a large file across
   multiple URLs, for example a transport stream video.
3. Implement your own `TargetFactory` from the interface.

Examples:

```kotlin
// Create a directory structure matching the URLs for each file
val targetFactory = URLPathFileTargetFactory(baseDir = Path.of("/downloads"), useFullPath = true)
```

```kotlin
// Concatenate all downloads into one file
val targetFactory = FileTargetFactory(path = Path.of("video.ts"), append = true)
```

#### Implementing your own `TargetFactory`

You can customize where the downloaded contents are written to by implementing your own
`TargetFactory` using its interface consisting of the function:

```kotlin
fun create(source: URL): Target
```

This function takes the URL that is about to be downloaded and returns an instance of
`Target`. A `Target` must have a `name` property and implement a `write` function that
writes the contents of the input stream to the intended target and returns the total
number of bytes transferred.  Optionally, you can also use the `progressCallback` to
notify the caller of the running total of bytes processed.

### Step 3: Execute the download

Once you have a `URLQueue` and a `TargetFactory`, you can use the `Processor` to
execute the download:

```kotlin
Processor().download(queue, targetFactory)
```

#### Custom header values

If the server requires custom header values, you can pass them as a `Map` to the
`Processor` constructor:

```kotlin
// Use custom request headers for the download requests
val headers = mapOf(
    "User-agent" to "custom/user-agent",
    "Referer" to "http://www.example.com/home.html"
)
    
val processor = Processor(headers)
```

#### Custom lifecycle monitoring

By default, the `Processor` will log all activity to the console, but you can override
and/or augment this behavior by passing an implementation of `Consumer<ProcessorEvent>` to the
download function.  (Note: There is also a `ProcessorEventConsumer` convenience interface that
extends `Consumer<ProcessorEvent>` and allows overriding a function for each `ProcessorEvent` type.)

In this example, we augment the logging callback with one that will delete small files:

```kotlin
val deleteSmallFiles = object : ProcessorEventConsumer {
   override fun onDownloadResult(url: URL, target: Target, result: Result<ResultData>) {
      result.onSuccess { data ->
         // delete the downloaded file if it is less than 1 KB
         if (data.bytes < 1024 && target is FileTarget) {
            Files.delete(target.file)
            println("Deleted ${target.file}")
         }
      }
   }
}

Processor().download(
   urlQueue = queue,
   targetFactory = targetFactory,
   callback = LoggingProcessorEventConsumer().andThen(deleteSmallFiles)
)
```
