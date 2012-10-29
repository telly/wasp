Wasp - Bitmap Network Utilities for Android
===========================================

![Wasp logo](https://raw.github.com/twitvid/wasp/master/img/wasp.png)

This library contains some helpers used to download images asynchronously and load delicious [Bitmaps](https://developer.android.com/reference/android/graphics/Bitmap.html).

###Examples

This will load the image to the image view asynchronously

```java
ImageView imageView = (ImageView) findViewById(R.id.image_view_id);
BitmapHelper bh = BitmapHelper.getInstance();
Bitmap bitmap = bh.getBitmap(imageUrl);
if (BitmapUtils.isBitmapValid(bitmap)) {
    imageView.setImageBitmap(bitmap);
} else {
    BitmapObserver observer = new BitmapObserver(imageView, imageUrl, new Handler());
    bh.registerBitmapObserver(this, observer);
}
```

If you are not using an `ImageView` you better use `CallbackBitmapObserver` class:

```java
CallbackBitmapObserver avatarObserver = new CallbackBitmapObserver(new CallbackBitmapObserver.BitmapCallback() {
    @Override
    public boolean stillNeedsUrl(String url) {
        return true;
    }

    @Override
    public void receiveBitmap(String uri, Bitmap bitmap) {

    }
}, imageUrl, new Handler());

BitmapHelper bh = BitmapHelper.getInstance();
Bitmap bitmap = bh.getBitmap(imageUrl);
if (BitmapUtils.isBitmapValid(bitmap)) {
    // do something with the bitmap
} else {
    bh.registerBitmapObserver(this, imageUrl, avatarObserver);
}
```

###Bitmap methods replacement

While manipulating bitmaps it is common to get OutOfMemory errors. `BitmapHelper` class keeps a LRU cache of
all processed bitmaps, and provides wrappers for `android.graphics.Bitmap` methods that evict previous
bitmaps in cache before allocating memory for new bitmaps.

```java
// this kind of statements
Bitmap bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
// can (and should) be replaced by this
Bitmap bitmap = BitmapHelper.getInstance().createBitmap(480, 800, Bitmap.Config.ARGB_8888);
```

There is also a wrapper method for createScaleBitmap method:

```java
// this kind of statements
Bitmap bitmap = Bitmap.createScaledBitmap(source, 480, 800, Bitmap.Config.ARGB_8888);
// can (and should )be replaced by this
Bitmap bitmap = BitmapHelper.getInstance().createScaledBitmap(source, 480, 800, Bitmap.Config.ARGB_8888);
```

Maven
=====

Wasp library is available through Maven. Just add this to your `pom.xml` file:

```xml
<dependency>
    <groupId>com.telly</groupId>
    <artifactId>wasp</artifactId>
    <version>1.7</version>
    <scope>compile</scope>
</dependency>
```

License
=======

    Copyright 2012 Telly

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.