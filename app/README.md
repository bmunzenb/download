# Download Command Line Application

```
Usage: download [<options>]

Options:
  --template=<text>    Incrementing URL template
  --start=<int>        Start value (defaults to 0)
  --end=<int>          End value, otherwise increment until error
  --append=<path>      Appends each download to this file
  --user-agent=<text>  Value for user-agent request header
  --referer=<text>     Value for referer request header
  -h, --help           Show this message and exit
```

## Options

| Option | Type | Required | Description                                                                                                                                                                                               |
| --- | --- | --- |-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `template` | text | Yes | A templated URL that contains a `%d` placeholder where the incremented value will be used.  This should use [Kotlin string formatting rules](https://kotlinlang.org/docs/strings.html#string-formatting). |
| `start` | Integer | No | The starting value to use for the incrementor.  If not specified, starts with `0`.                                                                                                                        |
| `end` | Integer | No | The ending value to use for the incrementor.  If not specified, the incrementor will continue to attempt downloading for the next value until an error occurs.                                            |
| `append` | Path | No | If specified, all downloads are concatenated to the specified file path, otherwise a path is created in the current working directory based on the URL.                                                   |
| `user-agent` | Text | No | If specified, overrides the value of the "User-agent" header in download requests.                                                                                                                        |
| `refere` | Text | No | If specified, sets the value of the "Referer" header in download requests.                                                                                                                                |

## Examples

Download files until an error occurs:

```shell
download --template "http://www.example.com/images/%d.jpg"
```

Download files with leading zeros, appending to the same output file, until an error occurs:

```shell
download --template "http://www.example.com/video_segments/%04d.ts" --append "video.ts"
```

Download the first 100 files:

```shell
download --template "http://www.example.com/documents/%d.pdf" --start 1 --end 100
```
