# Download Core

This is the core module containing all classes that implementing the downloading logic
for the command line application.

## How to use

Basic use involves three simple steps:
1. Define a queue of URLs to download
2. Define a target for writing downloaded content to
3. Execute the download

### Define a queue of URLs to download

This can be done one of three ways:
1. Use a base `URLQueue` where you can specify a list of URLs to download.  This is useful
   when you know the URLs ahead of time, for example from a playlist.
2. Use a `TemplateURLQueue` where you can define a URL template with placeholders for data
   which is supplied at runtime from a `ParamIterator`.  Use this method when there is a
   predictable pattern to the URLs, and/or when the number of URLs to download is unknown
   ahead of time.
3. Implement your own `URLQueue` from the interface.

#### `URLQueue`

Create a queue using one of the companion functions that takes a list of URL `String`s,
for example:

```kotlin
val queue = URLQueue.of(
    "http://www.example.com/document.txt",
    "http://www.example.com/video.mp4",
    "http://www.example.com/image.png"
)
```

#### `TemplateURLQueue`

Create a `TemplateURLQueue` using a Kotlin string format and a subclass of `ParamIterator`,
for example:

```kotlin
val params = ListParamIterator(
    listOf("documents", "report.txt"),
    listOf("images", "profile.png"),
    listOf("videos", "intro.mp4")
)

val queue = TemplateURLQueue("http://www.example.com/%s/%s", params)
```

The above will download the contents of three URLs:
1. `http://www.example.com/documents/report.txt`
2. `http://www.example.com/images/profile.png`
3. `http://www.example.com/videos/intro.mp4`

There are also subclasses of `ParamIterator` for simple incrementing values:

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

## Define a target for writing downloaded content to

Create a `TargetFactory` that defines where the contents of a URL should be written to
when downloaded.  There are four options available:
1. `FlatPathFileTargetFactory` will write the contents of all URLs to the same specified
   directory, using the URL path as the file name.
2. `URLPathFileTargetFactory` will write the contents of all URLs to a directory structure
   derived from the URLs.
3. `FileTargetFactory` will write the contents of all URLs to the specified file.  This
   supports appending to the file for each URL so that the contents of all URLs are
   concatenated together.  This is useful if the server has chunked a large file across
   multiple URLs, for example a transport stream video.
4. Implement your own `TargetFactory` from the interface.

Examples:

```kotlin
// Download all files to the same directory
val targetFactory = FlatPathFileTargetFactory(baseDir = File("/path/to/downloads"))
```

```kotlin
// Create a directory structure matching the URLs for each file
val targetFactory = URLPathFileTargetFactory(baseDir = File("/downloads"))
```

```kotlin
// Concatenate all downloads into one file
val targetFactory = FileTargetFactory(file = File("video.ts"), append = true)
```

## Execute the download

Once you have a `URLQueue` and a `TargetFactory`, you can call the `Processor` to
execute the download:

```kotlin
Processor().download(urlQueue = queue, targetFactory = targetFactory)
```

### Custom header values

If the server requires custom header values, you can specify them as a `Map` and pass
them into the `Processor` constructor:

```kotlin
// Use custom request headers for the download requests
val requestProperties = mapOf(
    "User-agent" to "custom/user-agent",
    "Referer" to "http://www.example.com/home.html"
)
    
val processor = Processor(requestProperties)
```

### Custom lifecycle monitoring

By default, the `Processor` will log all activity to the console, but you can override
and/or augment this behavior by passing an implementation of `Consumer<Status>` to the
download function.

In this example, we augment the logging callback with one that will delete small files:

```kotlin
val deleteSmallFiles = Consumer<Status> { status ->
   if (status is Status.DownloadResult) {
      val result = status.result
      val target = status.target

      // delete the downloaded file if it is smaller than 1024 bytes
      if (result is Result.Success && result.bytes < 1024 && target is FileTarget) {
         if (target.file.delete()) {
            println("Deleted ${target.file}")
         }
      }
   }
}

Processor().download(
   urlQueue = queue,
   targetFactory = targetFactory,
   callback = LoggingStatusConsumer().andThen(deleteSmallFiles)
)
```