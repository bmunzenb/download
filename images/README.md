# Mass Image Downloader (Experimental)

This is a utility for mass downloading images from a website.

## User Guide

### Step 1: Create an instance of `ImageDownloader`

Create an instance of `ImageDownloader`, passing the following parameters into the constructor:

| Parameter           | Required | Description                                                                                                                                                 |
|---------------------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `linkFilter`        | Yes      | A `Function` that takes a URL string to an HTML page and returns `true` if the URL should be processed for images and links.                                |
| `imageFilter`       | Yes      | A `Function` that takes a URL string to an image and returns `true` if the image should be downloaded.                                                      |
| `targetFactory`     | Yes      | A lambda that takes the referer URL string and returns a `TargetFactory` that provides a `Target` where the downloaded image will be written to.            |
| `callback`          | No       | An implementation of `Consumer<ImageDownloadStatus>` to receive lifecycle status notifications. If not specified, will log lifecycle events to the console. |
| `processedRegistry` | No       | An implementation of `ProcessedRegistry` where the status of each processed URL is stored. Defaults to an in-memory registry.                               |
| `depthFirst`        | No       | If set to `true`, will process links and images depth-first. Defaults to `false`.                                                                           |                                                                        

### Step 2: Call `execute`

Once an instance of `ImageDownloader` is ready, call `execute(url)`, passing in a URL string specifying the first HTML
page to start crawling.

### Example

In this example, we configure the downloader to crawl all pages where the URL starts with `http://www.example.com/` and
download all images where the URL starts with `http://www.example.com/images/` to a directory structure matching the
URLs.  (See the [Core Module Readme](../core/README.md) for more information on destination target factories.)

We then start the downloader at `http://www.example.com/index.html`:

```kotlin
val destinationPath = Path.of("/user/home/images")

ImageDownloader(
    linkFilter = { it.startsWith("http://www.example.com/") },
    imageFilter = { it.startsWith("http://www.example.com/images/") },
    targetFactory = { referer -> URLPathFileTargetFactory(destinationPath) }
).execute("http://www.example.com/index.html")
```