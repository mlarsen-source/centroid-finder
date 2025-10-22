## 1. JavaCV

https://github.com/bytedeco/javacv

- Pros:

  - frame-by-frame access, built in image processing
  - lightweight and well documented
  - very fast

- Cons:
  - difficult to learn
  - Platform-specific binaries - Need different natives for Windows, Mac, Linux
  - Deployment size - Your JAR becomes huge with all the native libraries

## 2. Humble Video

https://github.com/artclarke/humble-video

- Pros:

  - easier to understand than JavaCV
  - better documentation for java, less c++ translation needed
  - good error messages

- Cons:
  - smaller community
  - still limiited documentation
  - not pure java

## 3. jCodec

https://github.com/jcodec/jcodec

- Pros:

  - lightweight pure Java implementation is relatively easy to understand / implement
  - simple methods built in to extract frames from a video file
  - built in methods to directly convert individual frames to buffered images or png files

- Cons:
  - limited documentation available
  - developer expresses performance and image quality concerns in documentation
  - no easy way to extract individual frame timestamp data, will require additional code
