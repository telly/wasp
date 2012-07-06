Wasp - Bitmap Network Utilities for Android
===========================================

This library contains some helpers used to download images asynchronously and get their bitmap.

###Examples

This will load the image to the image view asynchronously

```java
ImageView imageView = (ImageView) findViewById(R.id.image_view_id);
BitmapHelper bh = BitmapHelper.getInstance();
Bitmap bitmap = bh.getBitmap(imageUrl);
if (BitmapUtils.isBitmapValid(bitmap)) {
    imageView.setImageBitmap(bitmap);
} else {
    imageView.setTag(imageUrl); // this is mandatory!
    BitmapObserver observer = new BitmapObserver(imageView, imageUrl, new Handler());
    bh.registerBitmapObserver(this, imageUrl, observer);
}
```

If you are not using an `ImageView` you better use `CallbackBitmapObserver` class:

```java
CallbackBitmapObserver avatarObserver = new CallbackBitmapObserver(
        new CallbackBitmapObserver.BitmapCallback() {
            @Override
            public boolean stillNeedsUrl(String url) {
                return true;
            }

            @Override
            public void receiveBitmap(Bitmap bitmap) {
                // do something with the bitmap
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

  [1]: https://github.com/eveliotc